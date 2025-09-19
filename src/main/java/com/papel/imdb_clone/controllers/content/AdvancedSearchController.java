package com.papel.imdb_clone.controllers.content;

import com.papel.imdb_clone.controllers.base.BaseController;
import com.papel.imdb_clone.controllers.content.ContentDetailsController;
import com.papel.imdb_clone.models.ContentType;
import com.papel.imdb_clone.models.SearchCriteria;
import com.papel.imdb_clone.services.WatchlistService;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.event.ActionEvent;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.util.Callback;
import org.controlsfx.control.textfield.TextFields;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.URL;
import java.time.LocalDate;
import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import javafx.scene.control.Alert.AlertType;

public class AdvancedSearchController extends BaseController {
    private static final Logger logger = LoggerFactory.getLogger(AdvancedSearchController.class);

    // Search fields from FXML
    @FXML
    private TextField titleField;
    @FXML
    private TextField directorField;
    @FXML
    private TextField castField;
    @FXML
    private TextField yearFromField;
    @FXML
    private TextField yearToField;
    @FXML
    private TextField yearFrom;
    @FXML
    private TextField yearTo;
    @FXML
    private CheckBox movieCheckBox;
    @FXML
    private CheckBox seriesCheckBox;
    @FXML
    private ComboBox<String> genreComboBox;
    @FXML
    private ComboBox<String> sortByCombo;
    @FXML
    private VBox resultsView;
    @FXML
    private TextField minRuntimeField;
    @FXML
    private TextField maxRuntimeField;
    @FXML
    private TextField genreSearchField;
    @FXML
    private TextField keywordsField;
    @FXML
    private Slider ratingSlider;
    @FXML
    private Label ratingValueLabel;
    @FXML
    private ComboBox<String> sortByComboBox;
    @FXML
    private ComboBox<String> resultsPerPageComboBox;
    @FXML
    private ToggleGroup contentTypeGroup;
    @FXML
    private ListView<String> genresListView;
    @FXML
    private Label selectedGenresCount;
    @FXML
    private CheckBox oscarWinnerCheckBox;
    @FXML
    private CheckBox goldenGlobeWinnerCheckBox;
    @FXML
    private CheckBox emmyWinnerCheckBox;
    @FXML
    private CheckBox cannesWinnerCheckBox;
    @FXML
    private CheckBox hasPosterCheckBox;
    @FXML
    private CheckBox hasTrailerCheckBox;
    @FXML
    private CheckBox isAdultContentCheckBox;
    @FXML
    private Label resultsCountLabel;
    @FXML
    private ComboBox<String> languageComboBox;
    @FXML
    private ComboBox<String> countryComboBox;

    // Results table
    @FXML
    private TableView<ContentItem> resultsTable;
    @FXML
    private TableColumn<ContentItem, String> resultTitleColumn;
    @FXML
    private TableColumn<ContentItem, String> resultYearColumn;
    @FXML
    private TableColumn<ContentItem, String> resultTypeColumn;
    @FXML
    private TableColumn<ContentItem, String> resultRatingColumn;
    @FXML
    private TableColumn<ContentItem, Void> resultActionsColumn;

    // Data
    private final ObservableList<String> allGenres = FXCollections.observableArrayList(
            "Action", "Adventure", "Animation", "Biography", "Comedy",
            "Crime", "Documentary", "Drama", "Family", "Fantasy",
            "Film-Noir", "History", "Horror", "Music", "Musical",
            "Mystery", "Romance", "Sci-Fi", "Sport", "Thriller", "War", "Western",
            "Action & Adventure", "Kids", "News", "Reality", "Sci-Fi & Fantasy",
            "Soap", "Talk", "War & Politics"
    );
    private final FilteredList<String> filteredGenres = new FilteredList<>(allGenres);
    private final Set<String> selectedGenres = new HashSet<>();

