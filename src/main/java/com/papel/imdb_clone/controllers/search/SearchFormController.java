package com.papel.imdb_clone.controllers.search;

import com.papel.imdb_clone.enums.Genre;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.CheckBoxListCell;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.VBox;
import javafx.scene.layout.HBox;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * Controller for handling the search form functionality in the application.
 * Manages user input for search criteria including title, genres, rating range,
 * year range, and content type filters. Provides methods to build and reset search criteria.
 * 
 * <p>This controller is responsible for:
 * <ul>
 *   <li>Capturing user input for search parameters</li>
 *   <li>Validating input values</li>
 *   <li>Building search criteria objects</li>
 *   <li>Notifying listeners about search events</li>
 *   <li>Managing the search form UI state</li>
 * </ul>
 * 
 * @see BaseSearchController
 */
public class SearchFormController extends BaseSearchController {
    private static final Logger logger = LoggerFactory.getLogger(SearchFormController.class);

    // UI Components
    @FXML
    private TextField titleField;
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
    @FXML
    private TextField selectedGenre;
    @FXML
    private ListView<CheckBox> genreDropdownList;
    @FXML
    private Button genreDropdownButton;
    @FXML
    private VBox genreListContainer;

    // State management
    private final ObservableList<String> selectedGenres = FXCollections.observableArrayList();
    private SearchFormListener searchFormListener;

    // Constants
    private static final int MIN_YEAR = 1888; // First motion picture
    private static final int MAX_YEAR = java.time.Year.now().getValue();
    private static final String YEAR_VALIDATION_REGEX = "^\\d{0,4}$";

    /**
     * Toggles the visibility of the genre dropdown menu.
     *
     * @param actionEvent The action event that triggered this method
     * @throws NullPointerException if actionEvent is null
     */
    @FXML
    private void toggleGenreDropdown(ActionEvent actionEvent) {
        if (actionEvent == null) {
            logger.warn("ActionEvent is null");
            return;
        }

        try {
            if (genreListContainer != null) {
                boolean isVisible = !genreListContainer.isVisible();
                genreListContainer.setVisible(isVisible);
                genreListContainer.setManaged(isVisible);
                updateDropdownButtonText(isVisible);
                actionEvent.consume();
            } else {
                logger.error("genreListContainer is not initialized");
            }
        } catch (Exception e) {
            logger.error("Error toggling genre dropdown", e);
            showError("UI Error", "Failed to toggle genre selection");
        }
    }

    private void updateDropdownButtonText(boolean isVisible) {
        if (genreDropdownButton != null) {
            genreDropdownButton.setText(isVisible ? "Hide" : "Show");
        }
        if (genreListContainer != null) {
            genreListContainer.setVisible(isVisible);
            genreListContainer.setManaged(isVisible);
        }
    }

    public void handleReset(ActionEvent actionEvent) {
        clearFormFields();
        notifySearchCriteriaChanged();
        resetSearchForm();
        actionEvent.consume();
        updateDropdownButtonText(false);
    }

    public void handleSearch(ActionEvent actionEvent) {
        try {
            SearchCriteria criteria = buildSearchCriteria();
            logger.info("Search button clicked with criteria: {}", criteria);
            
            // First notify about criteria change
            notifySearchCriteriaChanged(criteria);
            
            // Then trigger the search request
            if (searchFormListener != null) {
                searchFormListener.onSearchRequested(criteria);
            } else {
                logger.warn("Search form listener is not set");
            }
            
            actionEvent.consume();
            updateDropdownButtonText(false);
        } catch (Exception e) {
            logger.error("Error handling search", e);
            showError("Search Error", "Failed to process search: " + e.getMessage());
        }
    }

    private void notifySearchCriteriaChanged(SearchCriteria criteria) {
        if (searchFormListener != null) {
            searchFormListener.onSearchCriteriaChanged(criteria);
        }
    }

    /**
     * Listener interface for search form events.
     * Implement this interface to receive callbacks when search criteria change or search is requested.
     */
    @FunctionalInterface
    public interface SearchFormListener {
        /**
         * Called when search criteria have changed.
         *
         * @param criteria The updated search criteria
         */
        default void onSearchCriteriaChanged(SearchCriteria criteria) {
            // Default empty implementation
        }

