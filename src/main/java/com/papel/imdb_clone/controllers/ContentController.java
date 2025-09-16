package com.papel.imdb_clone.controllers;

import com.papel.imdb_clone.data.RefactoredDataManager;
import com.papel.imdb_clone.enums.Genre;
import com.papel.imdb_clone.exceptions.ContentNotFoundException;
import com.papel.imdb_clone.model.*;
import com.papel.imdb_clone.service.*;
import com.papel.imdb_clone.util.AppEventBus;
import com.papel.imdb_clone.util.AppStateManager;
import com.papel.imdb_clone.util.UIUtils;
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
import javafx.scene.layout.*;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URL;
import java.util.*;
import java.util.stream.Collectors;

import static com.papel.imdb_clone.util.UIUtils.showSuccess;

/**
 * Unified controller for managing all content types (movies, series, episodes, etc.).
 * Provides a single entry point for content-related operations.
 */
public class ContentController extends BaseController {
    private static final Logger logger = LoggerFactory.getLogger(ContentController.class);

    // UI Components
    // Service-related methods

    @FXML
    private ComboBox<String> movieSortBy;
    @FXML
    private TextField globalSearchField;
    @FXML
    private ComboBox<String> seriesSortBy;

    /**
     * Handles the Add Movie button click event.
     * Opens a dialog to add a new movie.
     */
    @FXML
    private void handleAddMovie(ActionEvent event) {
        try {
            // Create a dialog for adding a new movie
            Dialog<Movie> dialog = new Dialog<>();
            dialog.setTitle("Add New Movie");
            dialog.setHeaderText("Enter Movie Details");

            // Set the button types
            ButtonType addButton = new ButtonType("Add", ButtonBar.ButtonData.OK_DONE);
            dialog.getDialogPane().getButtonTypes().addAll(addButton, ButtonType.CANCEL);

            // Create form fields
            TextField titleField = new TextField();
            TextField yearField = new TextField();

            // Set up the grid
            GridPane grid = new GridPane();
            grid.setHgap(10);
            grid.setVgap(10);
            grid.setPadding(new Insets(20, 150, 10, 10));

            // Add fields to grid
            grid.add(new Label("Title:"), 0, 0);
            grid.add(titleField, 1, 0);
            grid.add(new Label("Year:"), 0, 1);
            grid.add(yearField, 1, 1);

            dialog.getDialogPane().setContent(grid);

            // Request focus on the title field by default
            Platform.runLater(titleField::requestFocus);

            // Convert the result to a Movie object when the Add button is clicked
            dialog.setResultConverter(dialogButton -> {
                if (dialogButton == addButton) {
                    try {
                        //create a new movie object
                        Movie movie = new Movie();
                        movie.setTitle(titleField.getText().trim());
                        movie.setStartYear(Integer.parseInt(yearField.getText().trim()));
                        return movie;
                    } catch (NumberFormatException e) {
                        showError("Invalid Input", "Please enter valid numbers for year and duration.");
                        return null;
                    }
                }
                return null;
            });

            // Show the dialog and process the result
            Optional<Movie> result = dialog.showAndWait();

            result.ifPresent(movie -> {
                // Add the movie to the data manager

                RefactoredDataManager dataManager = ServiceLocator.getInstance().getDataManager();
                dataManager.addMovie(movie);

                // Show success message
                showSuccess("Movie Added", String.format("Movie '%s' has been added successfully.", movie.getTitle()));

                // Refresh the movie list
                loadMovies();
            });

        } catch (Exception e) {
            logger.error("Error adding movie", e);
            showError("Error", "An error occurred while adding the movie: " + e.getMessage());
        }
    }

    private void loadMovies() {
        try {

            // Get movies from data manager
            RefactoredDataManager dataManager = ServiceLocator.getInstance().getDataManager();
            List<Movie> movies = dataManager.getMovies();

            // Update UI on JavaFX Application Thread
            Platform.runLater(() -> {
                try {
                    // Clear existing items and add all movies
                    movieTable.getItems().setAll(movies);

                    // Update status
                    statusLabel.setText(String.format("Loaded %d movies", movies.size()));
                    logger.info("Successfully loaded {} movies", movies.size());
                } catch (Exception e) {
                    // Log error and show error message
                    logger.error("Error updating movie table", e);
                    statusLabel.setText("Error loading movies");
                    showError("Error", "Failed to load movies: " + e.getMessage());
                } finally {
                    statusLabel.setText("Loaded movies");
                }
            });

        } catch (Exception e) {
            // Log error and show error message
            logger.error("Error in loadMovies", e);
            Platform.runLater(() -> {
                // Set status label
                statusLabel.setText("Error loading movies");
                showError("Error", "Failed to load movies: " + e.getMessage());
            });
        }
    }

    @FXML
    private Label statusLabel;

    @FXML
    private StackPane loadingIndicatorContainer;

    @FXML
    private ProgressIndicator movieLoadingIndicator;
    @FXML
    private TableView<Movie> movieTable;


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

