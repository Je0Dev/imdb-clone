package com.papel.imdb_clone.controllers;

import com.papel.imdb_clone.data.RefactoredDataManager;
import com.papel.imdb_clone.enums.Ethnicity;
import com.papel.imdb_clone.model.Actor;
import com.papel.imdb_clone.model.Director;
import com.papel.imdb_clone.service.CelebrityService;
import com.papel.imdb_clone.services.NavigationService;
import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.MouseEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;

public class CelebritiesController implements Initializable {
    private static final Logger logger = LoggerFactory.getLogger(CelebritiesController.class);

    // Actor UI Components
    @FXML
    private TableView<Actor> actorsTable;
    @FXML
    private TableColumn<Actor, String> actorNameColumn;
    @FXML
    private TableColumn<Actor, String> actorBirthDateColumn;
    @FXML
    private TableColumn<Actor, String> actorGenderColumn;
    @FXML
    private TableColumn<Actor, String> actorNationalityColumn;
    @FXML
    private TableColumn<Actor, String> actorNotableWorksColumn;
    @FXML
    private TextField actorSearchField;
    @FXML
    private Label statusLabel;

    // Director UI Components
    @FXML
    private TableView<Director> directorsTable;
    @FXML
    private TableColumn<Director, String> directorNameColumn;
    @FXML
    private TableColumn<Director, String> directorBirthDateColumn;
    @FXML
    private TableColumn<Director, String> directorGenderColumn;
    @FXML
    private TableColumn<Director, String> directorNationalityColumn;
    @FXML
    private TableColumn<Director, String> directorNotableWorksColumn;
    @FXML
    private TextField directorSearchField;
    
    @FXML
    private TextField unifiedSearchField;

    // Data
    private final ObservableList<Actor> actors = FXCollections.observableArrayList();
    private final ObservableList<Director> directors = FXCollections.observableArrayList();
    private final FilteredList<Actor> filteredActors = new FilteredList<>(actors);
    private final FilteredList<Director> filteredDirectors = new FilteredList<>(directors);