        /**
         * Called when a search is explicitly requested by the user.
         *
         * @param criteria The search criteria to use for the search
         */
        void onSearchRequested(SearchCriteria criteria);
    }

    /**
     * Sets the search form listener that will receive search events.
     *
     * @param listener The listener to be notified of search events
     * @throws IllegalArgumentException if listener is null
     */
    public void setSearchFormListener(SearchFormListener listener) {
        this.searchFormListener = Objects.requireNonNull(listener, "SearchFormListener cannot be null");
        logger.debug("Search form listener set to: {}", listener.getClass().getSimpleName());
    }


    /**
     * Initializes the search form with default values and configurations.
     * This method should be called after the FXML fields have been injected.
     *
     * @throws IllegalStateException if required UI components are not properly initialized
     */
    @FXML
    public void initializeForm() throws IllegalStateException {
        try {
            validateUIComponents();

            // Clear input fields
            clearFormFields();

            // Set default values
            setDefaultSelections();

            // Initialize components
            initializeRatingSlider();
            initializeSortOptions();
            initializeGenreSelection();

            logger.debug("Search form initialized successfully");
        } catch (Exception e) {
            String errorMsg = "Failed to initialize search form";
            logger.error(errorMsg, e);
            throw new IllegalStateException(errorMsg, e);
        }
    }

    /**
     * Validates that all required UI components are properly initialized.
     *
     * @throws IllegalStateException if any required component is null
     */
    private void validateUIComponents() {
        // Validate text fields
        Objects.requireNonNull(titleField, "titleField must be injected");
        Objects.requireNonNull(keywordsField, "keywordsField must be injected");
        Objects.requireNonNull(yearFrom, "yearFrom must be injected");
        Objects.requireNonNull(yearTo, "yearTo must be injected");
        Objects.requireNonNull(selectedGenre, "selectedGenre must be injected");

        // Validate rating components
        Objects.requireNonNull(ratingSlider, "ratingSlider must be injected");
        Objects.requireNonNull(ratingValueLabel, "ratingValueLabel must be injected");

        // Validate genre dropdown components
        Objects.requireNonNull(genreDropdownButton, "genreDropdownButton must be injected");
        Objects.requireNonNull(genreDropdownList, "genreDropdownList must be injected");
        Objects.requireNonNull(genreListContainer, "genreListContainer must be injected");

        // Validate other UI components
        Objects.requireNonNull(sortByCombo, "sortByCombo must be injected");
        Objects.requireNonNull(movieCheckBox, "movieCheckBox must be injected");
        Objects.requireNonNull(seriesCheckBox, "seriesCheckBox must be injected");
    }

    /**
     * Clears all form input fields.
     */
    private void clearFormFields() {
        if (titleField != null) titleField.clear();
        if (keywordsField != null) keywordsField.clear();
        if (yearFrom != null) yearFrom.clear();
        if (yearTo != null) yearTo.clear();
        if (selectedGenre != null) selectedGenre.clear();

        // Clear genre selections
        if (genreDropdownList != null) {
            for (CheckBox checkBox : genreDropdownList.getItems()) {
                checkBox.setSelected(false);
            }
        }
        if (selectedGenres != null) {
            selectedGenres.clear();
        }
        updateDropdownButtonText(false);
    }

    /**
     * Sets default selections for checkboxes and other form elements.
     */
    private void setDefaultSelections() {
        movieCheckBox.setSelected(true);
        seriesCheckBox.setSelected(true);
    }

    /**
     * Initializes the rating slider with default values and listeners.
     */
    private void initializeRatingSlider() {
        ratingSlider.setValue(0);
        updateRatingLabel(0);
    }

    /**
     * Initializes sort options in the combo box.
     */
    private void initializeSortOptions() {
        sortByCombo.getItems().setAll(
                "Relevance",
                "Title (A-Z)",
                "Title (Z-A)",
                "Year (Newest)",
                "Year (Oldest)",
                "Rating (High to Low)"
        );
        sortByCombo.getSelectionModel().selectFirst();
    }

