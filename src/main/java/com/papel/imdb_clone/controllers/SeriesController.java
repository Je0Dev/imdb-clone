package com.papel.imdb_clone.controllers;

import com.papel.imdb_clone.service.NavigationService;
import com.papel.imdb_clone.service.SeriesService;
import com.papel.imdb_clone.util.UIUtils;
import com.papel.imdb_clone.enums.Genre;
import com.papel.imdb_clone.model.Series;
import javafx.application.Platform;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.GridPane;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URL;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Controller for managing TV series in the application.
 * Handles all series-related operations including listing, adding, editing, and deleting series.
 */
public class SeriesController extends BaseController implements Initializable {
    private static final Logger logger = LoggerFactory.getLogger(SeriesController.class);
    
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
            logger.error("Error in handleManageSeries: " + e.getMessage(), e);
            showErrorAlert("Error", "Failed to manage series", e.getMessage());
        }
    }

    private void showSeriesManagementDialog(Series selectedSeries) {
        try {
        } catch (Exception e) {
            logger.error("Error in showSeriesManagementDialog: " + e.getMessage(), e);
            showErrorAlert("Error", "Failed to show series management dialog", e.getMessage());
        }
    }

    private void showErrorAlert(String error, String failedToManageSeries, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(error);
        alert.setHeaderText(failedToManageSeries);
        alert.setContentText(message);
        alert.showAndWait();
    }

    // UI Components
    @FXML private Label statusLabel;
    @FXML private TableView<Series> seriesTable;
    @FXML private TableColumn<Series, String> seriesTitleColumn;
    @FXML private TableColumn<Series, Integer> seriesYearColumn;
    @FXML private TableColumn<Series, String> seriesGenreColumn;
    @FXML private TableColumn<Series, Integer> seriesSeasonsColumn;
    @FXML private TableColumn<Series, Integer> seriesEpisodesColumn;
    @FXML private TableColumn<Series, Double> seriesRatingColumn;
    @FXML private TableColumn<Series, String> seriesCreatorColumn;
    @FXML
    private TableColumn<Series, String> seriesCastColumn;
    
    /**
     * Navigates back to the home view.
     */
    @FXML
    public void goToHome() {
        try {
            NavigationService navigationService = NavigationService.getInstance();
            navigationService.navigateTo("/fxml/home-view.fxml", 
                (Stage) seriesTable.getScene().getWindow(),
                "IMDb Clone - Home");
        } catch (Exception e) {
            logger.error("Error navigating to home view", e);
            UIUtils.showError("Navigation Error", "Failed to navigate to home view: " + e.getMessage());
        }
    }
    @FXML private TextField seriesSearchField;
    @FXML private ComboBox<String> seriesSortBy;

    // Data
    private final ObservableList<Series> allSeries = FXCollections.observableArrayList();
    private final ObservableList<Series> filteredSeries = FXCollections.observableArrayList();
    private final ObjectProperty<Series> selectedSeries = new SimpleObjectProperty<>();
    private SeriesService seriesService;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // Required by Initializable
    }

    @Override
    public void initializeController(int currentUserId) throws Exception {
        // Initialize series service if not already set
        if (seriesService == null) {
            seriesService = SeriesService.getInstance();
        }
        
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
    }
    
    // Setter for series service (for dependency injection)
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
        seriesRatingColumn.setCellValueFactory(new PropertyValueFactory<>("imdbRating"));
        seriesRatingColumn.setCellFactory(col -> new TableCell<Series, Double>() {
            @Override
            protected void updateItem(Double item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText("");
                } else {
                    setText(String.format("%.1f", item));
                }
                setStyle("-fx-alignment: CENTER; -fx-padding: 5; -fx-font-weight: bold; -fx-text-fill: #FFD700;");
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
        seriesSortBy.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) {
                sortSeriesTable(newVal);
            }
        });
    }

    private void loadSeries() {
        if (seriesService == null) {
            logger.error("SeriesService is not initialized");
            Platform.runLater(() -> 
                statusLabel.setText("Error: Series service not initialized")
            );
            return;
        }

        try {
            List<Series> seriesList = seriesService.getAll();
            Platform.runLater(() -> {
                allSeries.setAll(seriesList);
                filterSeries();
                statusLabel.setText(String.format("Loaded %d series", seriesList.size()));
                logger.info("Successfully loaded {} series", seriesList.size());
            });
        } catch (Exception e) {
            logger.error("Error loading series", e);
            showError("Error", "Failed to load series: " + e.getMessage());
        }
    }
    
    private void deleteSeries(Series series) {
        try {
            if (showConfirmationDialog("Are you sure you want to delete this series?")) {
                seriesService.delete(series.getId());
                loadSeries();
                showSuccess("Success", "Series deleted successfully.");
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
        seriesTable.setItems(filteredSeries);
    }

    private void sortSeriesTable(String sortOption) {
        // Implementation of sorting logic
        // This is a placeholder - implement actual sorting based on sortOption
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
    private void handleDeleteSeries(ActionEvent event) {
        Series selected = seriesTable.getSelectionModel().getSelectedItem();
        if (selected != null) {
            boolean confirm = showConfirmationDialog("Are you sure you want to delete '" + selected.getTitle() + "'?");
            if (confirm) {
                try {
                    boolean deleted = dataManager.getSeriesRepository().deleteById(selected.getId());
                    if (deleted) {
                        loadSeries();
                        showSuccess("Success", "Series deleted successfully.");
                    } else {
                        showError("Error", "Failed to delete series: Series not found");
                    }
                } catch (Exception e) {
                    logger.error("Error deleting series", e);
                    showError("Error", "Failed to delete series: " + e.getMessage());
                }
            }
        } else {
            showAlert("No Selection", "Please select a series to delete.");
        }
    }

    @FXML
    private void handleRateSeries(ActionEvent event) {
        Series selected = seriesTable.getSelectionModel().getSelectedItem();
        if (selected != null) {
            showRatingDialog(selected);
        } else {
            showAlert("No Selection", "Please select a series to rate.");
        }
    }

    @FXML
    private void handleManageSeasons(ActionEvent event) {
        Series selected = seriesTable.getSelectionModel().getSelectedItem();
        if (selected != null) {
            showSeasonManagementDialog(selected);
        } else {
            showAlert("No Selection", "Please select a series to manage seasons.");
        }
    }

    private boolean showSeriesEditDialog(Series series) {
        try {
            // Create a custom dialog
            Dialog<ButtonType> dialog = new Dialog<>();
            dialog.setTitle("New Series".equals(series.getTitle()) ? "Add New Series" : "Edit Series");
            dialog.setHeaderText("Enter series details:");
            
            // Set the button types
            ButtonType saveButtonType = new ButtonType("Save", ButtonBar.ButtonData.OK_DONE);
            dialog.getDialogPane().getButtonTypes().addAll(saveButtonType, ButtonType.CANCEL);
            
            // Create the form grid
            GridPane grid = new GridPane();
            grid.setHgap(10);
            grid.setVgap(10);
            grid.setPadding(new Insets(20, 150, 10, 10));
            
            // Create form fields
            TextField titleField = new TextField(series.getTitle());
            TextField yearField = new TextField(String.valueOf(Calendar.getInstance().get(Calendar.YEAR)));
            TextField directorField = new TextField(series.getDirector() != null ? series.getDirector() : "");
            
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
                // Update the series with the form data
                series.setTitle(titleField.getText().trim());
                try {
                    int year = Integer.parseInt(yearField.getText().trim());
                    Calendar cal = Calendar.getInstance();
                    cal.set(Calendar.YEAR, year);
                    series.setYear(cal.getTime());
                } catch (NumberFormatException e) {
                    showError("Invalid Year", "Please enter a valid year number.");
                    return false;
                }
                series.setDirector(directorField.getText().trim());
                return true;
            }
            return false;
        } catch (Exception e) {
            logger.error("Error showing series edit dialog", e);
            showError("Error", "Failed to show series editor: " + e.getMessage());
            return false;
        }
    }

    private void showRatingDialog(Series series) {
        // Implementation of rating dialog for series
        // This will be moved from ContentController
    }

    private void showSeasonManagementDialog(Series series) {
        // Implementation of season management dialog
        // This will be moved from ContentController
    }
}
