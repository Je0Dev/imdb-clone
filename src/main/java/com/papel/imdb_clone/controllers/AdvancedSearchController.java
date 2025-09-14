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
    // labels
    @FXML
    private Label statusLabel;
    @FXML
    private Label resultsCountLabel;
    // progress indicator
    @FXML
    private ProgressIndicator searchProgressIndicator;
    // search query
    private final String query;
    // min and max rating
    private Double minRating;
    private Double maxRating;

    // constructor
    public AdvancedSearchController() {
        this.searchService = ServiceLocator.getInstance().getService(SearchService.class);
        this.query = "";  // Initialize query with empty string
    }

    @FXML
    public void initialize() {
        // setup table columns
        setupTableColumns();
        setupGenreComboBox();
        setupRatingSlider();
        
        // Only setup sort options if sortByCombo is available
        if (sortByCombo != null) {
            setupSortOptions();
        } else {
            logger.debug("Sort combo box is not available in the view, skipping sort options setup");
        }
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
            // if genres is null or empty, return "N/A"
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

    // setup genre combo box
    private void setupGenreComboBox() {
        genreComboBox.getItems().addAll(
                "", // Empty option
                "Action", "Adventure", "Animation", "Comedy", "Crime",
                "Documentary", "Drama", "Family", "Fantasy", "History",
                "Horror", "Music", "Mystery", "Romance", "Sci-Fi",
                "Thriller", "War", "Western"
        );
    }

    // setup rating slider
    private void setupRatingSlider() {
        ratingSlider.valueProperty().addListener((obs, oldVal, newVal) -> {
            ratingValueLabel.setText(String.format("%.1f", newVal));
        });
    }

    // setup sort options
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

                // show no results message if no results
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

                // show error message if search fails
                Throwable exception = currentSearchTask.getException();
                logger.error("Search failed", exception);
                UIUtils.showError("Search Error", "An error occurred while performing the search: " +
                        (exception != null ? exception.getMessage() : "Unknown error"));
            });

            // Start the search in a background thread
            Thread searchThread = new Thread(currentSearchTask, "Search-Thread");
            // set thread as daemon,which will not prevent the application from exiting
            searchThread.setDaemon(true);
            searchThread.start();

        } catch (Exception e) {
            // show error message if search fails
            logger.error("Error performing search: {}", e.getMessage(), e);
            UIUtils.showError("Error", "Failed to perform search: " + e.getMessage());
        }
    }

    /**
     * Builds a SearchCriteria object based on the current UI state.
     *
     * @return Configured SearchCriteria object or null if there was an error
     * @throws IllegalArgumentException if there is an invalid input
     * @throws RuntimeException if there is an error building the search criteria
     */
    private SearchCriteria buildSearchCriteria() throws IllegalArgumentException, RuntimeException {
        try {
            // Get keywords,which is optional
            String keywords = keywordsField.getText().trim();

            // Get selected content types,which is optional and can be either movie or series
            ContentType contentType = null;
            if (movieCheckBox.isSelected() && seriesCheckBox.isSelected()) {
                // Will search both types, so leave contentType as null
            } else if (movieCheckBox.isSelected()) {
                contentType = ContentType.MOVIE;
            } else if (seriesCheckBox.isSelected()) {
                contentType = ContentType.SERIES;
            } else {
                // show error message if no content type is selected
                UIUtils.showError("Error", "Please select at least one content type (Movie/Series)");
                return null;
            }

            // Create criteria with optional keywords and content type
            SearchCriteria criteria = new SearchCriteria(contentType,keywords);

            // Parse year range - use null for no filter instead of default values
            Integer yearFromValue = null;
            Integer yearToValue = null;
            try {
                // parse year range both from and to values
                if (!yearFrom.getText().isEmpty()) {
                    yearFromValue = Integer.parseInt(yearFrom.getText().trim());
                }
                if (!yearTo.getText().isEmpty()) {
                    yearToValue = Integer.parseInt(yearTo.getText().trim());
                }
            } catch (NumberFormatException e) {
                // show error message if year is not valid
                logger.warn("Invalid year format", e);
                UIUtils.showError("Invalid Year", "Please enter valid year values");
                return null;
            }

            // Set year range both for max and min year
            if (yearFromValue != null) {
                criteria.setMinYear(yearFromValue);
            }
            if (yearToValue != null) {
                criteria.setMaxYear(yearToValue);
            }

            // Set rating filter - only apply if slider is moved from 0.
            double minRating = ratingSlider.getValue();
            if (minRating > 0.1) {  // Small threshold to account for floating point imprecision
                criteria.setMinRating(minRating);
                logger.debug("Setting min rating to: {}", minRating);
            }

            // Set genre if selected
            if (genreComboBox.getValue() != null && !genreComboBox.getValue().isEmpty()) {
                try {
                    // convert genre to uppercase and replace hyphens with underscores
                    String genreValue = genreComboBox.getValue().toUpperCase().replace("-", "_");
                    // set genre to criteria
                    Genre genre = Genre.valueOf(genreValue);
                    criteria.setGenre(genre);
                } catch (IllegalArgumentException e) {
                    logger.warn("Invalid genre selected: {}", genreComboBox.getValue(), e);
                }
            }

            // Set content type if specified (null means search both movies and series)
            criteria.setContentType(contentType);

            return criteria;
        } catch (Exception e) {
            // show error message if search criteria building fails
            logger.error("Error building search criteria", e);
            UIUtils.showError("Error", "An error occurred while building search criteria");
            return null;
        }
    }

    //update results count label,which shows the number of results found
    private void updateResultsCount(int count) {
        if (resultsCountLabel != null) {
            resultsCountLabel.setText(String.valueOf(count));
        }
    }

    //reset all filters to default values
    @FXML
    private void resetFilters() {
        try {
            //clear all text fields
            keywordsField.clear();
            movieCheckBox.setSelected(true);
            seriesCheckBox.setSelected(true);
            yearFrom.clear();
            yearTo.clear();
            //clear genre selection
            if (genreComboBox != null) {
                genreComboBox.getSelectionModel().clearSelection();
            }
            //reset rating slider to 0
            if (ratingSlider != null) {
                ratingSlider.setValue(0);
            }
            //reset sort by combo box to first item,if it's not empty
            if (sortByCombo != null && !sortByCombo.getItems().isEmpty()) {
                sortByCombo.getSelectionModel().selectFirst();
            }
        } catch (Exception e) {
            logger.error("Error resetting filters: ", e);
        }
    }
}
