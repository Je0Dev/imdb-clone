package com.papel.imdb_clone.controllers.content;


import com.papel.imdb_clone.enums.Genre;
import com.papel.imdb_clone.model.content.Content;
import com.papel.imdb_clone.service.content.ContentService;
import com.papel.imdb_clone.service.content.MoviesService;
import com.papel.imdb_clone.service.content.SeriesService;
import com.papel.imdb_clone.service.navigation.NavigationService;
import com.papel.imdb_clone.service.search.ServiceLocator;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.stage.Stage;
import javafx.stage.Window;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.*;


/**
 * Controller for editing content of a specific type.
 * @param <T> The type of content being edited (e.g., Movie, Series)
 */
public class EditContentController<T extends Content> {
    private static final Logger logger = LoggerFactory.getLogger(EditContentController.class);

    // Services
    private final NavigationService navigationService;

    // Content being edited
    private T content;
    private ContentService<T> contentService;

    // FXML injected fields
    @FXML private TextField titleField;
    @FXML private ComboBox<String> typeComboBox;
    @FXML private TextField yearField;
    @FXML private TextField ratingField;
    @FXML private TextField directorField;
    @FXML private TextArea plotArea;

    // Genre checkboxes
    @FXML private CheckBox actionCheckBox;
    @FXML private CheckBox adventureCheckBox;
    @FXML private CheckBox animationCheckBox;
    @FXML private CheckBox comedyCheckBox;
    @FXML private CheckBox crimeCheckBox;
    @FXML private CheckBox dramaCheckBox;
    @FXML private CheckBox familyCheckBox;
    @FXML private CheckBox fantasyCheckBox;
    @FXML private CheckBox historyCheckBox;
    @FXML private CheckBox horrorCheckBox;
    @FXML private CheckBox musicCheckBox;
    @FXML private CheckBox mysteryCheckBox;
    @FXML private CheckBox romanceCheckBox;
    @FXML private CheckBox scifiCheckBox;
    @FXML private CheckBox thrillerCheckBox;
    @FXML private CheckBox warCheckBox;
    @FXML private CheckBox westernCheckBox;

    @FXML private Label statusLabel;

    /**
     * Constructor initializes required services.
     */
    public EditContentController() {
        this.navigationService = NavigationService.getInstance();
    }

    /**
     * Initializes the controller class.
     */
    @FXML
    public void initialize() {
        try {
            setupValidation();
            
            // Get the content service based on the content type
            String contentType = (String) navigationService.getUserData("contentType");
            logger.info("Initializing EditContentController for content type: {}", contentType);
            
            if (contentType != null && !contentType.trim().isEmpty()) {
                try {
                    if (contentType.equalsIgnoreCase("movie")) {
                        MoviesService movieService = ServiceLocator.getInstance().getService(MoviesService.class);
                        if (movieService != null) {
                            @SuppressWarnings("unchecked") // Safe cast as we know the type matches
                            ContentService<T> service = (ContentService<T>) movieService;
                            this.contentService = service;
                            logger.info("Successfully initialized MovieService");
                        } else {
                            logger.error("Failed to get MovieService from ServiceLocator");
                        }
                    } else if (contentType.equalsIgnoreCase("series")) {
                        SeriesService seriesService = ServiceLocator.getInstance().getService(SeriesService.class);
                        if (seriesService != null) {
                            @SuppressWarnings("unchecked") // Safe cast as we know the type matches
                            ContentService<T> service = (ContentService<T>) seriesService;
                            this.contentService = service;
                            logger.info("Successfully initialized SeriesService");
                        } else {
                            logger.error("Failed to get SeriesService from ServiceLocator");
                        }
                    } else {
                        logger.warn("Unknown content type: {}", contentType);
                    }
                } catch (Exception e) {
                    logger.error("Error initializing content service: {}", e.getMessage(), e);
                }
            } else {
                logger.warn("No content type specified for EditContentController");
            }

            if (this.contentService == null) {
                String errorMsg = String.format("Failed to initialize content service for type: %s. " +
                    "Service is null. Check service registration in ServiceLocator.", contentType);
                logger.error(errorMsg);
                showError("Initialization Error", "Failed to initialize content service. Please try again later.");
                navigationService.goBack();
                return;
            }

            // Get the content ID from the navigation data
            Object contentIdObj = navigationService.getUserData("contentId");
            if (contentIdObj != null) {
                int contentId = (int) contentIdObj;
                logger.info("Loading content with ID: {}", contentId);

                // Load the content by ID
                Optional<T> contentOpt = contentService.getById(contentId);
                if (contentOpt.isPresent()) {
                    // Set the content by ID
                    this.content = contentOpt.get();
                    loadContent(this.content);
                    logger.info("Successfully loaded content: {}", this.content.getTitle());
                } else {
                    // Content not found
                    logger.error("Content not found with ID: {}", contentId);
                    showError("Error", "Content not found. Please try again.");
                    navigationService.goBack();
                }
            } else {
                // No content ID provided for editing
                logger.error("No content ID provided for editing");
                showError("Error", "No content selected for editing.");
                navigationService.goBack();
            }
        } catch (Exception e) {
            // Exception while initializing EditContentController
            logger.error("Error initializing EditContentController: {}", e.getMessage(), e);
            showError("Initialization Error", "Failed to initialize the edit form: " + e.getMessage());
            navigationService.goBack();
        }
        logger.info("EditContentController initialized successfully");
    }

