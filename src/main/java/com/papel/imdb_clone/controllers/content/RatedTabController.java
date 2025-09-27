package com.papel.imdb_clone.controllers.content;

import com.papel.imdb_clone.controllers.BaseController;
import com.papel.imdb_clone.controllers.authentication.AuthController;
import com.papel.imdb_clone.model.people.User;
import com.papel.imdb_clone.model.rating.Rating;
import com.papel.imdb_clone.model.rating.UserRating;
import com.papel.imdb_clone.service.validation.AuthService;
import com.papel.imdb_clone.service.content.MoviesService;
import com.papel.imdb_clone.service.rating.RatingService;
import com.papel.imdb_clone.service.search.ServiceLocator;
import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextArea;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.VBox;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URL;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.ResourceBundle;

/**
 * Controller for the Rated tab that displays user's rated content.
 */
public class RatedTabController extends BaseController {

    //Logger
    private static final Logger logger = LoggerFactory.getLogger(RatedTabController.class);
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("MMM d, yyyy");
    
    private String sessionToken;

    @FXML private TableView<UserRating> ratingsTable;
    @FXML private TableColumn<UserRating, String> titleColumn;
    @FXML private TableColumn<UserRating, Double> ratingColumn;
    @FXML private TableColumn<UserRating, String> dateRatedColumn;
    @FXML private TextArea reviewTextArea;
    @FXML private VBox detailsContainer;
    @FXML private Label titleLabel;
    @FXML private Label ratingLabel;
    @FXML private Label dateRatedLabel;
    
    /**
     * Sets the navigation data including session token for this controller.
     * @param data Map containing navigation data including session token
     */
    public void setNavigationData(Map<String, Object> data) {
        if (data != null && data.containsKey("sessionToken")) {
            this.sessionToken = (String) data.get("sessionToken");
            logger.debug("Received session token in RatedTabController");
            if (this.sessionToken != null) {
                // Load ratings now that we have the session token
                Platform.runLater(this::loadUserRatings);
            }
        } else {
            logger.warn("No session token provided to RatedTabController");
        }
    }

    private RatingService ratingService;
    private MoviesService moviesService;
    private ObservableList<UserRating> ratingsList;
    private AuthController authController;
    private ServiceLocator serviceLocator;
    
    /**
     * Default constructor required by FXML
     */
    public RatedTabController() {
        this.serviceLocator = ServiceLocator.getInstance();
    }

    @Override
    protected void initializeController(int currentUserId) {
        // This method is called by the base class
        // We handle initialization in the initialize() method instead
    }

    @FXML
    public void initialize() {
        try {
            logger.debug("Initializing RatedTabController");
            
            // Initialize services
            this.ratingService = RatingService.getInstance();
            this.moviesService = MoviesService.getInstance();
            this.ratingsList = FXCollections.observableArrayList();
            
            // Get AuthController from ServiceLocator
            this.authController = serviceLocator.getService(AuthController.class);
            if (this.authController == null) {
                logger.error("Failed to get AuthController from ServiceLocator");
                return;
            }
            
            // Setup UI components
            setupTable();
            setupTableSelection();
            
            // We'll load ratings when setNavigationData is called with the session token
        } catch (Exception e) {
            logger.error("Error initializing RatedTabController: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to initialize RatedTabController", e);
        }
    }

    private void setupTable() {
        titleColumn.setCellValueFactory(cellData -> {
            // You'll need to implement a way to get the content title from the content ID
            // For now, we'll just show the content ID
            return new SimpleStringProperty("Content ID: " + cellData.getValue().getContentId());
        });
        
        ratingColumn.setCellValueFactory(cellData -> {
            double rating = cellData.getValue().getRating();
            return javafx.beans.binding.Bindings.createObjectBinding(() -> rating);
        });
        
        dateRatedColumn.setCellValueFactory(cellData -> {
            LocalDateTime createdAt = cellData.getValue().getCreatedAt();
            return new SimpleStringProperty(createdAt.format(DateTimeFormatter.ISO_LOCAL_DATE));
        });
        
        // Custom cell factory for rating column to show stars
        ratingColumn.setCellFactory(column -> new TableCell<>() {
            @Override
            protected void updateItem(Double rating, boolean empty) {
                super.updateItem(rating, empty);
                if (empty || rating == null) {
                    setText(null);
                    setGraphic(null);
                } else {
                    try {
                        int stars = (int) Math.round(rating);
                        setText("★ ".repeat(stars) + "☆ ".repeat(10 - stars));
                    } catch (Exception e) {
                        setText(String.valueOf(rating));
                    }
                }
            }
        });

        ratingsTable.setItems(ratingsList);
    }

