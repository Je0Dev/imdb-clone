package com.papel.imdb_clone.controllers;

import com.papel.imdb_clone.data.SearchCriteria;
import com.papel.imdb_clone.enums.ContentType;
import com.papel.imdb_clone.enums.Genre;
import com.papel.imdb_clone.model.Content;
import com.papel.imdb_clone.service.SearchService;
import com.papel.imdb_clone.service.ServiceLocator;
import com.papel.imdb_clone.util.UIUtils;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Calendar;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * Controller for the advanced search functionality.
 * Handles search operations with various filters and displays results in a table.
 */
public class AdvancedSearchController {
    private static final Logger logger = LoggerFactory.getLogger(AdvancedSearchController.class);

    // Services
    private final SearchService searchService;
    private Task<ObservableList<Content>> currentSearchTask;
    private final long defaultCacheExpiration = 30;
    private final TimeUnit timeUnit = TimeUnit.MINUTES;

    // Search form fields
    @FXML
    private TextField titleField;
    @FXML
    private ComboBox<String> genreComboBox;
    @FXML
    private Slider ratingSlider;
    @FXML
    private Label ratingValueLabel;
    @FXML
    private TextField keywordsField;
    @FXML
    private TextField yearFrom;
    @FXML
    private TextField yearTo;
    @FXML
    private ComboBox<String> sortByCombo;
    @FXML
    private CheckBox movieCheckBox;
    @FXML
    private CheckBox seriesCheckBox;

    // Results display
    @FXML
    private TableView<Content> resultsTable;
    @FXML
    private TableColumn<Content, String> resultTitleColumn;
    @FXML
    private TableColumn<Content, Integer> resultYearColumn;
    @FXML
    private TableColumn<Content, String> resultTypeColumn;
    @FXML
    private TableColumn<Content, String> resultGenreColumn;
    @FXML
    private TableColumn<Content, String> resultImdbColumn;
    @FXML
    private Label statusLabel;
    @FXML
    private Label resultsCountLabel;
    @FXML
    private ProgressIndicator searchProgressIndicator;
    private final String query;
    private Double minRating;

    public AdvancedSearchController() {
        this.searchService = ServiceLocator.getInstance().getService(SearchService.class);
        this.query = "";  // Initialize query with empty string
    }

    @FXML
    public void initialize() {
        setupTableColumns();
        setupGenreComboBox();
        setupRatingSlider();
        setupSortOptions();
    }

    private void setupTableColumns() {
        // Set up table columns
        resultTitleColumn.setCellValueFactory(new PropertyValueFactory<>("title"));
        
        // Set up type column
        resultTypeColumn.setCellValueFactory(cellData -> {
            String type = cellData.getValue() instanceof com.papel.imdb_clone.model.Movie ? "Movie" : "Series";
            return new SimpleStringProperty(type);
        });
        
        // Set up year column to show the correct year value
        resultYearColumn.setCellValueFactory(cellData -> {
            Content content = cellData.getValue();
            int year = content.getStartYear();
            if (year == 0 && content.getYear() != null) {
                Calendar cal = Calendar.getInstance();
                cal.setTime(content.getYear());
                year = cal.get(Calendar.YEAR);
            }
            return new SimpleIntegerProperty(year).asObject();
        });
        
        // Set up genre column
        resultGenreColumn.setCellValueFactory(cellData -> {
            List<Genre> genres = cellData.getValue().getGenres();
            String genreString = genres != null && !genres.isEmpty() ?
                    genres.stream()
                          .map(Genre::getDisplayName)
                          .map(Object::toString)
                          .collect(java.util.stream.Collectors.joining(", ")) : "N/A";
            return new SimpleStringProperty(genreString);
        });
        
        // Set up IMDb rating column with proper null check and formatting
        resultImdbColumn.setCellValueFactory(cellData -> {
            Double rating = cellData.getValue().getImdbRating();
            String ratingStr = (rating != null && rating > 0) ? String.format("%.1f", rating) : "N/A";
            return new SimpleStringProperty(ratingStr);
        });
    }

    private void setupGenreComboBox() {
        genreComboBox.getItems().addAll(
                "", // Empty option
                "Action", "Adventure", "Animation", "Comedy", "Crime",
                "Documentary", "Drama", "Family", "Fantasy", "History",
                "Horror", "Music", "Mystery", "Romance", "Sci-Fi",
                "Thriller", "War", "Western"
        );
    }

    private void setupRatingSlider() {
        ratingSlider.valueProperty().addListener((obs, oldVal, newVal) -> {
            ratingValueLabel.setText(String.format("%.1f", newVal));
        });
    }

    private void setupSortOptions() {
        sortByCombo.getItems().addAll(
                "Relevance", "Title (A-Z)", "Title (Z-A)",
                "Year (Newest)", "Year (Oldest)",
                "Rating (Highest)", "Rating (Lowest)"
        );
        sortByCombo.getSelectionModel().selectFirst();
    }

