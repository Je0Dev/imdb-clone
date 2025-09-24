package com.papel.imdb_clone.controllers.content;

import com.papel.imdb_clone.model.content.Content;
import com.papel.imdb_clone.model.people.Actor;
import com.papel.imdb_clone.service.navigation.NavigationService;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.text.NumberFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

/**
 * Controller for displaying detailed information about a specific content (movie or TV show).
 * Handles the UI elements and data binding for the content details view.
 */

public class ContentDetailsController {
    /**
     * Explicit constructor for ContentDetailsController.
     * Required for JavaFX controller initialization.
     */
    public ContentDetailsController() {
        // No initialization needed
    }
    
    private static final Logger logger = LoggerFactory.getLogger(ContentDetailsController.class);
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("MMMM d, yyyy");
    
    // UI Elements - Header Section
    @FXML private Label titleLabel;
    @FXML private Label yearLabel;
    @FXML private Label contentRatingLabel;
    @FXML private Label runtimeLabel;
    @FXML private Label ratingLabel;
    @FXML private Label voteCountLabel;
    @FXML private Label genresLabel;
    @FXML private Text overviewText;
    
    // Media Section
    @FXML private ImageView posterImage;
    
    // Common Details Section
    @FXML private Label directorLabel;
    @FXML private Label castLabel;
    @FXML private Label releaseDateLabel;
    @FXML private Label statusLabel;
    @FXML private Label languageLabel;
    @FXML private Label countriesLabel;
    
    // TV Show Specific Section
    @FXML private VBox tvShowDetails;
    @FXML private Label seasonsLabel;
    @FXML private Label episodesLabel;
    @FXML private Label episodeRuntimeLabel;
    @FXML private Label networksLabel;
    
    // Movie Specific Section
    @FXML private VBox movieDetails;
    @FXML private Label budgetLabel;
    @FXML private Label revenueLabel;
    
    // Similar Content Section
    @FXML private HBox similarContentContainer;
    
    // Data State
    private String contentId;
    private Content content;
    private boolean isEditMode = false;

    /**
     * Initializes the controller after FXML loading is complete.
     * Sets up initial UI state and event handlers.
     */
    @FXML
    public void initialize() {
        logger.debug("Initializing ContentDetailsController");
        setupEventHandlers();
    }
    
    /**
     * Sets up event handlers for UI components.
     */
    private void setupEventHandlers() {
        // Add any event handlers here if needed
    }
    
    /**
     * Sets the content details to be displayed in the view.
     *
     * @param contentId the unique identifier for the content
     * @param contentType the type of content ("movie" or "tv")
     * @param title the title of the content
     * @param year the release year
     * @param rating the content rating (e.g., "PG-13")
     * @param runtime the runtime in minutes
     * @param voteAverage the average vote score (0-10)
     * @param voteCount the number of votes
     * @param genres list of genre names
     * @param overview the plot overview/summary
     * @param director the director's name
     * @param cast list of cast member names
     * @param releaseDate the release date
     * @param status the production status (e.g., "Released", "In Production")
     * @param language the original language
     * @param countries list of production countries
     * @param posterPath path/URL to the poster image
     * @throws IllegalArgumentException if required parameters are null or invalid
     */
    public void setContentDetails(String contentId, String contentType, String title, String year, 
                                 String rating, int runtime, double voteAverage, int voteCount,
                                 List<String> genres, String overview, String director, 
                                 List<String> cast, LocalDate releaseDate, String status,
                                 String language, List<String> countries, String posterPath) {
        
        validateContentDetails(contentId, contentType, title);
        
        this.contentId = contentId;
        
        // Set basic information
        setBasicInfo(title, year, rating, runtime, voteAverage, voteCount, genres, overview);
        
        // Set additional details
        setDetails(director, cast, releaseDate, status, language, countries);
        
        // Set content type specific UI
        setContentTypeSpecificUI(contentType);
        
        // Load poster image if path is provided
        if (posterPath != null && !posterPath.trim().isEmpty()) {
            loadPosterImage(posterPath);
        }
    }
    
