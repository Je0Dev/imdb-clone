package com.papel.imdb_clone.controllers.content;


import com.papel.imdb_clone.enums.Genre;
import com.papel.imdb_clone.model.content.Content;
import com.papel.imdb_clone.service.content.ContentService;
import com.papel.imdb_clone.service.content.MoviesService;
import com.papel.imdb_clone.service.content.SeriesService;
import com.papel.imdb_clone.service.navigation.NavigationService;
import com.papel.imdb_clone.service.search.ServiceLocator;
import com.papel.imdb_clone.util.UIUtils;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;
import com.papel.imdb_clone.util.UIUtils;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

import static com.papel.imdb_clone.util.UIUtils.showError;

/**
 * Controller for the Edit Content view.
 * Handles editing of content details including title, type, year, rating, etc.
 */
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
    private static final int DEFAULT_RUNTIME = 0;

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
            if (contentType != null) {
                if (contentType.equalsIgnoreCase("movie")) {
                    @SuppressWarnings("unchecked")
                    ContentService<T> movieService = (ContentService<T>) ServiceLocator.getInstance().getService(MoviesService.class);
                    this.contentService = movieService;
                } else if (contentType.equalsIgnoreCase("series")) {
                    @SuppressWarnings("unchecked")
                    ContentService<T> seriesService = (ContentService<T>) ServiceLocator.getInstance().getService(SeriesService.class);
                    this.contentService = seriesService;
                }
            }

            if (this.contentService == null) {
                logger.error("Failed to initialize content service for type: {}", contentType);
                showError("Error", "Failed to initialize content service");
                navigationService.goBack();
                return;
            }

            // Get the content ID from the navigation data
            Object contentIdObj = navigationService.getUserData("contentId");
            if (contentIdObj != null) {
                int contentId = (int) contentIdObj;
                logger.info("Loading content with ID: {}", contentId);

                // Load the content by ID
                @SuppressWarnings("unchecked")
                Optional<T> contentOpt = (Optional<T>) contentService.getById(contentId);
                if (contentOpt.isPresent()) {
                    this.content = contentOpt.get();
                    loadContent(this.content);
                    logger.info("Successfully loaded content: {}", this.content.getTitle());
                } else {
                    logger.error("Content not found with ID: {}", contentId);
                    showError("Error", "Content not found. Please try again.");
                    navigationService.goBack();
                }
            } else {
                logger.error("No content ID provided for editing");
                showError("Error", "No content selected for editing.");
                navigationService.goBack();
            }
        } catch (Exception e) {
            logger.error("Error initializing EditContentController: {}", e.getMessage(), e);
            showError("Initialization Error", "Failed to initialize the edit form: " + e.getMessage());
            navigationService.goBack();
        }
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
                    } else {
                        ratingField.setStyle("-fx-text-fill: white;");
                    }
                } catch (NumberFormatException e) {
                    ratingField.setStyle("-fx-text-fill: red;");
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
            }
        }
    }


    {
        logger.warn("Invalid rating format");
        // Update genres
        List<Genre> selectedGenres = new ArrayList<>();
        if (actionCheckBox.isSelected()) selectedGenres.add(Genre.ACTION);
        if (adventureCheckBox.isSelected()) selectedGenres.add(Genre.ADVENTURE);
        if (animationCheckBox.isSelected()) selectedGenres.add(Genre.ANIMATION);
        if (comedyCheckBox.isSelected()) selectedGenres.add(Genre.COMEDY);
        if (crimeCheckBox.isSelected()) selectedGenres.add(Genre.CRIME);
        if (dramaCheckBox.isSelected()) selectedGenres.add(Genre.DRAMA);
        if (familyCheckBox.isSelected()) selectedGenres.add(Genre.FAMILY);
        if (fantasyCheckBox.isSelected()) selectedGenres.add(Genre.FANTASY);
        if (historyCheckBox.isSelected()) selectedGenres.add(Genre.HISTORY);
        if (horrorCheckBox.isSelected()) selectedGenres.add(Genre.HORROR);
        if (musicCheckBox.isSelected()) selectedGenres.add(Genre.MUSICAL);
        if (mysteryCheckBox.isSelected()) selectedGenres.add(Genre.MYSTERY);
        if (romanceCheckBox.isSelected()) selectedGenres.add(Genre.ROMANCE);
        if (scifiCheckBox.isSelected()) selectedGenres.add(Genre.SCI_FI);
        if (thrillerCheckBox.isSelected()) selectedGenres.add(Genre.THRILLER);
        if (warCheckBox.isSelected()) selectedGenres.add(Genre.WAR);
        if (westernCheckBox.isSelected()) selectedGenres.add(Genre.WESTERN);

        // Update content fields
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
        }

        // Update rating
        try {
            content.setImdbRating(Double.parseDouble(ratingField.getText().trim()));
        } catch (NumberFormatException e) {
            logger.warn("Invalid rating format");
        }

        // Update genres
        content.setGenres(selectedGenres);

        showStatus();

        // Close the edit window after a short delay
        new java.util.Timer().schedule(
                new java.util.TimerTask() {
                    @Override
                    public void run() {
                        javafx.application.Platform.runLater(() -> {
                            Stage stage = (Stage) statusLabel.getScene().getWindow();
                            stage.close();
                            try {
                                contentService.update(content);
                                navigationService.goBack();
                            } catch (Exception e) {
                                logger.error("Error saving content", e);
                                showError("Save Error", "Failed to save changes: " + e.getMessage());
                            }
                        });
                    }
                },
                1000
        );

    }

    /**
     * Handles the cancel button action.
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


    private boolean validateForm() {
        StringBuilder errors = new StringBuilder();
        
        // Check required fields
        if (titleField.getText() == null || titleField.getText().trim().isEmpty()) {
            errors.append("• Title is required\n");
        }
        
        if (typeComboBox.getValue() == null || typeComboBox.getValue().trim().isEmpty()) {
            errors.append("• Type is required\n");
        }
        
        if (yearField.getText() == null || yearField.getText().trim().isEmpty()) {
            errors.append("• Year is required\n");
        } else {
            try {
                int year = Integer.parseInt(yearField.getText().trim());
                if (year < 1888 || year > 2100) {
                    errors.append("• Year must be between 1888 and 2100\n");
                }
            } catch (NumberFormatException e) {
                errors.append("• Year must be a valid number\n");
            }
        }
        
        if (ratingField.getText() == null || ratingField.getText().trim().isEmpty()) {
            errors.append("• Rating is required\n");
        } else {
            try {
                double rating = Double.parseDouble(ratingField.getText().trim());
                if (rating < 0 || rating > 10) {
                    errors.append("• Rating must be between 0.0 and 10.0\n");
                }
            } catch (NumberFormatException e) {
                errors.append("• Rating must be a valid number\n");
            }
        }
        
        // Show errors if any
        if (!errors.isEmpty()) {
            showError("Validation Error", "Please fix the following errors:\n\n" + errors);
            return false;
        }
        
        return true;
    }
    
    /**
     * Displays an error message to the user.
     * 
     * @param title   The title of the error dialog
     * @param message The error message to display
     */
    private void showError(String title, String message) {
        showError(title, message);
        statusLabel.setText("Error: " + title);
    }
    
    /**
     * Displays a status message to the user.
     */
    private void showStatus() {
        statusLabel.setText("Changes saved successfully");
    }
}