    /**
     * Sets up input validation for form fields.
     */
    private void setupValidation() {
        // Allow only numeric input for year and rating
        yearField.textProperty().addListener((observable, oldValue, newValue) -> {
            if (!newValue.matches("\\d*") && !newValue.isEmpty()) {
                yearField.setText(oldValue);
            } else if (!newValue.isEmpty()) {
                int year = Integer.parseInt(newValue);
                if (year < 1888 || year > 2100) { // Assuming 1888 as the first film year
                    yearField.setStyle("-fx-text-fill: red;");
                } else {
                    yearField.setStyle("-fx-text-fill: white;");
                }
            }
        });

        // Allow only numeric input for rating (0.0 to 10.0)
        ratingField.textProperty().addListener((observable, oldValue, newValue) -> {
            if (!newValue.matches("\\d*\\.?\\d*") && !newValue.isEmpty()) {
                ratingField.setText(oldValue);
            } else if (!newValue.isEmpty()) {
                try {
                    double rating = Double.parseDouble(newValue);
                    if (rating < 0 || rating > 10) {
                        ratingField.setStyle("-fx-text-fill: red;");
                        logger.info("Rating is out of range (0.0 to 10.0): {}", rating);
                    } else {
                        ratingField.setStyle("-fx-text-fill: white;");
                        logger.info("Rating is within range (0.0 to 10.0): {}", rating);
                    }
                } catch (NumberFormatException e) {
                    ratingField.setStyle("-fx-text-fill: red;");
                    logger.info("Rating is not a number: {}", newValue);
                }
            }
        });

    }

    /**
     * Sets the content to be edited and loads its data into the form.
     *
     * @param content The content to edit
     */
    @SuppressWarnings("unchecked")
    public void setContent(Content content) {
        this.content = (T) content;
        loadContent(this.content);
        logger.info("Content set successfully");
    }