    /**
     * Validates the required content details.
     *
     * @param contentId the content ID
     * @param contentType the content type
     * @param title the content title
     * @throws IllegalArgumentException if any required parameter is invalid
     */
    private void validateContentDetails(String contentId, String contentType, String title) {
        if (contentId == null || contentId.trim().isEmpty()) {
            throw new IllegalArgumentException("Content ID cannot be null or empty");
        }
        if (!"movie".equals(contentType) && !"tv".equals(contentType)) {
            throw new IllegalArgumentException("Content type must be either 'movie' or 'tv'");
        }
        if (title == null || title.trim().isEmpty()) {
            throw new IllegalArgumentException("Title cannot be null or empty");
        }
    }
    
    /**
     * Sets the basic information section of the content details.
     */
    private void setBasicInfo(String title, String year, String rating, int runtime, 
                             double voteAverage, int voteCount, List<String> genres, String overview) {
        titleLabel.setText(title);
        yearLabel.setText(year != null ? year : "N/A");
        contentRatingLabel.setText(rating != null ? rating : "N/A");
        runtimeLabel.setText(formatRuntime(runtime));
        ratingLabel.setText(String.format(Locale.US, "%.1f", voteAverage));
        voteCountLabel.setText(String.format("(%s)", formatNumber(voteCount)));
        genresLabel.setText(genres != null ? String.join(", ", genres) : "N/A");
        overviewText.setText(overview != null ? overview : "No overview available.");
    }
    
    /**
     * Sets the additional details section of the content.
     */
    private void setDetails(String director, List<String> cast, LocalDate releaseDate, 
                           String status, String language, List<String> countries) {
        directorLabel.setText(director != null ? director : "N/A");
        castLabel.setText(cast != null && !cast.isEmpty() ? String.join(", ", cast) : "N/A");
        releaseDateLabel.setText(releaseDate != null ? releaseDate.format(DATE_FORMATTER) : "N/A");
        statusLabel.setText(status != null ? status : "N/A");
        languageLabel.setText(language != null ? language.toUpperCase() : "N/A");
        countriesLabel.setText(countries != null && !countries.isEmpty() ? 
            String.join(", ", countries) : "N/A");
    }
    
    /**
     * Configures the UI based on the content type (movie or TV show).
     */
    private void setContentTypeSpecificUI(String contentType) {
        boolean isTvShow = "tv".equals(contentType);
        tvShowDetails.setVisible(isTvShow);
        movieDetails.setVisible(!isTvShow);
    }
    
    /**
     * Loads and displays the poster image from the given path.
     *
     * @param posterPath the path or URL to the poster image
     */
    private void loadPosterImage(String posterPath) {
        try {
            Image image = new Image(posterPath, true);
            posterImage.setImage(image);
            posterImage.setPreserveRatio(true);
            posterImage.setSmooth(true);
        } catch (Exception e) {
            logger.warn("Failed to load poster image from: " + posterPath, e);
            // Optionally set a default/placeholder image
        }
    }
    
    /**
     * Formats a duration in minutes into a human-readable string.
     *
     * @param minutes the duration in minutes
     * @return formatted string (e.g., "2h 30m"), or "N/A" if minutes is 0 or negative
     */
    private String formatRuntime(int minutes) {
        if (minutes <= 0) {
            return "N/A";
        }
        int hours = minutes / 60;
        int mins = minutes % 60;
        return String.format("%dh %02dm", hours, mins);
    }
    
    /**
     * Formats a number with comma as thousand separator.
     *
     * @param number the number to format
     * @return formatted string (e.g., "1,234,567")
     */
    private String formatNumber(int number) {
        return NumberFormat.getNumberInstance(Locale.US).format(number);
    }
    
    /**
     * Formats a currency amount into a human-readable string.
     *
     * @param amount the amount to format
     * @return formatted currency string (e.g., "$1,234,567"), or "N/A" if amount is 0 or negative
     */
    private String formatCurrency(long amount) {
        if (amount <= 0) {
            return "N/A";
        }
        return NumberFormat.getCurrencyInstance(Locale.US).format(amount);
    }
    
