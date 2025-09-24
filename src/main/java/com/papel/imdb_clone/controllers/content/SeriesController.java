package com.papel.imdb_clone.controllers.content;

import com.papel.imdb_clone.controllers.BaseController;
import com.papel.imdb_clone.enums.Ethnicity;
import com.papel.imdb_clone.model.content.Episode;
import com.papel.imdb_clone.model.content.Season;
import com.papel.imdb_clone.model.people.Actor;
import com.papel.imdb_clone.service.navigation.NavigationService;
import com.papel.imdb_clone.service.content.SeriesService;
import com.papel.imdb_clone.enums.Genre;
import com.papel.imdb_clone.model.content.Series;

import com.papel.imdb_clone.service.search.ServiceLocator;
import com.papel.imdb_clone.util.UIUtils;
import javafx.application.Platform;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.control.ButtonBar.ButtonData;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.util.Pair;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URL;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Controller for managing TV series in the IMDB Clone application.
 * 
 * <p>This controller handles all series-related operations including:
 * <ul>
 *   <li>Displaying and filtering series in a table view</li>
 *   <li>Adding, editing, and deleting series</li>
 *   <li>Managing seasons and episodes</li>
 *   <li>Handling user ratings and reviews</li>
 *   <li>Advanced search functionality</li>
 * </ul>
 * 
 * @author [Your Name]
 * @version 1.0
 * @since 2025-09-20
 */
public class SeriesController extends BaseController {
    /** Logger instance for this class */
    private static final Logger logger = LoggerFactory.getLogger(SeriesController.class);
    
    /** Data map for storing application state */
    private Map<String, Object> data;
    
    /** ID of the currently logged-in user */
    private int currentUserId;

    /**
     * Handles the action when the manage series button is clicked.
     * Validates if a series is selected and shows the management dialog.
     * 
     * @param event The action event that triggered this method
     */
    @FXML
    private void handleManageSeries(ActionEvent event) {
        try {
            Series selectedSeries = seriesTable.getSelectionModel().getSelectedItem();
            if (selectedSeries != null) {
                showSeriesManagementDialog(selectedSeries);
            } else {
                showAlert("No Selection", "Please select a series to manage.");
            }

        } catch (Exception e) {
            logger.error("Error in handleManageSeries: {}", e.getMessage(), e);
            showErrorAlert("Failed to manage series", e.getMessage());
        }
    }

    /**
     * Displays the series management dialog for the selected series.
     * 
     * @param selectedSeries The series to be managed
     */
    private void showSeriesManagementDialog(Series selectedSeries) {
        try {
            //add message here
            showSeriesEditDialog(selectedSeries);
            loadSeries();
            showSuccess("Success", "Series managed successfully!");
        } catch (Exception e) {
            logger.error("Error in showSeriesManagementDialog: {}", e.getMessage(), e);
            showErrorAlert("Failed to show series management dialog", e.getMessage());
        }
    }