    private void addSampleData() {
        // Add sample data to the results table
        resultsTable.getItems().add(new ContentItem("1", "The Shawshank Redemption", "1994", "Movie", 9.3));
        resultsTable.getItems().add(new ContentItem("2", "The Godfather", "1972", "Movie", 9.2));
        resultsTable.getItems().add(new ContentItem("3", "The Dark Knight", "2008", "Movie", 9.0));
        resultsTable.getItems().add(new ContentItem("4", "Breaking Bad", "2008-2013", "TV Show", 9.5));
        resultsTable.getItems().add(new ContentItem("5", "Game of Thrones", "2011-2019", "TV Show", 9.3));
    }

    private void showContentDetails(ContentItem item) {
        try {
            // Load the content details view
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/content/content-details.fxml"));
            Parent root = loader.load();

            // Get the controller and pass the content ID
            ContentDetailsController controller = loader.getController();
            controller.setContentId(item.getId());

            // Show the details in a new window
            Stage stage = new Stage();
            stage.setTitle(item.getTitle() + " - Details");
            stage.setScene(new Scene(root));
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.show();
        } catch (IOException e) {
            logger.error("Error showing content details", e);
            showError("Error", "Could not load content details: " + e.getMessage());
        }
    }

    private void addToWatchlist(ContentItem item) {
        try {
            // Get the current user's ID
            int userId = getCurrentUserId();

            // Get the watchlist service through dependency injection or service locator
            WatchlistService watchlistService = new WatchlistService();

            // Add the content to the watchlist
            boolean success = watchlistService.addToWatchlist(String.valueOf(userId), String.valueOf(item.getId()), item.getType()).isWatched();

            if (success) {
                showSuccess("Success", item.getTitle() + " has been added to your watchlist!");
            } else {
                showError("Error", "Could not add " + item.getTitle() + " to your watchlist. It might already be there.");
            }
        } catch (Exception e) {
            logger.error("Error adding to watchlist", e);
            showError("Error", "An error occurred while adding to watchlist: " + e.getMessage());
        }
    }

    // Helper method to get current user ID
    private int getCurrentUserId() {
        // TODO: Replace with actual user ID retrieval logic
        return 1; // Default user ID for testing
    }

