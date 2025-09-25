package com.papel.imdb_clone.controllers.content;

import com.papel.imdb_clone.controllers.BaseController;
import com.papel.imdb_clone.enums.Genre;
import com.papel.imdb_clone.model.people.Actor;
import com.papel.imdb_clone.service.content.MoviesService;
import com.papel.imdb_clone.service.navigation.NavigationService;
import com.papel.imdb_clone.util.UIUtils;
import com.papel.imdb_clone.model.content.Movie;
import javafx.application.Platform;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import java.net.URL;
import java.util.*;
import java.util.stream.Collectors;

import javafx.geometry.Insets;

/**
 * Controller for managing movies in the application.
 * Handles all movie-related operations including listing, adding, editing, and deleting movies.
 */
public class MoviesController extends BaseController {

    @FXML
    public Label resultsCountLabel;
    public Label itemCountLabel;
    
    private MoviesService moviesService;

    public MoviesController() {
        super();
        NavigationService navigationService = NavigationService.getInstance();
    }

    /**
     * Handles the delete movie button click event.
     * Shows a confirmation dialog and deletes the selected movie if confirmed.
     *
     * @param event The action event that triggered this method
     */
    @FXML
    private void handleDeleteMovie(ActionEvent event) {
        Movie selectedMovie = movieTable.getSelectionModel().getSelectedItem();
        logger.info("Selected movie: {}", selectedMovie);
        if (selectedMovie == null) {
            showAlert("No Selection", "Please select a movie to delete.");
            return;
        }

        // Show confirmation dialog
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        
        alert.setTitle("Confirm Deletion");
        alert.setHeaderText("Delete Movie");
        alert.setContentText(String.format("Are you sure you want to delete '%s'?\nThis action cannot be undone.",
            selectedMovie.getTitle()));

        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            try {
                // Call the service to delete the movie
                // For example: moviesService.deleteMovie(selectedMovie.getId());

                // Remove from the table
                movieTable.getItems().remove(selectedMovie);

                showAlert("Success", String.format("Movie '%s' was deleted successfully.",
                    selectedMovie.getTitle()));

                // Update the item count
                updateItemCount();

            } catch (Exception e) {
                logger.error("Error deleting movie: " + e.getMessage(), e);
                showAlert("Error", "Failed to delete movie: " + e.getMessage());
            }
        }
    }

    //updates the item count label in movies v
    private void updateItemCount() {
        itemCountLabel.setText(String.format("Total Movies: %d", movieTable.getItems().size()));
        itemCountLabel.setVisible(true);
        itemCountLabel.setStyle("-fx-text-fill: #2e7d32;");
    }

    /**
     * Handles the rate movie button click event.
     * Opens a dialog to allow the user to rate the selected movie.
     *
     * @param event The action event that triggered this method
     */
    @FXML
    private void handleRateMovie(ActionEvent event) {
        Movie selectedMovie = movieTable.getSelectionModel().getSelectedItem();
        if (selectedMovie == null) {
            showAlert("No Selection", "Please select a movie to rate.");
            return;
        }

        // Create a simple dialog to get the rating
        TextInputDialog dialog = new TextInputDialog("5.0");
        dialog.setTitle("Rate Movie");
        dialog.setHeaderText(String.format("Rate %s (%d) on a scale of 0-10", 
            selectedMovie.getTitle(), selectedMovie.getReleaseYear()));
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
                selectedMovie.setUserRating(currentUserId, (int) (rating * 2)); // Convert 0-10 to 0-20 for storage
                
                // Update the movie in the service
                moviesService.update(selectedMovie);
                
                // Update the UI to reflect the new rating
                showAlert("Success", String.format("You rated %s: %.1f/10\nNew average rating: %.1f/10",
                    selectedMovie.getTitle(),
                    rating,
                    selectedMovie.getImdbRating()));
                
                // Refresh the movie list to show updated ratings
                loadMovies();
                
            } catch (NumberFormatException e) {
                showAlert("Invalid Input", "Please enter a valid number between 0 and 10");
            } catch (Exception e) {
                logger.error("Error rating movie: {}", e.getMessage(), e);
                showAlert("Error", "Failed to save rating: " + e.getMessage());
            }
        });
    }

    /**
     * Gets the ID of the currently authenticated user.
     * 
     * @return The ID of the current user
     * @throws SecurityException if no user is currently authenticated
     */
    private int getCurrentUserId() {
        // This is a placeholder implementation that returns a default user ID
        // return SecurityContext.getCurrentUser().getId();
        
        // For now, return a default user ID
        return 1;
    }

    // UI Components
    @FXML private Label statusLabel;
    @FXML private TableView<Movie> movieTable;
    @FXML private TableColumn<Movie, String> movieTitleColumn;
    @FXML private TableColumn<Movie, String> movieYearColumn;
    @FXML private TableColumn<Movie, Integer> movieDurationColumn;
    @FXML private TableColumn<Movie, String> movieGenreColumn;
    @FXML private TableColumn<Movie, String> movieDirectorColumn;
    @FXML private TableColumn<Movie, String> movieCastColumn;
    @FXML private TableColumn<Movie, Double> movieRatingColumn;
    @FXML private TextField movieSearchField;
    @FXML private ComboBox<String> movieSortBy;
    private Map<String, Object> data;

    /**
     * Navigates back to the home view.
     */
    @FXML
    public void goToHome() {
        try {
            NavigationService navigationService = NavigationService.getInstance();
            navigationService.navigateTo("/fxml/base/home-view.fxml",
                    data, (Stage) movieTable.getScene().getWindow(),
                "IMDb Clone - Home");
        } catch (Exception e) {
            logger.error("Error navigating to home view", e);
            UIUtils.showError("Navigation Error", "Failed to navigate to home view: " + e.getMessage());
        }
    }

    // Data
    private final ObservableList<Movie> allMovies = FXCollections.observableArrayList();
    private final ObservableList<Movie> filteredMovies = FXCollections.observableArrayList();
    private final ObjectProperty<Movie> selectedMovie = new SimpleObjectProperty<>();

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // Required by Initializable
    }

    @Override
    public void initializeController(int currentUserId) throws Exception {
        try {
            // Initialize movies service
            this.moviesService = MoviesService.getInstance();

            // Set up the table columns first
            setupTableColumns();

            // Bind the table to the filtered movies list
            movieTable.setItems(filteredMovies);

            // Set up selection listener before loading data
            movieTable.getSelectionModel().selectedItemProperty().addListener(
                (obs, oldSelection, newSelection) -> selectedMovie.set(newSelection)
            );

            // Set up search and sort handlers
            setupSearchHandlers();
            setupSortHandlers();

            // Load data last, after UI is fully set up
            loadMovies();

        } catch (Exception e) {
            logger.error("Error initializing MoviesController", e);
            showError("Initialization Error", "Failed to initialize movie controller: " + e.getMessage());
            throw e;
        }
    }