    // Services
    private CelebrityService<Actor> actorService;
    private CelebrityService<Director> directorService;
    private RefactoredDataManager dataManager;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        try {
            // Initialize services
            dataManager = RefactoredDataManager.getInstance();
            actorService = dataManager.getActorService();
            directorService = dataManager.getDirectorService();

            // Initialize actor table
            initializeActorTable();

            // Initialize director table
            initializeDirectorTable();
            
            // Initialize unified search
            initializeUnifiedSearch();

            // Load initial data
            loadCelebrities();

        } catch (Exception e) {
            logger.error("Error initializing CelebritiesController: {}", e.getMessage(), e);
            showError("Initialization Error", "Failed to initialize the celebrities view: " + e.getMessage());
        }
    }

    private void initializeActorTable() {
        // Set up cell value factories
        actorNameColumn.setCellValueFactory(cellData ->
                new javafx.beans.property.SimpleStringProperty(
                        cellData.getValue().getFirstName() + " " + cellData.getValue().getLastName()
                )
        );
        actorBirthDateColumn.setCellValueFactory(new PropertyValueFactory<>("birthDate"));
        actorGenderColumn.setCellValueFactory(new PropertyValueFactory<>("gender"));
        actorNationalityColumn.setCellValueFactory(cellData -> {
            Ethnicity ethnicity = cellData.getValue().getEthnicity();
            return new SimpleStringProperty(ethnicity != null ? ethnicity.getLabel() : "");
        });
        actorNotableWorksColumn.setCellValueFactory(cellData ->
                new javafx.beans.property.SimpleStringProperty(
                        String.join(", ", cellData.getValue().getNotableWorks())
                )
        );

        // Set up search functionality
        actorSearchField.textProperty().addListener((observable, oldValue, newValue) -> {
            filterActors(newValue);
        });

        // Set the filtered list to the table
        actorsTable.setItems(filteredActors);
    }

    private void initializeDirectorTable() {
        // Set up cell value factories
        directorNameColumn.setCellValueFactory(cellData ->
                new javafx.beans.property.SimpleStringProperty(
                        cellData.getValue().getFirstName() + " " + cellData.getValue().getLastName()
                )
        );
        directorBirthDateColumn.setCellValueFactory(new PropertyValueFactory<>("birthDate"));
        directorGenderColumn.setCellValueFactory(new PropertyValueFactory<>("gender"));
        directorNationalityColumn.setCellValueFactory(cellData ->
                new SimpleStringProperty(cellData.getValue().getNationality())
        );
        directorNotableWorksColumn.setCellValueFactory(cellData ->
                new javafx.beans.property.SimpleStringProperty(
                        String.join(", ", cellData.getValue().getNotableWorks())
                )
        );

        // Set up search functionality
        directorSearchField.textProperty().addListener((observable, oldValue, newValue) -> {
            filterDirectors(newValue);
        });

        // Set the filtered list to the table
        directorsTable.setItems(filteredDirectors);
    }

    private void loadCelebrities() {
        try {
            // Load actors
            List<Actor> actorList = actorService.getAll();
            actors.setAll(actorList);
            filteredActors.setPredicate(actor -> true);

            // Load directors
            List<Director> directorList = directorService.getAll();
            directors.setAll(directorList);
            filteredDirectors.setPredicate(director -> true);

            updateStatus("Loaded " + actorList.size() + " actors and " + directorList.size() + " directors");
        } catch (Exception e) {
            logger.error("Error loading celebrities: {}", e.getMessage(), e);
            showError("Error", "Failed to load celebrities: " + e.getMessage());
        }
    }


    /**
     * Filters the actors based on the search text
     * @param searchText The text to search for in actor names, ethnicities, and notable works
     */
    private void filterActors(String searchText) {
        if (searchText == null || searchText.isEmpty()) {
            filteredActors.setPredicate(actor -> true);
        } else {
            String lowerCaseFilter = searchText.toLowerCase();
            filteredActors.setPredicate(actor -> {
                String fullName = (actor.getFirstName() + " " + actor.getLastName()).toLowerCase();
                return fullName.contains(lowerCaseFilter) ||
                       (actor.getEthnicity() != null && actor.getEthnicity().getLabel().toLowerCase().contains(lowerCaseFilter)) ||
                       (actor.getNotableWorks() != null &&
                               actor.getNotableWorks().stream()
                                       .anyMatch(work -> work != null && work.toLowerCase().contains(lowerCaseFilter)));
            });
        }
    }

    /**
     * Filters the directors based on the search text
     * @param searchText The text to search for in director names, nationalities, and notable works
     */
    private void filterDirectors(String searchText) {
        if (searchText == null || searchText.isEmpty()) {
            filteredDirectors.setPredicate(director -> true);
        } else {
            String lowerCaseFilter = searchText.toLowerCase();
            filteredDirectors.setPredicate(director -> {
                String fullName = (director.getFirstName() + " " + director.getLastName()).toLowerCase();
                return fullName.contains(lowerCaseFilter) ||
                        (director.getNationality() != null && director.getNationality().toLowerCase().contains(lowerCaseFilter)) ||
                        (director.getNotableWorks() != null &&
                                director.getNotableWorks().stream()
                                        .anyMatch(work -> work != null && work.toLowerCase().contains(lowerCaseFilter)));
            });
        }
    }
    
    /**
     * Initializes the unified search functionality that searches both actors and directors
     */
    private void initializeUnifiedSearch() {
        unifiedSearchField.textProperty().addListener((observable, oldValue, newValue) -> {
            // Update both actor and director filters
            filterActors(newValue);
            filterDirectors(newValue);
            
            // Update the tab-specific search fields to keep them in sync
            if (!newValue.equals(actorSearchField.getText())) {
                actorSearchField.setText(newValue);
            }
            if (!newValue.equals(directorSearchField.getText())) {
                directorSearchField.setText(newValue);
            }
        });
        
        // Update unified search when tab-specific searches change
        actorSearchField.textProperty().addListener((observable, oldValue, newValue) -> {
            if (!newValue.equals(unifiedSearchField.getText())) {
                unifiedSearchField.setText(newValue);
            }
        });
        
        directorSearchField.textProperty().addListener((observable, oldValue, newValue) -> {
            if (!newValue.equals(unifiedSearchField.getText())) {
                unifiedSearchField.setText(newValue);
            }
        });
    }

    private void updateStatus(String message) {
        if (statusLabel != null) {
            statusLabel.setText(message);
        }
    }

    private void showError(String title, String message) {
        Platform.runLater(() -> {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle(title);
            alert.setHeaderText(null);
            alert.setContentText(message);
            alert.showAndWait();
        });
    }

    public void goToHome(MouseEvent mouseEvent) {
        NavigationService.getInstance().showHome();
    }
}