    @FXML
    public void initialize() {
        // Initialize movie table columns
        movieTitleColumn.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getTitle()));
        movieYearColumn.setCellValueFactory(cellData -> new SimpleStringProperty(String.valueOf(cellData.getValue().getStartYear())));

        // Set up cell value factories for other columns
        movieGenreColumn.setCellValueFactory(cellData -> new SimpleStringProperty(
                //if genres is null or empty, return "N/A"
                cellData.getValue().getGenres().stream()
                .map(Genre::name)
                .collect(Collectors.joining(", "))
        ));
        // Set up cell value factory for rating column
        movieRatingColumn.setCellValueFactory(cellData -> {
            Double rating = cellData.getValue().getImdbRating();
            return new SimpleDoubleProperty(rating != null ? rating : 0.0).asObject();
        });

        // Load initial data
        loadMovies();
    }

    // Search fields
    @FXML
    private TextField movieSearchField;
    @FXML
    private TextField seriesSearchField;

    // Search related
    private final ObservableList<Movie> allMovies = FXCollections.observableArrayList();
    private final ObservableList<Movie> filteredMovies = FXCollections.observableArrayList();
    private final ObjectProperty<Movie> selectedMovie = new SimpleObjectProperty<>();
    // Add these lines with other field declarations
    private final ObservableList<Series> allSeries = FXCollections.observableArrayList();
    private final ObservableList<Series> filteredSeries = FXCollections.observableArrayList();
    // Services
    private ContentService<Content> contentService;
    private RefactoredDataManager dataManager;
    private ContentDataLoader contentDataLoader;
    private SearchService searchService;

    /**
     * Initializes all services required by the ContentController.
     * This method should be called during controller initialization.
     *
     * @throws IllegalStateException if any required service fails to initialize
     */
    private void initializeServices() throws IllegalStateException {
        logger.info("Initializing ContentController services...");

        try {
            // 1. Initialize DataManager
            this.dataManager = RefactoredDataManager.getInstance();
            if (this.dataManager == null) {
                throw new IllegalStateException("Failed to initialize DataManager");
            }

            // 2. Initialize ContentDataLoader with DataManager
            this.contentDataLoader = ContentDataLoader.getInstance(dataManager);
            if (this.contentDataLoader == null) {
                throw new IllegalStateException("Failed to initialize ContentDataLoader");
            }

            // 3. Initialize SearchService
            this.searchService = new SearchService(dataManager);

            // 4. Get ContentService from ServiceLocator
            ServiceLocator serviceLocator = ServiceLocator.getInstance();
            this.contentService = serviceLocator.getService(ContentService.class, "movie");
            if (this.contentService == null) {
                logger.warn("ContentService for 'movie' not found in ServiceLocator");
            }


            logger.info("All services initialized successfully");

        } catch (Exception e) {
            // Log error and throw exception
            String errorMsg = "Failed to initialize services: " + e.getMessage();
            logger.error(errorMsg, e);
            throw new IllegalStateException(errorMsg, e);
        }
    }

    // State variables
    private Stage primaryStage;
    private User currentUser;
    private String sessionToken;
    private int year;

    // Initialize controller
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        logger.info("Initializing ContentController...");

        try {
            // Initialize services first
            try {
                initializeServices();
                logger.info("Services initialized successfully");

                // Load initial data if dataManager is available
                if (dataManager != null) {
                    Platform.runLater(this::loadInitialData);
                } else {
                    logger.error("DataManager initialization failed. Content loading will be skipped.");
                }
                // Initialize UI components
            } catch (Exception e) {
                logger.error("Failed to initialize services: {}", e.getMessage(), e);
                // Show error alert
                Platform.runLater(() ->
                        showAlert("Initialization Error",
                                "Failed to initialize required services.\n" +
                                        "Please restart the application.\n\n" +
                                        "Error: " + e.getMessage())
                );
                return; // Stop further initialization if services can't be initialized
            }

            // Initialize tables and columns
            setupTableColumns();
            setupEventListeners();

            // Initialize movie table if it exists
            if (movieTable != null) {
                movieTable.setPlaceholder(new Label("No movies found"));
                allMovies.clear();
                filteredMovies.clear();
                movieTable.setItems(filteredMovies);

                // Add double-click handler for movie table
                movieTable.setRowFactory(tv -> {
                    TableRow<Movie> row = new TableRow<>();
                    row.setOnMouseClicked(event -> {
                        if (event.getClickCount() == 2 && !row.isEmpty()) {
                            Movie movie = row.getItem();
                            showContentDetails(movie);
                        }
                    });
                    return row;
                });

                // Set up sorting
                if (movieSortBy != null) {
                    movieSortBy.getItems().addAll("Title (A-Z)", "Title (Z-A)", "Year (Newest)", "Year (Oldest)", "Rating (High to Low)");
                    movieSortBy.setValue("Title (A-Z)");
                    // Add listener to sort movie table, if sort by is changed
                    movieSortBy.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
                        if (newVal != null) {
                            sortMovieTable(newVal);
                        }
                    });
                }

            }

            // Initialize series table if it exists
            if (seriesTable != null) {
                seriesTable.setPlaceholder(new Label("No series found"));
                allSeries.clear();
                filteredSeries.clear();
                seriesTable.setItems(filteredSeries);

                // Add double-click handler for series table
                seriesTable.setRowFactory(tv -> {
                    // Set up row factory for series table
                    TableRow<Series> row = new TableRow<>();
                    row.setOnMouseClicked(event -> {
                        // Handle double-click on series table,which opens the series details view
                        if (event.getClickCount() == 2 && !row.isEmpty()) {
                            Series series = row.getItem();
                            showContentDetails(series);
                        }
                    });
                    // Return row,which means the row factory is set up
                    return row;
                });

                // Set up sorting
                if (seriesSortBy != null) {
                    // Add items to series sort by combo box
                    seriesSortBy.getItems().addAll("Title (A-Z)", "Title (Z-A)", "Year (Newest)", "Year (Oldest)", "Rating (High to Low)");
                    seriesSortBy.setValue("Title (A-Z)");
                    // Add listener to sort series table, if sort by is changed
                    seriesSortBy.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
                        if (newVal != null) {
                            sortSeriesTable(newVal);
                        }
                    });
                }

            // Set up sorting for episodes tables

            }

            // Load initial data
            loadInitialData();

        } catch (Exception e) {
            // Log the error
            logger.error("Error initializing controller: {}", e.getMessage(), e);
            showAlert("Initialization Error", "Failed to initialize the application: " + e.getMessage());
        }
    }

    //show alert
    private void showAlert(String title, String message) {
        UIUtils.showAlert(Alert.AlertType.ERROR, title, message);
    }

    /**
     * Shows a dialog to rate a movie
     * @param movie The movie to rate
     */
    private void showRateMovieDialog(Movie movie) {
        if (movie == null) return;

        // Create a dialog for rating the movie
        Dialog<Double> dialog = new Dialog<>();
        dialog.setTitle("Rate Movie");
        dialog.setHeaderText(String.format("Rate '%s' (1-10)", movie.getTitle()));

        // Set the button types
        ButtonType rateButton = new ButtonType("Rate", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(rateButton, ButtonType.CANCEL);

        // Create form fields
        TextField ratingField = new TextField();
        ratingField.setPromptText("Enter rating (1-10)");

        // Set up the grid
        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 150, 10, 10));
        grid.add(new Label("Rating:"), 0, 0);
        grid.add(ratingField, 1, 0);

        dialog.getDialogPane().setContent(grid);

        // Request focus on the rating field by default
        Platform.runLater(ratingField::requestFocus);

        // Convert the result to a rating value when the Rate button is clicked
        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == rateButton) {
                try {
                    // Parse the rating field to double
                    double rating = Double.parseDouble(ratingField.getText().trim());
                    if (rating < 1 || rating > 10) {
                        showError("Invalid Rating", "Please enter a rating between 1 and 10.");
                        return null;
                    }
                    // Return the rating
                    return rating;
                } catch (NumberFormatException e) {
                    // Show error alert
                    showError("Invalid Input", "Please enter a valid number for the rating.");
                    return null;
                }
            }
            return null;
        });

        // Show the dialog and process the result
        Optional<Double> result = dialog.showAndWait();

        result.ifPresent(rating -> {
            try {
                // Rate the movie using the content service
                contentService.rateContent(movie, rating.floatValue());

                // Show success message
                showSuccess("Rating Saved",
                    String.format("You rated '%s' %.1f/10", movie.getTitle(), rating));

                // Refresh the movie list
                loadMovies();

            } catch (Exception e) {
                logger.error("Error rating movie", e);
                showError("Error", "An error occurred while rating the movie: " + e.getMessage());
            }
        });
    }

    /**
     * Handles deleting a movie
     * @param movie The movie to delete
     */
    private void handleDeleteMovie(Movie movie) {
        if (movie == null) return;

        // Confirm deletion
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Confirm Deletion");
        alert.setHeaderText("Delete Movie");
        alert.setContentText(String.format("Are you sure you want to delete '%s'?", movie.getTitle()));

        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            try {
                // Delete the movie (you'll need to implement this)
                deleteMovie(movie);

                // Show success message
                showSuccess("Movie Deleted",
                    String.format("'%s' has been deleted successfully.", movie.getTitle()));

                // Refresh the movie list
                loadMovies();

            } catch (Exception e) {
                // Log the error
                logger.error("Error deleting movie", e);
                showError("Error", "An error occurred while deleting the movie: " + e.getMessage());
            }
        }
    }

    /**
     * Gets the current logged-in user
     * @return The current user, or null if not logged in
     */
    private User getCurrentUser() {
        try {
            // Get the current user from the authentication service
            AuthService authService = ServiceLocator.getInstance().getService(AuthService.class);
            if (authService != null) {
                return authService.getCurrentUser();
            }
        } catch (Exception e) {
            logger.error("Error getting current user", e);
        }
        return null;
    }

    /**
     * Saves a movie
     * @param movie The movie to save
     */
    private void saveMovie(Movie movie) {
        try {
            // Get the data manager,which is used to update the movie
            RefactoredDataManager dataManager = ServiceLocator.getInstance().getDataManager();
            if (dataManager != null) {
                // Update the movie in the data manager
                dataManager.updateMovie(movie);
            }
        } catch (Exception e) {
            logger.error("Error saving movie", e);
            throw new RuntimeException("Failed to save movie", e);
        }
    }

    /**
     * Deletes a movie
     * @param movie The movie to delete
     */
    private void deleteMovie(Movie movie) {
        try {
            // Get the data manager
            RefactoredDataManager dataManager = ServiceLocator.getInstance().getDataManager();
            if (dataManager != null) {
                // Delete the movie from the data manager
                dataManager.deleteMovie(movie.getId());
            }
        } catch (Exception e) {
            logger.error("Error deleting movie", e);
            throw new RuntimeException("Failed to delete movie", e);
        }
    }

    /**
     * Shows the content details view for a movie.
     *
     * @param movie The movie to show details for
     */
    private void showContentDetails(Movie movie) {
        // Use NavigationService to show content details
        NavigationService navigationService = NavigationService.getInstance();
        navigationService.showContentDetails(
                //pass the movie details to the content details view
                movie.getTitle(),
                String.valueOf(movie.getYear()),
                String.format("%.1f/10", movie.getImdbRating()),
                movie.getGenre() != null ? movie.getGenre().toString() : "N/A",
                movie.getBoxOffice(),
                movie.getAwards(),
                movie.getActors()
        );
    }

    /**
     * Shows the content details view for a series.
     *
     * @param series The series to show details for
     */
    private void showContentDetails(Series series) {
        // Use NavigationService to show content details
        NavigationService navigationService = NavigationService.getInstance();
        navigationService.showContentDetails(
                //pass the series details to the content details view
                series.getTitle(),
                String.valueOf(series.getYear()),
                String.format("%.1f/10", series.getImdbRating()),
                series.getGenre() != null ? series.getGenre().toString() : "N/A",
                "N/A", // Series typically don't have box office
                series.getAwards() != null ? series.getAwards() : Collections.singletonList("No awards"),
                series.getActors() != null ? series.getActors() : List.of()
        );
    }

    private void sortSeriesTable(String newVal) {
        switch (newVal) {
            //sort series table by title
            case "Title (A-Z)":
                seriesTable.getSortOrder().clear();
                seriesTable.getSortOrder().add(seriesTitleColumn);
                break;
            case "Title (Z-A)":
                seriesTable.getSortOrder().clear();
                seriesTable.getSortOrder().add(seriesTitleColumn);
                seriesTable.getSortOrder().add(seriesTitleColumn);
                break;
                //sort series table by year
            case "Year (Newest)":
                seriesTable.getSortOrder().clear();
                seriesTable.getSortOrder().add(seriesYearColumn);
                break;
            case "Year (Oldest)":
                seriesTable.getSortOrder().clear();
                seriesTable.getSortOrder().add(seriesYearColumn);
                seriesTable.getSortOrder().add(seriesYearColumn);
                break;
                //sort series table by rating
            case "Rating (High to Low)":
                seriesTable.getSortOrder().clear();
                seriesTable.getSortOrder().add(seriesRatingColumn);
                break;
            default:
                //clear the sort order,which will remove any existing sorting
                seriesTable.getSortOrder().clear();
                break;
        }
    }
    /**
     * Sets up event listeners for the content controller.
     */
    private void setupEventListeners() {
        AppEventBus eventBus = AppEventBus.getInstance();
        eventBus.subscribe(AppStateManager.EVT_USER_LOGGED_IN, event -> {
            logger.debug("User logged in");
        });
    }

    /**
     * Sorts the movie table based on the specified sort option.
     *
     * @param newVal The sort option selected by the user (e.g., "Title (A-Z)", "Year (Newest)")
     */
    private void sortMovieTable(String newVal) {
        switch (newVal) {
            //sort movie table by title
            case "Title (A-Z)":
                movieTable.getSortOrder().clear();
                movieTable.getSortOrder().add(movieTitleColumn);
                break;
            case "Title (Z-A)":
                movieTable.getSortOrder().clear();
                movieTable.getSortOrder().add(movieTitleColumn);
                movieTable.getSortOrder().add(movieTitleColumn);
                break;
                //sort movie table by year
            case "Year (Newest)":
                movieTable.getSortOrder().clear();
                movieTable.getSortOrder().add(movieYearColumn);
                break;
            case "Year (Oldest)":
                movieTable.getSortOrder().clear();
                movieTable.getSortOrder().add(movieYearColumn);
                movieTable.getSortOrder().add(movieYearColumn);
                break;
                //sort movie table by rating
            case "Rating (High-Low)":
                movieTable.getSortOrder().clear();
                movieTable.getSortOrder().add(movieRatingColumn);
                break;
            case "Rating (Low-High)":
                movieTable.getSortOrder().clear();
                movieTable.getSortOrder().add(movieRatingColumn);
                movieTable.getSortOrder().add(movieRatingColumn);
                break;
            default:
                //clear the sort order,which will remove any existing sorting
                movieTable.getSortOrder().clear();
                break;
        }
    }


    private void setupTableColumns() {
        // Setup movie table columns if they exist
        if (movieTable != null) {
            try {
                // Clear existing columns to avoid duplicates
                movieTable.getColumns().clear();

                // Configure table columns using FXML-injected columns
                if (movieTitleColumn != null) {
                    movieTitleColumn.setCellValueFactory(cellData -> {
                        try {
                            //get movie title from cell data
                            return new SimpleStringProperty(cellData.getValue().getTitle());
                        } catch (Exception e) {
                            //log error
                            logger.warn("Error getting movie title: {}", e.getMessage());
                            return new SimpleStringProperty("");
                        }
                    });
                    //add movie title column to table
                    movieTable.getColumns().add(movieTitleColumn);
                }

                if (movieYearColumn != null) {
                    //set movie year column value factory
                    movieYearColumn.setCellValueFactory(cellData -> {
                        try {
                            //get movie release date from cell data
                            Date releaseDate = cellData.getValue().getReleaseDate();
                            if (releaseDate != null) {
                                //get movie release year from release date
                                Calendar cal = Calendar.getInstance();
                                cal.setTime(releaseDate);
                                return new SimpleStringProperty(String.valueOf(cal.get(Calendar.YEAR)));
                            }
                        } catch (Exception e) {
                            logger.warn("Error getting movie year: {}", e.getMessage());
                        }
                        //return empty string if no release date
                        return new SimpleStringProperty("");
                    });
                    //add movie year column to table
                    movieTable.getColumns().add(movieYearColumn);
                }

                //set movie rating column value factory
                if (movieRatingColumn != null) {
                    movieRatingColumn.setCellValueFactory(cellData -> {
                        try {
                            //get movie rating from cell data
                            Movie movie = cellData.getValue();
                            Double rating = movie != null ? movie.getImdbRating() : 0.0;
                            return new SimpleDoubleProperty(rating).asObject();
                        } catch (Exception e) {
                            logger.warn("Error getting movie rating: {}", e.getMessage());
                            return new SimpleDoubleProperty(0.0).asObject();
                        }
                    });
                    //add movie rating column to table
                    movieTable.getColumns().add(movieRatingColumn);
                }

                logger.debug("Movie table columns initialized successfully");

                //refresh movie table
                refreshMovieTable();


            } catch (Exception e) {
                logger.error("Error setting up movie table columns: {}", e.getMessage(), e);
            }
        }

        // Setup series table columns if they exist
        if (seriesTable != null) {
            try {
                // Clear existing columns to avoid duplicates
                seriesTable.getColumns().clear();

                // Configure series table columns
                if (seriesTitleColumn != null) {
                    seriesTitleColumn.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getTitle()));
                }

                //set series year column value factory
                if (seriesYearColumn != null) {
                    seriesYearColumn.setCellValueFactory(cellData -> {
                        try {
                            //get series year from cell data
                            return new SimpleIntegerProperty(cellData.getValue().getYearAsInt()).asObject();
                        } catch (Exception e) {
                            logger.warn("Error getting series year: {}", e.getMessage());
                            return new SimpleIntegerProperty(0).asObject();
                        }
                    });
                    //add series year column to table
                    seriesTable.getColumns().add(seriesYearColumn);
                }

                //set series rating column value factory
                if (seriesRatingColumn != null) {
                    seriesRatingColumn.setCellValueFactory(cellData -> {
                        try {
                            Series series = cellData.getValue();
                            Double rating = series != null ? series.getRating() : 0.0;
                            return new SimpleDoubleProperty(rating).asObject();
                        } catch (Exception e) {
                            logger.warn("Error getting series rating: {}", e.getMessage());
                            return new SimpleDoubleProperty(0.0).asObject();
                        }
                    });
                    //add series rating column to table
                    seriesTable.getColumns().add(seriesRatingColumn);
                }
            } catch (Exception e) {
                logger.error("Error setting up series table columns: {}", e.getMessage(), e);
            }
        }
    }

    /**
     * Initializes the columns for the series table.
     * Sets up cell value factories and cell factories for each column.
     */
    private void initializeSeriesTableColumns() {
        if (seriesTable == null) {
            logger.warn("Cannot initialize columns: seriesTable is null");
            return;
        }

        try {
            // Clear existing columns to prevent duplicates
            seriesTable.getColumns().clear();

            // Set the items to the filtered list
            seriesTable.setItems(filteredSeries);

            // Title column
            if (seriesTitleColumn != null) {
                seriesTitleColumn.setCellValueFactory(cellData ->
                        new SimpleStringProperty(cellData.getValue().getTitle()));
            }

            // Year column
            if (seriesYearColumn != null) {
                seriesYearColumn.setCellValueFactory(cellData -> {
                    Series series = cellData.getValue();
                    //get series start year from cell data
                    if (series == null || series.getStartYear() == 0) {
                        return new SimpleIntegerProperty(0).asObject();
                    }
                    //return series start year
                    return new SimpleIntegerProperty(series.getYearAsInt()).asObject();
                });

                // Set cell factory to display "N/A" for invalid years
                seriesYearColumn.setCellFactory(column -> new TableCell<Series, Integer>() {
                    @Override
                    protected void updateItem(Integer year, boolean empty) {
                        super.updateItem(year, empty);
                        //set text to "N/A" if empty or null or less than or equal to 0
                        if (empty || year == null || year <= 0) {
                            setText("N/A");
                        } else {
                            //set text to year
                            setText(String.valueOf(year));
                        }
                    }
                });
            }

            // Genre column
            if (seriesGenreColumn != null) {
                seriesGenreColumn.setCellValueFactory(cellData -> {
                    Series series = cellData.getValue();
                    //get series genres from cell data
                    List<Genre> genres = series.getGenres();
                    //join genres with comma,whitespace and "," if more than one genre
                    String genreText = genres != null ?
                            genres.stream()
                                    .map(Genre::name)
                                    .collect(Collectors.joining(", ")) : "";
                    //return genre text
                    return new SimpleStringProperty(genreText);
                });
            }

            // Seasons column
            if (seriesSeasonsColumn != null) {
                //set series seasons column value factory
                seriesSeasonsColumn.setCellValueFactory(cellData ->
                        new SimpleIntegerProperty(cellData.getValue().getSeasons() != null ?
                                cellData.getValue().getSeasons().size() : 0).asObject());
            }

            // Episodes column
            if (seriesEpisodesColumn != null) {
                //set series episodes column value factory
                seriesEpisodesColumn.setCellValueFactory(cellData ->
                        new SimpleIntegerProperty(cellData.getValue().getTotalEpisodes()).asObject());
            }

            // Rating column
            if (seriesRatingColumn != null) {
                //set series rating column value factory
                seriesRatingColumn.setCellValueFactory(cellData ->
                        new SimpleDoubleProperty(cellData.getValue().getImdbRating()).asObject());
            }

            logger.debug("Series table columns initialized successfully");
        } catch (Exception e) {
            logger.error("Error initializing series table columns: {}", e.getMessage(), e);
        }
    }


    /**
     * Initializes the controller with the specified user ID.
     * This method should be called after the controller is created.
     *
     * @param currentUserId The ID of the current user, or -1 if no user is logged in
     */
    @Override
    protected void initializeController(int currentUserId) throws Exception {
        // This method is called by BaseController.initialize(int)
        logger.debug("initializeController called with userId: {}", currentUserId);
    }


    private void handleAddEpisode(Series series, Season season) {
        if (series == null || season == null) {
            showError("Error", "Series or season not selected");
            return;
        }

        // Create a dialog to get episode details
        Dialog<Episode> dialog = new Dialog<>();
        dialog.setTitle("Add Episode");
        dialog.setHeaderText(String.format("Add a new episode to %s - Season %d",
                series.getTitle(), season.getSeasonNumber()));

        // Set the button types
        ButtonType addButtonType = new ButtonType("Add", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(addButtonType, ButtonType.CANCEL);

        // Create the content
        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 150, 10, 10));

        // Create the input fields
        TextField episodeNumberField = new TextField();
        episodeNumberField.setPromptText("1");
        TextField titleField = new TextField();
        titleField.setPromptText("Episode Title");

        grid.add(new Label("Episode Number:"), 0, 0);
        grid.add(episodeNumberField, 1, 0);
        grid.add(new Label("Title:"), 0, 1);
        grid.add(titleField, 1, 1);

        dialog.getDialogPane().setContent(grid);

        // Request focus on the episode number field by default
        Platform.runLater(episodeNumberField::requestFocus);

        // Convert the result to an Episode object when the add button is clicked
        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == addButtonType) {
                try {
                    //get episode number from episode number field
                    int episodeNumber = Integer.parseInt(episodeNumberField.getText().trim());
                    String title = titleField.getText().trim();

                    // Validate input
                    if (title.isEmpty()) {
                        showError("Error", "Please enter a title for the episode");
                        return null;
                    }

                    // Check if episode already exists
                    if (season.getEpisodes() != null && season.getEpisodes().stream()
                            .anyMatch(e -> e.getEpisodeNumber() == episodeNumber)) {
                        showError("Error", "An episode with this number already exists in this season");
                        return null;
                    }

                    // Create and return the new episode
                    Episode episode = new Episode();
                    episode.setEpisodeNumber(episodeNumber);
                    episode.setTitle(title);
                    episode.setSeason(season);

                    return episode;
                } catch (NumberFormatException e) {
                    showError("Error", "Please enter a valid episode number");
                    return null;
                }
            }
            return null;
        });

        Optional<Episode> result = dialog.showAndWait();
        result.ifPresent(episode -> {
            try {
                // Add the episode to the season
                try {
                    if (episode.getEpisodeNumber() <= 0) {
                        throw new IllegalArgumentException("Episode number must be greater than 0");
                    }

                    // Initialize episodes list if null
                    if (season.getEpisodes() == null) {
                        season.setEpisodes(new ArrayList<>());
                    }

                    // Check for duplicate episode number
                    boolean episodeExists = season.getEpisodes().stream()
                            .anyMatch(e -> e.getEpisodeNumber() == episode.getEpisodeNumber());

                    if (episodeExists) {
                        throw new IllegalArgumentException("An episode with this number already exists in this season");
                    }

                    // Add the episode
                    season.getEpisodes().add(episode);

                    // Sort episodes by number
                    season.getEpisodes().sort(Comparator.comparingInt(Episode::getEpisodeNumber));

                    // Save the series
                    contentService.save(series);
                    refreshSeriesTable();

                    showAlert("Success", String.format("Episode %d added successfully", episode.getEpisodeNumber()));
                } catch (Exception e) {
                    logger.error("Error adding episode to season {} of series {}: {}",
                            season.getSeasonNumber(), series.getTitle(), e.getMessage(), e);
                    throw e; // Re-throw to be caught by the outer try-catch
                }
            } catch (Exception e) {
                logger.error("Error adding episode: {}", e.getMessage(), e);
                showError("Error", "Failed to add episode: " + e.getMessage());
            }
        });
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
                //check if input is empty
                if (!newValue.isEmpty()) {
                    double rating = Double.parseDouble(newValue);
                    okButton.setDisable(rating < 1 || rating > 10);
                } else {
                    okButton.setDisable(true);
                }
                //check if input is a number
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
                    //return rating
                    return Double.parseDouble(ratingInput.getText());
                } catch (NumberFormatException e) {
                    return 0.0;
                }
            }
            //return 0 if cancelled
            return 0.0;
        });

        // Show the dialog and return the result
        dialog.showAndWait();
        //return rating
        return dialog.getResult() != null ? dialog.getResult() : 0.0;
    }

    /**
     * Shows a confirmation dialog with the specified title and message.
     *
     * @param title   the title of the dialog
     * @param message the message to display in the dialog
     * @return true if the user clicked OK, false otherwise
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


    /**
     * Handles the "Rate Series" button click event.
     * Shows a rating dialog for the selected series and updates the rating if confirmed.
     */
    @FXML
    private void handleRateSeries() {
        //get selected series
        Series selectedSeries = seriesTable.getSelectionModel().getSelectedItem();
        if (selectedSeries != null) {
            double rating = showRatingDialog();
            if (rating > 0) {
                try {
                    //rate series
                    contentService.rateContent(selectedSeries, (float) rating);
                    refreshSeriesTable();
                    showSuccess("Success", String.format("You rated '%s' %.1f", selectedSeries.getTitle(), rating));
                } catch (ContentNotFoundException e) {
                    //log error and show error message
                    logger.error("Failed to find series with ID {}: {}", selectedSeries.getId(), e.getMessage());
                    showError("Error", "The selected series could not be found. It may have been deleted.");
                    refreshSeriesTable(); // Refresh to show current data
                } catch (Exception e) {
                    //log error and show error message
                    logger.error("Error rating series: {}", e.getMessage(), e);
                    showError("Error", "Failed to rate series: " + e.getMessage());
                }
                //refresh series table
                refreshSeriesTable();
            }
        } else {
            //show alert
            showAlert("No Series Selected", "Please select a series to rate.");
        }
    }



    /**
     * Initializes the columns for the movie table.
     * Sets up cell value factories and cell factories for each column.
     */
    private void initializeMovieTableColumns() {
        if (movieTable == null) {
            logger.warn("Cannot initialize columns: movieTable is null");
            return;
        }

        try {
            // Title column
            if (movieTitleColumn != null) {
                movieTitleColumn.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getTitle()));
            }

            // Year column
            if (movieYearColumn != null) {
                movieYearColumn.setCellValueFactory(cellData -> {
                    try {
                        Movie movie = cellData.getValue();
                        if (movie != null && movie.getReleaseDate() != null) {
                            Calendar cal = Calendar.getInstance();
                            cal.setTime(movie.getReleaseDate());
                            return new SimpleStringProperty(String.valueOf(cal.get(Calendar.YEAR)));
                        }
                        return new SimpleStringProperty("N/A");
                    } catch (Exception e) {
                        logger.warn("Error getting movie year: {}", e.getMessage());
                        return new SimpleStringProperty("N/A");
                    }
                });
            }

            // Genre column with proper null handling and formatting
            if (movieGenreColumn != null) {
                movieGenreColumn.setCellValueFactory(cellData -> {
                    try {
                        Movie movie = cellData.getValue();
                        if (movie == null) return new SimpleStringProperty("N/A");
                        
                        List<Genre> genres = movie.getGenres();
                        if (genres == null || genres.isEmpty()) {
                            return new SimpleStringProperty("N/A");
                        }
                        
                        String genreText = genres.stream()
                            .filter(Objects::nonNull)
                            .map(genre -> {
                                try {
                                    if (genre.getDisplayName() != null) {
                                        return genre.getDisplayName();
                                    }
                                    String name = genre.name();
                                    return (name != null && !name.isEmpty()) ? 
                                        name.charAt(0) + name.substring(1).toLowerCase() : "";
                                } catch (Exception e) {
                                    return "";
                                }
                            })
                            .filter(Objects::nonNull)
                            .map(String::valueOf)
                            .filter(s -> !s.trim().isEmpty())
                            .map(String::trim)
                            .distinct()
                            .sorted()
                            .reduce((s1, s2) -> s1 + ", " + s2)
                            .orElse("");
                            
                        return new SimpleStringProperty(genreText.isEmpty() ? "N/A" : genreText);
                    } catch (Exception e) {
                        logger.warn("Error getting genres for movie: {}", e.getMessage());
                        return new SimpleStringProperty("N/A");
                    }
                });
            }

            // Director column with null check
            if (movieDirectorColumn != null) {
                movieDirectorColumn.setCellValueFactory(cellData -> {
                    Movie movie = cellData.getValue();
                    return new SimpleStringProperty(movie != null && movie.getDirector() != null ? 
                        movie.getDirector() : "N/A");
                });
            }

            // Rating column
            if (movieRatingColumn != null) {
                movieRatingColumn.setCellValueFactory(cellData -> {
                    Double rating = cellData.getValue().getImdbRating();
                    return new SimpleDoubleProperty(rating != null ? rating : 0.0).asObject();
                });
            }


            logger.debug("Movie table columns initialized successfully");

            //refresh movie table
            refreshMovieTable();


        } catch (Exception e) {
            logger.error("Error initializing movie table columns: {}", e.getMessage(), e);
        }
    }

    /**
     * Loads the initial data for the controller.
     * This includes movies, series, and other content that should be displayed when the view loads.
     */
    private void loadInitialData() {
        if (dataManager == null) {
            logger.error("DataManager is not initialized. Cannot load content.");
            return;
        }

        // Load data in background thread
        Task<Void> task = new Task<>() {
            @Override
            protected Void call() {
                try {

                    // Load movies
                    List<Movie> movies = dataManager.getAllMovies();
                    logger.debug("Loaded {} movies from data manager", movies.size());

                    // Load series
                    List<Series> series = dataManager.getAllSeries();
                    logger.debug("Loaded {} series from data manager", series.size());

                    // Update UI on JavaFX Application Thread
                    Platform.runLater(() -> {
                        try {
                            // Update movies
                            allMovies.clear();
                            allMovies.addAll(movies);

                            filteredMovies.clear();
                            filteredMovies.addAll(movies);

                            // Initialize movie table if not already done
                            if (movieTable != null) {
                                if (movieTable.getColumns() == null || movieTable.getColumns().isEmpty()) {
                                    initializeMovieTableColumns();
                                }
                                // Set movie table items after initializing columns
                                movieTable.setItems(filteredMovies);
                                logger.debug("Set {} movies to movie table", filteredMovies.size());
                            }

                            // Update series
                            allSeries.clear();
                            allSeries.addAll(series);
                            filteredSeries.clear();
                            filteredSeries.addAll(series);

                            // Initialize series table columns if not already done
                            if (seriesTable != null) {
                                if (seriesTable.getColumns() == null || seriesTable.getColumns().isEmpty()) {
                                    initializeSeriesTableColumns();
                                }
                                // Set series table items after initializing columns
                                seriesTable.setItems(filteredSeries);
                                logger.debug("Set {} series to series table", filteredSeries.size());
                            }
                            // Log success
                            logger.info("Successfully loaded {} movies and {} series", movies.size(), series.size());
                        } catch (Exception e) {
                            logger.error("Error updating UI with loaded data: {}", e.getMessage(), e);
                            showAlert("Error", "Failed to load content: " + e.getMessage());
                        }
                    });

                    // Log success
                    logger.info("Successfully loaded initial data");
                } catch (Exception e) {
                    logger.error("Error loading initial data: {}", e.getMessage(), e);
                    Platform.runLater(() -> {
                        //show alert
                        showAlert("Error", "Failed to load content: " + e.getMessage());
                        if (statusLabel != null) {
                            statusLabel.setText("Error loading data");
                        }
                    });
                }
                // returning nothing if task is successful
                return null;
            }
        };

        // Start the task in a background thread
        Thread thread = new Thread(task);
        thread.setDaemon(true);
        thread.start();
    }

    /**
     * Refreshes the movie table with the latest data from the data source.
     * This method should be called after any changes to the movie data.
     */
    private void refreshMovieTable() {
        if (dataManager == null) {
            logger.error("DataManager is not initialized. Cannot refresh movie data.");
            return;
        }


        Task<Void> task = new Task<>() {
            {
                // Set up task completion handlers

                // Set up task success handler
                setOnSucceeded(e -> {
                    if (statusLabel != null) {
                        statusLabel.setText("Successfully loaded movie data");
                        logger.info("Successfully loaded movie data");
                    }
                });

                // Set up task failure handler
                setOnFailed(e -> {
                    if (statusLabel != null) {
                        statusLabel.setText("Error loading movie data");
                    }
                });
            }

            // Set up task execution
            @Override
            protected Void call() {
                try {
                    // Get the latest movies from the service
                    List<Movie> updatedMovies = dataManager.getAllMovies();

                    // Update UI on JavaFX Application Thread
                    Platform.runLater(() -> {
                        try {
                            // Update the observable lists
                            allMovies.setAll(updatedMovies);

                            // Get current search text
                            String searchText = (movieSearchField != null && movieSearchField.getText() != null)
                                    ? movieSearchField.getText().trim()
                                    : "";


                            // Make sure the table has been initialized
                            if (movieTable != null) {
                                if (movieTable.getColumns() == null || movieTable.getColumns().isEmpty()) {
                                    initializeMovieTableColumns();
                                }
                                // Set movie table items after initializing columns
                                movieTable.setItems(filteredMovies);
                                logger.debug("Set {} movies to movie table", filteredMovies.size());
                            }

                            logger.info("Refreshed movie table with {} items", updatedMovies.size());

                            // Update status
                            if (statusLabel != null) {
                                statusLabel.setText("");
                            }
                        } catch (Exception e) {
                            // Show error alert
                            logger.error("Error updating movie table: {}", e.getMessage(), e);
                            showAlert("Error", "Failed to refresh movie data: " + e.getMessage());
                        }
                    });
                    // Log success
                    logger.info("Successfully refreshed movie table");
                } catch (Exception e) {
                    logger.error("Error refreshing movie data: {}", e.getMessage(), e);
                    Platform.runLater(() -> {
                        // Show error alert
                        showAlert("Error", "Failed to refresh movie data: " + e.getMessage());
                        if (statusLabel != null) {
                            statusLabel.setText("Error loading movie data");
                        }
                    });
                }
                return null;
            }

        };

        // Start the task in a background thread
        Thread thread = new Thread(task);
        thread.setDaemon(true);
        thread.start();
    }

    /**
     * Refreshes the series table with the latest data from the data source.
     * This method should be called after any changes to the series data.
     */
    private void refreshSeriesTable() {
        if (dataManager == null) {
            logger.error("DataManager is not initialized. Cannot refresh series data.");
            return;
        }


        // Refresh the series table in a background thread
        Task<Void> task = new Task<>() {
            @Override
            protected Void call() {
                try {
                    // Get the latest series from the service
                    List<Series> updatedSeries = dataManager.getAllSeries();

                    // Update UI on JavaFX Application Thread
                    Platform.runLater(() -> {
                        try {
                            // Update the observable lists
                            allSeries.setAll(updatedSeries);

                            // Get current search text safely
                            String searchText = (seriesSearchField != null && seriesSearchField.getText() != null)
                                    ? seriesSearchField.getText().trim()
                                    : "";

                            // Apply any active filters
                            if (!searchText.isEmpty()) {
                                // If there's a search filter, apply it
                                List<Series> filtered = updatedSeries.stream()
                                        .filter(series -> series.getTitle().toLowerCase().contains(searchText.toLowerCase()))
                                        .collect(Collectors.toList());
                                filteredSeries.setAll(filtered);
                            } else {
                                // Otherwise, show all series
                                filteredSeries.setAll(updatedSeries);
                            }

                            // Make sure the table has been initialized
                            if (seriesTable != null) {
                                if (seriesTable.getColumns() == null || seriesTable.getColumns().isEmpty()) {
                                    initializeSeriesTableColumns();
                                }

                                // Set the items on the table
                                seriesTable.setItems(filteredSeries);

                                // Apply sorting if sort is active
                                if (seriesSortBy != null && seriesSortBy.getValue() != null) {
                                    sortSeriesTable(seriesSortBy.getValue());
                                }

                                // Refresh the table to show the updated data
                                seriesTable.refresh();
                            }

                            logger.info("Refreshed series table with {} items", updatedSeries.size());

                        } catch (Exception e) {
                            logger.error("Error updating series table: {}", e.getMessage(), e);
                            showAlert("Error", "Failed to refresh series data: " + e.getMessage());
                        }
                    });
                } catch (Exception e) {
                    // Log the error
                    logger.error("Error refreshing series data: {}", e.getMessage(), e);
                    Platform.runLater(() -> {
                        showAlert("Error", "Failed to refresh series data: " + e.getMessage());
                    });
                }
                return null;
            }
        };


        // Start the task in a background thread
        Thread thread = new Thread(task);
        thread.setDaemon(true);
        thread.start();
    }


    /**
     * Handles the "Manage Series" button click event.
     * Shows a dialog with options to manage the selected series, including adding seasons and episodes.
     *
     * @param event The action event that triggered this method
     */
    @FXML
    private void handleManageSeries(ActionEvent event) {
        // Get the selected series
        Series selectedSeries = seriesTable.getSelectionModel().getSelectedItem();
        if (selectedSeries == null) {
            showAlert("No Selection", "Please select a series to manage.");
            return;
        }

        // Create a custom dialog
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("Manage Series: " + selectedSeries.getTitle());
        dialog.setHeaderText("Manage " + selectedSeries.getTitle());

        // Set the button types
        ButtonType addSeasonButtonType = new ButtonType("Add Season", ButtonBar.ButtonData.APPLY);
        ButtonType addEpisodeButtonType = new ButtonType("Add Episode", ButtonBar.ButtonData.APPLY);
        dialog.getDialogPane().getButtonTypes().addAll(addSeasonButtonType, addEpisodeButtonType, ButtonType.CLOSE);

        // Create the content
        VBox content = new VBox(10);
        content.setPadding(new Insets(20, 20, 10, 20));

        // Add series info with null checks
        String title = selectedSeries.getTitle() != null ? selectedSeries.getTitle() : "Untitled Series";
        String yearString = "";
        try {
            // Get the start year safely
            int startYear = selectedSeries.getStartYear();
            if (startYear > 0) {
                yearString = " (" + startYear;
                yearString += ")";
            }
        } catch (Exception e) {
            logger.warn("Error formatting series year for {}: {}", title, e.getMessage());
        }

        // Get seasons count safely
        int seasonCount = 0;
        try {
            // Get the seasons count safely
            seasonCount = selectedSeries.getSeasons() != null ? selectedSeries.getSeasons().size() : 0;
        } catch (Exception e) {
            logger.error("Error getting seasons count: {}", e.getMessage());
        }

        // Add series info with null checks
        Label infoLabel = new Label(String.format("Manage %s%s\nSeasons: %d", title, yearString, seasonCount));
        infoLabel.setStyle("-fx-font-weight: bold; -fx-padding: 0 0 10 0;");
        content.getChildren().add(infoLabel);

        // Add season selector for adding episodes
        ComboBox<Season> seasonComboBox = new ComboBox<>();
        try {
            // Add seasons to the combo box
            if (selectedSeries.getSeasons() != null && !selectedSeries.getSeasons().isEmpty()) {
                // Add seasons to the combo box
                seasonComboBox.getItems().addAll(selectedSeries.getSeasons());
                // Set the cell factory for the combo box
                seasonComboBox.setCellFactory(param -> new ListCell<Season>() {
                    @Override
                    protected void updateItem(Season season, boolean empty) {
                        super.updateItem(season, empty);
                        if (empty || season == null) {
                            setText(null);
                        } else {
                            // Set the text for the combo box
                            setText(String.format("Season %d (%d episodes)",
                                    season.getSeasonNumber(),
                                    season.getEpisodes() != null ? season.getEpisodes().size() : 0));
                        }
                    }
                });
                // Set the button cell for the combo box
                seasonComboBox.setButtonCell(new ListCell<Season>() {
                    @Override
                    protected void updateItem(Season season, boolean empty) {
                        super.updateItem(season, empty);
                        if (empty || season == null) {
                            setText("Select a season");
                        } else {
                            setText(String.format("Season %d", season.getSeasonNumber()));
                        }
                    }
                });
                // Select the first season
                seasonComboBox.getSelectionModel().selectFirst();
            } else {
                seasonComboBox.setPromptText("No seasons available");
                seasonComboBox.setDisable(true);
            }
        } catch (Exception e) {
            logger.error("Error populating seasons: {}", e.getMessage(), e);
            seasonComboBox.setPromptText("Error loading seasons");
            seasonComboBox.setDisable(true);
        }
    // Add season selector
        content.getChildren().add(new Label("Select Season for Episode:"));
        content.getChildren().add(seasonComboBox);

        // Add episode count label
        Label episodeCountLabel = new Label("");
        if (seasonComboBox.getSelectionModel().getSelectedItem() != null) {
            Season selectedSeason = seasonComboBox.getSelectionModel().getSelectedItem();
            // Get the episode count
            int episodeCount = selectedSeason.getEpisodes() != null ? selectedSeason.getEpisodes().size() : 0;
            episodeCountLabel.setText(String.format("Episodes in selected season: %d", episodeCount));
        }
        content.getChildren().add(episodeCountLabel);

        // Update episode count when selection changes
        seasonComboBox.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) {
                // Get the episode count
                int count = newVal.getEpisodes() != null ? newVal.getEpisodes().size() : 0;
                episodeCountLabel.setText(String.format("Episodes in selected season: %d", count));
            } else {
                episodeCountLabel.setText("No season selected");
            }
        });

        // Add content to the dialog
        dialog.getDialogPane().setContent(content);

        // Show the dialog and handle the result
        dialog.showAndWait().ifPresent(buttonType -> {
            try {
                if (buttonType == addSeasonButtonType) {

                } else if (buttonType == addEpisodeButtonType) {
                    // Handle Add Episode
                    Season selectedSeason = seasonComboBox.getSelectionModel().getSelectedItem();
                    if (selectedSeason == null) {
                        showAlert("No Season Selected", "Please add a season first before adding episodes.");
                        return;
                    }
                    // Handle Add Episode
                    handleAddEpisode(selectedSeries, selectedSeason);
                }
                // Refresh the series table after any changes
                refreshSeriesTable();
            } catch (Exception e) {
                logger.error("Error managing series: {}", e.getMessage(), e);
                showError("Error", "Failed to perform action: " + e.getMessage());
            }
        });
    }

    // Handle rating a movie
    public void handleRateMovie(ActionEvent actionEvent) {
        Movie selectedMovie = movieTable.getSelectionModel().getSelectedItem();
        if (selectedMovie != null) {
            try {
                // Show rating dialog
                Optional<Number> result = Optional.of(showRatingDialog());
                if (result.isPresent()) {
                    int rating = result.get().intValue();

                    // Get the data manager
                    RefactoredDataManager dataManager = ServiceLocator.getInstance().getDataManager();
                    if (dataManager == null) {
                        throw new IllegalStateException("Unable to access data manager");
                    }

                    // Get the latest version of the movie from the data manager
                    Movie movieToRate = dataManager.getMovieRepository().findById(selectedMovie.getId())
                            .orElseThrow(() -> new ContentNotFoundException("Movie with id " + selectedMovie.getId() + " not found"));

                    // Update the movie's rating
                    movieToRate.setUserRating(rating);

                    // Save the updated movie using the data manager
                    dataManager.updateMovie(movieToRate);

                    // Refresh the table to show the updated rating
                    refreshMovieTable();

                    // Show success message
                    showSuccess("Success", String.format("Rated '%s' with %d stars",
                        movieToRate.getTitle(), rating));
                }
            } catch (ContentNotFoundException e) {
                logger.error("Content not found while rating movie: {}", e.getMessage());
                showError("Error", "Failed to rate movie: " + e.getMessage());
                // Refresh the table to show current state
                refreshMovieTable();
            } catch (Exception e) {
                logger.error("Error rating movie: {}", e.getMessage(), e);
                showError("Error", "Failed to rate movie: " + e.getMessage());
            }
        } else {
            showError("Error", "No movie selected. Please select a movie to rate.");
        }
    }

    public void handleAddSeries(ActionEvent actionEvent) {
        try {
            // Get the data manager
            RefactoredDataManager dataManager = ServiceLocator.getInstance().getDataManager();
            if (dataManager == null) {
                throw new IllegalStateException("Unable to access data manager");
            }

            // Create a new series with default values
            Series newSeries = new Series("New Series");
            newSeries.setStartYear(Calendar.getInstance().get(Calendar.YEAR));
            newSeries.setSeasons(new ArrayList<>());

            // Set default values for required fields
            newSeries.setGenre(Genre.DRAMA);
            newSeries.setActors(new ArrayList<>());
            newSeries.setGenres(List.of(Genre.DRAMA));

            // Show a dialog to edit the new series details
            boolean confirmed = showSeriesEditDialog(newSeries, "Add New Series");

            if (confirmed) {
                try {
                    // Save the new series using the data manager
                    dataManager.saveSeries(newSeries);
                    refreshSeriesTable();
                    showSuccess("Success", String.format("Added new series: '%s'", newSeries.getTitle()));
                } catch (Exception e) {
                    // Log the error
                    logger.error("Error saving series: {}", e.getMessage(), e);
                    showError("Error", "Failed to save series: " + e.getMessage());
                }
            }
        } catch (Exception e) {
            logger.error("Error creating series: {}", e.getMessage(), e);
            showError("Error", "Failed to create series: " + e.getMessage());
        }
    }

    /**
     * Shows a dialog for editing series details.
     *
     * @param series The series to edit
     * @param title The dialog title
     * @return true if the user clicked OK, false otherwise
     */
    private boolean showSeriesEditDialog(Series series, String title) {
        try {
            // Create a dialog
            Dialog<ButtonType> dialog = new Dialog<>();
            dialog.setTitle(title);
            dialog.setHeaderText("Edit Series Details");

            // Set the button types
            ButtonType saveButtonType = new ButtonType("Save", ButtonBar.ButtonData.OK_DONE);
            dialog.getDialogPane().getButtonTypes().addAll(saveButtonType, ButtonType.CANCEL);

            // Create the form
            GridPane grid = new GridPane();
            grid.setHgap(10);
            grid.setVgap(10);
            grid.setPadding(new Insets(20, 20, 10, 10));

            // Add form fields
            TextField titleField = new TextField(series.getTitle());
            titleField.setPromptText("Title");
            
            Spinner<Integer> yearSpinner = new Spinner<>(1900, 2100, 
                series.getStartYear() > 0 ? series.getStartYear() : Calendar.getInstance().get(Calendar.YEAR));
            yearSpinner.setEditable(true);


            // Add fields to grid
            grid.add(new Label("Title:"), 0, 0);
            grid.add(titleField, 1, 0);
            grid.add(new Label("Year:"), 0, 1);
            grid.add(yearSpinner, 1, 1);
            grid.add(new Label("Description:"), 0, 2);

            dialog.getDialogPane().setContent(grid);
            
            // Request focus on the title field by default
            Platform.runLater(titleField::requestFocus);

            // Convert the result to a series object when the save button is clicked
            dialog.setResultConverter(dialogButton -> {
                if (dialogButton == saveButtonType) {
                    // Update the series with the new values
                    series.setTitle(titleField.getText().trim());
                    series.setStartYear(yearSpinner.getValue());
                    return ButtonType.OK;
                }
                return null;
            });

            // Show the dialog and wait for user input
            Optional<ButtonType> result = dialog.showAndWait();
            return result.isPresent() && result.get() == ButtonType.OK;
            
        } catch (Exception e) {
            logger.error("Error showing series edit dialog: {}", e.getMessage(), e);
            showError("Error", "Failed to show series editor: " + e.getMessage());
            return false;
        }
    }

    // Handle delete series
    public void handleDeleteSeries(ActionEvent actionEvent) {
        Series selectedSeries = seriesTable.getSelectionModel().getSelectedItem();
        if (selectedSeries != null) {
            // Show confirmation dialog
            boolean confirmed = showConfirmationDialog("Confirm Delete", 
                String.format("Are you sure you want to delete '%s'?", selectedSeries.getTitle()));
            
            if (confirmed) {
                try {
                    // Use dataManager to delete the series directly
                    RefactoredDataManager dataManager = ServiceLocator.getInstance().getDataManager();
                    if (dataManager != null) {
                        dataManager.deleteSeries(selectedSeries);
                        refreshSeriesTable();
                        showSuccess("Success", String.format("Successfully deleted '%s'", selectedSeries.getTitle()));
                    } else {
                        throw new IllegalStateException("Unable to access data manager");
                    }
                } catch (Exception e) {
                    logger.error("Error deleting series: {}", e.getMessage(), e);
                    showError("Error", "Failed to delete series: " + e.getMessage());
                }
            }
        } else {
            showError("Error", "No series selected. Please select a series to delete.");
        }
    }

    public void handleDeleteMovie(ActionEvent actionEvent) {
        Movie selectedMovie = movieTable.getSelectionModel().getSelectedItem();
        if (selectedMovie != null) {
            boolean confirmed = showConfirmationDialog("Confirm Delete", 
                String.format("Are you sure you want to delete '%s'?", selectedMovie.getTitle()));
            
            if (confirmed) {
                try {
                    // Get the data manager
                    RefactoredDataManager dataManager = ServiceLocator.getInstance().getDataManager();
                    if (dataManager != null) {
                        // Delete the movie using the data manager
                        dataManager.deleteMovie(selectedMovie);
                        refreshMovieTable();
                        showSuccess("Success", String.format("Successfully deleted '%s'", selectedMovie.getTitle()));
                    } else {
                        throw new IllegalStateException("Unable to access data manager");
                    }
                } catch (Exception e) {
                    logger.error("Error deleting movie: {}", e.getMessage(), e);
                    showError("Error", "Failed to delete movie: " + e.getMessage());
                }
            }
        } else {
            showError("Error", "No movie selected. Please select a movie to delete.");
        }
    }
}
