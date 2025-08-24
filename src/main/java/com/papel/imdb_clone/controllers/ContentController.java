package com.papel.imdb_clone.controllers;

import com.papel.imdb_clone.data.RefactoredDataManager;
import com.papel.imdb_clone.data.SearchCriteria;
import com.papel.imdb_clone.enums.Genre;
import com.papel.imdb_clone.model.*;
import com.papel.imdb_clone.service.*;
import com.papel.imdb_clone.util.AppEventBus;
import com.papel.imdb_clone.util.AppStateManager;
import com.papel.imdb_clone.util.PauseTypingDetector;
import com.papel.imdb_clone.util.UIUtils;
import javafx.animation.PauseTransition;
import javafx.application.Platform;
import javafx.beans.property.*;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import javafx.stage.Popup;
import javafx.stage.Stage;
import javafx.util.Duration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URL;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

import static com.papel.imdb_clone.util.UIUtils.showSuccess;

/**
 * Unified controller for managing all content types (movies, series, episodes, etc.).
 * Provides a single entry point for content-related operations.
 */
public class ContentController extends BaseController {
    private static final Logger logger = LoggerFactory.getLogger(ContentController.class);

    // UI Components
    @FXML
    private ComboBox<String> movieSortBy;
    @FXML
    private TextField globalSearchField;
    @FXML
    private ComboBox<String> seriesSortBy;
    @FXML
    private Label statusLabel;
    @FXML
    private TableView<Movie> movieTable;
    @FXML
    private VBox suggestionsContainer;
    @FXML
    private Popup searchSuggestions;
    @FXML
    private Label movieCountLabel;
    @FXML
    private TabPane contentTabPane;
    @FXML
    private Tab moviesTab;
    @FXML
    private Tab seriesTab;
    @FXML
    private VBox contentContainer;

    // Table columns
    @FXML
    private TableColumn<Movie, String> movieTitleColumn;
    @FXML
    private TableColumn<Movie, String> movieYearColumn;
    @FXML
    private TableColumn<Movie, String> movieGenreColumn;
    @FXML
    private TableColumn<Movie, String> movieDirectorColumn;
    @FXML
    private TableColumn<Movie, Double> movieRatingColumn;
    @FXML
    private TableColumn<Movie, Integer> movieDurationColumn;
    @FXML
    private TableColumn<Movie, Void> movieActionsColumn;

    // Series table components
    @FXML
    private TableView<Series> seriesTable;
    @FXML
    private TableColumn<Series, String> seriesTitleColumn;
    @FXML
    private TableColumn<Series, Integer> seriesYearColumn;
    @FXML
    private TableColumn<Series, String> seriesGenreColumn;
    @FXML
    private TableColumn<Series, Integer> seriesSeasonsColumn;
    @FXML
    private TableColumn<Series, Integer> seriesEpisodesColumn;
    @FXML
    private TableColumn<Series, Double> seriesRatingColumn;
    @FXML
    private TableColumn<Series, Void> seriesActionsColumn;

    // Search fields
    @FXML
    private TextField movieSearchField;
    @FXML
    private TextField seriesSearchField;
    @FXML
    private TableView<Season> seasonsTableView;
    @FXML
    private TableView<Episode> episodesTableView;

    // Search related
    private final PauseTypingDetector pauseTypingDetector = new PauseTypingDetector(300);
    private final ObservableList<Movie> allMovies = FXCollections.observableArrayList();
    private final ObservableList<Movie> filteredMovies = FXCollections.observableArrayList();
    private final ObjectProperty<Movie> selectedMovie = new SimpleObjectProperty<>();

    // Services
    private ContentService<Content> contentService;
    private RefactoredDataManager dataManager;
    private ContentDataLoader contentDataLoader;
    private SearchService searchService;


