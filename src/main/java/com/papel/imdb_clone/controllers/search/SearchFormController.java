package com.papel.imdb_clone.controllers.search;

import com.papel.imdb_clone.enums.Genre;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.CheckBoxListCell;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * Controller for handling the search form functionality.
 */
public class SearchFormController extends BaseSearchController {
    private static final Logger logger = LoggerFactory.getLogger(SearchFormController.class);
    
    // Form fields
    @FXML private TextField titleField;
    @FXML private ComboBox<String> genreComboBox;
    @FXML private Slider ratingSlider;
    @FXML private Label ratingValueLabel;
    @FXML private TextField keywordsField;
    @FXML private TextField yearFrom;
    @FXML private TextField yearTo;
    @FXML private ComboBox<String> sortByCombo;
    @FXML private CheckBox movieCheckBox;
    @FXML private CheckBox seriesCheckBox;
    
    private final ObservableList<String> selectedGenres = FXCollections.observableArrayList();
    private SearchFormListener searchFormListener;
    
    public interface SearchFormListener {
        void onSearchCriteriaChanged(SearchCriteria criteria);
        void onSearchRequested(SearchCriteria criteria);
    }
    
    public void setSearchFormListener(SearchFormListener listener) {
        this.searchFormListener = listener;
    }
    
    @FXML
    /**
     * Initializes the search form with default values and configurations.
     * This method should be called after the FXML fields have been injected.
     */
    public void initializeForm() {
        // Clear all fields
        if (titleField != null) titleField.clear();
        if (keywordsField != null) keywordsField.clear();
        if (yearFrom != null) yearFrom.clear();
        if (yearTo != null) yearTo.clear();
        
        // Set default values
        if (movieCheckBox != null) movieCheckBox.setSelected(true);
        if (seriesCheckBox != null) seriesCheckBox.setSelected(true);
        
        // Initialize rating slider if available
        if (ratingSlider != null) {
            ratingSlider.setValue(0);
            updateRatingLabel(0);
        }
        
        // Initialize sort options if available
        if (sortByCombo != null) {
            sortByCombo.getItems().clear();
            sortByCombo.getItems().addAll("Relevance", "Title (A-Z)", "Title (Z-A)", "Year (Newest)", "Year (Oldest)", "Rating (High to Low)");
            sortByCombo.getSelectionModel().selectFirst();
        }
        
        // Initialize genre selection
        if (genreComboBox != null) {
            selectedGenres.clear();
            updateGenrePrompt();
        }
        
        logger.debug("Search form initialized with default values");
    }

    private void updateRatingLabel(int i) {
        if (ratingValueLabel != null) ratingValueLabel.setText(String.format("%.1f", (float) i));
        if (ratingSlider != null) ratingSlider.setValue(i);
    }

    @FXML
    public void initialize() {
        setupGenreComboBox();
        setupRatingSlider();
        setupSortOptions();
        setupYearFields();
        
        // Set default values
        movieCheckBox.setSelected(true);
        seriesCheckBox.setSelected(true);
    }
    
    private void setupYearFields() {
        yearFrom.setPromptText("Year from");
        yearTo.setPromptText("Year to");
        
        // Add numeric validation
        yearFrom.textProperty().addListener((obs, oldVal, newVal) -> {
            if (!newVal.matches("\\d*") && !newVal.isEmpty()) {
                yearFrom.setText(oldVal);
            } else if (newVal.length() > 4) {
                yearFrom.setText(oldVal);
            }
        });
        
        yearTo.textProperty().addListener((obs, oldVal, newVal) -> {
            if (!newVal.matches("\\d*") && !newVal.isEmpty()) {
                yearTo.setText(oldVal);
            } else if (newVal.length() > 4) {
                yearTo.setText(oldVal);
            }
        });
    }
    
