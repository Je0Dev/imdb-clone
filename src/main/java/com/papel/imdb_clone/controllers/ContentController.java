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
            TextArea descriptionArea = new TextArea();
            descriptionArea.setPrefRowCount(3);
            TextField yearField = new TextField();
            TextField durationField = new TextField();
            TextField imageUrlField = new TextField();
            
            // Set up the grid
            GridPane grid = new GridPane();
            grid.setHgap(10);
            grid.setVgap(10);
            grid.setPadding(new Insets(20, 150, 10, 10));
            
            // Add fields to grid
            grid.add(new Label("Title:"), 0, 0);
            grid.add(titleField, 1, 0);
            grid.add(new Label("Description:"), 0, 1);
            grid.add(descriptionArea, 1, 1);
            grid.add(new Label("Year:"), 0, 2);
            grid.add(yearField, 1, 2);
            grid.add(new Label("Duration (minutes):"), 0, 3);
            grid.add(durationField, 1, 3);
            grid.add(new Label("Image URL:"), 0, 4);
            grid.add(imageUrlField, 1, 4);
            
            dialog.getDialogPane().setContent(grid);
            
            // Request focus on the title field by default
            Platform.runLater(titleField::requestFocus);
            
            // Convert the result to a Movie object when the Add button is clicked
            dialog.setResultConverter(dialogButton -> {
                if (dialogButton == addButton) {
                    try {
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
            movieLoadingIndicator.setVisible(true);
            statusLabel.setText("Loading movies...");
            
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
                    logger.error("Error updating movie table", e);
                    statusLabel.setText("Error loading movies");
                    showError("Error", "Failed to load movies: " + e.getMessage());
                } finally {
                    movieLoadingIndicator.setVisible(false);
                }
            });
            
        } catch (Exception e) {
            logger.error("Error in loadMovies", e);
            Platform.runLater(() -> {
                statusLabel.setText("Error loading movies");
                showError("Error", "Failed to load movies: " + e.getMessage());
                movieLoadingIndicator.setVisible(false);
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
            cellData.getValue().getGenres().stream()
                .map(Genre::name)
                .collect(Collectors.joining(", "))
        ));
        
        movieRatingColumn.setCellValueFactory(cellData -> {
            Double rating = cellData.getValue().getImdbRating();
            return new SimpleDoubleProperty(rating != null ? rating : 0.0).asObject();
        });

        
        // Set up action buttons for each row
        setupActionButtons();
        
        // Load initial data
        loadMovies();
    }
    
    private void setupActionButtons() {
        // Add action buttons to the actions column
        movieActionsColumn.setCellFactory(param -> new TableCell<>() {
            private final Button editButton = new Button("Edit");
            private final Button deleteButton = new Button("Delete");
            
            {
                editButton.getStyleClass().add("edit-button");
                deleteButton.getStyleClass().add("delete-button");
                
                editButton.setOnAction(event -> {
                    Movie movie = getTableView().getItems().get(getIndex());
                    // Handle edit action
                    logger.info("Edit movie: {}", movie.getTitle());
                });
                
                deleteButton.setOnAction(event -> {
                    Movie movie = getTableView().getItems().get(getIndex());
                    // Handle delete action
                    logger.info("Delete movie: {}", movie.getTitle());
                });
            }
            
            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    HBox buttons = new HBox(5, editButton, deleteButton);
                    setGraphic(buttons);
                }
            }
        });
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
    private void initializeServices() {
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

            // 5. Initialize EncryptionService if needed
            this.encryptionService = EncryptionService.getInstance();

            logger.info("All services initialized successfully");

        } catch (Exception e) {
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
    private EncryptionService encryptionService;

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
            } catch (Exception e) {
                logger.error("Failed to initialize services: {}", e.getMessage(), e);
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
                    TableRow<Series> row = new TableRow<>();
                    row.setOnMouseClicked(event -> {
                        if (event.getClickCount() == 2 && !row.isEmpty()) {
                            Series series = row.getItem();
                            showContentDetails(series);
                        }
                    });
                    return row;
                });

                // Set up sorting
                if (seriesSortBy != null) {
                    seriesSortBy.getItems().addAll("Title (A-Z)", "Title (Z-A)", "Year (Newest)", "Year (Oldest)", "Rating (High to Low)");
                    seriesSortBy.setValue("Title (A-Z)");
                    seriesSortBy.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
                        if (newVal != null) {
                            sortSeriesTable(newVal);
                        }
                    });
                }

            }

            // Load initial data
            loadInitialData();

        } catch (Exception e) {
            logger.error("Error initializing controller: {}", e.getMessage(), e);
            showAlert("Initialization Error", "Failed to initialize the application: " + e.getMessage());
        }
    }

    private void showAlert(String title, String message) {
        UIUtils.showAlert(Alert.AlertType.ERROR, title, message);
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
            case "Title (A-Z)":
                seriesTable.getSortOrder().clear();
                seriesTable.getSortOrder().add(seriesTitleColumn);
                break;
            case "Title (Z-A)":
                seriesTable.getSortOrder().clear();
                seriesTable.getSortOrder().add(seriesTitleColumn);
                seriesTable.getSortOrder().add(seriesTitleColumn);
                break;
            case "Year (Newest)":
                seriesTable.getSortOrder().clear();
                seriesTable.getSortOrder().add(seriesYearColumn);
                break;
            case "Year (Oldest)":
                seriesTable.getSortOrder().clear();
                seriesTable.getSortOrder().add(seriesYearColumn);
                seriesTable.getSortOrder().add(seriesYearColumn);
                break;
            case "Rating (High to Low)":
                seriesTable.getSortOrder().clear();
                seriesTable.getSortOrder().add(seriesRatingColumn);
                break;
            default:
                seriesTable.getSortOrder().clear();
                break;
        }
    }

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
            case "Title (A-Z)":
                movieTable.getSortOrder().clear();
                movieTable.getSortOrder().add(movieTitleColumn);
                break;
            case "Title (Z-A)":
                movieTable.getSortOrder().clear();
                movieTable.getSortOrder().add(movieTitleColumn);
                movieTable.getSortOrder().add(movieTitleColumn);
                break;
            case "Year (Newest)":
                movieTable.getSortOrder().clear();
                movieTable.getSortOrder().add(movieYearColumn);
                break;
            case "Year (Oldest)":
                movieTable.getSortOrder().clear();
                movieTable.getSortOrder().add(movieYearColumn);
                movieTable.getSortOrder().add(movieYearColumn);
                break;
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
                            return new SimpleStringProperty(cellData.getValue().getTitle());
                        } catch (Exception e) {
                            logger.warn("Error getting movie title: {}", e.getMessage());
                            return new SimpleStringProperty("");
                        }
                    });
                    movieTable.getColumns().add(movieTitleColumn);
                }

                if (movieYearColumn != null) {
                    movieYearColumn.setCellValueFactory(cellData -> {
                        try {
                            Date releaseDate = cellData.getValue().getReleaseDate();
                            if (releaseDate != null) {
                                Calendar cal = Calendar.getInstance();
                                cal.setTime(releaseDate);
                                return new SimpleStringProperty(String.valueOf(cal.get(Calendar.YEAR)));
                            }
                        } catch (Exception e) {
                            logger.warn("Error getting movie year: {}", e.getMessage());
                        }
                        return new SimpleStringProperty("");
                    });
                    movieTable.getColumns().add(movieYearColumn);
                }

                if (movieRatingColumn != null) {
                    movieRatingColumn.setCellValueFactory(cellData -> {
                        try {
                            Movie movie = cellData.getValue();
                            Double rating = movie != null ? Movie.getRating(movie) : 0.0;
                            return new SimpleDoubleProperty(rating).asObject();
                        } catch (Exception e) {
                            logger.warn("Error getting movie rating: {}", e.getMessage());
                            return new SimpleDoubleProperty(0.0).asObject();
                        }
                    });
                    movieTable.getColumns().add(movieRatingColumn);
                }

                // Set the items after columns are configured
                movieTable.setItems(filteredMovies);

                // Add row selection listener
                movieTable.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
                    selectedMovie.set(newSelection);
                });
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
                    seriesTitleColumn.setCellValueFactory(cellData -> {
                        try {
                            return new SimpleStringProperty(cellData.getValue().getTitle());
                        } catch (Exception e) {
                            logger.warn("Error getting series title: {}", e.getMessage());
                            return new SimpleStringProperty("");
                        }
                    });
                    seriesTable.getColumns().add(seriesTitleColumn);
                }

                if (seriesYearColumn != null) {
                    seriesYearColumn.setCellValueFactory(cellData -> {
                        try {
                            return new SimpleIntegerProperty().asObject();
                        } catch (Exception e) {
                            logger.warn("Error getting series year: {}", e.getMessage());
                            return new SimpleIntegerProperty(0).asObject();
                        }
                    });
                    seriesTable.getColumns().add(seriesYearColumn);
                }

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
                    if (series == null) {
                        return new SimpleIntegerProperty(0).asObject();
                    }

                    // Simply get the year using the getter which has all the fallback logic
                    int year = series.getStartYear();
                    return new SimpleIntegerProperty(Math.max(year, 0)).asObject();
                });

                // Set cell factory to display "N/A" for invalid years
                seriesYearColumn.setCellFactory(column -> new TableCell<Series, Integer>() {
                    @Override
                    protected void updateItem(Integer year, boolean empty) {
                        super.updateItem(year, empty);
                        if (empty || year == null || year <= 0) {
                            setText("N/A");
                        } else {
                            setText(String.valueOf(year));
                        }
                    }
                });
            }

            // Genre column
            if (seriesGenreColumn != null) {
                seriesGenreColumn.setCellValueFactory(cellData -> {
                    Series series = cellData.getValue();
                    List<Genre> genres = series.getGenres();
                    String genreText = genres != null ?
                            genres.stream()
                                    .map(Genre::name)
                                    .collect(Collectors.joining(", ")) : "";
                    return new SimpleStringProperty(genreText);
                });
            }

            // Seasons column
            if (seriesSeasonsColumn != null) {
                seriesSeasonsColumn.setCellValueFactory(cellData ->
                        new SimpleIntegerProperty(cellData.getValue().getSeasons() != null ?
                                cellData.getValue().getSeasons().size() : 0).asObject());
            }

            // Episodes column
            if (seriesEpisodesColumn != null) {
                seriesEpisodesColumn.setCellValueFactory(cellData ->
                        new SimpleIntegerProperty(cellData.getValue().getTotalEpisodes()).asObject());
            }

            // Rating column
            if (seriesRatingColumn != null) {
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

        TextField episodeNumberField = new TextField();
        episodeNumberField.setPromptText("1");
        TextField titleField = new TextField();
        titleField.setPromptText("Episode Title");
        TextArea descriptionArea = new TextArea();
        descriptionArea.setPromptText("Episode description");
        descriptionArea.setWrapText(true);
        descriptionArea.setPrefRowCount(3);

        grid.add(new Label("Episode Number:"), 0, 0);
        grid.add(episodeNumberField, 1, 0);
        grid.add(new Label("Title:"), 0, 1);
        grid.add(titleField, 1, 1);
        grid.add(new Label("Description:"), 0, 2);
        grid.add(descriptionArea, 1, 2);

        dialog.getDialogPane().setContent(grid);

        // Request focus on the episode number field by default
        Platform.runLater(episodeNumberField::requestFocus);

        // Convert the result to an Episode object when the add button is clicked
        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == addButtonType) {
                try {
                    int episodeNumber = Integer.parseInt(episodeNumberField.getText().trim());
                    String title = titleField.getText().trim();
                    String description = descriptionArea.getText().trim();

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
        Series selectedSeries = seriesTable.getSelectionModel().getSelectedItem();
        if (selectedSeries != null) {
            double rating = showRatingDialog();
            if (rating > 0) {
                try {
                    contentService.rateContent(selectedSeries, (float) rating);
                    refreshSeriesTable();
                    showSuccess("Success", String.format("You rated '%s' %.1f", selectedSeries.getTitle(), rating));
                } catch (ContentNotFoundException e) {
                    logger.error("Failed to find series with ID {}: {}", selectedSeries.getId(), e.getMessage());
                    showError("Error", "The selected series could not be found. It may have been deleted.");
                    refreshSeriesTable(); // Refresh to show current data
                } catch (Exception e) {
                    logger.error("Error rating series: {}", e.getMessage(), e);
                    showError("Error", "Failed to rate series: " + e.getMessage());
                }
            }
        } else {
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
                movieYearColumn.setCellValueFactory(cellData ->
                        new SimpleStringProperty(String.valueOf(cellData.getValue().getYear()))
                );
            }

            // Genre column
            if (movieGenreColumn != null) {
                movieGenreColumn.setCellValueFactory(cellData -> {
                    try {
                        List<Genre> genres = cellData.getValue().getGenres();
                        if (genres == null || genres.isEmpty()) {
                            return new SimpleStringProperty("N/A");
                        }
                        String genreText = genres.stream()
                                .filter(Objects::nonNull)
                                .map(genre -> genre.name().charAt(0) + genre.name().substring(1).toLowerCase())
                                .collect(Collectors.joining(", "));
                        return new SimpleStringProperty(genreText.isEmpty() ? "N/A" : genreText);
                    } catch (Exception e) {
                        logger.debug("Error getting genres for movie: {}", e.getMessage());
                        return new SimpleStringProperty("N/A");
                    }
                });
            }

            // Director column
            if (movieDirectorColumn != null) {
                movieDirectorColumn.setCellValueFactory(cellData -> {
                    Movie movie = cellData.getValue();
                    if (movie.getDirector() != null) {
                        // Access director name directly from the director string
                        return new SimpleStringProperty(movie.getDirector());
                    }
                    return new SimpleStringProperty("");
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
                            logger.info("Successfully loaded {} movies and {} series", movies.size(), series.size());
                        } catch (Exception e) {
                            logger.error("Error updating UI with loaded data: {}", e.getMessage(), e);
                            showAlert("Error", "Failed to load content: " + e.getMessage());
                        }
                    });

                } catch (Exception e) {
                    logger.error("Error loading initial data: {}", e.getMessage(), e);
                    Platform.runLater(() -> {
                        showAlert("Error", "Failed to load content: " + e.getMessage());
                        if (statusLabel != null) {
                            statusLabel.setText("Error loading data");
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
     * Refreshes the movie table with the latest data from the data source.
     * This method should be called after any changes to the movie data.
     */
    private void refreshMovieTable() {
        if (dataManager == null) {
            logger.error("DataManager is not initialized. Cannot refresh movie data.");
            return;
        }

        // Show loading indicator
        if (loadingIndicatorContainer != null && movieLoadingIndicator != null) {
            loadingIndicatorContainer.setVisible(true);
            movieLoadingIndicator.setProgress(ProgressIndicator.INDETERMINATE_PROGRESS);
        }

        Task<Void> task = new Task<>() {
            {
                // Set up task completion handlers
                setOnSucceeded(e -> {
                    if (loadingIndicatorContainer != null) {
                        loadingIndicatorContainer.setVisible(false);
                    }
                    if (statusLabel != null) {
                        statusLabel.setText("");
                    }
                });

                setOnFailed(e -> {
                    if (loadingIndicatorContainer != null) {
                        loadingIndicatorContainer.setVisible(false);
                    }
                    if (statusLabel != null) {
                        statusLabel.setText("Error loading movie data");
                    }
                });
            }

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

                                // Apply sorting if sort is active
                                if (movieSortBy != null && movieSortBy.getValue() != null) {
                                    sortMovieTable(movieSortBy.getValue());
                                }

                                // Refresh the table to show the updated data
                                movieTable.refresh();
                            }

                            logger.info("Refreshed movie table with {} items", updatedMovies.size());

                            // Update status
                            if (statusLabel != null) {
                                statusLabel.setText("");
                            }
                        } catch (Exception e) {
                            logger.error("Error updating movie table: {}", e.getMessage(), e);
                            showAlert("Error", "Failed to refresh movie data: " + e.getMessage());
                        }
                    });
                } catch (Exception e) {
                    logger.error("Error refreshing movie data: {}", e.getMessage(), e);
                    Platform.runLater(() -> {
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
            seasonCount = selectedSeries.getSeasons() != null ? selectedSeries.getSeasons().size() : 0;
        } catch (Exception e) {
            logger.error("Error getting seasons count: {}", e.getMessage());
        }

        Label infoLabel = new Label(String.format("Manage %s%s\nSeasons: %d", title, yearString, seasonCount));
        infoLabel.setStyle("-fx-font-weight: bold; -fx-padding: 0 0 10 0;");
        content.getChildren().add(infoLabel);

        // Add season selector for adding episodes
        ComboBox<Season> seasonComboBox = new ComboBox<>();
        try {
            if (selectedSeries.getSeasons() != null && !selectedSeries.getSeasons().isEmpty()) {
                seasonComboBox.getItems().addAll(selectedSeries.getSeasons());
                seasonComboBox.setCellFactory(param -> new ListCell<Season>() {
                    @Override
                    protected void updateItem(Season season, boolean empty) {
                        super.updateItem(season, empty);
                        if (empty || season == null) {
                            setText(null);
                        } else {
                            setText(String.format("Season %d (%d episodes)",
                                    season.getSeasonNumber(),
                                    season.getEpisodes() != null ? season.getEpisodes().size() : 0));
                        }
                    }
                });
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

        content.getChildren().add(new Label("Select Season for Episode:"));
        content.getChildren().add(seasonComboBox);

        // Add episode count label
        Label episodeCountLabel = new Label("");
        if (seasonComboBox.getSelectionModel().getSelectedItem() != null) {
            Season selectedSeason = seasonComboBox.getSelectionModel().getSelectedItem();
            int episodeCount = selectedSeason.getEpisodes() != null ? selectedSeason.getEpisodes().size() : 0;
            episodeCountLabel.setText(String.format("Episodes in selected season: %d", episodeCount));
        }
        content.getChildren().add(episodeCountLabel);

        // Update episode count when selection changes
        seasonComboBox.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) {
                int count = newVal.getEpisodes() != null ? newVal.getEpisodes().size() : 0;
                episodeCountLabel.setText(String.format("Episodes in selected season: %d", count));
            } else {
                episodeCountLabel.setText("No season selected");
            }
        });

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
}