// Setter for content service (for dependency injection)
public void setContentService(MoviesService moviesService) {
    this.moviesService = moviesService;
}

private void setupTableColumns() {
    // Set up the movie title column
    movieTitleColumn.setCellValueFactory(cellData -> {
        Movie movie = cellData.getValue();
        return new SimpleStringProperty(movie != null ? movie.getTitle() : "");
    });

    // Set up the year column
    movieYearColumn.setCellValueFactory(cellData -> {
        Movie movie = cellData.getValue();
        if (movie != null && movie.getReleaseDate() != null) {
            Calendar cal = Calendar.getInstance();
            cal.setTime(movie.getReleaseDate());
            return new SimpleStringProperty(String.valueOf(cal.get(Calendar.YEAR)));
        }
        return new SimpleStringProperty("");
    });
    
    // Set up the duration column (in minutes)
    movieDurationColumn.setCellValueFactory(cellData -> {
        Movie movie = cellData.getValue();
        return new SimpleObjectProperty<Integer>(movie != null ? (Integer) movie.getDuration() : 0);
    });
    
    // Set up the genre column
    movieGenreColumn.setCellValueFactory(cellData -> {
        Movie movie = cellData.getValue();
        if (movie != null && movie.getGenre() != null) {
            return new SimpleStringProperty(movie.getGenre().toString());
        }
        return new SimpleStringProperty("");
    });
    
    // Set up the director column
    movieDirectorColumn.setCellValueFactory(cellData -> {
        Movie movie = cellData.getValue();
        return new SimpleStringProperty(movie != null && movie.getDirector() != null ? movie.getDirector() : "");
    });
    
    // Set up the cast column
    movieCastColumn.setCellValueFactory(cellData -> {
        Movie movie = cellData.getValue();
        if (movie != null && movie.getActors() != null && !movie.getActors().isEmpty()) {
            return new SimpleStringProperty(
                movie.getActors().stream()
                    .filter(Objects::nonNull)
                    .map(actor -> {
                        String firstName = actor.getFirstName() != null ? actor.getFirstName() : "";
                        String lastName = actor.getLastName() != null ? actor.getLastName() : "";
                        return (firstName + " " + lastName).trim();
                    })
                    .filter(name -> !name.isEmpty())
                    .collect(Collectors.joining(", "))
            );
        }
        return new SimpleStringProperty("");
    });
    
    // Set up the rating column
    movieRatingColumn.setCellValueFactory(cellData -> {
        Movie movie = cellData.getValue();
        return new SimpleObjectProperty<>(movie != null ? movie.getImdbRating() : 0.0);
        });
        
        // Set up year column
        movieYearColumn.setCellFactory(col -> new TableCell<Movie, String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? "" : item);
                setStyle("-fx-alignment: CENTER; -fx-padding: 5;");
            }
        });

        // Set up genre column
        movieGenreColumn.setCellValueFactory(cellData -> {
            Movie movie = cellData.getValue();
            if (movie == null) {
                return new SimpleStringProperty("");
            }
            String genres = movie.getGenres().stream()
                    .map(Enum::name)
                    .collect(Collectors.joining(", "));
            return new SimpleStringProperty(genres);
        });
        
        movieGenreColumn.setCellFactory(col -> new TableCell<Movie, String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? "" : item);
                setStyle("-fx-alignment: CENTER_LEFT; -fx-padding: 5 10;");
            }
        });

        // Set up director column
        movieDirectorColumn.setCellValueFactory(cellData -> {
            Movie movie = cellData.getValue();
            String directorName = "";
            if (movie != null) {
                directorName = movie.getDirector();
                if (directorName == null || directorName.trim().isEmpty()) {
                    directorName = "Unknown Director";
                }
            }
            return new SimpleStringProperty(directorName);
        });
        movieDirectorColumn.setCellFactory(col -> new TableCell<Movie, String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? "" : item);
                setStyle("-fx-alignment: CENTER_LEFT; -fx-padding: 5 10;");
            }
        });

        // Set up cast column with enhanced display
        movieCastColumn.setCellValueFactory(cellData -> {
            Movie movie = cellData.getValue();
            if (movie == null || movie.getActors() == null || movie.getActors().isEmpty()) {
                return new SimpleStringProperty("No cast information available");
            }
            
            // Get all actors
            List<Actor> allActors = new ArrayList<>(movie.getActors());

            
            // Create a string with up to 5 actors for the main display
            int maxActorsToShow = Math.min(allActors.size(), 5);
            String actorsDisplay = allActors.stream()
                    .limit(maxActorsToShow)
                    .map(actor -> {
                        String role = (actor.getRole() != null && !actor.getRole().trim().isEmpty())
                                ? " (" + actor.getRole().trim() + ")"
                                : "";
                        return actor.getFullName() + role;
                    })
                    .collect(Collectors.joining(", "));

            // Add "+X more" indicator if there are more actors
            if (allActors.size() > maxActorsToShow) {
                actorsDisplay += " +" + (allActors.size() - maxActorsToShow) + " more";
            }
            
            return new SimpleStringProperty(actorsDisplay);
        });

        // Set up cast column cell factory with tooltips
        movieCastColumn.setCellFactory(col -> new TableCell<Movie, String>() {
            private final Tooltip tooltip = new Tooltip();
            
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                
                if (empty || item == null) {
                    setText("");
                    setTooltip(null);
                    setStyle("");
                    return;
                }
                
                // Get the movie for this row
                Movie movie = getTableView().getItems().get(getIndex());
                if (movie == null || movie.getActors() == null || movie.getActors().isEmpty()) {
                    setText("No cast");
                    setTooltip(null);
                    return;
                }
                
                // Set the display text with bold style
                setText(item);
                setStyle("-fx-font-weight: bold; -fx-text-fill: #e0e0e0;");
                
                // Create a detailed tooltip with all actors and their roles
                String tooltipText = movie.getActors().stream()
                        .map(actor -> {
                            String role = (actor.getRole() != null && !actor.getRole().trim().isEmpty())
                                    ? " as " + actor.getRole()
                                    : "";
                            return "• " + actor.getFullName() + role;
                        })
                        .collect(Collectors.joining("\n"));
                
                tooltip.setText(tooltipText);
                tooltip.setStyle(
                    "-fx-font-size: 12px;" +
                    "-fx-padding: 5px;" +
                    "-fx-background-color: #2a2a2a;" +
                    "-fx-text-fill: #ffffff;" +
                    "-fx-border-color: #444;"
                );
                tooltip.setMaxWidth(300);
                tooltip.setWrapText(true);
                
                // Only show tooltip if there are actors to show
                setTooltip(tooltip);
                
                // Style the cell with bold text
                setStyle(
                    "-fx-alignment: CENTER_LEFT; " +
                    "-fx-padding: 8 12; " +
                    "-fx-font-size: 13px; " +
                    "-fx-font-weight: bold; " +
                    "-fx-text-fill: #e0e0e0;"
                );
                setWrapText(true);
            }
        });

        // Set up rating column
        movieRatingColumn.setCellValueFactory(cellData -> {
            Movie movie = cellData.getValue();
            return new SimpleObjectProperty<>(movie != null ? movie.getImdbRating() : 0.0);
        });
        movieRatingColumn.setCellFactory(col -> new TableCell<Movie, Double>() {
            @Override
            protected void updateItem(Double item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null || item == 0.0) {
                    setText("");
                } else {
                    setText(String.format("%.1f", item));
                }
                setStyle("-fx-alignment: CENTER; -fx-padding: 5; -fx-font-weight: bold; -fx-text-fill: #FFD700;");
            }
        });
    }

    private void setupSearchHandlers() {
        movieSearchField.textProperty().addListener((obs, oldVal, newVal) -> filterMovies());
    }

    private void setupSortHandlers() {
        // Initialize sort options
        movieSortBy.getItems().addAll(
            "Title (A-Z)",
            "Title (Z-A)",
            "Year (Newest First)",
            "Year (Oldest First)",
            "Rating (High to Low)",
            "Rating (Low to High)"
        );

        // Set default sort option
        movieSortBy.getSelectionModel().selectFirst();

        // Add listener for sort selection changes
        movieSortBy.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) {
                sortMovieTable(newVal);
            }
        });
    }
    private void loadMovies() {
        if (moviesService == null) {
            logger.error("ContentService is not initialized");
            Platform.runLater(() ->
                statusLabel.setText("Error: Content service not initialized")
            );
            return;
        }

        logger.info("Starting to load movies...");
        logger.debug("ContentService type: {}", moviesService.getContentType());

        try {
            List<Movie> movies = moviesService.getAll();
            logger.info("Retrieved {} movies from service", movies.size());

            if (movies.isEmpty()) {
                logger.warn("No movies found in the database");
                Platform.runLater(() ->
                    statusLabel.setText("No movies found")
                );
                return;
            }

            // Log first few movies for debugging
            int logCount = Math.min(3, movies.size());
            for (int i = 0; i < logCount; i++) {
                Movie movie = movies.get(i);
                logger.debug("Movie #{}: {} ({}), Genres: {}, Director: {}",
                    i + 1,
                    movie.getTitle(),
                    movie.getYear(),
                    movie.getGenres().stream().map(Enum::name).collect(Collectors.joining(", ")),
                    movie.getDirector()
                );
            }

            Platform.runLater(() -> {
                try {
                    // Clear existing data
                    allMovies.clear();
                    
                    // Update results count
                    if (resultsCountLabel != null) {
                        resultsCountLabel.setText(String.format("Results: %d", movies.size()));
                    } else {
                        logger.warn("resultsCountLabel is not initialized");
                    }
                    
                    // Use a Set to filter out duplicates based on title, year, and director
                    Set<String> uniqueMovieKeys = new HashSet<>();
                    List<Movie> uniqueMovies = new ArrayList<>();
                    
                    for (Movie movie : movies) {
                        String key = movie.getTitle().toLowerCase() + "_" + movie.getYear() + "_" + 
                                   (movie.getDirector() != null ? movie.getDirector().toLowerCase() : "");
                        
                        if (uniqueMovieKeys.add(key)) { // add returns true if the key was not already in the set
                            uniqueMovies.add(movie);
                        } else {
                            logger.debug("Skipping duplicate movie: {} ({})", movie.getTitle(), movie.getYear());
                        }
                    }
                    
                    // Add only unique movies to the observable list
                    allMovies.addAll(uniqueMovies);
                    logger.info("Added {} unique movies to allMovies list ({} duplicates filtered out)", 
                              uniqueMovies.size(), movies.size() - uniqueMovies.size());
                    
                    // Update the filtered list and UI
                    filterMovies();
                    
                    // Update results count
                    if (resultsCountLabel != null) {
                        resultsCountLabel.setText(String.format("Results: %d", uniqueMovies.size()));
                    }
                    
                    statusLabel.setText(String.format("Loaded %d unique movies", uniqueMovies.size()));
                    logger.info("Successfully loaded and displayed {} unique movies", uniqueMovies.size());
                } catch (Exception e) {
                    logger.error("Error in Platform.runLater", e);
                    showError("Error", "Failed to update UI: " + e.getMessage());
                }
            });
        } catch (Exception e) {
            logger.error("Error loading movies", e);
            Platform.runLater(() ->
                showError("Error", "Failed to load movies: " + e.getMessage())
            );
        }
    }

    private void filterMovies() {
        String searchText = movieSearchField.getText().toLowerCase();
        if (searchText.isEmpty()) {
            filteredMovies.setAll(allMovies);
        } else {
            filteredMovies.setAll(allMovies.filtered(movie -> {
                boolean titleMatch = movie.getTitle().toLowerCase().contains(searchText);
                boolean directorMatch = movie.getDirector() != null && movie.getDirector().toLowerCase().contains(searchText);
                boolean genreMatch = movie.getGenres().stream()
                    .map(Enum::name)
                    .anyMatch(genre -> genre.toLowerCase().contains(searchText));
                return titleMatch || directorMatch || genreMatch;
            }));
        }
        movieTable.setItems(filteredMovies);
    }

    private void sortMovieTable(String sortOption) {
        switch (sortOption) {
            case "Title":
                allMovies.sort(Comparator.comparing(Movie::getTitle));
                break;
            case "Director":
                allMovies.sort(Comparator.comparing(Movie::getDirector));
                break;
            case "Year":
                allMovies.sort(Comparator.comparing(Movie::getYear));
                break;
            case "Genre":
                allMovies.sort((m1, m2) -> {
                    // Get the first genre's name for each movie, or empty string if no genres
                    String genre1 = m1.getGenres().isEmpty() ? "" : m1.getGenres().getFirst().name();
                    String genre2 = m2.getGenres().isEmpty() ? "" : m2.getGenres().getFirst().name();
                    return genre1.compareTo(genre2);
                });
                break;
            default:
        }
    }

    @FXML
    private void handleEditMovie(ActionEvent event) {
        Movie selected = movieTable.getSelectionModel().getSelectedItem();
        if (selected != null) {
            if (showMovieEditDialog(selected)) {
                try {
                    // Save changes if user clicked OK
                    moviesService.update(selected);
                    loadMovies(); // Refresh the table
                    showSuccess("Success", "Movie updated successfully!");
                } catch (Exception e) {
                    logger.error("Error updating movie", e);
                    showError("Error", "Failed to update movie: " + e.getMessage());
                }
            }
        } else {
            showAlert("No Selection", "Please select a movie to edit.");
        }
    }

    
    /**
     * Handles the "Add Movie" button click.
     * Creates a new movie and opens the edit dialog for it.
     * @param event The action event
     */
    @FXML
    private void handleAddMovie(ActionEvent event) {
        try {
            // Create a new movie with default values
            Calendar cal = Calendar.getInstance();
            cal.set(Calendar.YEAR, 2023); // Default to current year
            cal.set(Calendar.MONTH, Calendar.JANUARY);
            cal.set(Calendar.DAY_OF_MONTH, 1);
            
            // Create a new movie with default values
            Movie newMovie;
            newMovie = new Movie(
                "New Movie",
                Calendar.getInstance().get(Calendar.YEAR),
                Genre.DRAMA.name(),
                "Unknown Director",
                new HashMap<>(),
                0.0
            );
            newMovie.setReleaseDate(cal.getTime());
            newMovie.setActors(new ArrayList<>());
            
            // Show the edit dialog for the new movie
            if (showMovieEditDialog(newMovie)) {
                // Save the new movie
                moviesService.save(newMovie);

                // Update the filtered list and UI
                filterMovies();

                // Update results count
                if (resultsCountLabel != null) {
                    resultsCountLabel.setText(String.format("Results: %d", allMovies.size()));
                }

                // Update the table view
                movieTable.setItems(allMovies);

                // Refresh the movie list
                loadMovies();
                
                // Show success message
                statusLabel.setText("Movie added successfully!");

                // Clear the status message after 3 seconds
                new java.util.Timer().schedule(
                    new java.util.TimerTask() {
                        @Override
                        public void run() {
                            Platform.runLater(() -> statusLabel.setText(""));
                        }
                    },
                    3000
                );

            }
        } catch (Exception e) {
            logger.error("Error adding new movie: {}", e.getMessage(), e);
            showError("Add Movie Error", "Failed to add new movie: " + e.getMessage());
        }
    }


    @FXML
    private boolean showMovieEditDialog(Movie movie) {
        try {
            // Create a custom dialog
            Dialog<ButtonType> dialog = new Dialog<>();
            dialog.setTitle("New Movie".equals(movie.getTitle()) ? "Add New Movie" : "Edit Movie");
            dialog.setHeaderText("Enter movie details:");
            dialog.setResizable(false);
            dialog.setDialogPane(new DialogPane());

            // Set the button types
            ButtonType saveButtonType = new ButtonType("Save", ButtonBar.ButtonData.OK_DONE);
            dialog.getDialogPane().getButtonTypes().addAll(saveButtonType, ButtonType.CANCEL);


            // Create the form grid
            GridPane grid = new GridPane();
            grid.setHgap(10);
            grid.setVgap(10);
            grid.setPadding(new Insets(20, 150, 10, 10));

            // Create form fields
            TextField titleField = new TextField(movie.getTitle());
            TextField yearField = new TextField(String.valueOf(Calendar.getInstance().get(Calendar.YEAR)));
            TextField directorField = new TextField(movie.getDirector() != null ? movie.getDirector() : "");
            TextField genreField = new TextField(movie.getGenres().stream().map(Enum::name).collect(Collectors.joining(", ")));
            TextField ratingField = new TextField(String.valueOf(movie.getRating()));
            String actorNames = movie.getActors().stream()
                    .<String>map(actor -> actor.getName().toString())
                    .collect(Collectors.joining(", "));
            TextField actorsField = new TextField(actorNames);

            // Add fields to grid
            grid.add(new Label("Title:"), 0, 0);
            grid.add(titleField, 1, 0);
            grid.add(new Label("Year:"), 0, 1);
            grid.add(yearField, 1, 1);
            grid.add(new Label("Director:"), 0, 2);
            grid.add(directorField, 1, 2);
            grid.add(new Label("Genre:"), 0, 3);
            grid.add(genreField, 1, 3);
            grid.add(new Label("Rating:"), 0, 4);
            grid.add(ratingField, 1, 4);
            grid.add(new Label("Actors:"), 0, 5);
            grid.add(actorsField, 1, 5);

            // Add the form to the dialog
            dialog.getDialogPane().setContent(grid);
            dialog.getDialogPane().setMinWidth(400);
            dialog.getDialogPane().setMinHeight(300);

            // Request focus on the title field by default
            Platform.runLater(titleField::requestFocus);

            // Convert the result to a response
            Optional<ButtonType> result = dialog.showAndWait();

            if (result.isPresent() && result.get() == saveButtonType) {
                // Update the movie with the form data
                movie.setTitle(titleField.getText().trim());
                try {
                    int year = Integer.parseInt(yearField.getText().trim());
                    Calendar cal = Calendar.getInstance();
                    cal.set(Calendar.YEAR, year);
                    movie.setReleaseDate(cal.getTime());
                    movie.setRating(Double.parseDouble(ratingField.getText().trim()));
                    movie.setGenres(Arrays.stream(genreField.getText().trim().split(","))
                            .map(String::trim)
                            .map(Genre::valueOf)
                            .collect(Collectors.toList()));
                    movie.setActors(Arrays.stream(actorsField.getText().trim().split(","))
                            .map(String::trim)
                            .map(Actor::new)
                            .collect(Collectors.toList()));
                } catch (NumberFormatException e) {
                    showError("Invalid Year", "Please enter a valid year number.");
                    logger.error("Error parsing year", e);
                    return false;
                }
                movie.setDirector(directorField.getText().trim());
                return true;
            }
            return false;
        } catch (Exception e) {
            logger.error("Error showing movie edit dialog", e);
            showError("Error", "Failed to show movie editor: " + e.getMessage());
            return false;
        }
    }

    @FXML
    public void handleRefresh(ActionEvent actionEvent) {
        try {
            logger.info("Refreshing movies...");
            loadMovies();
            showSuccess("Success", "Movies refreshed successfully.");
        } catch (Exception e) {
            logger.error("Error refreshing movies", e);
            showError("Error", "Failed to refresh movies: " + e.getMessage());
        }
    }


    @FXML
    private void showAdvancedSearchDialog(ActionEvent event) {
        // Create a custom dialog
        Dialog<Map<String, Object>> dialog = new Dialog<>();
        dialog.setTitle("Advanced Search");
        dialog.setHeaderText("Enter your search criteria");
        dialog.setDialogPane(new DialogPane());

        // Set the button types
        ButtonType searchButtonType = new ButtonType("Search", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(searchButtonType, ButtonType.CANCEL);

        // Create the search criteria form
        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 150, 10, 10));

        TextField titleField = new TextField();
        titleField.setPromptText("Title");
        TextField directorField = new TextField();
        directorField.setPromptText("Director");
        TextField yearFromField = new TextField();
        yearFromField.setPromptText("From Year");
        TextField yearToField = new TextField();
        yearToField.setPromptText("To Year");
        TextField ratingFromField = new TextField();
        ratingFromField.setPromptText("From Rating");
        TextField ratingToField = new TextField();
        ratingToField.setPromptText("To Rating");
        TextField actorsField = new TextField();
        actorsField.setPromptText("Actors");
        TextField genresField = new TextField();
        genresField.setPromptText("Genres");

        // Add sort options
        ComboBox<String> sortByCombo = new ComboBox<>();
        sortByCombo.getItems().addAll("Title (A-Z)", "Title (Z-A)", "Year (Newest)", "Year (Oldest)", "Rating (High-Low)", "Rating (Low-High)");
        sortByCombo.setPromptText("Sort by...");

        // Add genre checkboxes
        VBox genreBox = new VBox(5);
        genreBox.setPadding(new Insets(5));
        ScrollPane genreScrollPane = new ScrollPane(genreBox);
        genreScrollPane.setFitToWidth(true);
        
        // Create checkboxes for each genre with a select all option
        Map<CheckBox, Genre> genreMap = new HashMap<>();
        
        // Add select all checkbox
        CheckBox selectAll = new CheckBox("Select All Genres");
        selectAll.setSelected(true);
        selectAll.selectedProperty().addListener((obs, wasSelected, isSelected) -> {
            genreMap.keySet().forEach(cb -> cb.setSelected(isSelected));
        });
        //selectAll checkbox to the genreBox
        genreBox.getChildren().add(selectAll);
        
        // Add genre checkboxes
        for (Genre genre : Genre.values()) {
            CheckBox checkBox = new CheckBox(genre.name());
            genreBox.getChildren().add(checkBox);
            genreMap.put(checkBox, genre);
        }

        // Add components to grid
        int row = 0;
        grid.add(new Label("Title:"), 0, row);
        grid.add(titleField, 1, row++);

        grid.add(new Label("Director:"), 0, row);
        grid.add(directorField, 1, row++);
        grid.add(new Label("Actors:"), 0, row);
        grid.add(actorsField, 1, row++);

        grid.add(new Label("Year Range:"), 0, row);
        grid.add(yearFromField, 1, row++);
        grid.add(yearToField, 2, row++);

        grid.add(new Label("Rating Range:"), 0, row);
        grid.add(ratingFromField, 1, row++);
        grid.add(ratingToField, 2, row++);

        grid.add(new Label("Genres:"), 0, row);
        grid.add(genreScrollPane, 1, row++);


        HBox yearBox = new HBox(5);
        yearBox.getChildren().addAll(yearFromField, new Label("to"), yearToField);
        grid.add(yearBox, 1, row++);
        
        // Add sort option
        grid.add(new Label("Sort By:"), 0, row);
        grid.add(sortByCombo, 1, row++);

        // Add some padding
        grid.setPadding(new Insets(15));

        // Set the dialog content
        dialog.getDialogPane().setContent(grid);

        // Request focus on the title field by default
        Platform.runLater(titleField::requestFocus);

        // Convert the result to a map of search criteria when the search button is clicked
        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == searchButtonType) {


                // Create a map of search criteria
                Map<String, Object> searchCriteria = new HashMap<>();
                if (!titleField.getText().isEmpty()) searchCriteria.put("title", titleField.getText().trim());
                if (!directorField.getText().isEmpty()) searchCriteria.put("director", directorField.getText().trim());
                if (!yearFromField.getText().isEmpty()) searchCriteria.put("yearFrom", Integer.parseInt(yearFromField.getText().trim()));
                if (!yearToField.getText().isEmpty()) searchCriteria.put("yearTo", Integer.parseInt(yearToField.getText().trim()));
                if (!ratingFromField.getText().isEmpty()) searchCriteria.put("ratingFrom", Double.parseDouble(ratingFromField.getText().trim()));
                if (!ratingToField.getText().isEmpty()) searchCriteria.put("ratingTo", Double.parseDouble(ratingToField.getText().trim()));
                if (!actorsField.getText().isEmpty()) searchCriteria.put("actors", actorsField.getText().trim());
                if (!genresField.getText().isEmpty()) searchCriteria.put("genres", genresField.getText().trim());

                
                // Add sort option
                if (sortByCombo.getValue() != null) {
                    searchCriteria.put("sortBy", sortByCombo.getValue());
                }
                
                // Get selected genres (only if not all are selected)
                List<Genre> selectedGenres = genreMap.entrySet().stream()
                    .filter(entry -> entry.getKey().isSelected())
                    .map(Map.Entry::getValue)
                    .collect(Collectors.toList());
                
                // Only add genres filter if not all genres are selected
                if (!selectedGenres.isEmpty() && selectedGenres.size() < genreMap.size()) {
                    searchCriteria.put("genres", selectedGenres);
                }
                
                // Add year range if provided
                if (!yearFromField.getText().trim().isEmpty()) {
                    try {
                        searchCriteria.put("yearFrom", Integer.parseInt(yearFromField.getText().trim()));
                    } catch (NumberFormatException e) {
                        // Handle invalid year format
                        showError("Invalid Year", "Please enter a valid year number");
                        return null;
                    }
                }
                if (!yearToField.getText().trim().isEmpty()) {
                    try {
                        searchCriteria.put("yearTo", Integer.parseInt(yearToField.getText().trim()));
                    } catch (NumberFormatException e) {
                        // Handle invalid year format
                        showError("Invalid Year", "Please enter a valid year number");
                        return null;
                    }
                }

                // Add rating range if provided for from-to
                if (!ratingFromField.getText().trim().isEmpty()) {
                    try {
                        searchCriteria.put("ratingFrom", Double.parseDouble(ratingFromField.getText().trim()));
                    } catch (NumberFormatException e) {
                        // Handle invalid rating format
                        showError("Invalid Rating", "Please enter a valid rating number");
                        return null;
                    }
                }

                if (!ratingToField.getText().trim().isEmpty()) {
                    try {
                        searchCriteria.put("ratingTo", Double.parseDouble(ratingToField.getText().trim()));
                    } catch (NumberFormatException e) {
                        // Handle invalid rating format
                        showError("Invalid Rating", "Please enter a valid rating number");
                        return null;
                    }
                }

                return searchCriteria;
            }
            return null;
        });
        
        // Show the dialog and process the results
        Optional<Map<String, Object>> result = dialog.showAndWait();
        
        result.ifPresent(searchCriteria -> {
            try {
                // Apply the search criteria to filter movies
                Set<String> uniqueKeys = new HashSet<>();
                List<Movie> filteredMovies = allMovies.stream()
                    .filter(movie -> {
                        // Create a unique key for each movie (title + year)
                        String uniqueKey = movie.getTitle().toLowerCase() + "_" + 
                            (movie.getReleaseDate() != null ? 
                                new java.text.SimpleDateFormat("yyyy").format(movie.getReleaseDate()) : "");
                        
                        // Skip if we've already seen this movie
                        if (uniqueKeys.contains(uniqueKey)) {
                            return false;
                        }
                        
                        boolean matches = true;
                        
                        // Filter by title (case-insensitive partial match)
                        if (searchCriteria.containsKey("title")) {
                            String searchTitle = ((String) searchCriteria.get("title")).toLowerCase();
                            matches = movie.getTitle().toLowerCase().contains(searchTitle);
                            if (!matches) return false;
                        }
                        
                        // Filter by director (case-insensitive partial match)
                        if (searchCriteria.containsKey("director") && movie.getDirector() != null) {
                            String searchDirector = ((String) searchCriteria.get("director")).toLowerCase();
                            matches = movie.getDirector().toLowerCase().contains(searchDirector);
                            if (!matches) return false;
                        }
                        
                        // Filter by year range
                        if (searchCriteria.containsKey("yearFrom")) {
                            int yearFrom = (int) searchCriteria.get("yearFrom");
                            Calendar cal = Calendar.getInstance();
                            cal.setTime(movie.getReleaseDate());
                            matches = cal.get(Calendar.YEAR) >= yearFrom;
                            if (!matches) return false;
                        }
                        
                        if (searchCriteria.containsKey("yearTo")) {
                            int yearTo = (int) searchCriteria.get("yearTo");
                            Calendar cal = Calendar.getInstance();
                            cal.setTime(movie.getReleaseDate());
                            matches = cal.get(Calendar.YEAR) <= yearTo;
                            if (!matches) return false;
                        }
                        
                        // Filter by genres (must match all selected genres)
                        if (searchCriteria.containsKey("genres")) {
                            @SuppressWarnings("unchecked")
                            List<Genre> selectedGenres = (List<Genre>) searchCriteria.get("genres");
                            matches = new HashSet<>(movie.getGenres()).containsAll(selectedGenres);
                            if (!matches) return false;
                        }

                        // Filter by actors (case-insensitive partial match)
                        if (searchCriteria.containsKey("actors")) {
                            String searchActors = ((String) searchCriteria.get("actors")).toLowerCase();
                            matches = movie.getActors().stream()
                                .anyMatch(actor -> actor.toLowerCase().contains(searchActors));
                            if (!matches) return false;
                        }
                        
                        // If we get here, the movie matches all criteria
                        uniqueKeys.add(uniqueKey);
                        return true;
                    })
                    .collect(Collectors.toList());

                // Sort the filtered movies
                filteredMovies.sort(Comparator.comparing(Movie::getTitle, String.CASE_INSENSITIVE_ORDER));

                // Set the filtered movies to the table
                movieTable.setItems(FXCollections.observableList(filteredMovies));

                
                // Apply sorting if specified
                if (searchCriteria.containsKey("sortBy")) {
                    String sortOption = (String) searchCriteria.get("sortBy");
                    switch (sortOption) {
                        case "Title (A-Z)":
                            filteredMovies.sort(Comparator.comparing(Movie::getTitle, String.CASE_INSENSITIVE_ORDER));
                            break;
                        case "Title (Z-A)":
                            filteredMovies.sort(Comparator.comparing(Movie::getTitle, String.CASE_INSENSITIVE_ORDER).reversed());
                            break;
                        case "Year (Newest)":
                            filteredMovies.sort(Comparator.comparing(movie -> movie.getRating(), 
                                Comparator.nullsLast(Comparator.reverseOrder())));
                            break;
                        case "Rating (High-Low)":
                            filteredMovies.sort(Comparator.comparing(movie -> movie.getRating(), 
                                Comparator.nullsLast(Comparator.reverseOrder())));
                            break;
                        case "Rating (Low-High)":
                            filteredMovies.sort(Comparator.comparing(movie -> movie.getRating(), 
                                Comparator.nullsLast(Comparator.naturalOrder())));
                            break;
                        default:
                            break;
                    }
                }

                // Update the UI with filtered and sorted results
                Platform.runLater(() -> {
                    allMovies.setAll(filteredMovies);
                    filterMovies(); // Apply any additional filtering from the search bar
                    statusLabel.setText(String.format("Found %d matching movie%s", 
                        filteredMovies.size(),
                        filteredMovies.size() != 1 ? "s" : ""));
                    
                    if (filteredMovies.isEmpty()) {
                        showInformation();
                        statusLabel.setText("No movies match your search criteria.");
                        movieTable.setItems(FXCollections.observableList(filteredMovies));
                        movieTable.refresh();
                    }
                });
                
            } catch (Exception e) {
                logger.error("Error performing advanced search", e);
                showError("Search Error", "An error occurred while performing the search: " + e.getMessage());
            }
        });

    }

    //show information message
    private void showInformation() {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("No Results");
        alert.setHeaderText(null);
        alert.setContentText("No movies match your search criteria.");
        alert.showAndWait();
    }
}