    private void setupGenreComboBox() {
        // Set up the cell factory to display checkboxes
        genreComboBox.setCellFactory(lv -> new ListCell<>() {
            private final CheckBox checkBox = new CheckBox();
            
            {
                checkBox.selectedProperty().addListener((obs, wasSelected, isNowSelected) -> {
                    String item = getItem();
                    if (item != null) {
                        if (isNowSelected) {
                            if (!selectedGenres.contains(item)) {
                                selectedGenres.add(item);
                            }
                        } else {
                            selectedGenres.remove(item);
                        }
                        updateGenrePrompt();
                    }
                });
            }
            
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setGraphic(null);
                } else {
                    checkBox.setText(item);
                    checkBox.setSelected(selectedGenres.contains(item));
                    setGraphic(checkBox);
                    setText(null);
                }
            }
        });
        
        // Add all genres to the combo box
        genreComboBox.getItems().addAll(
            "Action", "Adventure", "Animation", "Comedy", "Crime",
            "Documentary", "Drama", "Family", "Fantasy", "History",
            "Horror", "Music", "Mystery", "Romance", "Sci-Fi",
            "Thriller", "War", "Western"
        );
        
        // Update the prompt text initially
        updateGenrePrompt();
    }
    
    private void updateGenrePrompt() {
        if (selectedGenres.isEmpty()) {
            genreComboBox.setPromptText("Select genres...");
            genreComboBox.setValue("");
        } else if (selectedGenres.size() == 1) {
            genreComboBox.setPromptText(selectedGenres.get(0));
            genreComboBox.setValue(selectedGenres.get(0));
        } else {
            genreComboBox.setPromptText(selectedGenres.size() + " genres selected");
            genreComboBox.setValue("");
        }
    }
    
    private void setupRatingSlider() {
        if (ratingSlider != null && ratingValueLabel != null) {
            ratingSlider.valueProperty().addListener((obs, oldVal, newVal) -> {
                ratingValueLabel.setText(String.format("%.1f", newVal));
            });
        }
    }
    
    private void setupSortOptions() {
        if (sortByCombo != null) {
            sortByCombo.getItems().addAll(
                "Relevance", "Title (A-Z)", "Title (Z-A)",
                "Year (Newest)", "Year (Oldest)",
                "Rating (Highest)", "Rating (Lowest)"
            );
            sortByCombo.getSelectionModel().selectFirst();
        }
    }
    
    @FXML
    private void handleSearch() {
        SearchCriteria criteria = buildSearchCriteria();
        if (searchFormListener != null) {
            searchFormListener.onSearchRequested(criteria);
        }
    }
    
    @FXML
    void handleReset() {
        resetSearchForm();
        if (searchFormListener != null) {
            searchFormListener.onSearchRequested(new SearchCriteria(""));
        }
    }
    
    public SearchCriteria buildSearchCriteria() {
        try {
            // Initialize with default values
            String query = "";
            String title = "";
            Double minRating = null;
            Double maxRating = null;
            Integer yearFromValue = null;
            Integer yearToValue = null;
            
            // Set title/query if provided
            if (titleField != null && !titleField.getText().trim().isEmpty()) {
                title = titleField.getText().trim();
                query = title;
            }
            
            // Set keywords if provided
            if (keywordsField != null && !keywordsField.getText().trim().isEmpty()) {
                title = keywordsField.getText().trim();
                query = title;
            }
            
            // Set content type if any selected
            com.papel.imdb_clone.enums.ContentType contentType = null;
            if (movieCheckBox != null && movieCheckBox.isSelected() && 
                (seriesCheckBox == null || !seriesCheckBox.isSelected())) {
                contentType = com.papel.imdb_clone.enums.ContentType.MOVIE;
            } else if (seriesCheckBox != null && seriesCheckBox.isSelected() && 
                      (movieCheckBox == null || !movieCheckBox.isSelected())) {
                contentType = com.papel.imdb_clone.enums.ContentType.TV_SERIES;
            }
            
            // Set year range if provided
            try {
                if (yearFrom != null && !yearFrom.getText().trim().isEmpty()) {
                    yearFromValue = Integer.parseInt(yearFrom.getText().trim());
                }
                if (yearTo != null && !yearTo.getText().trim().isEmpty()) {
                    yearToValue = Integer.parseInt(yearTo.getText().trim());
                }
            } catch (NumberFormatException e) {
                logger.warn("Invalid year format", e);
            }
            
            // Set rating if slider is used
            if (ratingSlider != null && ratingSlider.getValue() > 0) {
                minRating = ratingSlider.getValue();
                maxRating = 10.0; // Default max rating
            }
            
            // Set genres if any selected
            List<Genre> genres = selectedGenres.stream()
                .map(genreName -> {
                    try {
                        return Genre.valueOf(genreName.toUpperCase());
                    } catch (IllegalArgumentException e) {
                        logger.warn("Invalid genre selected: " + genreName, e);
                        return null;
                    }
                })
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
            
            // Create SearchCriteria with the most appropriate constructor
            SearchCriteria criteria;
            if (contentType != null) {
                criteria = new SearchCriteria(contentType, title);
            } else {
                criteria = new SearchCriteria(query, minRating, maxRating, genres, yearFromValue);
            }
            
            // Set additional properties
            if (yearToValue != null) {
                criteria.setMaxYear(yearToValue);
            }

            // Set sort order if specified
            if (sortByCombo != null && sortByCombo.getValue() != null) {
                String sortBy = sortByCombo.getValue();
                if (sortBy.startsWith("Title")) {
                    criteria = new SearchCriteria("title", sortBy.contains("(A-Z)"));
                } else if (sortBy.startsWith("Year")) {
                    criteria = new SearchCriteria("year", sortBy.contains("Newest") ? "desc" : "asc");
                } else if (sortBy.startsWith("Rating")) {
                    criteria = new SearchCriteria("rating", sortBy.contains("Highest") ? "desc" : "asc");
                }
            }

            return criteria;

        } catch (Exception e) {
            logger.error("Error building search criteria", e);
            showError("Search Error", "Failed to build search criteria: " + e.getMessage());
            return new SearchCriteria("");
        }
    }
    
    private void resetSearchForm() {
        // Clear input fields
        if (titleField != null) titleField.clear();
        if (keywordsField != null) keywordsField.clear();
        if (yearFrom != null) yearFrom.clear();
        if (yearTo != null) yearTo.clear();
        
        // Reset checkboxes
        if (movieCheckBox != null) movieCheckBox.setSelected(true);
        if (seriesCheckBox != null) seriesCheckBox.setSelected(true);
        
        // Reset rating slider
        if (ratingSlider != null) {
            ratingSlider.setValue(0);
        }
        
        // Reset genre selection
        if (genreComboBox != null) {
            selectedGenres.clear();
            genreComboBox.setValue("");
            genreComboBox.setPromptText("Select genres...");
        }
        
        // Reset sort options
        if (sortByCombo != null && !sortByCombo.getItems().isEmpty()) {
            sortByCombo.getSelectionModel().selectFirst();
        }
    }
}