    /**
     * Loads content data into the form for editing.
     *
     * @param content The content to edit
     */
    private void loadContent(T content) {
        if (content == null) {
            logger.warn("Attempted to load null content");
            return;
        }

        // Set the content to be edited
        this.content = content;

        // Populate form fields
        titleField.setText(content.getTitle());

        // Set the first genre as the main genre in the combo box
        List<Genre> genres = content.getGenres();
        if (genres != null && !genres.isEmpty()) {
            typeComboBox.setValue(genres.getFirst().getDisplayName().toString());
        }

        // Format the year
        if (content.getYear() != null) {
            Calendar cal = Calendar.getInstance();
            cal.setTime(content.getYear());
            yearField.setText(String.valueOf(cal.get(Calendar.YEAR)));
        }

        // Set rating
        if (content.getImdbRating() != null) {
            ratingField.setText(String.format("%.1f", content.getImdbRating()));
        }

        directorField.setText(content.getDirector());
        plotArea.setText(content.getTitle()); // Using title as plot since there's no plot field in Content

        // Set genre checkboxes
        if (genres != null) {
            for (Genre genre : genres) {
                switch (genre) {
                    case ACTION: actionCheckBox.setSelected(true); break;
                    case ADVENTURE: adventureCheckBox.setSelected(true); break;
                    case ANIMATION: animationCheckBox.setSelected(true); break;
                    case COMEDY: comedyCheckBox.setSelected(true); break;
                    case CRIME: crimeCheckBox.setSelected(true); break;
                    case DRAMA: dramaCheckBox.setSelected(true); break;
                    case FAMILY: familyCheckBox.setSelected(true); break;
                    case FANTASY: fantasyCheckBox.setSelected(true); break;
                    case HISTORY: historyCheckBox.setSelected(true); break;
                    case HORROR: horrorCheckBox.setSelected(true); break;
                    case MUSICAL: musicCheckBox.setSelected(true); break;
                    case MYSTERY: mysteryCheckBox.setSelected(true); break;
                    case ROMANCE: romanceCheckBox.setSelected(true); break;
                    case SCI_FI: scifiCheckBox.setSelected(true); break;
                    case THRILLER: thrillerCheckBox.setSelected(true); break;
                    case WAR: warCheckBox.setSelected(true); break;
                    case WESTERN: westernCheckBox.setSelected(true); break;
                    default: break;
                }
                logger.info("Genre checkbox set: {}", genre.getDisplayName());
            }
        }
    }
    
    /**
     * Handles the save button action.
     * Saves the content with the current form values.
     */
    @FXML
    private void handleSave() {
        if (content == null) {
            logger.error("Cannot save: No content is currently being edited");
            showError("Save Error", "No content is currently being edited.");
            return;
        }

        try {
            // Update content fields
            content.setTitle(titleField.getText().trim());
            content.setDirector(directorField.getText().trim());

            // Update year
            try {
                int year = Integer.parseInt(yearField.getText().trim());
                Calendar cal = Calendar.getInstance();
                cal.set(Calendar.YEAR, year);
                content.setYear(cal.getTime());
                content.setStartYear(year);
            } catch (NumberFormatException e) {
                logger.warn("Invalid year format");
                showError("Invalid Year", "Please enter a valid year.");
                return;
            }

            // Update rating
            try {
                content.setImdbRating(Double.parseDouble(ratingField.getText().trim()));
            } catch (NumberFormatException e) {
                logger.warn("Invalid rating format");
                showError("Invalid Rating", "Please enter a valid rating.");
                return;
            }

            // Update genres
            List<Genre> selectedGenres = new ArrayList<>();
            if (actionCheckBox != null && actionCheckBox.isSelected()) selectedGenres.add(Genre.ACTION);
            if (adventureCheckBox != null && adventureCheckBox.isSelected()) selectedGenres.add(Genre.ADVENTURE);
            if (animationCheckBox != null && animationCheckBox.isSelected()) selectedGenres.add(Genre.ANIMATION);
            if (comedyCheckBox != null && comedyCheckBox.isSelected()) selectedGenres.add(Genre.COMEDY);
            if (crimeCheckBox != null && crimeCheckBox.isSelected()) selectedGenres.add(Genre.CRIME);
            if (dramaCheckBox != null && dramaCheckBox.isSelected()) selectedGenres.add(Genre.DRAMA);
            if (familyCheckBox != null && familyCheckBox.isSelected()) selectedGenres.add(Genre.FAMILY);
            if (fantasyCheckBox != null && fantasyCheckBox.isSelected()) selectedGenres.add(Genre.FANTASY);
            if (historyCheckBox != null && historyCheckBox.isSelected()) selectedGenres.add(Genre.HISTORY);
            if (horrorCheckBox != null && horrorCheckBox.isSelected()) selectedGenres.add(Genre.HORROR);
            if (musicCheckBox != null && musicCheckBox.isSelected()) selectedGenres.add(Genre.MUSICAL);
            if (mysteryCheckBox != null && mysteryCheckBox.isSelected()) selectedGenres.add(Genre.MYSTERY);
            if (romanceCheckBox != null && romanceCheckBox.isSelected()) selectedGenres.add(Genre.ROMANCE);
            if (scifiCheckBox != null && scifiCheckBox.isSelected()) selectedGenres.add(Genre.SCI_FI);
            if (thrillerCheckBox != null && thrillerCheckBox.isSelected()) selectedGenres.add(Genre.THRILLER);
            if (warCheckBox != null && warCheckBox.isSelected()) selectedGenres.add(Genre.WAR);
            if (westernCheckBox != null && westernCheckBox.isSelected()) selectedGenres.add(Genre.WESTERN);

            // Set genres
            content.setGenres(selectedGenres);

            // Save the content
            contentService.update(content);
            showStatus();

            // Close the window after a short delay
            new java.util.Timer().schedule(
                    new java.util.TimerTask() {
                        @Override
                        public void run() {
                            // Close the window
                            javafx.application.Platform.runLater(() -> {
                                if (statusLabel != null && statusLabel.getScene() != null) {
                                    Stage stage = (Stage) statusLabel.getScene().getWindow();
                                    if (stage != null) {
                                        stage.close();
                                    }

                                }
                                logger.info("Navigating back to home");
                                navigationService.goBack();
                            });
                        }
                    },
                    1000
            );

            //Close the window
            navigationService.goBack();
            showStatus();

        } catch (Exception e) {
            logger.error("Error saving content", e);
            showError("Save Error", "Failed to save changes: " + e.getMessage());
        }
    }