    // State variables
    private Stage primaryStage;
    private User currentUser;
    private String sessionToken;
    private int year;
    private EncryptionService encryptionService;
    private String query;
    private Object movieDetailsController;
    private ContentController watchlistService;
    private ContentController movieService;
    private ContentController seriesService;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        try {
            initializeServices();
            setupEventListeners();
            setupSearchFunctionality();
            setupTableColumns();

            // Set column resize policy programmatically
            if (movieTable != null) {
                movieTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
            }

            loadInitialData();

            // Set default sort options
            if (movieSortBy != null) {
                movieSortBy.getItems().addAll("Title (A-Z)", "Title (Z-A)", "Year (Newest)", "Year (Oldest)", "Rating (High to Low)");
                movieSortBy.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
                    if (newVal != null) {
                        sortMovieTable(newVal);
                    }
                });
                movieSortBy.getSelectionModel().selectFirst();
            }

            if (seriesSortBy != null) {
                seriesSortBy.getItems().addAll("Title (A-Z)", "Title (Z-A)", "Year (Newest)", "Year (Oldest)", "Rating (High to Low)");
                seriesSortBy.getSelectionModel().selectFirst();
            }

            // Update UI based on initial auth state
            updateUIForAuthState();
            logger.info("ContentController initialized successfully");
        } catch (Exception e) {
            logger.error("Error initializing ContentController: {}", e.getMessage(), e);
            UIUtils.showError("Initialization Error", "Failed to initialize content management");
        }
    }

    private void setupSearchFunctionality() {
        // Check if globalSearchField is available
        if (globalSearchField == null) {
            logger.warn("globalSearchField is not available. Search functionality will be disabled.");
            return;
        }

        // Setup search field listener
        globalSearchField.textProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue != null && !newValue.trim().isEmpty()) {
                pauseTypingDetector.runOnPause(() -> {
                    Platform.runLater(() -> performSearch(newValue.trim()));
                });
            } else {
                clearSearch();
            }
        });

        // Handle search field focus
        globalSearchField.focusedProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal && !globalSearchField.getText().isEmpty()) {
                showSearchSuggestions();
            } else if (!newVal) {
                // Small delay to allow for clicking on a suggestion
                PauseTransition delay = new PauseTransition(Duration.millis(200));
                delay.setOnFinished(event -> searchSuggestions.hide());
                delay.play();
            }
        });
    }

    private void setupTableColumns() {
        if (movieTable == null) {
            logger.warn("movieTable is not initialized");
            return;
        }

        // Clear existing columns to avoid duplicates
        movieTable.getColumns().clear();

        // Configure table columns using FXML-injected columns
        if (movieTitleColumn != null) {
            movieTitleColumn.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getTitle()));
            movieTable.getColumns().add(movieTitleColumn);
        }

        if (movieYearColumn != null) {
            movieYearColumn.setCellValueFactory(cellData -> {
                Date releaseDate = cellData.getValue().getReleaseDate();
                if (releaseDate != null) {
                    Calendar cal = Calendar.getInstance();
                    cal.setTime(releaseDate);
                    return new SimpleStringProperty(String.valueOf(cal.get(Calendar.YEAR)));
                }
                return new SimpleStringProperty("");
            });
            movieTable.getColumns().add(movieYearColumn);
        }

        if (movieRatingColumn != null) {
            movieRatingColumn.setCellValueFactory(cellData -> {
                Movie movie = cellData.getValue();
                Double rating = movie != null ? Movie.getRating(movie) : 0.0;
                return new SimpleDoubleProperty(rating).asObject();
            });
            movieTable.getColumns().add(movieRatingColumn);
        }

        // Set the items after columns are configured
        movieTable.setItems(filteredMovies);

        // Add row selection listener
        movieTable.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
            selectedMovie.set(newSelection);
        });
    }

    private void performSearch(String query) {
        if (query == null || query.trim().isEmpty()) {
            filteredMovies.setAll(allMovies);
            return;
        }

        try {
            // Assuming searchService has a search method that takes a query and returns List<Content>
            SearchCriteria criteria = new SearchCriteria(query);
            List<Content> results = searchService.search(criteria);
            List<Movie> movieResults = new ArrayList<>();
            for (Content content : results) {
                if (content instanceof Movie) {
                    movieResults.add((Movie) content);
                }
            }
            filteredMovies.setAll(movieResults);
            updateStatusLabel(movieResults.size());
        } catch (Exception e) {
            logger.error("Error performing search: " + e.getMessage(), e);
            updateStatusLabel(0);
        }
    }

    private void updateSearchSuggestions(List<Movie> movies) {
        suggestionsContainer.getChildren().clear();
        if (movies.isEmpty()) {
            searchSuggestions.hide();
            return;
        }

        for (Movie movie : movies) {
            Label suggestion = new Label(movie.getTitle() + " (" + (movie.getReleaseDate() != null ? movie.getReleaseDate().getYear() + 1900 : "N/A") + ")");
            suggestion.getStyleClass().add("search-suggestion");
            suggestion.setOnMouseClicked(e -> {
                globalSearchField.setText(movie.getTitle());
                searchSuggestions.hide();
                performSearch(movie.getTitle());
            });
            suggestionsContainer.getChildren().add(suggestion);
        }

        if (globalSearchField.getScene() != null && globalSearchField.getScene().getWindow() != null) {
            searchSuggestions.show(globalSearchField,
                    javafx.stage.Screen.getPrimary().getVisualBounds().getMinX() + globalSearchField.localToScene(0, 0).getX() + globalSearchField.getScene().getX() + globalSearchField.getScene().getWindow().getX(),
                    javafx.stage.Screen.getPrimary().getVisualBounds().getMinY() + globalSearchField.localToScene(0, 0).getY() + globalSearchField.getScene().getY() + globalSearchField.getScene().getWindow().getY() + globalSearchField.getHeight()
            );
        }
    }

    private void showSearchSuggestions() {
        if (!suggestionsContainer.getChildren().isEmpty()) {
            if (!searchSuggestions.isShowing()) {
                searchSuggestions.show(globalSearchField,
                        javafx.stage.Screen.getPrimary().getVisualBounds().getMinX() + globalSearchField.localToScene(0, 0).getX() + globalSearchField.getScene().getX() + globalSearchField.getScene().getWindow().getX(),
                        javafx.stage.Screen.getPrimary().getVisualBounds().getMinY() + globalSearchField.localToScene(0, 0).getY() + globalSearchField.getScene().getY() + globalSearchField.getScene().getWindow().getY() + globalSearchField.getHeight()
                );
            }
        } else {
            searchSuggestions.hide();
        }
    }

    private void clearSearch() {
        filteredMovies.setAll(allMovies);
        suggestionsContainer.getChildren().clear();
        searchSuggestions.hide();
        updateStatusLabel(allMovies.size());
    }

    private void updateStatusLabel(int count) {
        statusLabel.setText(String.format("Showing %d %s", count, count == 1 ? "item" : "items"));
    }

    @Override
    protected void initializeController(int currentUserId) throws Exception {
        // This method is called by BaseController.initialize(int)
        // For now, we'll use the parameterless initialize method above
        logger.debug("initializeController called with userId: {}", currentUserId);
    }

    /**
     * Initializes all services required by this controller.
     * Package-private to allow initialization from UICoordinator.
     */
    public void initializeServices() {
        try {
            this.dataManager = RefactoredDataManager.getInstance();
            this.contentDataLoader = ContentDataLoader.getInstance(dataManager);
            this.searchService = new SearchService(dataManager);

            // Initialize content service
            ServiceLocator serviceLocator = ServiceLocator.getInstance();
            this.contentService = serviceLocator.getService(ContentService.class);

            logger.debug("Services initialized successfully");
        } catch (Exception e) {
            logger.error("Error initializing services: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to initialize services", e);
        }
    }

    /**
     * Sets up event listeners for application-wide events.
     */
    private void setupEventListeners() {
        AppEventBus eventBus = AppEventBus.getInstance();

        // Listen for login events
        eventBus.subscribe(AppStateManager.EVT_USER_LOGGED_IN, event -> {
            logger.debug("User logged in, updating UI");
            Platform.runLater(this::updateUIForAuthState);
        });

        // Listen for logout events
        eventBus.subscribe(AppStateManager.EVT_USER_LOGGED_OUT, event -> {
            logger.debug("User logged out, updating UI");
            Platform.runLater(this::updateUIForAuthState);
        });

        // Listen for content updates
        eventBus.subscribe("CONTENT_UPDATED", event -> {
            logger.debug("Content updated, refreshing view");
        });
    }

    /**
     * Initializes content data asynchronously.
     *
     * @return CompletableFuture that completes when content data is loaded
     */
    private CompletableFuture<Void> initializeContentData() {
        CompletableFuture<Void> future = new CompletableFuture<>();

        // Show loading indicator
        showLoadingIndicator(true);

        // Run content loading in background
        Task<Void> loadTask = new Task<>() {
            @Override
            protected Void call() {
                try {
                    contentDataLoader.initializeContentData();
                    return null;
                } catch (Exception e) {
                    throw new RuntimeException("Failed to load content data", e);
                }
            }
        };

        // Handle task completion
        loadTask.setOnSucceeded(e -> {
            showLoadingIndicator(false);
            future.complete(null);
        });

        loadTask.setOnFailed(e -> {
            showLoadingIndicator(false);
            Throwable ex = loadTask.getException();
            logger.error("Error loading content data: {}", ex.getMessage(), ex);
            UIUtils.showError("Loading Error", "Failed to load content data: " + ex.getMessage());
            future.completeExceptionally(ex);
        });

        // Start the task in a background thread
        new Thread(loadTask).start();

        return future;
    }

    /**
     * Shows or hides the loading indicator.
     * @param show Whether to show the loading indicator
     */
    /**
     * Updates the UI based on the current authentication state.
     */
    private void updateUIForAuthState() {
        boolean isAuthenticated = UserService.getInstance(RefactoredDataManager.getInstance(), EncryptionService.getInstance()).isAuthenticated();

        // Update UI elements based on authentication state
        if (isAuthenticated) {
            User currentUser = UserService.getInstance(RefactoredDataManager.getInstance(), EncryptionService.getInstance()).getCurrentUser();
            logger.debug("Updating UI for authenticated user: {}", currentUser.getUsername());

            // Enable/disable UI components based on user role if needed
            // Example: adminMenu.setVisible(currentUser.isAdmin());

        } else {
            logger.debug("Updating UI for unauthenticated user");
            // Reset UI for logged out state
        }
    }

    /**
     * Shows or hides the loading indicator.
     *
     * @param show Whether to show the loading indicator
     */
    private void showLoadingIndicator(boolean show) {
        if (contentContainer == null) {
            logger.warn("Content container is not initialized");
            return;
        }

        Platform.runLater(() -> {
            try {
                // Use UIUtils for consistent loading indicators
                if (show) {
                    UIUtils.showLoadingIndicator(contentContainer, "Loading content...");
                } else {
                    UIUtils.hideLoadingIndicator();
                }
            } catch (Exception e) {
                logger.error("Error updating loading indicator: {}", e.getMessage(), e);
            }
        });
    }

    /**
     * Shows a dialog to add a new movie and returns the created movie.
     *
     * @return the newly created Movie, or null if cancelled
     */
    @FXML
    private Movie showAddMovieDialog() {
        // Create a custom dialog
        Dialog<Movie> dialog = new Dialog<>();
        dialog.setTitle("Add New Movie");
        dialog.setHeaderText("Enter movie details");

        // Set the button types
        ButtonType addButtonType = new ButtonType("Add", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(addButtonType, ButtonType.CANCEL);

        // Create the form fields
        TextField titleField = new TextField();
        titleField.setPromptText("Title");

        TextField yearField = new TextField();
        yearField.setPromptText("Year (e.g., 2023)");

        TextField genreField = new TextField();
        genreField.setPromptText("Genre (comma-separated)");

        TextField directorField = new TextField();
        directorField.setPromptText("Director");

        TextField durationField = new TextField();
        durationField.setPromptText("Duration in minutes");

        TextArea descriptionArea = new TextArea();
        descriptionArea.setPromptText("Description");
        descriptionArea.setWrapText(true);
        descriptionArea.setPrefRowCount(3);

        // Create and configure the form layout
        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 150, 10, 10));

        // Add fields to the grid
        grid.add(new Label("Title:"), 0, 0);
        grid.add(titleField, 1, 0);
        grid.add(new Label("Year:"), 0, 1);
        grid.add(yearField, 1, 1);
        grid.add(new Label("Genre:"), 0, 2);
        grid.add(genreField, 1, 2);
        grid.add(new Label("Director:"), 0, 3);
        grid.add(directorField, 1, 3);
        grid.add(new Label("Duration (min):"), 0, 4);
        grid.add(durationField, 1, 4);
        grid.add(new Label("Description:"), 0, 5);
        grid.add(descriptionArea, 1, 5);

        // Enable/Disable add button depending on whether a title was entered
        Node addButton = dialog.getDialogPane().lookupButton(addButtonType);
        addButton.setDisable(true);

        // Do some validation
        titleField.textProperty().addListener((observable, oldValue, newValue) -> {
            addButton.setDisable(newValue.trim().isEmpty());
        });

        // Set the dialog content
        dialog.getDialogPane().setContent(grid);

        // Request focus on the title field by default
        Platform.runLater(titleField::requestFocus);

        // Convert the result to a Movie object when the add button is clicked
        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == addButtonType) {
                try {
                    Movie movie = new Movie();
                    movie.setTitle(titleField.getText().trim());

                    // Set genres (comma-separated)
                    if (!genreField.getText().trim().isEmpty()) {
                        movie.setGenre(Genre.valueOf(genreField.getText().trim()));
                    }

                    // Set director if provided
                    if (!directorField.getText().trim().isEmpty()) {
                        movie.setDirector(directorField.getText().trim());
                    }

                    // Parse duration with validation
                    try {
                        if (!durationField.getText().trim().isEmpty()) {
                            int duration = Integer.parseInt(durationField.getText().trim());
                            movie.setDuration(duration);
                        }
                    } catch (NumberFormatException e) {
                        showAlert("Invalid Duration", "Please enter a valid duration in minutes");
                        return null;
                    }

                    // Set description if provided
                    if (!descriptionArea.getText().trim().isEmpty()) {
                        movie.setDescription(descriptionArea.getText().trim());
                    }

                    return movie;
                } catch (Exception e) {
                    logger.error("Error creating movie: {}", e.getMessage(), e);
                    showAlert("Error", "Failed to create movie: " + e.getMessage());
                    return null;
                }
            }
            return null;
        });

        // Show the dialog and return the result
        Optional<Movie> result = dialog.showAndWait();
        return result.orElse(null);
    }

    /**
     * Handles the delete series action from the UI.
     * Deletes the selected series after confirmation.
     *
     * @param actionEvent The action event that triggered this method
     */
    @FXML
    private void handleDeleteSeriesAction(ActionEvent actionEvent) {
        Series selectedSeries = seriesTable.getSelectionModel().getSelectedItem();
        if (selectedSeries != null) {
            boolean confirmed = showConfirmationDialog(
                    "Delete Series",
                    String.format("Are you sure you want to delete '%s'? This action cannot be undone.", selectedSeries.getTitle())
            );

            if (confirmed) {
                try {
                    contentService.deleteContent(selectedSeries);
                    refreshSeriesTable();
                    showSuccess("Success", String.format("Series '%s' has been deleted successfully.", selectedSeries.getTitle()));
                } catch (Exception e) {
                    logger.error("Error deleting series: {}", e.getMessage(), e);
                    showError("Error", "Failed to delete series: " + e.getMessage());
                }
            }
        } else {
            showWarning("No Selection", "Please select a series to delete.");
        }
    }

    /**
     * Handles the rate series action from the UI.
     * Shows a dialog to rate the selected series.
     *
     * @param actionEvent The action event that triggered this method
     */
    @FXML
    private void handleRateSeriesAction(ActionEvent actionEvent) {
        Series selectedSeries = seriesTable.getSelectionModel().getSelectedItem();
        if (selectedSeries != null) {
            try {
                // Show rating dialog and get the result
                double rating = showRatingDialog();

                if (rating > 0) {  // User didn't cancel
                    contentService.rateContent(selectedSeries, (float) rating);
                    refreshSeriesTable();
                    showSuccess("Success", String.format("You rated '%s' %.1f", selectedSeries.getTitle(), rating));
                }
            } catch (NumberFormatException e) {
                showError("Invalid Input", "Please enter a valid number between 1.0 and 10.0");
            } catch (Exception e) {
                logger.error("Error rating series: {}", e.getMessage(), e);
                showError("Error", "Failed to rate series: " + e.getMessage());
            }
        } else {
            showWarning("No Selection", "Please select a series to rate.");
        }
    }

    public void handleAddSeason(ActionEvent actionEvent) {
    }

    public void handleAddEpisode(ActionEvent actionEvent) {

    }

    /**
     * Shows a dialog to add a new series and returns the created series.
     *
     * @return the newly created Series, or null if cancelled
     */
    private Series showAddSeriesDialog() {
        // Create a custom dialog
        Dialog<Series> dialog = new Dialog<>();
        dialog.setTitle("Add New Series");
        dialog.setHeaderText("Enter series details");

        // Set the button types
        ButtonType addButtonType = new ButtonType("Add", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(addButtonType, ButtonType.CANCEL);

        // Create the form fields
        TextField titleField = new TextField();
        titleField.setPromptText("Title");

        TextField yearField = new TextField();
        yearField.setPromptText("Year (e.g., 2023)");

        TextField genreField = new TextField();
        genreField.setPromptText("Genre (comma-separated)");

        TextField creatorField = new TextField();
        creatorField.setPromptText("Creator");

        TextField seasonsField = new TextField();
        seasonsField.setPromptText("Number of seasons");

        TextArea descriptionArea = new TextArea();
        descriptionArea.setPromptText("Description");
        descriptionArea.setWrapText(true);
        descriptionArea.setPrefRowCount(3);

        // Create and configure the form layout
        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 150, 10, 10));

        // Add fields to the grid
        grid.add(new Label("Title:"), 0, 0);
        grid.add(titleField, 1, 0);
        grid.add(new Label("Year:"), 0, 1);
        grid.add(yearField, 1, 1);
        grid.add(new Label("Genre:"), 0, 2);
        grid.add(genreField, 1, 2);
        grid.add(new Label("Creator:"), 0, 3);
        grid.add(creatorField, 1, 3);
        grid.add(new Label("Seasons:"), 0, 4);
        grid.add(seasonsField, 1, 4);
        grid.add(new Label("Description:"), 0, 5);
        grid.add(descriptionArea, 1, 5);

        // Enable/Disable add button depending on whether a title was entered
        Node addButton = dialog.getDialogPane().lookupButton(addButtonType);
        addButton.setDisable(true);

        // Do some validation
        titleField.textProperty().addListener((observable, oldValue, newValue) -> {
            addButton.setDisable(newValue.trim().isEmpty());
        });

        // Set the dialog content
        dialog.getDialogPane().setContent(grid);

        // Request focus on the title field by default
        Platform.runLater(titleField::requestFocus);

        // Convert the result to a Series object when the add button is clicked
        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == addButtonType) {
                try {
                    Series series = new Series();
                    series.setTitle(titleField.getText().trim());

                    // Parse year with validation
                    try {
                        int year = Integer.parseInt(yearField.getText().trim());
                        series.setYear(year);
                    } catch (NumberFormatException e) {
                        showAlert("Invalid Year", "Please enter a valid year (e.g., 2023)");
                        return null;
                    }

                    // Set genres (comma-separated)
                    if (!genreField.getText().trim().isEmpty()) {
                        series.setGenre(Genre.valueOf(genreField.getText().trim()));
                    }

                    // Set creator if provided
                    if (!creatorField.getText().trim().isEmpty()) {
                        series.setCreator(creatorField.getText().trim());
                    }

                    // Parse number of seasons with validation
                    try {
                        if (!seasonsField.getText().trim().isEmpty()) {
                            int seasons = Integer.parseInt(seasonsField.getText().trim());
                            if (seasons < 0) {
                                throw new NumberFormatException("Number of seasons cannot be negative");
                            }
                            // Note: You might want to create Season objects here in a real implementation
                        }
                    } catch (NumberFormatException e) {
                        showAlert("Invalid Seasons", "Please enter a valid number of seasons");
                        return null;
                    }

                    // Set description if provided
                    if (!descriptionArea.getText().trim().isEmpty()) {
                        series.setDescription(descriptionArea.getText().trim());
                    }

                    return series;
                } catch (Exception e) {
                    logger.error("Error creating series: {}", e.getMessage(), e);
                    showAlert("Error", "Failed to create series: " + e.getMessage());
                    return null;
                }
            }
            return null;
        });

        // Show the dialog and return the result
        Optional<Series> result = dialog.showAndWait();
        return result.orElse(null);
    }

    @FXML
    private void handleAddMovie() {
        // Open a dialog to add a new movie
        Movie newMovie = showAddMovieDialog();
        if (newMovie != null) {
            contentService.addContent(newMovie);
            refreshMovieTable();
        }

    }

    @FXML
    private void handleRateMovie() {
        Movie selectedMovie = movieTable.getSelectionModel().getSelectedItem();
        if (selectedMovie != null) {
            double rating = showRatingDialog();
            if (rating > 0) {
                contentService.rateContent(selectedMovie, (float) rating);
                refreshMovieTable();
            }
        } else {
            showAlert("No Movie Selected", "Please select a movie to rate.");
        }
    }

    private void showAlert(String noMovieSelected, String s) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("No Movie Selected");
        alert.setHeaderText(noMovieSelected);
        alert.setContentText(s);
        alert.showAndWait();
    }

    /**
     * Shows a dialog to input a rating value.
     * Validates that the input is a number between 1.0 and 10.0.
     *
     * @return The rating value, or 0 if cancelled or invalid
     */
    private double showRatingDialog() {
        // Create a new dialog
        Dialog<Double> dialog = new Dialog<>();
        dialog.setTitle("Rate Content");
        dialog.setHeaderText("Please enter a rating (1-10):");

        // Set the button types
        ButtonType okButtonType = new ButtonType("OK", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(okButtonType, ButtonType.CANCEL);

        // Create the rating input field
        TextField ratingInput = new TextField();
        ratingInput.setPromptText("1-10");

        // Set the dialog content
        VBox content = new VBox(10);
        content.getChildren().addAll(new Label("Rating (1-10):"), ratingInput);
        dialog.getDialogPane().setContent(content);

        // Enable/disable OK button based on input validity
        Node okButton = dialog.getDialogPane().lookupButton(okButtonType);
        okButton.setDisable(true);

        // Add input validation
        ratingInput.textProperty().addListener((observable, oldValue, newValue) -> {
            try {
                if (!newValue.isEmpty()) {
                    double rating = Double.parseDouble(newValue);
                    okButton.setDisable(rating < 1 || rating > 10);
                } else {
                    okButton.setDisable(true);
                }
            } catch (NumberFormatException e) {
                okButton.setDisable(true);
            }
        });

        // Convert the result to a double when OK is clicked
        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == okButtonType) {
                try {
                    return Double.parseDouble(ratingInput.getText());
                } catch (NumberFormatException e) {
                    return 0.0;
                }
            }
            return 0.0;
        });

        // Show the dialog and return the result
        dialog.showAndWait();
        return dialog.getResult() != null ? dialog.getResult() : 0.0;
    }

    @FXML
    private void handleDeleteMovie() {
        Movie selectedMovie = movieTable.getSelectionModel().getSelectedItem();
        if (selectedMovie != null) {
            boolean confirmed = showConfirmationDialog("Delete Movie", "Are you sure you want to delete this movie?");
            if (confirmed) {
                contentService.deleteContent(selectedMovie);
                refreshMovieTable();
            }
        } else {
            showAlert("No Movie Selected", "Please select a movie to delete.");
        }
    }

    /**
     * Shows a confirmation dialog with the specified title and message.
     *
     * @param title   the title of the dialog
     * @param message the message to display in the dialog
     * @return true if the user clicked OK, false otherwise
     */
    /**
     * Shows a confirmation dialog with the specified title and message.
     *
     * @param title   The title of the dialog
     * @param message The message to display in the dialog
     * @return true if the user confirmed, false otherwise
     */
    private boolean showConfirmationDialog(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle(title);
        alert.setHeaderText(message);
        alert.setContentText("Are you sure you want to proceed?");

        // Show the dialog and wait for user response
        java.util.Optional<ButtonType> result = alert.showAndWait();

        // Return true if user clicked OK, false otherwise
        return result.isPresent() && result.get() == ButtonType.OK;
    }

    @FXML
    private void handleAddSeries() {
        // Open a dialog to add a new series
        Series newSeries = showAddSeriesDialog();
        if (newSeries != null) {
            contentService.addContent(newSeries);
            refreshSeriesTable();
        }
    }

    @FXML
    private void handleRateSeries() {
        Series selectedSeries = seriesTable.getSelectionModel().getSelectedItem();
        if (selectedSeries != null) {
            double rating = showRatingDialog();
            if (rating > 0) {
                contentService.rateContent(selectedSeries, (float) rating);
                refreshSeriesTable();
            }
        } else {
            showAlert("No Series Selected", "Please select a series to rate.");
        }
    }

    @FXML
    private void handleDeleteSeries() {
        Series selectedSeries = seriesTable.getSelectionModel().getSelectedItem();
        if (selectedSeries != null) {
            boolean confirmed = showConfirmationDialog("Delete Series", "Are you sure you want to delete this series?");
            if (confirmed) {
                contentService.deleteContent(selectedSeries);
                refreshSeriesTable();
            }
        } else {
            showAlert("No Series Selected", "Please select a series to delete.");
        }
    }

    private void initializeMovieTableColumns() {
        // Initialize movie table columns
        movieTitleColumn.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getTitle()));
        movieYearColumn.setCellValueFactory(cellData -> {
            Date date = cellData.getValue().getReleaseDate();
            if (date != null) {
                Calendar cal = Calendar.getInstance();
                cal.setTime(date);
                return new SimpleStringProperty(String.valueOf(cal.get(Calendar.YEAR)));
            }
            return new SimpleStringProperty("");
        });
        movieGenreColumn.setCellValueFactory(cellData -> {
            List<Genre> genres = cellData.getValue().getGenres();
            String genreText = genres != null ?
                    genres.stream().map(Enum::name).collect(Collectors.joining(", ")) : "";
            return new SimpleStringProperty(genreText);
        });
        movieRatingColumn.setCellValueFactory(cellData ->
                new SimpleDoubleProperty(Movie.getRating(cellData.getValue())).asObject()
        );
        movieDurationColumn.setCellValueFactory(cellData -> {
            Integer duration = cellData.getValue().getDuration();
            return new SimpleIntegerProperty(duration != null ? duration : 0).asObject();
        });
    }

    private void sortMovieTable(String sortOption) {
        if (sortOption == null) return;

        Comparator<Movie> comparator = switch (sortOption) {
            case "Title (A-Z)" -> Comparator.comparing(Movie::getTitle);
            case "Title (Z-A)" -> Comparator.comparing(Movie::getTitle).reversed();
            case "Year (Newest)" -> (m1, m2) -> {
                Date d1 = m1.getReleaseDate();
                Date d2 = m2.getReleaseDate();
                if (d1 == null && d2 == null) return 0;
                if (d1 == null) return 1;
                if (d2 == null) return -1;
                return d2.compareTo(d1);
            };
            case "Year (Oldest)" -> (m1, m2) -> {
                Date d1 = m1.getReleaseDate();
                Date d2 = m2.getReleaseDate();
                if (d1 == null && d2 == null) return 0;
                if (d1 == null) return 1;
                if (d2 == null) return -1;
                return d1.compareTo(d2);
            };
            case "Rating (Highest)" -> (m1, m2) -> Double.compare(
                    Movie.getRating(m2),
                    Movie.getRating(m1)
            );
            default -> (m1, m2) -> 0;
        };

        FXCollections.sort(movieTable.getItems(), comparator);
    }

    private void loadInitialData() {
        try {
            // Show loading indicator
            showLoadingIndicator(true);

            // Load data in background
            Task<Void> loadTask = new Task<>() {
                @Override
                protected Void call() {
                    // Load movies
                    List<Movie> movies = contentService.getAllMovies();
                    Platform.runLater(() -> {
                        allMovies.setAll(movies);
                        filteredMovies.setAll(movies);
                        updateStatusLabel(movies.size());

                        // Update movie count
                        if (movieCountLabel != null) {
                            movieCountLabel.setText(String.valueOf(movies.size()));
                        }

                        // Initial sort
                        if (movieSortBy != null) {
                            sortMovieTable(movieSortBy.getSelectionModel().getSelectedItem());
                        }
                    });
                    return null;
                }
            };

            // Handle completion
            loadTask.setOnFailed(e -> {
                Throwable ex = loadTask.getException();
                logger.error("Error loading initial data: {}", ex.getMessage(), ex);
                Platform.runLater(() ->
                        UIUtils.showError("Loading Error", "Failed to load initial data: " + ex.getMessage()));
            });

            // Start the task in a background thread
            new Thread(loadTask).start();

        } catch (Exception e) {
            logger.error("Error in loadInitialData: {}", e.getMessage(), e);
            UIUtils.showError("Error", "Failed to load initial data: " + e.getMessage());
        }
    }

    private void refreshMovieTable() {
        // Refresh the movie table with updated data
        List<Movie> movies = contentService.getAllMovies();
        movieTable.setItems(FXCollections.observableArrayList(movies));
        if (movieSortBy != null) {
            sortMovieTable(movieSortBy.getSelectionModel().getSelectedItem());
        }
    }

    private void refreshSeriesTable() {
        // Refresh the series table with updated data
        seriesTable.setItems(FXCollections.observableArrayList(contentService.getAllSeries()));
    }


    public void handleEditSeries(ActionEvent actionEvent) {
    }

    public void setUserSession(User currentUser, String sessionToken) {
        this.currentUser = currentUser;
        this.sessionToken = sessionToken;
    }
}
