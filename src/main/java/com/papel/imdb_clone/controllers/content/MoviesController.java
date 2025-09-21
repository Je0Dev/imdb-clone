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
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import com.papel.imdb_clone.service.search.ServiceLocator;
import javafx.util.Pair;

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
    private final NavigationService navigationService;
    
    public MoviesController() {
        super();
        this.navigationService = NavigationService.getInstance();
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
        
        // Create a dialog to get the rating
        Dialog<Pair<Double, String>> dialog = new Dialog<>();
        dialog.setTitle("Rate Movie");
        dialog.setHeaderText(String.format("Rate: %s (%d)", selectedMovie.getTitle(), selectedMovie.getReleaseYear()));
        
        // Set the button types
        ButtonType rateButtonType = new ButtonType("Rate", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(rateButtonType, ButtonType.CANCEL);
        
        // Create the rating input fields
        Spinner<Double> ratingSpinner = new Spinner<>(0.0, 10.0, 5.0, 0.5);
        ratingSpinner.setEditable(true);
        TextArea commentArea = new TextArea();
        commentArea.setPromptText("Your review (optional)");
        commentArea.setWrapText(true);
        
        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 150, 10, 10));
        
        grid.add(new Label("Rating (0-10):"), 0, 0);
        grid.add(ratingSpinner, 1, 0);
        grid.add(new Label("Review:"), 0, 1);
        grid.add(commentArea, 1, 1);
        
        dialog.getDialogPane().setContent(grid);
        
        // Request focus on the rating spinner by default
        Platform.runLater(() -> ratingSpinner.requestFocus());
        
        // Convert the result to a rating and comment when the rate button is clicked
        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == rateButtonType) {
                return new Pair<>(ratingSpinner.getValue(), commentArea.getText().trim());
            }
            return null;
        });
        
        Optional<Pair<Double, String>> result = dialog.showAndWait();
        
        result.ifPresent(ratingAndComment -> {
            double rating = ratingAndComment.getKey();
            String comment = ratingAndComment.getValue();
            
            try {
                // Here you would typically call a service to save the rating
                // For example: ratingService.rateMovie(selectedMovie.getId(), currentUserId, rating, comment);
                
                // Update the UI to reflect the new rating
                showAlert("Success", String.format("You rated %s: %.1f/10", selectedMovie.getTitle(), rating)
                         );
                
                // Refresh the movie list to show updated ratings
                loadMovies();
                
            } catch (Exception e) {
                logger.error("Error rating movie: {}", e.getMessage(), e);
                showAlert("Error", "Failed to save rating: " + e.getMessage());
            }
        });
    }
    
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

        // Set up year column to show only the year part
        movieYearColumn.setCellValueFactory(cellData -> {
            Movie movie = cellData.getValue();
            if (movie != null && movie.getYear() != null) {
                Calendar cal = Calendar.getInstance();
                cal.setTime(movie.getYear());
                return new SimpleStringProperty(String.valueOf(cal.get(Calendar.YEAR)));
            }
            return new SimpleStringProperty("");
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
            
            // Sort actors by importance (e.g., by role or billing order if available)
            List<Actor> sortedActors = new ArrayList<>(movie.getActors());
            
            // Show at least 3 actors if available, or all if less than 3
            int maxActorsToShow = Math.min(sortedActors.size(), 5); // Show up to 5 actors
            
            String actors = sortedActors.stream()
                    .limit(maxActorsToShow)
                    .map(actor -> {
                        String role = (actor.getRole() != null && !actor.getRole().trim().isEmpty())
                                ? " (" + actor.getRole().trim() + ")"
                                : "";
                        return "• " + actor.getFullName() + role;
                    })
                    .collect(Collectors.joining("\n"));

            // Add "and X more..." if there are more actors than shown
            if (sortedActors.size() > maxActorsToShow) {
                actors += "\nand " + (sortedActors.size() - maxActorsToShow) + " more...";
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

    private void deleteMovie(Movie movie) {
        try {
            if (showConfirmationDialog("Confirm Deletion", "Are you sure you want to delete this movie?")) {
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
     * Handles the refresh button action to reload all movies.
     */

    @FXML
    private void handleManageMovie(ActionEvent event) {
        try {
            Movie selectedMovie = movieTable.getSelectionModel().getSelectedItem();
            if (selectedMovie != null) {
                showMovieManagementDialog(selectedMovie);
            } else {
                showAlert("No Selection", "Please select a movie to manage.");
            }
        } catch (Exception e) {
            logger.error("Error in handleManageMovie: {}", e.getMessage(), e);
            showError("Error", "Failed to manage movie: " + e.getMessage());
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
            Date defaultDate = cal.getTime();
            
            // Create a new movie with default values
            Movie newMovie = new Movie("New Movie", defaultDate, 
                new ArrayList<>(Collections.singletonList(Genre.DRAMA)),
                0.0, "Unknown Director", new ArrayList<>());
            
            // Show the edit dialog for the new movie
            if (showMovieEditDialog(newMovie)) {
                // Save the new movie
                moviesService.save(newMovie);
                
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
    
    private void showMovieManagementDialog(Movie movie) {
        try {
            showMovieEditDialog(movie);
            loadMovies();
            showSuccess("Success", "Movie managed successfully!");
        } catch (Exception e) {
            logger.error("Error in showMovieManagementDialog: {}", e.getMessage(), e);
            showError("Error", "Failed to show movie management dialog: " + e.getMessage());
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
        try {
            // Create a custom dialog
            Dialog<Pair<Double, String>> dialog = new Dialog<>();
            dialog.setTitle("Rate Movie");
            dialog.setHeaderText("Rate " + movie.getTitle());

            // Set the button types
            ButtonType rateButtonType = new ButtonType("Rate", ButtonBar.ButtonData.OK_DONE);
            dialog.getDialogPane().getButtonTypes().addAll(rateButtonType, ButtonType.CANCEL);

            // Create the rating components
            GridPane grid = new GridPane();
            grid.setHgap(10);
            grid.setVgap(10);
            grid.setPadding(new Insets(20, 150, 10, 10));

            Slider ratingSlider = new Slider(1, 10, 5);
            ratingSlider.setShowTickMarks(true);
            ratingSlider.setShowTickLabels(true);
            ratingSlider.setMajorTickUnit(1);
            ratingSlider.setMinorTickCount(0);
            ratingSlider.setSnapToTicks(true);
            ratingSlider.setBlockIncrement(1);

            Label ratingValue = new Label("5.0");
            Label ratingLabel = new Label("Rating (1-10):");

            TextArea commentArea = new TextArea();
            commentArea.setPromptText("Optional comment...");
            commentArea.setWrapText(true);
            commentArea.setPrefRowCount(3);

            // Update the rating value label when slider changes
            ratingSlider.valueProperty().addListener((obs, oldVal, newVal) -> {
                ratingValue.setText(String.format("%.1f", newVal));
            });

            grid.add(ratingLabel, 0, 0);
            grid.add(ratingSlider, 1, 0);
            grid.add(ratingValue, 2, 0);
            grid.add(new Label("Comment:"), 0, 1);
            grid.add(commentArea, 1, 2, 2, 1);

            // Enable/Disable login button depending on whether a rating was entered
            Node rateButton = dialog.getDialogPane().lookupButton(rateButtonType);
            rateButton.setDisable(false);

            dialog.getDialogPane().setContent(grid);

            // Request focus on the slider by default
            Platform.runLater(ratingSlider::requestFocus);

            // Convert the result to a rating-comment pair when the login button is clicked
            dialog.setResultConverter(dialogButton -> {
                if (dialogButton == rateButtonType) {
                    return new Pair<>(ratingSlider.getValue(), commentArea.getText());
                }
                return null;
            });

            Optional<Pair<Double, String>> result = dialog.showAndWait();

            result.ifPresent(ratingComment -> {
                double rating = ratingComment.getKey();
                String comment = ratingComment.getValue();

                // Here you would typically save the rating to your data model
                // For example: movie.addRating(rating, comment);
                // And then update the movie in your service
                // moviesService.update(movie);

                // For now, we'll just show a confirmation
                showSuccess("Rating Submitted",
                    String.format("You rated %s with %.1f stars!",
                    movie.getTitle(), rating) +
                    (comment.isEmpty() ? "" : "\n\nComment: " + comment));

                // Refresh the movie list to show the updated rating
                loadMovies();
            });
        } catch (Exception e) {
            logger.error("Error showing rating dialog", e);
            showError("Error", "Failed to show rating dialog: " + e.getMessage());
        }
    }

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

        // Add sort options
        ComboBox<String> sortByCombo = new ComboBox<>();
        sortByCombo.getItems().addAll("Title (A-Z)", "Title (Z-A)", "Year (Newest)", "Year (Oldest)", "Rating (High-Low)", "Rating (Low-High)");
        sortByCombo.setPromptText("Sort by...");

        // Add genre checkboxes
        VBox genreBox = new VBox(5);
        genreBox.setPadding(new Insets(5));
        ScrollPane genreScrollPane = new ScrollPane(genreBox);
        genreScrollPane.setPrefHeight(150);
        genreScrollPane.setFitToWidth(true);
        
        // Create checkboxes for each genre with a select all option
        Map<CheckBox, Genre> genreMap = new HashMap<>();
        
        // Add select all checkbox
        CheckBox selectAll = new CheckBox("Select All Genres");
        selectAll.setSelected(true);
        selectAll.selectedProperty().addListener((obs, wasSelected, isSelected) -> {
            genreMap.keySet().forEach(cb -> cb.setSelected(isSelected));
        });
        genreBox.getChildren().add(selectAll);
        
        // Add genre checkboxes
        for (Genre genre : Genre.values()) {
            CheckBox checkBox = new CheckBox(genre.name());
            checkBox.setSelected(true);
            genreMap.put(checkBox, genre);
            genreBox.getChildren().add(checkBox);
        }

        // Add components to grid
        int row = 0;
        grid.add(new Label("Title:"), 0, row);
        grid.add(titleField, 1, row++);
        grid.add(new Label("Director:"), 0, row);
        grid.add(directorField, 1, row++);
        grid.add(new Label("Year Range:"), 0, row);
        HBox yearBox = new HBox(5);
        yearBox.getChildren().addAll(yearFromField, new Label("to"), yearToField);
        grid.add(yearBox, 1, row++);
        
        // Add sort option
        grid.add(new Label("Sort By:"), 0, row);
        grid.add(sortByCombo, 1, row++);
        
        grid.add(new Label("Genres:"), 0, row);
        grid.add(genreScrollPane, 1, row++);
        
        // Add some padding
        grid.setPadding(new Insets(15));

        // Set the dialog content
        dialog.getDialogPane().setContent(grid);

        // Request focus on the title field by default
        Platform.runLater(titleField::requestFocus);

        // Convert the result to a map of search criteria when the search button is clicked
        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == searchButtonType) {
                Map<String, Object> searchCriteria = new HashMap<>();
                if (!titleField.getText().isEmpty()) searchCriteria.put("title", titleField.getText().trim());
                if (!directorField.getText().isEmpty()) searchCriteria.put("director", directorField.getText().trim());
                
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
                        
                        // If we get here, the movie matches all criteria
                        uniqueKeys.add(uniqueKey);
                        return true;
                    })
                    .collect(Collectors.toList());
                
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
                            filteredMovies.sort(Comparator.comparing(m -> m.getReleaseDate(), 
                                Comparator.nullsLast(Comparator.reverseOrder())));
                            break;
                        case "Year (Oldest)":
                            filteredMovies.sort(Comparator.comparing(m -> m.getReleaseDate(), 
                                Comparator.nullsFirst(Comparator.naturalOrder())));
                            break;
                        case "Rating (High-Low)":
                            filteredMovies.sort(Comparator.comparing(Movie::getRating, 
                                Comparator.nullsLast(Comparator.reverseOrder())));
                            break;
                        case "Rating (Low-High)":
                            filteredMovies.sort(Comparator.comparing(Movie::getRating, 
                                Comparator.nullsLast(Comparator.naturalOrder())));
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
                    }
                });
                
            } catch (Exception e) {
                logger.error("Error performing advanced search", e);
                showError("Search Error", "An error occurred while performing the search: " + e.getMessage());
            }
        });
    }

    private void showInformation() {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("No Results");
        alert.setHeaderText(null);
        alert.setContentText("No movies match your search criteria.");
        alert.showAndWait();
    }
}