    /**
     * Handles the cancel button action while being in the edit content view
     */
    @FXML
    private void handleCancel() {
        try {
            // Try to get the window from statusLabel if available
            Node source = statusLabel != null ? statusLabel : titleField;
            if (source != null && source.getScene() != null) {
                Stage stage = (Stage) source.getScene().getWindow();
                if (stage != null) {
                    stage.close();
                    return;
                }
            }

            // Fallback: try to get the window from any other available component
            for (Node node : new Node[]{titleField, typeComboBox, yearField}) {
                if (node != null && node.getScene() != null) {
                    Stage stage = (Stage) node.getScene().getWindow();
                    if (stage != null) {
                        stage.close();
                        return;
                    }
                }
            }

            logger.warn("Could not close window: No valid window reference found");
        } catch (Exception e) {
            logger.error("Error while trying to close the window", e);
        }
    }


    /**
     * Displays an error message to the user.
     * 
     * @param title   The title of the error dialog
     * @param message The error message to display
     */
    private void showError(String title, String message) {
        // Log the error first
        logger.error("Error: {} - {}", title, message);
        
        // Show error dialog
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        
        try {
            // Only try to position the dialog if the UI is already initialized
            if (statusLabel != null && statusLabel.getScene() != null && statusLabel.getScene().getWindow() != null) {
                Window mainWindow = statusLabel.getScene().getWindow();
                alert.setX(mainWindow.getX() + mainWindow.getWidth() / 4);
                alert.setY(mainWindow.getY() + mainWindow.getHeight() / 4);
                statusLabel.setText("Error: " + title);
            }
        } catch (Exception e) {
            logger.warn("Could not position error dialog: {}", e.getMessage());
            // Continue showing the dialog without custom positioning
        }
        
        alert.showAndWait();
    }
    
    /**
     * Displays a status message to the user.
     */
    private void showStatus() {
        statusLabel.setText("Changes saved successfully");
    }

    //go back to previous view
    @FXML
    public void handleBack(ActionEvent actionEvent) {
        navigationService.goBack();
        actionEvent.consume();

    }
}