    protected void showError(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    protected void showSuccess(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void initializeResultsTable() {
        // Initialize the results table with a placeholder for when no results are found
        resultsTable.setPlaceholder(new Label("No results found. Try adjusting your search criteria."));

        // Set up the table columns with appropriate cell value factories
        resultTitleColumn.setCellValueFactory(new PropertyValueFactory<>("title"));
        resultYearColumn.setCellValueFactory(new PropertyValueFactory<>("year"));
        resultTypeColumn.setCellValueFactory(new PropertyValueFactory<>("type"));
        resultRatingColumn.setCellValueFactory(cellData ->
                new SimpleStringProperty(String.format("%.1f", cellData.getValue().getRating()))
        );

        // Set up the actions column with buttons
        resultActionsColumn.setCellFactory(column -> new TableCell<>() {
            private final Button viewDetailsBtn = new Button("View Details");
            private final Button addToWatchlistBtn = new Button("Add to Watchlist");
            private final HBox buttons = new HBox(5, viewDetailsBtn, addToWatchlistBtn);

            {
                // Style buttons
                viewDetailsBtn.getStyleClass().add("btn-primary");
                addToWatchlistBtn.getStyleClass().add("btn-success");

                // Set button actions
                viewDetailsBtn.setOnAction(event -> {
                    ContentItem item = getTableView().getItems().get(getIndex());
                    showContentDetails(item);
                });

                addToWatchlistBtn.setOnAction(event -> {
                    ContentItem item = getTableView().getItems().get(getIndex());
                    addToWatchlist(item);
                });

                // Set button styles
                viewDetailsBtn.setStyle("-fx-padding: 5 10; -fx-font-size: 12px;");
                addToWatchlistBtn.setStyle("-fx-padding: 5 10; -fx-font-size: 12px;");
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    setGraphic(buttons);
                }
            }
        });

        // Set column resize policy to make better use of available space
        resultsTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        // Add the table to the results view if it's not already added
        if (!resultsView.getChildren().contains(resultsTable)) {
            resultsView.getChildren().add(resultsTable);
        }
    }

    private void initializeAdditionalFilters() {
        // This method is intentionally left empty as all additional filters
        // are already initialized in other methods like setupContentTypeGroup()
    }

    private void initializeSortOptions() {
        ObservableList<String> sortOptions = FXCollections.observableArrayList();
        sortOptions.addAll(Arrays.toString(SortOption.values()));
        sortByComboBox.setItems(sortOptions);
        sortByComboBox.setValue(String.valueOf(SortOption.RELEVANCE));
    }

    private void setupRatingSlider() {
        ratingSlider.valueProperty().addListener((obs, oldVal, newVal) -> {
            double roundedValue = Math.round(newVal.doubleValue() * 10) / 10.0;
            ratingValueLabel.setText(String.format("%.1f", roundedValue));
        });

        // Set initial value
        ratingSlider.setValue(7.0);
        ratingValueLabel.setText("7.0");
    }

    private void setupGenres() {
        // Add genres to the combo box
        genreComboBox.getItems().addAll(
                "Action", "Adventure", "Animation", "Comedy", "Crime",
                "Documentary", "Drama", "Family", "Fantasy", "Horror",
                "Music", "Mystery", "Romance", "Sci-Fi", "Thriller"
        );
    }

    private void setupSortOptions() {
        sortByCombo.getItems().addAll(
                "Relevance", "Title (A-Z)", "Title (Z-A)",
                "Year (Newest First)", "Year (Oldest First)",
                "Rating (Highest First)", "Rating (Lowest First)"
        );
        sortByCombo.setValue("Relevance");
    }

    @FXML
    private void resetFilters() {
        // Reset all form fields
        keywordsField.clear();
        movieCheckBox.setSelected(true);
        seriesCheckBox.setSelected(true);
        yearFrom.clear();
        yearTo.clear();
        genreComboBox.getSelectionModel().clearSelection();
        ratingSlider.setValue(0);
        sortByCombo.setValue("Relevance");
    }

    @FXML
    private void goToHome() {
        // TODO: Implement navigation to home
        showSuccess("Navigation", "Will navigate to home page");
    }

    private void initializeGenres() {
        // Set up the search field with type-ahead functionality
        TextFields.bindAutoCompletion(genreSearchField, t -> {
            String text = genreSearchField.getText().toLowerCase();
            return genreSuggestions.filtered(genre ->
                    genre.toLowerCase().contains(text) && !selectedGenres.contains(genre)
            );
        });

        // Handle adding a genre when Enter is pressed
        genreSearchField.setOnKeyPressed(event -> {
            if (event.getCode() == KeyCode.ENTER) {
                String newGenre = genreSearchField.getText().trim();
                if (!newGenre.isEmpty() && !selectedGenres.contains(newGenre)) {
                    selectedGenres.add(newGenre);
                    updateGenresList();
                    genreSearchField.clear();
                }
            }
        });

        // Set up the list view with removable tags
        updateGenresList();
    }

    private void updateGenresList() {
        // Clear existing items
        ObservableList<Node> genreTags = FXCollections.observableArrayList();

        for (String genre : selectedGenres) {
            HBox tag = new HBox(5);
            tag.setAlignment(Pos.CENTER_LEFT);
            tag.setStyle("-fx-background-color: #e3f2fd; -fx-padding: 3 8 3 8; -fx-background-radius: 12;");

            Label genreLabel = new Label(genre);
            Button removeButton = new Button("×");
            removeButton.setStyle(
                    "-fx-background-color: transparent; " +
                            "-fx-text-fill: #1976d2; " +
                            "-fx-font-weight: bold; " +
                            "-fx-padding: 0 0 0 5;"
            );

            removeButton.setOnAction(e -> {
                selectedGenres.remove(genre);
                updateGenresList();
            });

            tag.getChildren().addAll(genreLabel, removeButton);
            genreTags.add(tag);
        }

        // Clear and add all genre tags
        genresListView.getItems().clear();
        genresListView.getItems().addAll(String.valueOf(genreTags));
        updateSelectedGenresCount();
    }

    private void updateSelectedGenresCount() {
        if (selectedGenresCount != null) {
            selectedGenresCount.setText(String.format("(%d selected)", selectedGenres.size()));
        }
    }

    private Predicate<String> createGenrePredicate(String searchText) {
        if (searchText == null || searchText.isEmpty()) {
            return genre -> true;
        }
        String lowerCaseFilter = searchText.toLowerCase();
        return genre -> genre.toLowerCase().contains(lowerCaseFilter);
    }

    private void setupRuntimeValidation() {
        addTextLimiter(yearToField, 4);
    }

    private void addNumericValidation(TextField textField) {
        textField.addEventFilter(KeyEvent.KEY_TYPED, event -> {
            if (!event.getCharacter().matches("\\d*")) {
                event.consume();
            }
        });
    }

    private void addTextLimiter(TextField textField, int maxLength) {
        textField.textProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue.length() > maxLength) {
                textField.setText(oldValue);
            }
        });
    }

    private void setupSortingOptions() {
        // Add all sort options to the combo box
        sortByComboBox.getItems().addAll(
                Arrays.stream(SortOption.values())
                        .map(SortOption::toString)
                        .collect(Collectors.toList())
        );

        // Set default sort option
        sortByComboBox.setValue(SortOption.RELEVANCE.toString());

        // Add tooltip to explain sort options
        Tooltip sortTooltip = new Tooltip();
        sortTooltip.setText("Select how to sort the search results");
        sortByComboBox.setTooltip(sortTooltip);
    }

    private void setupContentTypeGroup() {
        // Set the default selected radio button
        if (contentTypeGroup.getToggles().size() > 0) {
            contentTypeGroup.selectToggle(contentTypeGroup.getToggles().get(0));
        }
    }

    private void setupResultsPerPage() {
        // Set default value if not already set
        if (resultsPerPageComboBox.getValue() == null) {
            resultsPerPageComboBox.setValue("20");
        }
    }

    @FXML
    private void handleSearch() {
        if (!validateInputs()) {
            return;
        }

        // Build search criteria
        SearchCriteria criteria = buildSearchCriteria();

        try {
            // TODO: Call your search service with the criteria
            // List<Content> results = searchService.search(criteria);

            // For now, just show a success message with the search criteria
            showSearchSummary(criteria);

            showSuccess("Search Completed", "Found X results matching your criteria.");
        } catch (Exception e) {
            logger.error("Error performing search", e);
            showError("Search Error", "An error occurred while performing the search: " + e.getMessage());
        }
    }

    private SearchCriteria buildSearchCriteria() {
        SearchCriteria criteria = new SearchCriteria();

        // Basic criteria
        criteria.setTitle(titleField.getText().trim());
        criteria.setDirector(directorField.getText().trim());
        criteria.setCast(parseCommaSeparatedValues(castField.getText()));
        criteria.setKeywords(parseCommaSeparatedValues(keywordsField.getText()));

        // Year range
        if (!yearFromField.getText().isEmpty()) {
            criteria.setYearFrom(Integer.parseInt(yearFromField.getText()));
        }
        if (!yearToField.getText().isEmpty()) {
            criteria.setYearTo(Integer.parseInt(yearToField.getText()));
        }

        // Rating
        criteria.setMinRating(ratingSlider.getValue());

        // Content type
        Toggle selectedToggle = contentTypeGroup.getSelectedToggle();
        if (selectedToggle != null) {
            criteria.setContentType(ContentType.valueOf(selectedToggle.getUserData().toString()));
        }

        // Genres
        criteria.setGenres(new ArrayList<>(selectedGenres));

        // Runtime
        if (!minRuntimeField.getText().isEmpty()) {
            criteria.setMinRuntime(Integer.parseInt(minRuntimeField.getText()));
        }
        if (!maxRuntimeField.getText().isEmpty()) {
            criteria.setMaxRuntime(Integer.parseInt(maxRuntimeField.getText()));
        }

        // Awards
        criteria.setOscarWinner(oscarWinnerCheckBox.isSelected());
        criteria.setGoldenGlobeWinner(goldenGlobeWinnerCheckBox.isSelected());
        criteria.setEmmyWinner(emmyWinnerCheckBox.isSelected());
        criteria.setCannesWinner(cannesWinnerCheckBox.isSelected());

        // Content details
        criteria.setHasPoster(hasPosterCheckBox.isSelected());
        criteria.setHasTrailer(hasTrailerCheckBox.isSelected());
        criteria.setIncludeAdultContent(isAdultContentCheckBox.isSelected());

        // Sorting
        String selectedSort = sortByComboBox.getValue();
        if (selectedSort != null) {
            SortOption sortOption = Arrays.stream(SortOption.values())
                    .filter(option -> option.toString().equals(selectedSort))
                    .findFirst()
                    .orElse(SortOption.RELEVANCE);

            criteria.setSortBy(sortOption.getSortBy());
            criteria.setSortOrder(sortOption.getSortOrder());
        }

        // Pagination
        if (resultsPerPageComboBox.getValue() != null) {
            criteria.setPageSize(Integer.parseInt(resultsPerPageComboBox.getValue()));
        }

        return criteria;
    }

    private List<String> parseCommaSeparatedValues(String input) {
        if (input == null || input.trim().isEmpty()) {
            return Collections.emptyList();
        }
        return Arrays.stream(input.split(","))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .collect(Collectors.toList());
    }

    private void showSearchSummary(SearchCriteria criteria) {
        // This would display a summary of the search criteria
        // You can implement this to show a dialog or update a label
        logger.info("Search criteria: {}", criteria);
    }

    @FXML
    private void handleReset() {
        // Reset basic tab
        titleField.clear();
        directorField.clear();
        castField.clear();
        yearFromField.clear();
        yearToField.clear();
        ratingSlider.setValue(5.0);

        // Reset content type
        if (contentTypeGroup.getToggles().size() > 0) {
            contentTypeGroup.selectToggle(contentTypeGroup.getToggles().get(0));
        }

        // Reset genres tab
        genreSearchField.clear();
        selectedGenres.clear();
        genresListView.refresh();
        updateSelectedGenresCount();
        keywordsField.clear();

        // Reset advanced tab
        minRuntimeField.clear();
        maxRuntimeField.clear();
        oscarWinnerCheckBox.setSelected(false);
        goldenGlobeWinnerCheckBox.setSelected(false);
        emmyWinnerCheckBox.setSelected(false);
        cannesWinnerCheckBox.setSelected(false);
        hasPosterCheckBox.setSelected(true);
        hasTrailerCheckBox.setSelected(false);
        isAdultContentCheckBox.setSelected(false);

        // Reset sort and pagination
        sortByComboBox.setValue(SortOption.RELEVANCE.toString());
        resultsPerPageComboBox.setValue("20");
    }

    @FXML
    private void handleSelectAllGenres() {
        selectedGenres.addAll(allGenres);
        genresListView.refresh();
    }

    @FXML
    private void handleDeselectAllGenres() {
        selectedGenres.clear();
        genresListView.refresh();
    }

    private boolean validateInputs() {
        // Validate year range
        if (!validateYearRange()) {
            showError("Validation Error", "Invalid year range. 'From' year must be before or equal to 'To' year.");
            return false;
        }

        // Validate year format
        if ((!yearFromField.getText().isEmpty() && !isValidYear(yearFromField.getText())) ||
                (!yearToField.getText().isEmpty() && !isValidYear(yearToField.getText()))) {
            showError("Validation Error", "Please enter valid years (e.g., 1990).");
            return false;
        }

        // Validate runtime range
        if (!validateRuntimeRange()) {
            showError("Validation Error", "Invalid runtime range. 'Min' must be less than or equal to 'Max'.");
            return false;
        }

        // Validate runtime format
        if ((!minRuntimeField.getText().isEmpty() && !isValidRuntime(minRuntimeField.getText())) ||
                (!maxRuntimeField.getText().isEmpty() && !isValidRuntime(maxRuntimeField.getText()))) {
            showError("Validation Error", "Please enter valid runtime in minutes (e.g., 90).");
            return false;
        }

        // Validate at least one search criteria is provided
        if (isSearchFormEmpty()) {
            showError("Validation Error", "Please provide at least one search criteria.");
            return false;
        }

        return true;
    }

    private boolean isSearchFormEmpty() {
        return titleField.getText().trim().isEmpty() &&
                directorField.getText().trim().isEmpty() &&
                castField.getText().trim().isEmpty() &&
                yearFromField.getText().trim().isEmpty() &&
                yearToField.getText().trim().isEmpty() &&
                selectedGenres.isEmpty() &&
                keywordsField.getText().trim().isEmpty() &&
                !oscarWinnerCheckBox.isSelected() &&
                !goldenGlobeWinnerCheckBox.isSelected() &&
                !emmyWinnerCheckBox.isSelected() &&
                !cannesWinnerCheckBox.isSelected();
    }

    private boolean isValidRuntime(String runtime) {
        if (runtime == null || runtime.trim().isEmpty()) {
            return true;
        }

        try {
            int minutes = Integer.parseInt(runtime);
            return minutes > 0 && minutes <= 1000; // Reasonable runtime limit
        } catch (NumberFormatException e) {
            return false;
        }
    }

    private boolean validateYearRange() {
        String fromYear = yearFrom.getText();
        String toYear = yearTo.getText();

        if (fromYear.isEmpty() || toYear.isEmpty()) {
            return true; // Empty is valid (means no filter)
        }

        try {
            int from = Integer.parseInt(fromYear);
            int to = Integer.parseInt(toYear);
            return from <= to;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    private boolean validateRuntimeRange() {
        String min = minRuntimeField.getText();
        String max = maxRuntimeField.getText();

        if (min.isEmpty() || max.isEmpty()) {
            return true; // Empty is valid (means no filter)
        }

        try {
            int minRuntime = Integer.parseInt(min);
            int maxRuntime = Integer.parseInt(max);
            return minRuntime <= maxRuntime;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    private boolean isValidYear(String year) {
        if (year == null || year.trim().isEmpty()) {
            return true; // Empty is valid (means no filter)
        }

        try {
            int yearValue = Integer.parseInt(year);
            return yearValue >= 1888 && yearValue <= java.time.Year.now().getValue();
        } catch (NumberFormatException e) {
            return false;
        }
    }

    public void validateNumericInput(KeyEvent keyEvent) {
    }

    // Search results table

    // Model class for search results
    public static class ContentItem {
        private final String id;
        private final String title;
        private final String year;
        private final String type;
        private final double rating;

        public ContentItem(String id, String title, String year, String type, double rating) {
            this.id = id;
            this.title = title;
            this.year = year;
            this.type = type;
            this.rating = rating;
        }

        public String getId() {
            return id;
        }

        public String getTitle() {
            return title;
        }

        public String getYear() {
            return year;
        }

        public String getType() {
            return type;
        }

        public double getRating() {
            return rating;
        }
    }

    // Genre suggestions list
    private final ObservableList<String> genreSuggestions = FXCollections.observableArrayList(
            "Action", "Adventure", "Animation", "Comedy", "Crime", "Documentary",
            "Drama", "Family", "Fantasy", "History", "Horror", "Music", "Mystery",
            "Romance", "Science Fiction", "TV Movie", "Thriller", "War", "Western"
    );

    // Sort options
    private enum SortOption {
        RELEVANCE("Relevance", "relevance", "desc"),
        TITLE_ASC("Title (A-Z)", "title", "asc"),
        TITLE_DESC("Title (Z-A)", "title", "desc"),
        YEAR_ASC("Year (Oldest First)", "year", "asc"),
        YEAR_DESC("Year (Newest First)", "year", "desc"),
        RATING_ASC("Rating (Lowest First)", "rating", "asc"),
        RATING_DESC("Rating (Highest First)", "rating", "desc"),
        POPULARITY_DESC("Most Popular", "popularity", "desc"),
        RUNTIME_ASC("Runtime (Shortest First)", "runtime", "asc"),
        RUNTIME_DESC("Runtime (Longest First)", "runtime", "desc");

        private final String displayName;
        private final String sortBy;
        private final String sortOrder;

        SortOption(String displayName, String sortBy, String sortOrder) {
            this.displayName = displayName;
            this.sortBy = sortBy;
            this.sortOrder = sortOrder;
        }

        @Override
        public String toString() {
            return displayName;
        }

        public String getSortBy() {
            return sortBy;
        }

        public String getSortOrder() {
            return sortOrder;
        }

    }

    @Override
    protected void initializeController(int currentUserId) throws Exception {
        setupRatingSlider();
        setupGenresList();
        setupRuntimeValidation();
        setupYearValidation();
        setupSortingOptions();
        setupContentTypeGroup();
        setupResultsPerPage();

        // Set default values
        hasPosterCheckBox.setSelected(true);
    }

    private void setupYearValidation() {
        yearFromField.textProperty().addListener((obs, oldVal, newVal) -> {
            if (!isValidYear(newVal)) {
                yearFromField.setText(oldVal);
            }
        });
        yearToField.textProperty().addListener((obs, oldVal, newVal) -> {
            if (!isValidYear(newVal)) {
                yearToField.setText(oldVal);
            }
        });

        yearFromField.addEventFilter(KeyEvent.KEY_TYPED, event -> {
            if (!Character.isDigit(event.getCharacter().charAt(0))) {
                event.consume();
            }
        });
        yearToField.addEventFilter(KeyEvent.KEY_TYPED, event -> {
            if (!Character.isDigit(event.getCharacter().charAt(0))) {
                event.consume();
            }
        });
    }

    private void setupGenresList() {
        if (genresListView == null) {
            logger.error("genresListView is null. Check FXML id matches the field name in controller.");
            return;
        }

        // Set up the cell factory to display genre names as strings
        genresListView.setCellFactory(lv -> new ListCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setGraphic(null);
                } else {
                    setText(item);
                }
            }
        });
        
        // Set the items to an empty observable list initially
        genresListView.setItems(FXCollections.observableArrayList());
    }


    @FXML
    public void handleAddGenre(ActionEvent event) {
        String genre = genreSearchField.getText().trim();
        if (!genre.isEmpty() && !selectedGenres.contains(genre)) {
            selectedGenres.add(genre);
            // Update the ListView items
            genresListView.getItems().add(genre);
            updateSelectedGenresCount();
            genreSearchField.clear();
            genreSearchField.clear();
        }
    }

    @FXML
    public void initialize(URL location, ResourceBundle resources) {
        try {
            initializeController(0); // Default user ID, should be set by the parent controller

            // Initialize UI components
            initializeResultsTable();
            setupRatingSlider();
            setupGenres();
            setupSortOptions();

            // Set up additional filter options
            initializeAdditionalFilters();

            // Add sample data for demonstration
            addSampleData();

        } catch (Exception e) {
            logger.error("Error initializing AdvancedSearchController", e);
            showError("Initialization Error", "Failed to initialize the search form: " + e.getMessage());
        }
    }
}
