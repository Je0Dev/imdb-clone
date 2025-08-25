package com.papel.imdb_clone.controllers;

import com.papel.imdb_clone.data.SearchCriteria;
import com.papel.imdb_clone.enums.ContentType;
import com.papel.imdb_clone.enums.Genre;
import com.papel.imdb_clone.model.Content;
import com.papel.imdb_clone.service.SearchService;
import com.papel.imdb_clone.service.ServiceLocator;
import com.papel.imdb_clone.util.UIUtils;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collections;
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
    // Search form fields
    @FXML
    private TextField titleField;
    @FXML
    private ComboBox<String> genreComboBox;
    @FXML
    private TextField actorField;
    @FXML
    private TextField directorField;
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
    private TextField descriptionField;

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
    private TableColumn<Content, String> resultDirectorColumn;
    @FXML
    private Label statusLabel;
    @FXML
    private Label resultsCountLabel;
    @FXML
    private ProgressIndicator searchProgressIndicator;
    @FXML
    private CheckBox movieCheckBox;
    @FXML
    private CheckBox seriesCheckBox;

    // Pagination
    @FXML
    private Button prevPageButton;
    @FXML
    private Button nextPageButton;
    @FXML
    private Label pageInfoLabel;

    private int currentPage = 1;
    private final int itemsPerPage = 10;
    private final int totalItems = 0;

    public AdvancedSearchController() {
        this.searchService = ServiceLocator.getInstance().getService(SearchService.class);
    }

    @FXML
    public void initialize() {
        setupTableColumns();
        setupGenreComboBox();
        setupRatingSlider();
        updatePageInfo();
        setupSortOptions();
    }

    private void setupTableColumns() {
        // Set up table columns
        resultTitleColumn.setCellValueFactory(new PropertyValueFactory<>("title"));
        resultTypeColumn.setCellValueFactory(cellData -> {
            String type = cellData.getValue() instanceof com.papel.imdb_clone.model.Movie ? "Movie" : "Series";
            return new SimpleStringProperty(type);
        });
        resultYearColumn.setCellValueFactory(new PropertyValueFactory<>("year"));
        resultGenreColumn.setCellValueFactory(cellData -> {
            List<Genre> genres = cellData.getValue().getGenres();
            String genreString = genres != null && !genres.isEmpty() ?
                    genres.stream().map(Genre::name).collect(java.util.stream.Collectors.joining(", ")) : "N/A";
            return new SimpleStringProperty(genreString);
        });
        resultImdbColumn.setCellValueFactory(cellData ->
                new SimpleStringProperty(String.format("%.1f", cellData.getValue().getImdbRating())));
        resultDirectorColumn.setCellValueFactory(cellData -> {
            String director = cellData.getValue().getDirector();
            return new SimpleStringProperty(director != null ? director : "N/A");
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

    @FXML
    private void handleSearch() {
        try {
            if (currentSearchTask != null && !currentSearchTask.isDone()) {
                currentSearchTask.cancel(true);
            }

            SearchCriteria criteria = buildSearchCriteria();
            if (criteria == null) {
                return; // Validation failed
            }

            searchProgressIndicator.setVisible(true);
            statusLabel.setText("Searching...");

            currentSearchTask = new Task<>() {
                @Override
                protected ObservableList<Content> call() throws Exception {
                    List<Content> results = searchService.searchContent(criteria);
                    // Apply pagination
                    int fromIndex = (currentPage - 1) * itemsPerPage;
                    int toIndex = Math.min(fromIndex + itemsPerPage, results.size());
                    return FXCollections.observableArrayList(
                            fromIndex < toIndex ? results.subList(fromIndex, toIndex) : Collections.emptyList()
                    );
                }
            };

            currentSearchTask.setOnSucceeded(e -> {
                ObservableList<Content> results = currentSearchTask.getValue();
                resultsTable.setItems(results);
                updateResultsCount(results.size());
                searchProgressIndicator.setVisible(false);
                statusLabel.setText("");
            });

            currentSearchTask.setOnFailed(e -> {
                Throwable ex = currentSearchTask.getException();
                logger.error("Search failed", ex);
                statusLabel.setText("Search failed: " + ex.getMessage());
                searchProgressIndicator.setVisible(false);
            });

            new Thread(currentSearchTask).start();
        } catch (Exception e) {
            logger.error("Error in handleSearch", e);
            statusLabel.setText("Error: " + e.getMessage());
            searchProgressIndicator.setVisible(false);
        }
    }

    private SearchCriteria buildSearchCriteria() {
        try {
            String keywords = keywordsField.getText().trim();

            // Get selected content types
            List<String> contentTypes = List.of();
            if (movieCheckBox.isSelected() && seriesCheckBox.isSelected()) {
                contentTypes = List.of("movie", "series");
            } else if (movieCheckBox.isSelected()) {
                contentTypes = List.of("movie");
            } else if (seriesCheckBox.isSelected()) {
                contentTypes = List.of("series");
            } else {
                UIUtils.showError("Error", "Please select at least one content type (Movie/Series)");
                return null;
            }

            // Validate year inputs
            Integer yearFromValue = null;
            if (!yearFrom.getText().trim().isEmpty()) {
                try {
                    yearFromValue = Integer.parseInt(yearFrom.getText().trim());
                } catch (NumberFormatException e) {
                    UIUtils.showError("Invalid Input", "Please enter a valid start year");
                    return null;
                }
            }

            Integer yearToValue = null;
            if (!yearTo.getText().trim().isEmpty()) {
                try {
                    yearToValue = Integer.parseInt(yearTo.getText().trim());
                } catch (NumberFormatException e) {
                    UIUtils.showError("Invalid Input", "Please enter a valid end year");
                    return null;
                }
            }

            // Validate year range
            if (yearFromValue != null && yearToValue != null && yearFromValue > yearToValue) {
                UIUtils.showError("Invalid Range", "Start year cannot be greater than end year");
                return null;
            }

            // Create SearchCriteria with required parameters
            SearchCriteria criteria = new SearchCriteria(
                    keywords,
                    actorField.getText().trim().isEmpty() ? null : actorField.getText().trim(),
                    directorField.getText().trim().isEmpty() ? null : directorField.getText().trim(),
                    ratingSlider.getValue() > 0 ? ratingSlider.getValue() : null,
                    10.0, // max rating
                    null, // min user rating
                    null, // max user rating
                    null, // min duration
                    null, // max duration
                    new ArrayList<>(), // empty genres list for now
                    descriptionField.getText().trim().isEmpty() ? null : descriptionField.getText().trim(),
                    yearFromValue,
                    yearToValue
            );

            // Set genres if any selected
            if (genreComboBox.getValue() != null && !genreComboBox.getValue().isEmpty()) {
                try {
                    Genre genre = Genre.valueOf(genreComboBox.getValue().toUpperCase().replace("-", "_"));
                    criteria.setGenre(genre);
                } catch (IllegalArgumentException e) {
                    logger.warn("Invalid genre selected: " + genreComboBox.getValue());
                }
            }

            // Set content type if specified
            if (!contentTypes.isEmpty()) {
                try {
                    String type = contentTypes.get(0); // Get first content type if multiple
                    ContentType contentType = ContentType.valueOf(type.toUpperCase());
                    criteria.setContentType(contentType);
                } catch (IllegalArgumentException e) {
                    logger.warn("Invalid content type: " + contentTypes);
                }
            }

            return criteria;
        } catch (Exception e) {
            logger.error("Error building search criteria", e);
            UIUtils.showError("Error", "An error occurred while building search criteria");
            return null;
        }
    }

    @FXML
    private void nextPage() {
        currentPage++;
        updatePageInfo();
        handleSearch();
    }

    @FXML
    private void previousPage() {
        if (currentPage > 1) {
            currentPage--;
            updatePageInfo();
            handleSearch();
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
        currentPage = 1;
        updatePageInfo();
    }


    private void updateResultsCount(int count) {
        resultsCountLabel.setText(String.valueOf(count));
    }

    private void updatePageInfo() {
        pageInfoLabel.setText(String.format("Page %d", currentPage));
        prevPageButton.setDisable(currentPage <= 1);
    }

}
