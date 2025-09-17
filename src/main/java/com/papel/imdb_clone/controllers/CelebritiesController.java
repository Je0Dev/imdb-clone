package com.papel.imdb_clone.controllers;

import com.papel.imdb_clone.data.RefactoredDataManager;
import com.papel.imdb_clone.enums.Ethnicity;
import com.papel.imdb_clone.model.Actor;
import com.papel.imdb_clone.model.Director;
import com.papel.imdb_clone.service.CelebrityService;
import com.papel.imdb_clone.service.NavigationService;
import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.input.MouseEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URL;
import java.util.*;
import java.util.stream.Collectors;

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

    //initialize the controller
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
            // Log error and show error message
            logger.error("Error initializing CelebritiesController: {}", e.getMessage(), e);
            showError("Initialization Error", "Failed to initialize the celebrities view: " + e.getMessage());
        }
    }

    private void initializeActorTable() {
        try {
            // Set up cell value factory for actor name
            actorNameColumn.setCellValueFactory(cellData -> {
                try {
                    Actor actor = cellData.getValue();
                    if (actor != null) {
                        String firstName = actor.getFirstName() != null ? actor.getFirstName() : "";
                        String lastName = actor.getLastName() != null ? actor.getLastName() : "";
                        return new SimpleStringProperty(String.format("%s %s", firstName, lastName).trim());
                    }
                } catch (Exception e) {
                    logger.error("Error getting actor name: {}", e.getMessage());
                }
                return new SimpleStringProperty("N/A");
            });

            // Set up cell value factory for birth date
            actorBirthDateColumn.setCellValueFactory(cellData -> {
                try {
                    if (cellData.getValue() != null && cellData.getValue().getBirthDate() != null) {
                        return new SimpleStringProperty(cellData.getValue().getBirthDate().toString());
                    }
                } catch (Exception e) {
                    logger.error("Error getting birth date: {}", e.getMessage());
                }
                return new SimpleStringProperty("N/A");
            });

            // Simplified gender column
            actorGenderColumn.setCellValueFactory(cellData -> {
                try {
                    if (cellData.getValue() != null) {
                        char gender = cellData.getValue().getGender();
                        switch (gender) {
                            case 'M': return new SimpleStringProperty("Male");
                            case 'F': return new SimpleStringProperty("Female");
                            default: return new SimpleStringProperty("Other");
                        }
                    }
                } catch (Exception e) {
                    logger.error("Error getting gender: {}", e.getMessage());
                }
                return new SimpleStringProperty("N/A");
            });

            // Simplified ethnicity column
            actorNationalityColumn.setCellValueFactory(cellData -> {
                try {
                    if (cellData.getValue() != null && cellData.getValue().getEthnicity() != null) {
                        return new SimpleStringProperty(cellData.getValue().getEthnicity().getLabel());
                    }
                } catch (Exception e) {
                    logger.error("Error getting ethnicity: {}", e.getMessage());
                }
                return new SimpleStringProperty("N/A");
            });

            // Notable works column
            actorNotableWorksColumn.setCellValueFactory(cellData -> {
                try {
                    Actor actor = cellData.getValue();
                    if (actor != null) {
                        List<String> notableWorks = actor.getNotableWorks();
                        if (notableWorks != null && !notableWorks.isEmpty()) {
                            // Format the notable works as a comma-separated list
                            String worksText = String.join(", ", notableWorks);
                            logger.debug("Notable works for {} {}: {}", 
                                actor.getFirstName(), actor.getLastName(), worksText);
                            return new SimpleStringProperty(worksText);
                        } else {
                            logger.debug("No notable works found for {} {}", 
                                actor.getFirstName(), actor.getLastName());
                        }
                    }
                } catch (Exception e) {
                    logger.error("Error getting notable works: {}", e.getMessage(), e);
                }
                return new SimpleStringProperty("No notable works");
            });

            // Set the items to the table
            actorsTable.setItems(filteredActors);
            
        } catch (Exception e) {
            logger.error("Error initializing actor table: {}", e.getMessage(), e);
            showError("Initialization Error", "Failed to initialize actor table: " + e.getMessage());
        }
    }

    private void initializeDirectorTable() {
        // Set up cell value factories
        directorNameColumn.setCellValueFactory(cellData ->
                new SimpleStringProperty(
                        String.format("%s %s",
                                cellData.getValue().getFirstName() != null ? cellData.getValue().getFirstName() : "",
                                cellData.getValue().getLastName() != null ? cellData.getValue().getLastName() : ""
                        ).trim()
                )
        );

        // Set up cell value factory for birth date
        directorBirthDateColumn.setCellValueFactory(cellData ->
                new SimpleStringProperty(
                        cellData.getValue().getBirthDate() != null ?
                                cellData.getValue().getBirthDate().toString() : "N/A"
                )
        );

        // Set up cell value factory for gender
        directorGenderColumn.setCellValueFactory(cellData -> {
            try {
                Director director = cellData.getValue();
                if (director != null) {
                    try {
                        // First try the standard getter
                        Object gender = director.getGender();
                        {
                            String genderStr = String.valueOf(gender);
                            if (!genderStr.trim().isEmpty() && !"null".equalsIgnoreCase(genderStr)) {
                                // Capitalize first letter
                                return new SimpleStringProperty(
                                    genderStr.substring(0, 1).toUpperCase() + 
                                    genderStr.substring(1).toLowerCase()
                                );
                            }
                        }
                        
                        // Try reflection as fallback
                        try {
                            java.lang.reflect.Field genderField = director.getClass().getDeclaredField("gender");
                            genderField.setAccessible(true);
                            Object genderValue = genderField.get(director);
                            if (genderValue != null) {
                                String genderStr = String.valueOf(genderValue);
                                if (!genderStr.trim().isEmpty() && !"null".equalsIgnoreCase(genderStr)) {
                                    return new SimpleStringProperty(
                                        genderStr.substring(0, 1).toUpperCase() + 
                                        genderStr.substring(1).toLowerCase()
                                    );
                                }
                            }
                        } catch (Exception e) {
                            logger.debug("Could not get gender through reflection: {}", e.getMessage());
                        }
                    } catch (Exception e) {
                        logger.debug("Error getting gender: {}", e.getMessage());
                    }
                }
                return new SimpleStringProperty("N/A");
            } catch (Exception e) {
                // Log error and return "N/A"
                logger.error("Unexpected error getting director gender: {}", e.getMessage(), e);
                return new SimpleStringProperty("N/A");
            }
        });

        // Set up cell value factory for nationality/ethnicity
        directorNationalityColumn.setCellValueFactory(cellData -> {
            try {
                Director director = cellData.getValue();
                if (director != null) {
                    // Get the director's ethnicity
                    Ethnicity ethnicity = director.getEthnicity();
                    if (ethnicity != null && ethnicity.getLabel() != null && !ethnicity.getLabel().trim().isEmpty()) {
                        return new SimpleStringProperty(ethnicity.getLabel());
                    }

                    // Try to get nationality from other fields if ethnicity is not set
                    if (director.getNationality() != null && !director.getNationality().trim().isEmpty()) {
                        return new SimpleStringProperty(director.getNationality());
                    }

                }
                return new SimpleStringProperty("N/A");
            } catch (Exception e) {
                logger.debug("Error getting director nationality: {}", e.getMessage());
                return new SimpleStringProperty("N/A");
            }
        });

        // Set up cell value factory for notable works
        directorNotableWorksColumn.setCellValueFactory(cellData -> {
            try {
                Director director = cellData.getValue();
                if (director != null) {
                    // First try the standard method if it exists
                    try {
                        if (director.getClass().getMethod("getNotableWorks") != null) {
                            List<String> works = director.getNotableWorks();
                            // Check if the list is not null and not empty
                            if (works != null && !works.isEmpty()) {
                                // Join the list into a string,which is separated by commas
                                String worksText = works.stream()
                                        .filter(work -> work != null && !work.trim().isEmpty())
                                        .collect(Collectors.joining(", "));
                                return new SimpleStringProperty(worksText.isEmpty() ? "No notable works" : worksText);
                            }
                        }
                    } catch (NoSuchMethodException e) {
                        // Method doesn't exist, try reflection
                        try {
                            java.lang.reflect.Field worksField = director.getClass().getDeclaredField("notableWorks");
                            worksField.setAccessible(true);
                            Object works = worksField.get(director);

                            // Check if the object is a list
                            if (works instanceof List<?> worksList) {
                                if (!worksList.isEmpty()) {
                                    // Join the list into a string,which is separated by commas
                                    String worksText = worksList.stream()
                                            .map(Object::toString)
                                            .filter(work -> work != null && !work.trim().isEmpty())
                                            .collect(Collectors.joining(", "));
                                    return new SimpleStringProperty(worksText.isEmpty() ? "No notable works" : worksText);
                                }
                                // If the list is empty, return "No notable works"
                            } else if (works instanceof String) {
                                String worksStr = ((String) works).trim();
                                return new SimpleStringProperty(worksStr.isEmpty() ? "No notable works" : worksStr);
                            }
                        } catch (Exception ex) {
                            logger.debug("Could not get notable works through reflection: {}", ex.getMessage());
                        }
                    }
                }
                // If the director is null or the notable works are not available, return "Not available"
                return new SimpleStringProperty("No notable works");
            } catch (Exception e) {
                logger.debug("Unexpected error getting notable works: {}", e.getMessage());
                return new SimpleStringProperty("Not available");
            }
        });

        // Set up search functionality
        directorSearchField.textProperty().addListener((observable, oldValue, newValue) -> {
            filterDirectors(newValue);
        });

        // Set the filtered list to the table
        directorsTable.setItems(filteredDirectors);
    }

    private void loadCelebrities() {
        logger.info("Starting to load celebrities...");
        try {
            // Validate services
            if (actorService == null || directorService == null) {
                String errorMsg = "Actor or Director service is not initialized. ActorService: " + 
                    (actorService == null ? "null" : "initialized") + 
                    ", DirectorService: " + (directorService == null ? "null" : "initialized");
                logger.error(errorMsg);
                throw new IllegalStateException(errorMsg);
            }

            // Load actors with error handling
            List<Actor> actorList = new ArrayList<>();
            try {
                logger.debug("Fetching actors from actorService...");
                actorList = actorService.getAll();
                if (actorList == null) {
                    logger.warn("Actor service returned null list");
                    actorList = Collections.emptyList();
                } else {
                    logger.debug("Successfully loaded {} actors", actorList.size());
                    // Log first few actors for debugging
                    int maxActorsToLog = Math.min(3, actorList.size());
                    for (int i = 0; i < maxActorsToLog; i++) {
                        Actor actor = actorList.get(i);
                        logger.debug("Actor[{}]: {} {}, Notable Works: {}", 
                            i, 
                            actor.getFirstName(), 
                            actor.getLastName(),
                            actor.getNotableWorks() != null ? actor.getNotableWorks().toString() : "null");
                    }
                }
            } catch (Exception e) {
                logger.error("Error loading actors: {}", e.getMessage(), e);
                showError("Error", "Failed to load actors: " + e.getMessage());
            }

            // Load directors with error handling
            List<Director> directorList = new ArrayList<>();
            try {
                logger.debug("Fetching directors from directorService...");
                directorList = directorService.getAll();
                if (directorList == null) {
                    logger.warn("Director service returned null list");
                    directorList = Collections.emptyList();
                } else {
                    logger.debug("Successfully loaded {} directors", directorList.size());
                    // Log first few directors for debugging
                    int maxDirectorsToLog = Math.min(3, directorList.size());
                    for (int i = 0; i < maxDirectorsToLog; i++) {
                        Director director = directorList.get(i);
                        logger.debug("Director[{}]: {} {}, Notable Works: {}", 
                            i,
                            director.getFirstName(),
                            director.getLastName(),
                            director.getNotableWorks() != null ? director.getNotableWorks().toString() : "null");
                    }
                }
            } catch (Exception e) {
                logger.error("Error loading directors: {}", e.getMessage(), e);
                showError("Error", "Failed to load directors: " + e.getMessage());
            }

            // Update UI on the JavaFX Application Thread
            List<Actor> finalActorList = actorList;
            List<Director> finalDirectorList = directorList;
            Platform.runLater(() -> {
                try {
                    logger.debug("Updating UI with {} actors and {} directors", 
                        finalActorList.size(), finalDirectorList.size());
                    
                    actors.setAll(finalActorList);
                    directors.setAll(finalDirectorList);
                    
                    // Verify the data was set correctly
                    logger.debug("After setting, actors list has {} items, directors list has {} items", 
                        actors.size(), directors.size());
                    
                    // Reset filters
                    filteredActors.setPredicate(actor -> true);
                    filteredDirectors.setPredicate(director -> true);
                    
                    updateStatus("Loaded " + actors.size() + " actors and " + directors.size() + " directors");
                    
                    // Log table items count after update
                    logger.debug("Table items - Actors: {}, Directors: {}", 
                        actorsTable.getItems().size(), 
                        directorsTable.getItems().size());
                        
                } catch (Exception e) {
                    logger.error("Error updating UI: {}", e.getMessage(), e);
                    showError("Error", "Failed to update UI: " + e.getMessage());
                }
            });

        } catch (Exception e) {
            logger.error("Unexpected error in loadCelebrities: {}", e.getMessage(), e);
            showError("Error", "An unexpected error occurred: " + e.getMessage());
        } finally {
            logger.info("Finished loading celebrities");
        }
    }

    /**
     * Filters the actors based on the search text
     *
     * @param searchText The text to search for in actor names, ethnicities, and notable works
     */
    private void filterActors(String searchText) {
        if (searchText == null || searchText.isEmpty()) {
            // If the search text is empty, show all actors
            filteredActors.setPredicate(actor -> true);
        } else {
            // Convert the search text to lower case
            String lowerCaseFilter = searchText.toLowerCase();
            filteredActors.setPredicate(actor -> {
                if (actor == null) return false;

                // Check name
                String firstName = actor.getFirstName() != null ? actor.getFirstName().toLowerCase() : "";
                String lastName = actor.getLastName() != null ? actor.getLastName().toLowerCase() : "";
                String fullName = (firstName + " " + lastName).trim();

                // Check ethnicity
                String ethnicity = actor.getEthnicity() != null ?
                        actor.getEthnicity().getLabel().toLowerCase() : "";

                // Check notable works if available
                boolean worksMatch = false;
                try {
                    List<String> works = actor.getNotableWorks();
                    if (works != null) {
                        // Check if the list contains the search text
                        worksMatch = works.stream()
                                .filter(Objects::nonNull)
                                .anyMatch(work -> work.toLowerCase().contains(lowerCaseFilter));
                    }
                } catch (Exception e) {
                    logger.debug("Error checking notable works for actor: {}", e.getMessage());
                }

                // Return true if any of the conditions are met
                return fullName.contains(lowerCaseFilter) ||
                        ethnicity.contains(lowerCaseFilter) ||
                        worksMatch;
            });
        }
    }

    /**
     * Filters the directors based on the search text
     *
     * @param searchText The text to search for in director names, nationalities, and notable works
     */
    private void filterDirectors(String searchText) {
        if (searchText == null || searchText.isEmpty()) {
            // If the search text is empty, show all directors
            filteredDirectors.setPredicate(director -> true);
        } else {
            String lowerCaseFilter = searchText.toLowerCase();
            // Set the predicate  for the filtered directors
            filteredDirectors.setPredicate(director -> {
                if (director == null) return false;

                // Check name
                String firstName = director.getFirstName() != null ? director.getFirstName().toLowerCase() : "";
                String lastName = director.getLastName() != null ? director.getLastName().toLowerCase() : "";
                String fullName = (firstName + " " + lastName).trim();

                // Check ethnicity as nationality
                String nationality = "";
                try {
                    // Get the nationality of the director
                    Ethnicity ethnicity = director.getEthnicity();
                    nationality = ethnicity != null ? ethnicity.getLabel().toLowerCase() : "";
                } catch (Exception e) {
                    logger.debug("Error getting director nationality: {}", e.getMessage());
                }

                // Check notable works using reflection
                boolean worksMatch = false;
                try {
                    // Get the notable works of the director
                    java.lang.reflect.Field field = director.getClass().getDeclaredField("notableWorks");
                    field.setAccessible(true);
                    String notableWorks = (String) field.get(director);
                    if (notableWorks != null) {
                        // Check if the notable works contain the search text
                        worksMatch = notableWorks.toLowerCase().contains(lowerCaseFilter);
                    }
                } catch (Exception e) {
                    logger.debug("Error checking notable works for director: {}", e.getMessage());
                }

                // Return true if any of the conditions are met
                return fullName.contains(lowerCaseFilter) ||
                        nationality.contains(lowerCaseFilter) ||
                        worksMatch;
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

    //update the status label
    private void updateStatus(String message) {
        if (statusLabel != null) {
            statusLabel.setText(message);
        }
    }
    //show error message
    private void showError(String title, String message) {
        Platform.runLater(() -> {
            //show error message
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle(title);
            alert.setHeaderText(null);
            alert.setContentText(message);
            alert.showAndWait();
        });
    }

    //go to homes
    public void goToHome(MouseEvent mouseEvent) {
        NavigationService.getInstance().showHome();
    }
}