    /**
     * Initializes genre selection components.
     */
    private void initializeGenreSelection() {
        // Add all available genres to the dropdown list
        List<String> allGenres = List.of(
                "Action", "Adventure", "Animation", "Comedy", "Crime",
                "Documentary", "Drama", "Family", "Fantasy", "History",
                "Horror", "Music", "Mystery", "Romance", "Sci-Fi",
                "Thriller", "War", "Western"
        );

        // Create checkboxes for each genre
        for (String genre : allGenres) {
            CheckBox checkBox = new CheckBox(genre);
            checkBox.setStyle("-fx-text-fill: white; -fx-font-size: 14;");
            checkBox.selectedProperty().addListener((obs, wasSelected, isSelected) -> {
                if (isSelected) {
                    if (!selectedGenres.contains(genre)) {
                        selectedGenres.add(genre);
                    }
                } else {
                    selectedGenres.remove(genre);
                }
                updateGenrePrompt();
                notifySearchCriteriaChanged();
            });
            genreDropdownList.getItems().add(checkBox);
        }

        updateGenrePrompt();
    }

    /**
     * Updates the genre prompt text based on the currently selected genres.
     * Shows a prompt when no genres are selected, the genre name when one is selected,
     * or a count when multiple genres are selected.
     */
    private void updateGenrePrompt() {
        if (selectedGenre == null || genreDropdownButton == null) {
            logger.warn("Required fields are not initialized");
            return;
        }

        if (selectedGenres.isEmpty()) {
            selectedGenre.setText("");
            genreDropdownButton.setText("Select Genres...");
        } else if (selectedGenres.size() == 1) {
            String genre = selectedGenres.getFirst();
            selectedGenre.setText(genre);
            genreDropdownButton.setText(genre);
        } else {
            selectedGenre.setText(String.join(", ", selectedGenres));
        }
    }

    /**
     * Updates the rating label with the specified value.
     *
     * @param value The rating value to display (0.0 to 10.0)
     * @throws IllegalStateException if UI components are not properly initialized
     */
    private void updateRatingLabel(double value) {
        try {
            if (value < 0 || value > 10) {
                throw new IllegalArgumentException("Rating must be between 0 and 10");
            }
            ratingValueLabel.setText(String.format("%.1f", value));
        } catch (Exception e) {
            logger.error("Error updating rating label", e);
            throw new IllegalStateException("Failed to update rating display", e);
        }
    }

    /**
     * Initializes the controller after all FXML components have been loaded.
     * This method is automatically called by JavaFX after the FXML file has been processed.
     *
     * @throws IllegalStateException if any required component fails to initialize
     */
    @FXML
    public void initialize() {
        try {
            // Initialize UI components
            setupGenreDropdown();
            setupRatingSlider();
            setupSortOptions();
            setupYearFields();

            // Set default values
            movieCheckBox.setSelected(true);
            seriesCheckBox.setSelected(true);

            logger.debug("SearchFormController initialized successfully");
        } catch (Exception e) {
            String errorMsg = "Failed to initialize SearchFormController";
            logger.error(errorMsg, e);
            throw new IllegalStateException(errorMsg, e);
        }
    }

    private void setupSortOptions() {
        sortByCombo.valueProperty().addListener((obs, oldVal, newVal) -> {
            notifySearchCriteriaChanged();
        });
    }

    private void setupRatingSlider() {
        ratingSlider.valueProperty().addListener((obs, oldVal, newVal) -> {
            updateRatingLabel(newVal.doubleValue());
        });
    }

    /**
     * Sets up the genre dropdown button and its behavior.
     *
     * @throws IllegalStateException if required UI components are not initialized
     */
    private void setupGenreDropdown() {
        // No longer needed as we're using a standard ComboBox
        // This method is kept for backward compatibility
    }