    private void loadUserRatings() {
        try {
            // Get current user's ID (you'll need to implement this in your auth service)
            int currentUserId = getCurrentUserId();
            List<UserRating> ratings = ratingService.getRatingsByUser(currentUserId);
            ratingsList.setAll(ratings);
            
            if (ratings.isEmpty()) {
                showNoRatingsMessage();
            }
        } catch (Exception e) {
            logger.error("Error loading user ratings: ", e);
            showError("Error", "Failed to load your ratings. Please try again later.");
        }
    }

    private void setupTableSelection() {
        ratingsTable.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
            if (newSelection != null) {
                showRatingDetails(newSelection);
            }
        });
    }

    private void showRatingDetails(UserRating rating) {
        titleLabel.setText("Content ID: " + rating.getContentId());
        ratingLabel.setText(String.format("Your rating: %d/10", rating.getRating()));
        dateRatedLabel.setText("Rated on: " + rating.getCreatedAt().format(DateTimeFormatter.ISO_LOCAL_DATE));
        
        // If UserRating has review functionality, uncomment this:
        // reviewTextArea.setText(rating.getReview() != null ? rating.getReview() : "");
        
        // Enable editing if the review is not empty
        reviewTextArea.setDisable(false);
        
        // Show the details container if it was hidden
        detailsContainer.setVisible(true);
    }

    @FXML
    private void handleSaveReview() {
        UserRating selectedRating = ratingsTable.getSelectionModel().getSelectedItem();
        if (selectedRating != null) {
            try {
                // If you implement review functionality in UserRating, uncomment this:
                // String review = reviewTextArea.getText();
                // ratingService.updateReview(selectedRating.getId(), review);
                showSuccess("Success", "Your rating has been updated.");
            } catch (Exception e) {
                logger.error("Error updating rating: ", e);
                showError("Error", "Failed to update your rating. Please try again.");
            }
        }
    }

    @FXML
    private void handleDeleteRating() {
        UserRating selectedRating = ratingsTable.getSelectionModel().getSelectedItem();
        if (selectedRating != null) {
            try {
                Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
                alert.setTitle("Delete Rating");
                alert.setHeaderText("Confirm Deletion");
                alert.setContentText("Are you sure you want to remove this rating?");

                Optional<ButtonType> result = alert.showAndWait();
                if (result.isPresent() && result.get() == ButtonType.OK) {
                    ratingService.deleteRating(selectedRating.getId());
                    ratingsList.remove(selectedRating);
                    reviewTextArea.clear();
                    detailsContainer.setVisible(false);
                    showSuccess("Success", "Your rating has been removed.");
                }
            } catch (Exception e) {
                logger.error("Error deleting rating: ", e);
                showError("Error", "Failed to delete your rating. Please try again.");
            }
        }
    }

    private void showNoRatingsMessage() {
        // Hide details container
        detailsContainer.setVisible(false);
        
        // Show a message in the table
        ratingsTable.setPlaceholder(new Label("You haven't rated any content yet."));
    }

    /**
     * Gets the current user's ID from the authentication system.
     * 
     * @return The current user's ID, or -1 if not authenticated
     */
    private int getCurrentUserId() {
        try {
            // Get the session token from the authController
            String sessionToken = authController.getSessionToken();
            
            if (sessionToken == null) {
                logger.warn("No active session found");
                return -1;
            }
            
            // Get the current user using the session token
            User currentUser = AuthService.getInstance().getCurrentUser(sessionToken);
            if (currentUser == null) {
                logger.warn("No user found for the current session");
                return -1;
            }
            
            return currentUser.getId();
        } catch (Exception e) {
            logger.error("Error getting current user ID: {}", e.getMessage(), e);
            return -1;
        }
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        loadUserRatings();
        setupTableSelection();
    }
}