    /**
     * Displays an error alert dialog with the specified title and message.
     * 
     * @param title   The title of the error dialog
     * @param message The detailed error message to display
     */
    private void showErrorAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Error");
        alert.setHeaderText(title);
        alert.setContentText(message);
        alert.showAndWait();
    }

    // UI Components
    /** Status label for displaying messages to the user */
    @FXML private Label statusLabel;
    
    /** Label for displaying the number of search results */
    @FXML private Label resultsCountLabel;
    
    /** Table view that displays the list of series */
    @FXML private TableView<Series> seriesTable;
    
    /** Column for displaying series titles */
    @FXML private TableColumn<Series, String> seriesTitleColumn;
    
    /** Column for displaying series release years */
    @FXML private TableColumn<Series, Integer> seriesYearColumn;
    
    /** Column for displaying series genres */
    @FXML private TableColumn<Series, String> seriesGenreColumn;
    
    /** Column for displaying the number of seasons in each series */
    @FXML private TableColumn<Series, Integer> seriesSeasonsColumn;
    
    /** Column for displaying the total number of episodes in each series */
    @FXML private TableColumn<Series, Integer> seriesEpisodesColumn;
    
    /** Column for displaying average user ratings of series */
    @FXML private TableColumn<Series, Double> seriesRatingColumn;
    
    /** Column for displaying the creator(s) of each series */
    @FXML private TableColumn<Series, String> seriesCreatorColumn;
    
    /** Column for displaying the main cast of each series */
    @FXML private TableColumn<Series, String> seriesCastColumn;
    
    /**
     * Navigates back to the home view.
     */
    @FXML
    public void goToHome() {
        try {
            NavigationService navigationService = NavigationService.getInstance();
            navigationService.navigateTo("/fxml/base/home-view.fxml",
                    data, (Stage) seriesTable.getScene().getWindow(),
                "IMDb Clone - Home");
        } catch (Exception e) {
            logger.error("Error navigating to home view", e);
            UIUtils.showError("Navigation Error", "Failed to navigate to home view: " + e.getMessage());
        }
    }
    /** Text field for searching series by title or other criteria */
    @FXML private TextField seriesSearchField;
    
    /** Combo box for selecting sort criteria for the series list */
    @FXML private ComboBox<String> seriesSortBy;

    // Data
    /** List containing all series loaded from the database */
    private final ObservableList<Series> allSeries = FXCollections.observableArrayList();
    
    /** List containing series that match the current filter criteria */
    private final ObservableList<Series> filteredSeries = FXCollections.observableArrayList();
    
    /** Property binding for the currently selected series in the table */
    private final ObjectProperty<Series> selectedSeries = new SimpleObjectProperty<>();
    
    /** Service for handling series-related business logic */
    private SeriesService seriesService;
    private final NavigationService navigationService;
    
    public SeriesController() {
        super();
        this.navigationService = NavigationService.getInstance();
    }

    /**
     * Initializes the controller class. This method is automatically called
     * by JavaFX after the FXML file has been loaded.
     * 
     * @param location  The location used to resolve relative paths for the root object, or null if not known
     * @param resources The resources used to localize the root object, or null if not localized
     */
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // This method is called by JavaFX during FXML loading
        // The actual initialization with user context happens in initializeController
    }

    /**
     * Initializes the controller with the current user's context.
     * This method sets up the controller with the current user's ID and initializes
     * the UI components and data bindings.
     * 
     * @param currentUserId The ID of the currently logged-in user
     * @throws Exception if there is an error during initialization
     */
    @Override
    public void initializeController(int currentUserId) throws Exception {
        this.currentUserId = currentUserId;
        
        // Initialize dataManager through the service locator instead of calling super.initialize()
        this.dataManager = ServiceLocator.getInstance().getDataManager();
        
        // SeriesService is already initialized in the constructor
        
        // Set up the table
        setupTableColumns();
        
        // Bind the table to the filtered series list
        seriesTable.setItems(filteredSeries);
        
        // Load data and set up handlers
        loadSeries();
        setupSearchHandlers();
        setupSortHandlers();
        
        // Set up selection listener
        seriesTable.getSelectionModel().selectedItemProperty().addListener(
            (obs, oldSelection, newSelection) -> selectedSeries.set(newSelection)
        );
        
        logger.info("SeriesController initialized with user ID: {}", currentUserId);
    }
    
    // Setter for series service (for dependency injection)
    /**
     * Sets the series service for this controller.
     * This method allows for dependency injection of the SeriesService.
     * 
     * @param seriesService The SeriesService instance to be used by this controller
     */
    public void setContentService(SeriesService seriesService) {
        this.seriesService = seriesService;
    }


    private void setupTableColumns() {
        // Set up title column
        seriesTitleColumn.setCellValueFactory(new PropertyValueFactory<>("title"));
        seriesTitleColumn.setCellFactory(col -> new TableCell<Series, String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? "" : item);
                setStyle("-fx-alignment: CENTER_LEFT; -fx-padding: 5 10;");
            }
        });

        // Set up year column
        seriesYearColumn.setCellValueFactory(cellData -> 
            new SimpleObjectProperty<>(cellData.getValue().getStartYear()));
        seriesYearColumn.setCellFactory(col -> new TableCell<Series, Integer>() {
            @Override
            protected void updateItem(Integer item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? "" : String.valueOf(item));
                setStyle("-fx-alignment: CENTER; -fx-padding: 5;");
            }
        });

        // Set up genre column
        seriesGenreColumn.setCellValueFactory(cellData -> {
            List<String> genreNames = new ArrayList<>();
            for (Genre genre : cellData.getValue().getGenres()) {
                genreNames.add(genre.name());
            }
            return new SimpleObjectProperty<>(String.join(", ", genreNames));
        });
        seriesGenreColumn.setCellFactory(col -> new TableCell<Series, String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? "" : item);
                setStyle("-fx-alignment: CENTER_LEFT; -fx-padding: 5 10;");
            }
        });

        // Set up seasons column
        seriesSeasonsColumn.setCellValueFactory(cellData -> 
            new SimpleObjectProperty<>(cellData.getValue().getSeasons().size()));
        seriesSeasonsColumn.setCellFactory(col -> new TableCell<Series, Integer>() {
            @Override
            protected void updateItem(Integer item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? "" : String.valueOf(item));
                setStyle("-fx-alignment: CENTER; -fx-padding: 5;");
            }
        });

        // Set up episodes column
        seriesEpisodesColumn.setCellValueFactory(cellData -> 
            new SimpleObjectProperty<>(cellData.getValue().getTotalEpisodes()));
        seriesEpisodesColumn.setCellFactory(col -> new TableCell<Series, Integer>() {
            @Override
            protected void updateItem(Integer item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? "" : String.valueOf(item));
                setStyle("-fx-alignment: CENTER; -fx-padding: 5;");
            }
        });

        // Set up rating column
        seriesRatingColumn.setCellValueFactory(cellData -> {
            // First try to get the rating from the Series class (user rating)
            Double rating = cellData.getValue().getRating();
            // If no user rating, fall back to IMDb rating
            if (rating == 0.0) {
                rating = cellData.getValue().getImdbRating();
            }
            return new SimpleObjectProperty<>(rating);
        });
        
        seriesRatingColumn.setCellFactory(col -> new TableCell<Series, Double>() {
            @Override
            protected void updateItem(Double item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null || item == 0.0) {
                    setText("N/A");
                    setStyle("-fx-alignment: CENTER; -fx-padding: 5; -fx-text-fill: #888;");
                } else {
                    setText(String.format("%.1f", item));
                    // Color code based on rating
                    if (item >= 8.0) {
                        setStyle("-fx-alignment: CENTER; -fx-padding: 5; -fx-font-weight: bold; -fx-text-fill: #4CAF50;");
                    } else if (item >= 6.0) {
                        setStyle("-fx-alignment: CENTER; -fx-padding: 5; -fx-font-weight: bold; -fx-text-fill: #FFC107;");
                    } else {
                        setStyle("-fx-alignment: CENTER; -fx-padding: 5; -fx-font-weight: bold; -fx-text-fill: #F44336;");
                    }
                }
            }
        });

        // Set up creator column
        seriesCreatorColumn.setCellValueFactory(new PropertyValueFactory<>("creator"));
        seriesCreatorColumn.setCellFactory(col -> new TableCell<Series, String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? "" : item);
                setStyle("-fx-alignment: CENTER_LEFT; -fx-padding: 5 10;");
            }
        });

        // Set up cast column
        seriesCastColumn.setCellValueFactory(cellData -> {
            String actors = cellData.getValue().getActors().stream()
                .map(actor -> String.valueOf(actor.getName()))
                .collect(Collectors.joining(", "));
            return new SimpleStringProperty(actors);
        });
        seriesCastColumn.setCellFactory(col -> new TableCell<Series, String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? "" : item);
                setStyle("-fx-alignment: CENTER_LEFT; -fx-padding: 5 10; -fx-wrap-text: true;");
                setWrapText(true);
            }
        });
    }

    private void setupSearchHandlers() {
        seriesSearchField.textProperty().addListener((obs, oldVal, newVal) -> filterSeries());
    }

    private void setupSortHandlers() {
        // Add sort options to the ComboBox
        seriesSortBy.getItems().addAll(
            "Title (A-Z)",
            "Title (Z-A)",
            "Year (Newest First)",
            "Year (Oldest First)",
            "Rating (Highest First)",
            "Rating (Lowest First)",
            "Seasons (Most First)",
            "Seasons (Fewest First)",
            "Episodes (Most First)",
            "Episodes (Fewest First)"
        );
        
        // Set default sort
        seriesSortBy.getSelectionModel().selectFirst();
        
        // Add listener for sort changes
        seriesSortBy.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) {
                sortSeriesTable(newVal);
            }
        });
    }

    private void loadSeries() {
        if (seriesService == null) {
            //add message here
            Platform.runLater(() -> 
                statusLabel.setText("Error: Series service not initialized")
            );
            return;
        }

        try {
            List<Series> seriesList = seriesService.getAll();
            logger.info("Retrieved {} series from service", seriesList.size());
            
            Platform.runLater(() -> {
                try {
                    // Clear existing data
                    allSeries.clear();
                    
                    // Use a Set to filter out duplicates based on title and start year
                    Set<String> uniqueSeriesKeys = new HashSet<>();
                    List<Series> uniqueSeries = new ArrayList<>();
                    
                    for (Series series : seriesList) {
                        String key = series.getTitle().toLowerCase() + "_" + series.getStartYear();
                        if (uniqueSeriesKeys.add(key)) { // add returns true if the key was not already in the set
                            uniqueSeries.add(series);
                        } else {
                            logger.debug("Skipping duplicate series: {} ({})", series.getTitle(), series.getStartYear());
                        }
                    }
                    
                    // Add only unique series to the observable list
                    allSeries.addAll(uniqueSeries);
                    logger.info("Added {} unique series to allSeries list ({} duplicates filtered out)", 
                              uniqueSeries.size(), seriesList.size() - uniqueSeries.size());
                    
                    // Update the filtered list and UI
                    filterSeries();
                    
                    // Update results count
                    if (resultsCountLabel != null) {
                        resultsCountLabel.setText(String.format("Results: %d", uniqueSeries.size()));
                    } else {
                        logger.warn("resultsCountLabel is not initialized");
                    }
                    
                    statusLabel.setText(String.format("Loaded %d unique series", uniqueSeries.size()));
                    logger.info("Successfully loaded and displayed {} unique series", uniqueSeries.size());
                } catch (Exception e) {
                    logger.error("Error in Platform.runLater", e);
                    statusLabel.setText("Error updating UI: " + e.getMessage());
                }
            });
        } catch (Exception e) {
            logger.error("Error loading series", e);
            Platform.runLater(() -> 
                statusLabel.setText("Failed to load series: " + e.getMessage())
            );
        }
    }

    @FXML
    private void handleDeleteSeries(ActionEvent event) {
        try {
            Series selectedSeries = seriesTable.getSelectionModel().getSelectedItem();
            if (selectedSeries != null) {
                if (showConfirmationDialog("Confirm Deletion ","Are you sure you want to delete this series?")) {
                    seriesService.delete(selectedSeries.getId());
                    loadSeries();
                    showSuccess("Success", "Series deleted successfully.");
                }
            } else {
                showAlert("No Selection", "Please select a series to delete.");
            }
        } catch (Exception e) {
            logger.error("Error deleting series", e);
            showError("Error", "Failed to delete series: " + e.getMessage());
        }
    }

    private void filterSeries() {
        String searchText = seriesSearchField.getText().toLowerCase();
        if (searchText.isEmpty()) {
            filteredSeries.setAll(allSeries);
        } else {
            filteredSeries.setAll(allSeries.filtered(series -> {
                boolean titleMatch = series.getTitle().toLowerCase().contains(searchText);
                boolean creatorMatch = series.getCreator() != null && series.getCreator().toLowerCase().contains(searchText);
                boolean genreMatch = series.getGenres().stream()
                    .map(Enum::name)
                    .anyMatch(genre -> genre.toLowerCase().contains(searchText));
                return titleMatch || creatorMatch || genreMatch;
            }));
        }
        
        // Update the results count label
        if (resultsCountLabel != null) {
            int resultCount = filteredSeries.size();
            resultsCountLabel.setText(String.format("Results: %d", resultCount));
            
            // Update status label with search feedback
            if (!searchText.isEmpty()) {
                statusLabel.setText(String.format("Found %d series matching: %s", resultCount, searchText));
            }
        }
        
        seriesTable.setItems(filteredSeries);
    }

    private void sortSeriesTable(String sortOption) {
        if (sortOption == null) {
            return;
        }
        
        Comparator<Series> comparator = switch (sortOption) {
            case "Title (A-Z)" -> Comparator.comparing(Series::getTitle, String.CASE_INSENSITIVE_ORDER);
            case "Title (Z-A)" -> Comparator.comparing(Series::getTitle, String.CASE_INSENSITIVE_ORDER).reversed();
            case "Year (Newest First)" -> Comparator.comparingInt(Series::getStartYear).reversed();
            case "Year (Oldest First)" -> Comparator.comparingInt(Series::getStartYear);
            case "Rating (Highest First)" -> Comparator.comparingDouble(Series::getRating).reversed();
            case "Rating (Lowest First)" -> Comparator.comparingDouble(Series::getRating);
            case "Seasons (Most First)" -> Comparator.comparingInt(Series::getTotalSeasons).reversed();
            case "Seasons (Fewest First)" -> Comparator.comparingInt(Series::getTotalSeasons);
            case "Episodes (Most First)", "Episodes (Fewest First)" -> (s1, s2) -> Integer.compare(
                    s1.getSeasons().stream().mapToInt(season -> season.getEpisodes().size()).sum(),
                    s2.getSeasons().stream().mapToInt(season -> season.getEpisodes().size()).sum()
            );
            default -> null;

            // Determine the appropriate comparator based on the selected sort option
        };

        if (comparator != null) {
            // Create a sorted list and update the table
            FXCollections.sort(filteredSeries, comparator);
            
            // If the table has a sort order, clear it to prevent interference with our custom sort
            if (!seriesTable.getSortOrder().isEmpty()) {
                seriesTable.getSortOrder().clear();
            }
        }
    }

    /**
     * Shows the advanced search dialog with multiple genre selection and improved visibility.
     */
    @FXML
    private void showAdvancedSearchDialog() {
        try {
            // Create a custom dialog
            Dialog<Map<String, Object>> dialog = new Dialog<>();
            dialog.setTitle("Advanced Search");
            dialog.setHeaderText("Search for series with advanced filters");

            // Set the button types
            ButtonType searchButtonType = new ButtonType("Search", ButtonData.OK_DONE);
            dialog.getDialogPane().getButtonTypes().addAll(searchButtonType, ButtonType.CANCEL);

            // Create the search form
            GridPane grid = new GridPane();
            grid.setHgap(10);
            grid.setVgap(10);
            grid.setPadding(new Insets(20, 20, 10, 10));
            
            // Title field
            TextField titleField = new TextField();
            titleField.setPromptText("Title");
            titleField.setStyle("-fx-pref-width: 300px; -fx-padding: 8; -fx-background-radius: 5; -fx-border-radius: 5; -fx-border-color: #666;");
            
            // Year range fields
            Spinner<Integer> yearFromSpinner = new Spinner<>(1900, 2100, 1990);
            Spinner<Integer> yearToSpinner = new Spinner<>(1900, 2100, Calendar.getInstance().get(Calendar.YEAR));
            yearFromSpinner.setStyle("-fx-pref-width: 100px; -fx-padding: 5;");
            yearToSpinner.setStyle("-fx-pref-width: 100px; -fx-padding: 5;");
            
            // Genre selection (checkboxes in a scrollable pane)
            VBox genreBox = new VBox(5);
            genreBox.setStyle("-fx-padding: 5; -fx-background-color: #f5f5f5; -fx-border-color: #ddd; -fx-border-radius: 5; -fx-border-width: 1;");
            ScrollPane genreScroll = new ScrollPane(genreBox);
            genreScroll.setFitToWidth(true);
            genreScroll.setPrefHeight(150);
            genreScroll.setStyle("-fx-background: #f5f5f5; -fx-border-color: #ddd;");
            
            // Add checkboxes for each genre
            Map<CheckBox, Genre> genreCheckBoxes = new HashMap<>();
            for (Genre genre : Genre.values()) {
                CheckBox checkBox = new CheckBox(genre.name());
                checkBox.setStyle("-fx-text-fill: #333; -fx-font-size: 13;");
                genreBox.getChildren().add(checkBox);
                genreCheckBoxes.put(checkBox, genre);
            }
            
            // Rating slider
            Slider ratingSlider = new Slider(0, 10, 0);
            ratingSlider.setShowTickMarks(true);
            ratingSlider.setShowTickLabels(true);
            ratingSlider.setMajorTickUnit(2);
            ratingSlider.setMinorTickCount(1);
            ratingSlider.setSnapToTicks(true);
            ratingSlider.setStyle("-fx-padding: 5 0 0 0;");
            
            // Sort by combo box with improved visibility
            ComboBox<String> sortByCombo = new ComboBox<>();
            sortByCombo.getItems().addAll("Relevance", "Title (A-Z)", "Title (Z-A)", "Year (Newest)", "Year (Oldest)", "Rating (Highest)", "Rating (Lowest)");
            sortByCombo.setValue("Relevance");
            sortByCombo.setStyle(
                "-fx-pref-width: 200px; " +
                "-fx-background-color: white; " +
                "-fx-text-fill: #333; " +
                "-fx-font-size: 13px; " +
                "-fx-border-color: #666; " +
                "-fx-border-radius: 5; " +
                "-fx-padding: 8;"
            );
            
            // Add components to grid
            grid.add(new Label("Title:"), 0, 0);
            grid.add(titleField, 1, 0, 2, 1);
            
            grid.add(new Label("Year Range:"), 0, 1);
            HBox yearBox = new HBox(10, yearFromSpinner, new Label("to"), yearToSpinner);
            yearBox.setAlignment(Pos.CENTER_LEFT);
            grid.add(yearBox, 1, 1, 2, 1);
            
            grid.add(new Label("Genres:"), 0, 2);
            grid.add(genreScroll, 1, 2, 2, 1);
            
            grid.add(new Label("Minimum Rating:"), 0, 3);
            grid.add(ratingSlider, 1, 3, 2, 1);
            
            grid.add(new Label("Sort By:"), 0, 4);
            grid.add(sortByCombo, 1, 4, 2, 1);
            
            // Style the dialog pane
            dialog.getDialogPane().setContent(grid);
            dialog.getDialogPane().setStyle(
                "-fx-background-color: #f9f9f9; " +
                "-fx-padding: 10;"
            );
            
            // Style the buttons
            Node searchButton = dialog.getDialogPane().lookupButton(searchButtonType);
            searchButton.setStyle(
                "-fx-background-color: #4CAF50; " +
                "-fx-text-fill: white; " +
                "-fx-font-weight: bold; " +
                "-fx-padding: 8 16; " +
                "-fx-background-radius: 5;"
            );
            
            Node cancelButton = dialog.getDialogPane().lookupButton(ButtonType.CANCEL);
            cancelButton.setStyle(
                "-fx-background-color: #f44336; " +
                "-fx-text-fill: white; " +
                "-fx-font-weight: bold; " +
                "-fx-padding: 8 16; " +
                "-fx-background-radius: 5;"
            );
            
            // Convert the result to a map when the search button is clicked
            dialog.setResultConverter(dialogButton -> {
                if (dialogButton == searchButtonType) {
                    Map<String, Object> searchParams = new HashMap<>();
                    searchParams.put("title", titleField.getText().trim());
                    searchParams.put("yearFrom", yearFromSpinner.getValue());
                    searchParams.put("yearTo", yearToSpinner.getValue());
                    
                    // Get selected genres
                    List<Genre> selectedGenres = genreCheckBoxes.entrySet().stream()
                        .filter(entry -> entry.getKey().isSelected())
                        .map(Map.Entry::getValue)
                        .collect(Collectors.toList());
                    searchParams.put("genres", selectedGenres);
                    
                    searchParams.put("minRating", ratingSlider.getValue());
                    searchParams.put("sortBy", sortByCombo.getValue());
                    
                    return searchParams;
                }
                return null;
            });
            
            // Show the dialog and process the result
            Optional<Map<String, Object>> result = dialog.showAndWait();
            result.ifPresent(this::performAdvancedSearch);
            
        } catch (Exception e) {
            logger.error("Error showing advanced search dialog", e);
            showError("Error", "Failed to show advanced search dialog: " + e.getMessage());
        }
    }
    
    /**
     * Performs the advanced search with the given parameters.
     * @param searchParams The search parameters
     */
    private void performAdvancedSearch(Map<String, Object> searchParams) {
        try {
            String title = (String) searchParams.get("title");
            int yearFrom = (int) searchParams.get("yearFrom");
            int yearTo = (int) searchParams.get("yearTo");
            @SuppressWarnings("unchecked")
            List<Genre> genres = (List<Genre>) searchParams.get("genres");
            double minRating = (double) searchParams.get("minRating");
            String sortBy = (String) searchParams.get("sortBy");
            
            // Apply filters
            List<Series> filtered = allSeries.stream()
                .filter(series -> title.isEmpty() || series.getTitle().toLowerCase().contains(title.toLowerCase()))
                .filter(series -> series.getStartYear() >= yearFrom && series.getStartYear() <= yearTo)
                .filter(series -> {
                    if (genres.isEmpty()) return true;
                    return genres.stream().anyMatch(genre -> series.getGenres().contains(genre));
                })
                .filter(series -> series.getRating() >= minRating)
                .collect(Collectors.toList());
            
            // Apply sorting
            Comparator<Series> comparator = getSortComparator(sortBy);
            if (comparator != null) {
                filtered.sort(comparator);
            }
            
            // Update the table
            filteredSeries.setAll(filtered);
            
            // Show result count
            statusLabel.setText(String.format("Found %d series matching your criteria", filtered.size()));
            
        } catch (Exception e) {
            logger.error("Error performing advanced search", e);
            showError("Search Error", "Failed to perform search: " + e.getMessage());
        }
    }
    
    /**
     * Returns a comparator based on the sort option.
     */
    private Comparator<Series> getSortComparator(String sortOption) {
        if (sortOption == null) return null;

        return switch (sortOption) {
            case "Title (A-Z)" -> Comparator.comparing(Series::getTitle, String.CASE_INSENSITIVE_ORDER);
            case "Title (Z-A)" -> Comparator.comparing(Series::getTitle, String.CASE_INSENSITIVE_ORDER).reversed();
            case "Year (Newest)" -> Comparator.comparingInt(Series::getStartYear).reversed();
            case "Year (Oldest)" -> Comparator.comparingInt(Series::getStartYear);
            case "Rating (Highest)" -> Comparator.comparingDouble(Series::getRating).reversed();
            case "Rating (Lowest)" -> Comparator.comparingDouble(Series::getRating);
            default -> // Relevance or unknown
                    null;
        };
    }
    
    @FXML
    private void handleAddSeries(ActionEvent event) {
        if (seriesService == null) {
            logger.error("SeriesService is not initialized");
            showError("Error", "Series service is not available");
            return;
        }
        
        try {
            // Create a new series with default values
            Series newSeries = new Series("New Series");
            
            // Show the edit dialog for the new series
            if (showSeriesEditDialog(newSeries)) {
                // Save the new series using the instance method
                seriesService.save(newSeries);
                loadSeries();
                showSuccess("Success", "Series added successfully!");
            }
        } catch (Exception e) {
            logger.error("Error adding series", e);
            showError("Error", "Failed to add series: " + e.getMessage());
        }
    }

    @FXML
    private void handleEditSeries(ActionEvent event) {
        Series selected = seriesTable.getSelectionModel().getSelectedItem();
        if (selected != null) {
            showSeriesEditDialog(selected);
        } else {
            showAlert("No Selection", "Please select a series to edit.");
        }
    }

    @FXML
    private void handleRateSeries(ActionEvent event) {
        Series selected = seriesTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert("No Selection", "Please select a series to rate.");
            return;
        }

        // Create a simple dialog to get the rating
        TextInputDialog dialog = new TextInputDialog("5.0");
        dialog.setTitle("Rate Series");
        dialog.setHeaderText(String.format("Rate %s (%d) on a scale of 0-10", 
            selected.getTitle(), selected.getReleaseYear()));
        dialog.setContentText("Rating (0-10):");

        // Show the dialog and process the result
        dialog.showAndWait().ifPresent(ratingStr -> {
            try {
                double rating = Double.parseDouble(ratingStr);
                if (rating < 0 || rating > 10) {
                    showAlert("Invalid Rating", "Please enter a rating between 0 and 10");
                    return;
                }

                // Get the current user ID
                int currentUserId = getCurrentUserId();
                
                // Save the user's rating
                selected.setUserRating(currentUserId, (int) (rating * 2)); // Convert 0-10 to 0-20 for storage
                
                // Update the series in the service
                seriesService.update(selected);
                
                // Show success message
                showAlert("Success", String.format("You rated %s: %.1f/10\nNew average rating: %.1f/10",
                    selected.getTitle(),
                    rating,
                    selected.getImdbRating()));
                
                // Refresh the series list to show updated ratings
                loadSeries();
                
            } catch (NumberFormatException e) {
                showAlert("Invalid Input", "Please enter a valid number between 0 and 10");
            } catch (Exception e) {
                logger.error("Error rating series: {}", e.getMessage(), e);
                showAlert("Error", "Failed to save rating: " + e.getMessage());
            }
        });
    }
    
    /**
     * Handles the refresh button action to reload all series.
     */
    @FXML
    private void handleRefresh(ActionEvent event) {
        try {
            logger.info("Refreshing series...");
            loadSeries();
            showSuccess("Success", "Series refreshed successfully.");
        } catch (Exception e) {
            logger.error("Error refreshing series", e);
            showError("Error", "Failed to refresh series: " + e.getMessage());
        }
    }
    
    //add functionality here
    @FXML
    private void handleManageSeasons(ActionEvent event) {
        Series selected = seriesTable.getSelectionModel().getSelectedItem();
        if (selected != null) {
            showSeasonManagementDialog(selected);
        } else {
            showAlert("No Selection", "Please select a series to manage seasons.");
        }
    }

    /**
     * Shows a dialog for editing series details.
     * @param series The series to edit
     * @return true if the user clicked OK, false otherwise
     */
    private boolean showSeriesEditDialog(Series series) {
        try {
            // Create a custom dialog
            Dialog<ButtonType> dialog = new Dialog<>();
            dialog.setTitle(series.getTitle() != null && !series.getTitle().isEmpty() ? "Edit Series" : "Add New Series");
            dialog.setHeaderText("Series Details");

            // Set the button types
            ButtonType saveButtonType = new ButtonType("Save", ButtonData.OK_DONE);
            dialog.getDialogPane().getButtonTypes().addAll(saveButtonType, ButtonType.CANCEL);

            // Create the form with scroll pane for many fields
            GridPane grid = new GridPane();
            grid.setHgap(10);
            grid.setVgap(10);
            grid.setPadding(new Insets(20, 20, 10, 10));

            // Title
            TextField titleField = new TextField(series.getTitle());
            titleField.setPromptText("Title");
            titleField.setMinWidth(300);

            // Start Year
            Spinner<Integer> startYearSpinner = new Spinner<>(1900, 2100, 
                series.getReleaseYear() > 0 ? series.getReleaseYear() : Calendar.getInstance().get(Calendar.YEAR));
            startYearSpinner.setEditable(true);

            // End Year
            Spinner<Integer> endYearSpinner = new Spinner<>(1900, 2100, 
                series.getEndYear() > 0 ? series.getEndYear() : Calendar.getInstance().get(Calendar.YEAR));
            endYearSpinner.setEditable(true);

            // Genre Selection (multi-select)
            VBox genreBox = new VBox(5);
            genreBox.setStyle("-fx-padding: 5; -fx-background-color: #f5f5f5; -fx-border-color: #ddd; -fx-border-radius: 5;");
            ScrollPane genreScroll = new ScrollPane(genreBox);
            genreScroll.setFitToWidth(true);
            genreScroll.setPrefHeight(100);
            
            // Add checkboxes for each genre
            Map<CheckBox, Genre> genreCheckBoxes = new HashMap<>();
            Set<Genre> selectedGenres = new HashSet<>(series.getGenres());
            
            for (Genre genre : Genre.values()) {
                CheckBox checkBox = new CheckBox(genre.name());
                checkBox.setSelected(selectedGenres.contains(genre));
                checkBox.setStyle("-fx-text-fill: #333; -fx-font-size: 13;");
                genreBox.getChildren().add(checkBox);
                genreCheckBoxes.put(checkBox, genre);
            }

            // Rating
            Spinner<Double> ratingSpinner = new Spinner<>(0.0, 10.0, 
                series.getImdbRating() != null ? series.getImdbRating() : 0.0, 0.1);
            ratingSpinner.setEditable(true);

            // Director
            TextField directorField = new TextField(series.getDirector() != null ? series.getDirector() : "");
            directorField.setPromptText("Director");

            // Cast (comma-separated)
            TextArea castArea = new TextArea();
            castArea.setPromptText("Enter cast members, one per line");
            castArea.setPrefRowCount(3);
            if (series.getActors() != null && !series.getActors().isEmpty()) {
                StringBuilder castText = new StringBuilder();
                for (Actor actor : series.getActors()) {
                    if (actor != null && actor.getFullName() != null) {
                        if (!castText.isEmpty()) {
                            castText.append("\n");
                        }
                        castText.append(actor.getFullName());
                    }
                }
                castArea.setText(castText.toString());
            }

            // Add fields to grid
            int row = 0;
            grid.add(new Label("Title*:"), 0, row);
            grid.add(titleField, 1, row++);
            
            grid.add(new Label("Start Year*:"), 0, row);
            grid.add(startYearSpinner, 1, row++);
            
            grid.add(new Label("End Year:"), 0, row);
            grid.add(endYearSpinner, 1, row++);
            
            grid.add(new Label("Genres*:"), 0, row);
            grid.add(genreScroll, 1, row++);
            
            grid.add(new Label("Rating (0-10):"), 0, row);
            grid.add(ratingSpinner, 1, row++);
            
            grid.add(new Label("Director:"), 0, row);
            grid.add(directorField, 1, row++);
            
            grid.add(new Label("Cast (one per line):"), 0, row);
            grid.add(castArea, 1, row++);

            // Make the dialog resizable
            ScrollPane scrollPane = new ScrollPane(grid);
            scrollPane.setFitToWidth(true);
            scrollPane.setFitToHeight(true);
            
            dialog.getDialogPane().setContent(scrollPane);
            dialog.setResizable(true);
            dialog.getDialogPane().setPrefSize(500, 500);

            // Request focus on the title field by default
            Platform.runLater(titleField::requestFocus);

            // Convert the result to a series when the save button is clicked
            dialog.setResultConverter(dialogButton -> {
                if (dialogButton == saveButtonType) {
                    try {
                        // Basic validation
                        if (titleField.getText().trim().isEmpty()) {
                            showError("Error", "Title is required");
                            return null;
                        }

                        // Update series with new values
                        series.setTitle(titleField.getText().trim());
                        
                        // Set the years
                        Calendar cal = Calendar.getInstance();
                        cal.set(Calendar.YEAR, startYearSpinner.getValue());
                        series.setYear(cal.getTime());
                        series.setEndYear(endYearSpinner.getValue());
                        
                        // Update genres
                        List<Genre> newGenres = genreCheckBoxes.entrySet().stream()
                            .filter(entry -> entry.getKey().isSelected())
                            .map(Map.Entry::getValue)
                            .toList();
                        series.getGenres().clear();
                        series.getGenres().addAll(newGenres);
                        
                        // Update rating
                        series.setImdbRating(ratingSpinner.getValue());
                        
                        // Update director
                        series.setDirector(directorField.getText().trim());
                        
                        // Update cast
                        if (!castArea.getText().trim().isEmpty()) {
                            List<Actor> actors = new ArrayList<>();
                            for (String nameLine : castArea.getText().split("\\n")) {
                                String name = nameLine.trim();
                                if (!name.isEmpty()) {
                                    // Create a new Actor with default values where required
                                    // Using current date, 'U' for unknown gender, and null ethnicity as defaults
                                    Actor actor = new Actor(
                                        name, // First name
                                        "",   // Last name (empty since we don't have this info)
                                        java.time.LocalDate.now(), // Default to current date
                                        'U',  // 'U' for unknown gender
                                        (Ethnicity)null // No ethnicity specified
                                    );
                                    actors.add(actor);
                                }
                            }
                            series.setActors(actors);
                        } else {
                            series.setActors(new ArrayList<>());
                        }
                        
                        return saveButtonType;
                    } catch (Exception e) {
                        logger.error("Error updating series", e);
                        showError("Error", "Failed to update series: " + e.getMessage());
                        return null;
                    }
                }
                return null;
            });

            Optional<ButtonType> result = dialog.showAndWait();
            return result.isPresent() && result.get() == saveButtonType;
        } catch (Exception e) {
            logger.error("Error showing series edit dialog", e);
            showError("Error", "Failed to show series editor: " + e.getMessage());
            return false;
        }
    }

    /**
     * Shows a dialog for rating a TV series.
     * @param series The series to be rated
     */
    private void showRatingDialog(Series series) {
        try {
            // Create a custom dialog
            Dialog<Pair<Integer, String>> dialog = new Dialog<>();
            dialog.setTitle("Rate Series");
            dialog.setHeaderText("Rate " + series.getTitle());

            // Set the button types
            ButtonType rateButtonType = new ButtonType("Submit Rating", ButtonData.OK_DONE);
            dialog.getDialogPane().getButtonTypes().addAll(rateButtonType, ButtonType.CANCEL);

            // Create the rating components
            GridPane grid = new GridPane();
            grid.setHgap(10);
            grid.setVgap(10);
            grid.setPadding(new Insets(20, 20, 10, 10));

            // Create a rating control (using a slider for simplicity)
            Slider ratingSlider = new Slider(1, 10, 5);
            ratingSlider.setShowTickMarks(true);
            ratingSlider.setShowTickLabels(true);
            ratingSlider.setMajorTickUnit(1);
            ratingSlider.setMinorTickCount(0);
            ratingSlider.setSnapToTicks(true);
            ratingSlider.setBlockIncrement(1);

            Label ratingValue = new Label("5");
            Label ratingLabel = new Label("Your Rating (1-10):");
            
            // Add a text area for optional comments
            TextArea commentArea = new TextArea();
            commentArea.setPromptText("Share your thoughts about this series (optional)...");
            commentArea.setWrapText(true);
            commentArea.setPrefRowCount(3);

            // Update the rating value label when slider changes
            ratingSlider.valueProperty().addListener((obs, oldVal, newVal) -> {
                ratingValue.setText(String.format("%d", newVal.intValue()));
            });

            // Add components to grid
            grid.add(ratingLabel, 0, 0);
            grid.add(ratingSlider, 1, 0);
            grid.add(ratingValue, 2, 0);
            grid.add(new Label("Your Review:"), 0, 1);
            grid.add(commentArea, 1, 2, 2, 1);

            // Enable/Disable rate button based on input validation
            Node rateButton = dialog.getDialogPane().lookupButton(rateButtonType);
            rateButton.setDisable(false);

            dialog.getDialogPane().setContent(grid);

            // Request focus on the slider by default
            Platform.runLater(ratingSlider::requestFocus);

            // Convert the result to a rating-comment pair when the rate button is clicked
            dialog.setResultConverter(dialogButton -> {
                if (dialogButton == rateButtonType) {
                    return new Pair<>(ratingSlider.valueProperty().intValue(), commentArea.getText().trim());
                }
                return null;
            });

            // Show the dialog and process the result
            Optional<Pair<Integer, String>> result = dialog.showAndWait();

            result.ifPresent(ratingComment -> {
                int rating = ratingComment.getKey();
                String comment = ratingComment.getValue();
                
                try {
                    // Update the series rating
                    Map<Integer, Integer> userRatings = new HashMap<>(series.getUserRatings());
                    userRatings.put(getCurrentUserId(), rating);
                    series.setUserRatings(userRatings);
                    
                    // Save the updated series
                    seriesService.update(series);
                    
                    // Show success message
                    showSuccess("Rating Submitted", 
                        String.format("You rated %s with %d stars!", series.getTitle(), rating) + 
                        (comment.isEmpty() ? "" : "\n\nYour review: " + comment));
                    
                    // Refresh the series list to show the updated rating
                    loadSeries();
                } catch (Exception e) {
                    logger.error("Error saving rating", e);
                    showError("Error", "Failed to save your rating: " + e.getMessage());
                }
            });
        } catch (Exception e) {
            logger.error("Error showing rating dialog", e);
            showError("Error", "Failed to show rating dialog: " + e.getMessage());
        }
    }
    
    /**
     * Gets the current user's ID.
     * This is a placeholder - you'll need to implement this based on your authentication system.
     * @return The current user's ID
     */
    private int getCurrentUserId() {
        return currentUserId;
    }

    /**
     * Shows a dialog for managing seasons of a TV series.
     * @param series The series whose seasons are being managed
     */
    private void showSeasonManagementDialog(Series series) {
        try {
            // Create a custom dialog
            Dialog<Void> dialog = new Dialog<>();
            dialog.setTitle("Manage Seasons");
            dialog.setHeaderText("Manage seasons for " + series.getTitle());

            // Set the button types
            ButtonType doneButtonType = new ButtonType("Done", ButtonData.FINISH);
            dialog.getDialogPane().getButtonTypes().addAll(doneButtonType);

            // Create the main container
            VBox container = new VBox(10);
            container.setPadding(new Insets(10));

            // Create a list view for seasons
            ListView<Season> seasonListView = new ListView<>();
            ObservableList<Season> seasons = FXCollections.observableArrayList(series.getSeasons());
            seasonListView.setItems(seasons);
            
            // Set cell factory to display season information
            seasonListView.setCellFactory(lv -> new ListCell<Season>() {
                @Override
                protected void updateItem(Season season, boolean empty) {
                    super.updateItem(season, empty);
                    if (empty || season == null) {
                        setText(null);
                    } else {
                        setText(String.format("Season %d (%d episodes, %d)", 
                            season.getSeasonNumber(), 
                            season.getEpisodes() != null ? season.getEpisodes().size() : 0,
                            season.getYear()));
                    }
                }
            });

            // Create buttons for season management
            Button addButton = new Button("Add Season");
            Button editButton = new Button("Edit Season");
            Button removeButton = new Button("Remove Season");

            // Disable edit/remove buttons when no season is selected
            editButton.disableProperty().bind(seasonListView.getSelectionModel().selectedItemProperty().isNull());
            removeButton.disableProperty().bind(seasonListView.getSelectionModel().selectedItemProperty().isNull());

            // Add season button action
            addButton.setOnAction(e -> {
                // Create a dialog to add a new season
                Dialog<Season> addDialog = new Dialog<>();
                addDialog.setTitle("Add New Season");
                addDialog.setHeaderText("Add a new season to " + series.getTitle());

                // Set the button types
                ButtonType addButtonType = new ButtonType("Add", ButtonData.OK_DONE);
                addDialog.getDialogPane().getButtonTypes().addAll(addButtonType, ButtonType.CANCEL);

                // Create form fields
                GridPane grid = new GridPane();
                grid.setHgap(10);
                grid.setVgap(10);
                grid.setPadding(new Insets(20, 20, 10, 10));

                Spinner<Integer> seasonNumberSpinner = new Spinner<>(1, 100, series.getSeasons().size() + 1);
                Spinner<Integer> yearSpinner = new Spinner<>(1900, 2100, Calendar.getInstance().get(Calendar.YEAR));
                Spinner<Integer> episodesSpinner = new Spinner<>(1, 100, 10);

                grid.add(new Label("Season Number:"), 0, 0);
                grid.add(seasonNumberSpinner, 1, 0);
                grid.add(new Label("Year:"), 0, 1);
                grid.add(yearSpinner, 1, 1);
                grid.add(new Label("Number of Episodes:"), 0, 2);
                grid.add(episodesSpinner, 1, 2);

                addDialog.getDialogPane().setContent(grid);
                addDialog.setResultConverter(dialogButton -> {
                    if (dialogButton == addButtonType) {
                        int newSeasonNumber = seasonNumberSpinner.getValue();
                        Season newSeason = new Season(newSeasonNumber, series);
                        newSeason.setYear(yearSpinner.getValue());
                        // Add episodes to the season
                        List<Episode> episodes = new ArrayList<>();
                        for (int i = 1; i <= episodesSpinner.getValue(); i++) {
                            Episode episode = new Episode();
                            episode.setEpisodeNumber(i);
                            episode.setTitle(String.format("Episode %d", i));
                            episodes.add(episode);
                        }
                        newSeason.setEpisodes(episodes);
                        return newSeason;
                    }
                    return null;
                });

                Optional<Season> result = addDialog.showAndWait();
                result.ifPresent(season -> {
                    // Add the new season to the series
                    series.addSeason(season);
                    seasons.setAll(series.getSeasons()); // Refresh the list
                    seriesService.update(series);
                    showSuccess("Success", "Season added successfully!");
                });
            });

            // Edit season button action
            editButton.setOnAction(e -> {
                Season selected = seasonListView.getSelectionModel().getSelectedItem();
                if (selected != null) {
                    // Create a dialog to edit the selected season
                    Dialog<Season> editDialog = new Dialog<>();
                    editDialog.setTitle("Edit Season");
                    editDialog.setHeaderText("Edit Season " + selected.getSeasonNumber());

                    // Set the button types
                    ButtonType saveButtonType = new ButtonType("Save", ButtonData.OK_DONE);
                    editDialog.getDialogPane().getButtonTypes().addAll(saveButtonType, ButtonType.CANCEL);

                    // Create form fields with current values
                    GridPane grid = new GridPane();
                    grid.setHgap(10);
                    grid.setVgap(10);
                    grid.setPadding(new Insets(20, 20, 10, 10));

                    Spinner<Integer> seasonNumberSpinner = new Spinner<>(1, 100, selected.getSeasonNumber());
                    Spinner<Integer> yearSpinner = new Spinner<>(1900, 2100, selected.getYear());
                    Spinner<Integer> episodesSpinner = new Spinner<>(1, 100, selected.getEpisodes().size());

                    grid.add(new Label("Season Number:"), 0, 0);
                    grid.add(seasonNumberSpinner, 1, 0);
                    grid.add(new Label("Year:"), 0, 1);
                    grid.add(yearSpinner, 1, 1);
                    grid.add(new Label("Number of Episodes:"), 0, 2);
                    grid.add(episodesSpinner, 1, 2);

                    editDialog.getDialogPane().setContent(grid);
                    editDialog.setResultConverter(dialogButton -> {
                        if (dialogButton == saveButtonType) {
                            selected.setSeasonNumber(seasonNumberSpinner.getValue());
                            selected.setYear(yearSpinner.getValue());
                            
                            // Update number of episodes if changed
                            int episodeCount = episodesSpinner.getValue();
                            List<Episode> episodes = selected.getEpisodes();
                            if (episodeCount > episodes.size()) {
                                // Add new episodes
                                for (int i = episodes.size() + 1; i <= episodeCount; i++) {
                                    Episode episode = new Episode();
                                    episode.setEpisodeNumber(i);
                                    episode.setTitle(String.format("Episode %d", i));
                                    episodes.add(episode);
                                }
                            } else if (episodeCount < episodes.size()) {
                                // Remove extra episodes
                                episodes.subList(episodeCount, episodes.size()).clear();
                            }
                            return selected;
                        }
                        return null;
                    });

                    editDialog.showAndWait().ifPresent(updatedSeason -> {
                        // Update the season in the series
                        seriesService.update(series);
                        seasonListView.refresh();
                        showSuccess("Success", "Season updated successfully!");
                    });
                }
            });

            // Remove season button action
            removeButton.setOnAction(e -> {
                Season selected = seasonListView.getSelectionModel().getSelectedItem();
                if (selected != null && showConfirmation(
                        "Are you sure you want to remove Season " + selected.getSeasonNumber() + "?")) {
                    series.getSeasons().remove(selected);
                    seasons.setAll(series.getSeasons()); // Refresh the list
                    seriesService.update(series);
                    showSuccess("Success", "Season removed successfully!");
                }
            });

            // Add components to the container
            HBox buttonBox = new HBox(10, addButton, editButton, removeButton);
            container.getChildren().addAll(seasonListView, buttonBox);
            dialog.getDialogPane().setContent(container);

            // Show the dialog
            dialog.showAndWait();

        } catch (Exception e) {
            logger.error("Error showing season management dialog", e);
            showError("Error", "Failed to show season management dialog: " + e.getMessage());
        }
    }

    private boolean showConfirmation(String s) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Confirm Removal");
        alert.setHeaderText(null);
        alert.setContentText(s);
        Optional<ButtonType> result = alert.showAndWait();
        return result.isPresent() && result.get() == ButtonType.OK;
    }
}
