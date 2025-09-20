package com.papel.imdb_clone.controllers.content;

import com.papel.imdb_clone.model.content.Content;
import com.papel.imdb_clone.model.people.Actor;
import com.papel.imdb_clone.service.navigation.NavigationService;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;
import javafx.stage.Stage;

import java.text.NumberFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

public class ContentDetailsController {
    // UI Elements - Header
    @FXML private Label titleLabel;
    @FXML private Label yearLabel;
    @FXML private Label contentRatingLabel;
    @FXML private Label runtimeLabel;
    @FXML private Label ratingLabel;
    @FXML private Label voteCountLabel;
    @FXML private Label genresLabel;
    @FXML private Text overviewText;
    
    // Media
    @FXML private ImageView posterImage;
    
    // Details
    @FXML private Label directorLabel;
    @FXML private Label castLabel;
    @FXML private Label releaseDateLabel;
    @FXML private Label statusLabel;
    @FXML private Label languageLabel;
    @FXML private Label countriesLabel;
    
    // TV Show Specific
    @FXML private VBox tvShowDetails;
    @FXML private Label seasonsLabel;
    @FXML private Label episodesLabel;
    @FXML private Label episodeRuntimeLabel;
    @FXML private Label networksLabel;
    
    // Movie Specific
    @FXML private VBox movieDetails;
    @FXML private Label budgetLabel;
    @FXML private Label revenueLabel;
    
    // Similar Content
    @FXML private HBox similarContentContainer;
    
    // Data
    private String contentId;

    @FXML
    public void initialize() {
        // Initialize any UI components if needed
    }
    
    /**
     * Set content details for a movie or TV show
     * @param contentId The ID of the content
     * @param contentType The type of content ("movie" or "tv")
     * @param title The title of the content
     * @param year The release year
     * @param rating The content rating (e.g., "PG-13")
     * @param runtime The runtime in minutes
     * @param voteAverage The average vote score (0-10)
     * @param voteCount The number of votes
     * @param genres List of genres
     * @param overview The plot overview
     * @param director The director's name
     * @param cast List of cast members
     * @param releaseDate The release date
     * @param status The status (Released, In Production, etc.)
     * @param language The original language
     * @param countries Production countries
     * @param posterPath Path to the poster image
     */
    public void setContentDetails(String contentId, String contentType, String title, String year, 
                                 String rating, int runtime, double voteAverage, int voteCount,
                                 List<String> genres, String overview, String director, 
                                 List<String> cast, LocalDate releaseDate, String status,
                                 String language, List<String> countries, String posterPath) {
        
        this.contentId = contentId;
        // "movie" or "tv"

        // Set basic info
        titleLabel.setText(title);
        yearLabel.setText(year);
        contentRatingLabel.setText(rating);
        runtimeLabel.setText(formatRuntime(runtime));
        ratingLabel.setText(String.format("%.1f", voteAverage));
        voteCountLabel.setText(String.format("(%s votes)", formatNumber(voteCount)));
        genresLabel.setText(String.join(", ", genres));
        overviewText.setText(overview != null && !overview.isEmpty() ? overview : "No overview available.");
        
        // Set poster image
        if (posterPath != null && !posterPath.isEmpty()) {
            try {
                // In a real app, you would load the image from your API or local resources
                // For now, we'll use a placeholder
                posterImage.setImage(new Image("file:src/main/resources/images/placeholder_poster.png"));
            } catch (Exception e) {
                System.err.println("Error loading poster image: " + e.getMessage());
            }
        }
        
        // Set details
        directorLabel.setText(director != null ? director : "N/A");
        castLabel.setText(cast != null ? String.join(", ", cast) : "N/A");
        releaseDateLabel.setText(releaseDate != null ? 
            releaseDate.format(DateTimeFormatter.ofPattern("MMMM d, yyyy")) : "N/A");
        statusLabel.setText(status != null ? status : "N/A");
        languageLabel.setText(language != null ? new Locale(language).getDisplayLanguage() : "N/A");
        countriesLabel.setText(countries != null ? String.join(", ", countries) : "N/A");
        
        // Show/hide content type specific sections
        if ("tv".equals(contentType)) {
            tvShowDetails.setVisible(true);
            movieDetails.setVisible(false);
        } else {
            tvShowDetails.setVisible(false);
            movieDetails.setVisible(true);
        }
    }
    
    // Helper method to format runtime (e.g., 120 -> "2h 0m")
    private String formatRuntime(int minutes) {
        if (minutes <= 0) return "N/A";
        int hours = minutes / 60;
        int mins = minutes % 60;
        return String.format("%dh %02dm", hours, mins);
    }
    
    // Helper method to format numbers with commas
    private String formatNumber(int number) {
        return NumberFormat.getNumberInstance(Locale.US).format(number);
    }
    
    // Set TV show specific details
    public void setTvShowDetails(int seasons, int episodes, List<Integer> episodeRuntimes, List<String> networks) {
        seasonsLabel.setText(String.valueOf(seasons));
        episodesLabel.setText(String.valueOf(episodes));
        
        if (episodeRuntimes != null && !episodeRuntimes.isEmpty()) {
            // Calculate average runtime
            int avgRuntime = (int) episodeRuntimes.stream()
                .mapToInt(Integer::intValue)
                .average()
                .orElse(0);
            episodeRuntimeLabel.setText(avgRuntime > 0 ? formatRuntime(avgRuntime) + " (avg)" : "N/A");
        } else {
            episodeRuntimeLabel.setText("N/A");
        }
        
        networksLabel.setText(networks != null ? String.join(", ", networks) : "N/A");
    }
    
    // Set movie specific details
    public void setMovieDetails(long budget, long revenue) {
        budgetLabel.setText(budget > 0 ? formatCurrency(budget) : "N/A");
        revenueLabel.setText(revenue > 0 ? formatCurrency(revenue) : "N/A");
    }
    
    // Helper method to format currency
    private String formatCurrency(long amount) {
        return NumberFormat.getCurrencyInstance(Locale.US).format(amount);
    }

    // Handle close button action
    @FXML
    private void handleClose() {
        // Close the current window
        Stage stage = (Stage) titleLabel.getScene().getWindow();
        stage.close();
    }

    // Navigation
    @FXML
    private void goBack() {
        NavigationService.getInstance().goBack();
    }

    /**
     * Toggle edit mode for the content details
     * @param editMode true to enable editing, false to disable
     */
    public void setEditMode(boolean editMode) {
        if (editMode) {
            // Show a dialog indicating edit mode is enabled
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Edit Mode");
            alert.setHeaderText("Edit Mode Enabled");
            alert.setContentText("You are now in edit mode. Make your changes and click 'Save' when done.");
            alert.showAndWait();
            
            //  enable form fields for editing here
            //titleField.setEditable(true);
            //overviewArea.setEditable(true);
            // etc.
        }
    }
    public void setContentId(String id) {
        this.contentId = id;
    }

    public void setContent(Content content) {
    }
}