    /**
     * Configures the year input fields with validation and formatting.
     *
     * @throws IllegalStateException if year fields are not properly initialized
     */
    private void setupYearFields() {
        try {
            // Set prompt text with valid year range
            yearFrom.setPromptText("Year from (" + MIN_YEAR + " - " + MAX_YEAR + ")");
            yearTo.setPromptText("Year to (" + MIN_YEAR + " - " + MAX_YEAR + ")");

            // Configure input validation for yearFrom field
            yearFrom.textProperty().addListener((obs, oldVal, newVal) -> {
                if (!newVal.matches(YEAR_VALIDATION_REGEX)) {
                    yearFrom.setText(oldVal);
                    return;
                }

                if (!newVal.isEmpty()) {
                    try {
                        int year = Integer.parseInt(newVal);
                        if (year < MIN_YEAR || year > MAX_YEAR) {
                            yearFrom.setText(oldVal);
                            showWarning("Invalid Year", "Year must be between " + MIN_YEAR + " and " + MAX_YEAR);
                        }
                    } catch (NumberFormatException e) {
                        logger.warn("Invalid year format: " + newVal, e);
                        yearFrom.setText(oldVal);
                    }
                }

                // Notify listeners of search criteria change
                notifySearchCriteriaChanged();
            });

            // Configure input validation for yearTo field
            yearTo.textProperty().addListener((obs, oldVal, newVal) -> {
                if (!newVal.matches(YEAR_VALIDATION_REGEX)) {
                    yearTo.setText(oldVal);
                    return;
                }

                if (!newVal.isEmpty()) {
                    try {
                        int year = Integer.parseInt(newVal);
                        if (year < MIN_YEAR || year > MAX_YEAR) {
                            yearTo.setText(oldVal);
                            showWarning("Invalid Year", "Year must be between " + MIN_YEAR + " and " + MAX_YEAR);
                        }
                    } catch (NumberFormatException e) {
                        logger.warn("Invalid year format: " + newVal, e);
                        yearTo.setText(oldVal);
                    }
                }

                // Notify listeners of search criteria change
                notifySearchCriteriaChanged();
            });

            // Add focus listeners to validate year ranges when focus is lost
            yearFrom.focusedProperty().addListener((obs, wasFocused, isNowFocused) -> {
                if (!isNowFocused) {
                    validateYearRange();
                }
            });

            yearTo.focusedProperty().addListener((obs, wasFocused, isNowFocused) -> {
                if (!isNowFocused) {
                    validateYearRange();
                }
            });

            logger.debug("Year fields initialized successfully");
        } catch (Exception e) {
            String errorMsg = "Failed to initialize year fields";
            logger.error(errorMsg, e);
            throw new IllegalStateException(errorMsg, e);
        }
    }

    private void showWarning(String invalidYear, String s) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle("Invalid Year");
        alert.setHeaderText(null);
        alert.setContentText(invalidYear);
        alert.showAndWait();
    }

    /**
     * Validates that the year range is valid (from <= to).
     * Shows a warning if the range is invalid and swaps the values if needed.
     */
    private void validateYearRange() {
        try {
            if (yearFrom.getText().isEmpty() || yearTo.getText().isEmpty()) {
                return; // Can't validate if either field is empty
            }

            int fromYear = Integer.parseInt(yearFrom.getText());
            int toYear = Integer.parseInt(yearTo.getText());

            if (fromYear > toYear) {
                showWarning("Invalid Year Range",
                        "'Year from' cannot be after 'Year to'. Adjusting values.");

                // Swap the values
                yearFrom.setText(String.valueOf(toYear));
                yearTo.setText(String.valueOf(fromYear));

                logger.debug("Adjusted year range: {} - {}", toYear, fromYear);
            }
        } catch (NumberFormatException e) {
            logger.debug("Skipping year range validation due to invalid input format");
        }
    }

    /**
     * Notifies the search form listener that the search criteria have changed.
     * This method builds the current search criteria and notifies the listener if one is set.
     */
    private void notifySearchCriteriaChanged() {
        try {
            if (searchFormListener != null) {
                SearchCriteria criteria = buildSearchCriteria();
                searchFormListener.onSearchCriteriaChanged(criteria);
            }
        } catch (Exception e) {
            logger.error("Error notifying search criteria changed", e);
            showError("Search Error", "Failed to update search criteria: " + e.getMessage());
        }
    }

    /**
     * Builds search criteria based on the current form values.
     * @return SearchCriteria object containing the search parameters
     */
    private SearchCriteria buildSearchCriteria() {
        String sortBy = sortByCombo != null ? sortByCombo.getValue() : "";
        SearchCriteria criteria = new SearchCriteria("title", sortBy.contains("(A-Z)"));
        
        if (sortBy.startsWith("Year")) {
            criteria = new SearchCriteria("year", sortBy.contains("Newest") ? "desc" : "asc");
        } else if (sortBy.startsWith("Rating")) {
            criteria = new SearchCriteria("rating", sortBy.contains("Highest") ? "desc" : "asc");
        }
        
        // Add other search criteria here as needed
        return criteria;
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

        // Reset sort options
        if (sortByCombo != null && !sortByCombo.getItems().isEmpty()) {
            sortByCombo.getSelectionModel().selectFirst();
        }
    }
}