    /**
     * Sets the TV show specific details.
     *
     * @param seasons the number of seasons
     * @param episodes the total number of episodes
     * @param episodeRuntimes list of episode runtimes in minutes (can be empty)
     * @param networks list of network names (can be empty)
     */
    public void setTvShowDetails(int seasons, int episodes, List<Integer> episodeRuntimes, List<String> networks) {
        seasonsLabel.setText(String.valueOf(seasons));
        episodesLabel.setText(String.valueOf(episodes));
        episodeRuntimeLabel.setText(calculateAverageRuntime(episodeRuntimes));
        networksLabel.setText(formatNetworks(networks));
    }
    
    /**
     * Calculates and formats the average runtime from a list of episode runtimes.
     *
     * @param runtimes list of episode runtimes in minutes
     * @return formatted average runtime string (e.g., "45m (avg)")
     */
    private String calculateAverageRuntime(List<Integer> runtimes) {
        if (runtimes == null || runtimes.isEmpty()) {
            return "N/A";
        }
        
        double average = runtimes.stream()
            .mapToInt(Integer::intValue)
            .average()
            .orElse(0);
            
        int avgRuntime = (int) Math.round(average);
        return avgRuntime > 0 ? formatRuntime(avgRuntime) + " (avg)" : "N/A";
    }
    
    /**
     * Formats a list of network names into a comma-separated string.
     *
     * @param networks list of network names
     * @return formatted string of networks, or "N/A" if the list is empty
     */
    private String formatNetworks(List<String> networks) {
        return networks != null && !networks.isEmpty() ? 
            String.join(", ", networks) : "N/A";
    }
    
    /**
     * Sets the movie specific financial details.
     *
     * @param budget the production budget in USD
     * @param revenue the total revenue in USD
     */
    public void setMovieDetails(long budget, long revenue) {
        budgetLabel.setText(formatCurrency(budget));
        revenueLabel.setText(formatCurrency(revenue));
    }

    /**
     * Handles the close button action.
     * Closes the current window.
     */
    @FXML
    private void handleClose() {
        logger.debug("Closing content details window");
        Stage stage = (Stage) titleLabel.getScene().getWindow();
        stage.close();
    }

    /**
     * Handles the back button action.
     * Navigates back to the previous view using the navigation service.
     */
    @FXML
    private void goBack() {
        logger.debug("Navigating back from content details");
        NavigationService.getInstance().goBack();
    }

    /**
     * Toggles edit mode for the content details.
     * 
     * @param editMode true to enable editing, false to disable
     */
    public void setEditMode(boolean editMode) {
        if (this.isEditMode == editMode) {
            return; // No change needed
        }
        
        this.isEditMode = editMode;
        
        if (editMode) {
            showEditModeNotification();
            enableEditing(true);
        } else {
            enableEditing(false);
        }
    }
    
    /**
     * Shows a notification when edit mode is enabled.
     */
    private void showEditModeNotification() {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Edit Mode");
        alert.setHeaderText("Edit Mode Enabled");
        alert.setContentText("You are now in edit mode. Make your changes and click 'Save' when done.");
        alert.showAndWait();
    }
    
    /**
     * Enables or disables editing of the content details.
     * 
     * @param enable true to enable editing, false to disable
     */
    private void enableEditing(boolean enable) {
        // Example of how to enable/disable editing for various fields
        // In a real implementation, you would add logic for all editable fields
        // titleField.setEditable(enable);
        // overviewText.setEditable(enable);
        // etc.
    }
    
    /**
     * Sets the content ID for this controller.
     * 
     * @param id the content ID to set
     * @throws IllegalArgumentException if id is null or empty
     */
    public void setContentId(String id) {
        if (id == null || id.trim().isEmpty()) {
            throw new IllegalArgumentException("Content ID cannot be null or empty");
        }
        this.contentId = id;
    }
    
    /**
     * Gets the current content ID.
     * 
     * @return the content ID, or null if not set
     */
    public String getContentId() {
        return contentId;
    }
    
    /**
     * Gets the current content object.
     * 
     * @return the content object, or null if not set
     */
    public Content getContent() {
        return content;
    }


    public void setContent(Content content) {
        this.content = content;
    }
}
