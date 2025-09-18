package com.papel.imdb_clone.controllers;

import com.papel.imdb_clone.data.SearchCriteria;
import com.papel.imdb_clone.enums.ContentType;
import com.papel.imdb_clone.enums.Genre;
import com.papel.imdb_clone.model.Content;
import com.papel.imdb_clone.service.NavigationService;
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
import com.papel.imdb_clone.controllers.coordinator.UICoordinator;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Calendar;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

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
    
    /**
     * Navigates back to the home view.
     */
    @FXML
    public void goToHome() {
        try {
            NavigationService navigationService = NavigationService.getInstance();
            navigationService.navigateTo("/fxml/home-view.fxml", 
                (Stage) resultsTable.getScene().getWindow(),
                "IMDb Clone - Home");
        } catch (Exception e) {
            logger.error("Error navigating to home view", e);
            UIUtils.showError("Navigation Error", "Failed to navigate to home view: " + e.getMessage());
        }
    }
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
        try {
            // First ensure ServiceLocator is initialized
            ServiceLocator serviceLocator = ServiceLocator.getInstance();
            
            // Now get the SearchService
            this.searchService = ServiceLocator.getService(SearchService.class);
            if (this.searchService == null) {
                String errorMsg = "Failed to initialize SearchService: service is null";
                logger.error(errorMsg);
                throw new IllegalStateException(errorMsg);
            }
            
            this.query = "";  // Initialize query with empty string
            logger.info("AdvancedSearchController initialized with SearchService");
            
        } catch (Exception e) {
            String errorMsg = String.format("Error initializing AdvancedSearchController: %s", e.getMessage());
            logger.error(errorMsg, e);
            throw new RuntimeException(errorMsg, e);
        }
    }

    @FXML
    public void initialize() {
        // setup table columns
        setupTableColumns();
        setupGenreComboBox();
        setupRatingSlider();
        setupYearFrom();
        setupYearTo();
        setupCheckBoxes();
        
        // Only setup sort options if sortByCombo is available
        if (sortByCombo != null) {
            setupSortOptions();
        } else {
            logger.debug("Sort combo box is not available in the view, skipping sort options setup");
        }
    }

    private void setupYearFrom() {
        yearFrom.setPromptText("Year from");
    }

    private void setupYearTo() {
        yearTo.setPromptText("Year to");
    }

    private void setupCheckBoxes() {
        movieCheckBox.setSelected(true);
        seriesCheckBox.setSelected(true);
    }

    private void setupTableColumns() {
        try {
            // Set up title column with null check and tooltip
            resultTitleColumn.setCellValueFactory(cellData -> {
                Content content = cellData.getValue();
                String title = (content != null && content.getTitle() != null) ? content.getTitle() : "N/A";
                return new SimpleStringProperty(title);
            });
            
            // Add tooltip to title column
            resultTitleColumn.setCellFactory(column -> new TableCell<Content, String>() {
                // override updateItem to add tooltip
                @Override
                protected void updateItem(String item, boolean empty) {
                    super.updateItem(item, empty);
                    if (item == null || empty) {
                        setText(null);
                        setTooltip(null);
                    } else {
                        setText(item);
                        Tooltip tooltip = new Tooltip(item);
                        tooltip.setStyle("-fx-font-size: 12px; -fx-padding: 5px;");
                        setTooltip(tooltip);
                    }
                }
            });
            
            // Set up type column with proper styling
            resultTypeColumn.setCellValueFactory(cellData -> {
                Content content = cellData.getValue();
                if (content == null) return new SimpleStringProperty("N/A");
                String type = content instanceof com.papel.imdb_clone.model.Movie ? "Movie" : "Series";
                return new SimpleStringProperty(type);
            });
            
            // Set up year column with proper null checks and formatting
            resultYearColumn.setCellValueFactory(cellData -> {
                Content content = cellData.getValue();
                if (content == null) return new SimpleIntegerProperty(0).asObject();
                
                int year = content.getStartYear();
                if (year <= 0 && content.getYear() != null) {
                    try {
                        Calendar cal = Calendar.getInstance();
                        cal.setTime(content.getYear());
                        year = cal.get(Calendar.YEAR);
                    } catch (Exception e) {
                        logger.warn("Error parsing year for content: {}", content.getTitle(), e);
                    }
                }
                return new SimpleIntegerProperty(Math.max(year, 0)).asObject();
            });
            
            // Set up genre column with better null handling and formatting
            resultGenreColumn.setCellValueFactory(cellData -> {
                try {
                    Content content = cellData.getValue();
                    if (content == null) {
                        return new SimpleStringProperty("No Content");
                    }
                    
                    List<Genre> genres = content.getGenres();
                    if (genres == null || genres.isEmpty()) {
                        return new SimpleStringProperty("No genres");
                    }
                    
                    String genreString = genres.stream()
                        .filter(Objects::nonNull)
                        .map(genre -> {
                            try {
                                try {
                                    String displayName = (String) genre.getDisplayName();
                                    if (displayName != null && !displayName.trim().isEmpty()) {
                                        return displayName.trim();
                                    }
                                    // Fallback to enum name formatting if display name is not available
                                    String name = genre.name();
                                    if (!name.trim().isEmpty()) {
                                        return name.charAt(0) + name.substring(1).toLowerCase();
                                    }
                                } catch (Exception e) {
                                    logger.debug("Error processing genre display name", e);
                                }
                                return "";
                            } catch (Exception e) {
                                logger.warn("Error formatting genre: {}", genre, e);
                                return "";
                            }
                        })
                        .filter(genre -> !genre.isEmpty())
                        .distinct()
                        .collect(Collectors.joining(", "));
                    
                    return new SimpleStringProperty(genreString.isEmpty() ? "No genres" : genreString);
                } catch (Exception e) {
                    logger.warn("Error getting genres for content", e);
                    return new SimpleStringProperty("No genres");
                }
            });
            
            // Set up IMDb rating column with proper null check and formatting
            resultImdbColumn.setCellValueFactory(cellData -> {
                Content content = cellData.getValue();
                if (content == null) return new SimpleStringProperty("No rating");
                
                try {
                    Double rating = content.getImdbRating();
                    if (rating == null || rating <= 0) {
                        return new SimpleStringProperty("No rating");
                    }
                    return new SimpleStringProperty(String.format("%.1f", rating));
                } catch (Exception e) {
                    logger.warn("Error getting rating for content: {}", content.getTitle(), e);
                    return new SimpleStringProperty("No rating");
                }
            });
            
            // Set up row factory for better row highlighting and readablity
            resultsTable.setRowFactory(tv -> {
                TableRow<Content> row = new TableRow<>();
                row.hoverProperty().addListener((obs, wasHovered, isNowHovered) -> {
                    if (isNowHovered && !row.isEmpty()) {
                        row.setStyle("-fx-background-color: #f0f0f0; -fx-cursor: hand;");
                    } else {
                        row.setStyle("");
                    }
                });
                return row;
            });
            
            // Set up row factory to handle row clicks
            resultsTable.setRowFactory(tv -> {
                TableRow<Content> row = new TableRow<>();
                row.setOnMouseClicked(event -> {
                    if (event.getClickCount() == 2 && !row.isEmpty()) {
                        Content rowData = row.getItem();
                        // Handle double click on row
                        if (rowData != null) {
                            try {
                                // Open content details view
                                try {
                                    // Use the application's navigation method if available
                                    logger.info("Navigating to content details for: {}", rowData.getTitle());
                                    // Example: mainController.showContentDetails(rowData);
                                    UIUtils.showContentDetails(rowData);
                                } catch (Exception e) {
                                    logger.error("Error navigating to content details", e);
                                    UIUtils.showError("Navigation Error", "Could not open content details: " + e.getMessage());
                                }
                            } catch (Exception e) {
                                logger.error("Error navigating to content details", e);
                                UIUtils.showError("Error", "Could not open content details: " + e.getMessage());
                            }
                        }
                    }
                });
                return row;
            });
            
        } catch (Exception e) {
            logger.error("Error setting up table columns", e);
            UIUtils.showError("Error", "Failed to initialize table columns: " + e.getMessage());
        }
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
                    // build search criteria
                    SearchCriteria criteria = buildSearchCriteria();
                    if (criteria == null) {
                        return FXCollections.observableArrayList();
                    }
                    // perform search and return results
                    List<Content> results = searchService.searchContent(criteria);
                    return FXCollections.observableArrayList(results);
                }
            };

            // Handle successful search completion
            currentSearchTask.setOnSucceeded(event -> {
                ObservableList<Content> results = currentSearchTask.getValue();
                // set results to table
                resultsTable.setItems(results);
                // update results count
                updateResultsCount(results.size());

                // clear status label
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
                    // log genre
                    logger.debug("Setting genre to: {}", genre);
                    // show genre in status label
                    if (statusLabel != null) {
                        statusLabel.setText("Genre: " + genre);
                    }
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

    //reset all filters to default values from the reset filters button
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