    /**
     * Handles the search button click event.
     * Performs a search based on the current criteria and updates the results table.
     */
    @FXML
    private void handleSearch() {
        try {
            // Cancel any existing search
            if (currentSearchTask != null && currentSearchTask.isRunning()) {
                currentSearchTask.cancel();
            }

            // Show loading indicator
            searchProgressIndicator.setVisible(true);
            if (statusLabel != null) {
                statusLabel.setText("Searching...");
            }

            // Create and configure the search task
            currentSearchTask = new Task<ObservableList<Content>>() {
                @Override
                protected ObservableList<Content> call() throws Exception {
                    SearchCriteria criteria = buildSearchCriteria();
                    if (criteria == null) {
                        return FXCollections.observableArrayList();
                    }
                    List<Content> results = searchService.searchContent(criteria);
                    return FXCollections.observableArrayList(results);
                }
            };

            // Handle successful search completion
            currentSearchTask.setOnSucceeded(event -> {
                ObservableList<Content> results = currentSearchTask.getValue();
                resultsTable.setItems(results);
                updateResultsCount(results.size());
                searchProgressIndicator.setVisible(false);

                if (statusLabel != null) {
                    statusLabel.setText("");
                }

                if (results.isEmpty()) {
                    UIUtils.showInfo("No Results", "No content found matching your criteria.");
                }
            });

            // Handle search failure
            currentSearchTask.setOnFailed(event -> {
                searchProgressIndicator.setVisible(false);
                if (statusLabel != null) {
                    statusLabel.setText("Search failed");
                }

                Throwable exception = currentSearchTask.getException();
                logger.error("Search failed", exception);
                UIUtils.showError("Search Error", "An error occurred while performing the search: " +
                        (exception != null ? exception.getMessage() : "Unknown error"));
            });

            // Start the search in a background thread
            Thread searchThread = new Thread(currentSearchTask, "Search-Thread");
            searchThread.setDaemon(true);
            searchThread.start();

        } catch (Exception e) {
            searchProgressIndicator.setVisible(false);
            logger.error("Error performing search: {}", e.getMessage(), e);
            UIUtils.showError("Error", "Failed to perform search: " + e.getMessage());
        }
    }

    /**
     * Builds a SearchCriteria object based on the current UI state.
     *
     * @return Configured SearchCriteria object or null if there was an error
     */
    private SearchCriteria buildSearchCriteria() {
        try {
            String keywords = keywordsField.getText().trim();

            // Get selected content types
            ContentType contentType = null;
            if (movieCheckBox.isSelected() && seriesCheckBox.isSelected()) {
                // Will search both types, so leave contentType as null
            } else if (movieCheckBox.isSelected()) {
                contentType = ContentType.MOVIE;
            } else if (seriesCheckBox.isSelected()) {
                contentType = ContentType.SERIES;
            } else {
                UIUtils.showError("Error", "Please select at least one content type (Movie/Series)");
                return null;
            }

            // Create criteria with basic filters
            SearchCriteria criteria = new SearchCriteria(keywords);

            // Parse year range - use null for no filter instead of default values
            Integer yearFromValue = null;
            Integer yearToValue = null;
            try {
                if (!yearFrom.getText().isEmpty()) {
                    yearFromValue = Integer.parseInt(yearFrom.getText().trim());
                }
                if (!yearTo.getText().isEmpty()) {
                    yearToValue = Integer.parseInt(yearTo.getText().trim());
                }
            } catch (NumberFormatException e) {
                logger.warn("Invalid year format", e);
                UIUtils.showError("Invalid Year", "Please enter valid year values");
                return null;
            }

            // Set year range
            if (yearFromValue != null) {
                criteria.setMinYear(yearFromValue);
            }
            if (yearToValue != null) {
                criteria.setMaxYear(yearToValue);
            }

            // Set rating filter - only apply if slider is moved from 0
            double minRating = ratingSlider.getValue();
            if (minRating > 0.1) {  // Small threshold to account for floating point imprecision
                criteria.setMinRating(minRating);
                logger.debug("Setting min rating to: {}", minRating);
            }

            // Set genre if selected
            if (genreComboBox.getValue() != null && !genreComboBox.getValue().isEmpty()) {
                try {
                    String genreValue = genreComboBox.getValue().toUpperCase().replace("-", "_");
                    Genre genre = Genre.valueOf(genreValue);
                    criteria.setGenre(genre);
                } catch (IllegalArgumentException e) {
                    logger.warn("Invalid genre selected: {}", genreComboBox.getValue(), e);
                }
            }

            // Set content type if specified (null means search both)
            criteria.setContentType(contentType);

            return criteria;
        } catch (Exception e) {
            logger.error("Error building search criteria", e);
            UIUtils.showError("Error", "An error occurred while building search criteria");
            return null;
        }
    }

    private void updateResultsCount(int count) {
        if (resultsCountLabel != null) {
            resultsCountLabel.setText(String.valueOf(count));
        }
    }

    @FXML
    private void resetFilters() {
        keywordsField.clear();
        movieCheckBox.setSelected(true);
        seriesCheckBox.setSelected(true);
        yearFrom.clear();
        yearTo.clear();
        genreComboBox.getSelectionModel().clearSelection();
        ratingSlider.setValue(0);
        sortByCombo.getSelectionModel().selectFirst();
    }
}
