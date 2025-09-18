package com.papel.imdb_clone.controllers;

import com.papel.imdb_clone.service.MoviesService;
import com.papel.imdb_clone.service.NavigationService;
import com.papel.imdb_clone.util.UIUtils;
import com.papel.imdb_clone.model.Movie;
import javafx.application.Platform;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;

import javafx.stage.Stage;

import java.net.URL;
import java.util.*;
import java.util.stream.Collectors;

import javafx.geometry.Insets;

/**
 * Controller for managing movies in the application.
 * Handles all movie-related operations including listing, adding, editing, and deleting movies.
 */
public class MoviesController extends BaseController implements Initializable {
    // UI Components
    @FXML private Label statusLabel;
    @FXML private TableView<Movie> movieTable;
    @FXML private TableColumn<Movie, String> movieTitleColumn;
    @FXML private TableColumn<Movie, String> movieYearColumn;
    @FXML private TableColumn<Movie, String> movieGenreColumn;
    @FXML private TableColumn<Movie, String> movieDirectorColumn;
    @FXML private TableColumn<Movie, String> movieCastColumn;
    @FXML private TableColumn<Movie, Double> movieRatingColumn;
    @FXML private TextField movieSearchField;
    @FXML private ComboBox<String> movieSortBy;

    /**
     * Navigates back to the home view.
     */
    @FXML
    public void goToHome() {
        try {
            NavigationService navigationService = NavigationService.getInstance();
            navigationService.navigateTo("/fxml/home-view.fxml", 
                (Stage) movieTable.getScene().getWindow(),
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
    private MoviesService moviesService;

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
        // Set up title column
        movieTitleColumn.setCellValueFactory(cellData -> {
            Movie movie = cellData.getValue();
            return new SimpleStringProperty(movie != null ? movie.getTitle() : "");
        });
        movieTitleColumn.setCellFactory(col -> new TableCell<Movie, String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? "" : item);
                setStyle("-fx-alignment: CENTER_LEFT; -fx-padding: 5 10;");
            }
        });

        // Set up year column
        movieYearColumn.setCellValueFactory(cellData -> {
            Movie movie = cellData.getValue();
            return new SimpleStringProperty(movie != null ? String.valueOf(movie.getYear()) : "");
        });
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
            // Get up to 3 main actors/actresses
            String actors = movie.getActors().stream()
                    .limit(3) // Show only first 3 actors
                    .map(actor -> {
                        String role = actor.getRole() != null && !actor.getRole().isEmpty() 
                                ? " (" + actor.getRole() + ")" 
                                : "";
                        return actor.getFullName() + role;
                    })
                    .collect(Collectors.joining("\n"));
            
            // Add "and X more..." if there are more than 3 actors
            if (movie.getActors().size() > 3) {
                actors += "\nand " + (movie.getActors().size() - 3) + " more...";
            }
            
            return new SimpleStringProperty(actors);
        });
        movieCastColumn.setCellFactory(col -> new TableCell<Movie, String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? "No cast information available" : item);
                setStyle(
                    "-fx-alignment: CENTER_LEFT; " +
                    "-fx-padding: 5 10; " +
                    "-fx-wrap-text: true; " +
                    "-fx-font-size: 12px;"
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
                    allMovies.clear();
                    allMovies.addAll(movies);
                    logger.info("Added {} movies to allMovies list", movies.size());
                    
                    // Refresh the table
                    movieTable.getItems().clear();
                    movieTable.getItems().addAll(movies);
                    statusLabel.setText(String.format("Loaded %d movies", movies.size()));
                    
                    // Apply any existing filters
                    filterMovies();
                    
                    statusLabel.setText(String.format("Loaded %d movies", movies.size()));
                    logger.info("Successfully loaded and displayed {} movies", movies.size());
                    
                    // Debug: Log table columns and items
                    logger.debug("Table columns: {}", movieTable.getColumns());
                    logger.debug("Table items count: {}", movieTable.getItems().size());
                    
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

    private void deleteMovie(Movie movie) {
        try {
            if (showConfirmationDialog("Are you sure you want to delete this movie?")) {
                moviesService.delete(movie.getId());
                loadMovies();
                showSuccess("Success", "Movie deleted successfully.");
            }
        } catch (Exception e) {
            logger.error("Error deleting movie", e);
            showError("Error", "Failed to delete movie: " + e.getMessage());
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
        // Implementation of sorting logic
        // This is a placeholder - implement actual sorting based on sortOption
    }

    @FXML
    private void handleAddMovie(ActionEvent event) {
        try {
            // Create a new movie with default values
            Movie newMovie = new Movie("New Movie", Calendar.getInstance().get(Calendar.YEAR), 
                "ACTION", "Unknown", new HashMap<>(), 0.0);
            
            // Show the edit dialog for the new movie
            if (showMovieEditDialog(newMovie)) {
                // Save the new movie
                moviesService.save(newMovie);
                loadMovies();
                showSuccess("Success", "Movie added successfully!");
            }
        } catch (Exception e) {
            logger.error("Error adding movie", e);
            showError("Error", "Failed to add movie: " + e.getMessage());
        }
    }

    @FXML
    private void handleEditMovie(ActionEvent event) {
        Movie selected = movieTable.getSelectionModel().getSelectedItem();
        if (selected != null) {
            showMovieEditDialog(selected);
        } else {
            showAlert("No Selection", "Please select a movie to edit.");
        }
    }

    @FXML
    private void handleDeleteMovie(ActionEvent event) {
        Movie selected = movieTable.getSelectionModel().getSelectedItem();
        if (selected != null) {
            boolean confirm = showConfirmationDialog("Are you sure you want to delete '" + selected.getTitle() + "'?");
            if (confirm) {
                try {
                    deleteMovie(selected);
                } catch (Exception e) {
                    logger.error("Error deleting movie", e);
                    showError("Error", "Failed to delete movie: " + e.getMessage());
                }
            }
        } else {
            showAlert("No Selection", "Please select a movie to delete.");
        }
    }

    @FXML
    private void handleRateMovie(ActionEvent event) {
        Movie selected = movieTable.getSelectionModel().getSelectedItem();
        if (selected != null) {
            showRatingDialog(selected);
        } else {
            showAlert("No Selection", "Please select a movie to rate.");
        }
    }

    private boolean showMovieEditDialog(Movie movie) {
        try {
            // Create a custom dialog
            Dialog<ButtonType> dialog = new Dialog<>();
            dialog.setTitle("New Movie".equals(movie.getTitle()) ? "Add New Movie" : "Edit Movie");
            dialog.setHeaderText("Enter movie details:");
            
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
            
            // Add fields to grid
            grid.add(new Label("Title:"), 0, 0);
            grid.add(titleField, 1, 0);
            grid.add(new Label("Year:"), 0, 1);
            grid.add(yearField, 1, 1);
            grid.add(new Label("Director:"), 0, 2);
            grid.add(directorField, 1, 2);
            
            // Add the form to the dialog
            dialog.getDialogPane().setContent(grid);
            
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
                } catch (NumberFormatException e) {
                    showError("Invalid Year", "Please enter a valid year number.");
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

    private void showRatingDialog(Movie movie) {
        // Implementation of rating dialog
        // This will be moved from ContentController
    }

    // Helper methods
    // addFormField is now inherited from BaseController
}
