package com.papel.imdb_clone.controllers.people;

import com.papel.imdb_clone.data.DataManager;
import com.papel.imdb_clone.enums.Ethnicity;
import com.papel.imdb_clone.model.people.Actor;
import com.papel.imdb_clone.model.people.Director;
import com.papel.imdb_clone.service.people.CelebrityService;
import com.papel.imdb_clone.service.navigation.NavigationService;
import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.GridPane;
import javafx.geometry.Insets;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.Optional;
import java.util.List;
import javafx.stage.Stage;
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
    private Map<String, Object> data;

    //initialize the controller
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        try {
            // Initialize services
            DataManager dataManager = DataManager.getInstance();
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
                        logger.debug("Actor name for actor {} {}: {}",
                            actor.getFirstName(), actor.getLastName(),
                            String.format("%s %s", firstName, lastName).trim());
                        return new SimpleStringProperty(String.format("%s %s", firstName, lastName).trim());
                    }
                } catch (Exception e) {
                    logger.error("Error getting actor name: {}", e.getMessage());
                }
                return new SimpleStringProperty("N/A");
            });

            // Set up cell value factory for birth date with proper formatting
            actorBirthDateColumn.setCellValueFactory(cellData -> {
                try {
                    if (cellData.getValue() != null && cellData.getValue().getBirthDate() != null) {
                        // Format the date as yyyy-MM-dd for consistency
                        String birthDate = cellData.getValue().getBirthDate().toString();
                        logger.debug("Birth date for actor {} {}: {}",
                            cellData.getValue().getFirstName(), cellData.getValue().getLastName(),
                            cellData.getValue().getBirthDate().toString());
                        return new SimpleStringProperty(cellData.getValue().getBirthDate().toString());
                    }
                } catch (Exception e) {
                    logger.error("Error getting birth date: {}", e.getMessage());
                }
                return new SimpleStringProperty("N/A");
            });

            // Gender column with proper handling
            actorGenderColumn.setCellValueFactory(cellData -> {
                try {
                    if (cellData.getValue() != null) {
                        char gender = cellData.getValue().getGender();
                        if (gender == 'M' || gender == 'm') {
                            return new SimpleStringProperty("Male");
                        } else if (gender == 'F' || gender == 'f') {
                            return new SimpleStringProperty("Female");
                        } else {
                            return new SimpleStringProperty("Other");
                        }
                    }
                } catch (Exception e) {
                    logger.error("Error getting gender: {}", e.getMessage());
                }
                // Log error and show error message
                return new SimpleStringProperty("N/A");
            });

            // Nationality/Ethnicity column with proper handling
            actorNationalityColumn.setCellValueFactory(cellData -> {
                try {
                    Actor actor = cellData.getValue();
                    if (actor != null) {
                        Ethnicity ethnicity = actor.getEthnicity();
                        if (ethnicity != null) {
                            return new SimpleStringProperty(ethnicity.getLabel());
                        } else if (actor.getNationality() != null) {
                            return new SimpleStringProperty(actor.getNationality().getLabel());
                        }
                    }
                } catch (Exception e) {
                    logger.error("Error getting nationality/ethnicity: {}", e.getMessage());
                }
                return new SimpleStringProperty("N/A");
            });

            // Notable works column with improved formatting and null safety
            actorNotableWorksColumn.setCellValueFactory(cellData -> {
                try {
                    Actor actor = cellData.getValue();
                    if (actor != null) {
                        try {
                            List<String> notableWorks = actor.getNotableWorks();
                            if (notableWorks != null && !notableWorks.isEmpty()) {
                                // Filter out any null or empty strings from notable works
                                List<String> validWorks = notableWorks.stream()
                                    .filter(work -> work != null && !work.trim().isEmpty())
                                    .collect(Collectors.toList());
                                
                                if (!validWorks.isEmpty()) {
                                    // Format the notable works as a comma-separated list, limit to 3 items for display
                                    int maxWorks = Math.min(3, validWorks.size());
                                    String worksText = String.join(", ", validWorks.subList(0, maxWorks));
                                    if (validWorks.size() > 3) {
                                        worksText += "...";
                                    }
                                    logger.debug("Notable works for {} {}: {}", 
                                        actor.getFirstName(), actor.getLastName(), worksText);
                                    return new SimpleStringProperty(worksText);
                                }
                            }
                            logger.debug("No valid notable works found for {} {}", 
                                actor.getFirstName(), actor.getLastName());
                        } catch (Exception e) {
                            logger.warn("Error processing notable works for {} {}: {}", 
                                actor.getFirstName(), actor.getLastName(), e.getMessage());
                        }
                    }
                } catch (Exception e) {
                    logger.error("Unexpected error getting actor notable works: {}", e.getMessage(), e);
                }
                return new SimpleStringProperty("No notable works");
            });

            // Set the items to the table
            actorsTable.setItems(filteredActors);
            
            // Enable sorting on all columns
            actorNameColumn.setSortable(true);
            actorBirthDateColumn.setSortable(true);
            actorGenderColumn.setSortable(true);
            actorNationalityColumn.setSortable(true);
            actorNotableWorksColumn.setSortable(true);
            
        } catch (Exception e) {
            logger.error("Error initializing actor table: {}", e.getMessage(), e);
            showError("Initialization Error", "Failed to initialize actor table: " + e.getMessage());
        }
    }

    @FXML
    private void handleGoToHome() {
        try {
            NavigationService.getInstance().navigateTo("/fxml/base/home-view.fxml",
                    data, (Stage) actorsTable.getScene().getWindow(),"IMDb Clone - Home");
        } catch (Exception e) {
            logger.error("Error navigating to home: {}", e.getMessage(), e);
            showError("Navigation Error", "Failed to navigate to home: " + e.getMessage());
        }
    }
    
    /**
     * Handles the refresh button action to reload all celebrities.
     */
    @FXML
    private void handleRefresh() {
        try {
            logger.info("Refreshing celebrities...");
            // Clear search fields
            if (actorSearchField != null) actorSearchField.clear();
            if (directorSearchField != null) directorSearchField.clear();
            if (unifiedSearchField != null) unifiedSearchField.clear();
            
            // Reload all celebrities
            loadCelebrities();
            
            // Show success message
            if (statusLabel != null) {
                statusLabel.setText("Celebrities refreshed successfully");
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
            logger.error("Error refreshing celebrities: {}", e.getMessage(), e);
            showError("Refresh Error", "Failed to refresh celebrities: " + e.getMessage());
        }
    }

    private void initializeDirectorTable() {
        // Set up cell value factories
        directorNameColumn.setCellValueFactory(cellData -> {
            try {
                Director director = cellData.getValue();
                if (director != null) {
                    String firstName = director.getFirstName() != null ? director.getFirstName() : "";
                    String lastName = director.getLastName() != null ? director.getLastName() : "";
                    return new SimpleStringProperty(String.format("%s %s", firstName, lastName).trim());
                }
            } catch (Exception e) {
                logger.error("Error getting director name: {}", e.getMessage());
            }
            return new SimpleStringProperty("N/A");
        });

        // Set up cell value factory for birth date with proper formatting
        directorBirthDateColumn.setCellValueFactory(cellData -> {
            try {
                if (cellData.getValue() != null && cellData.getValue().getBirthDate() != null) {
                    return new SimpleStringProperty(cellData.getValue().getBirthDate().toString());
                }
            } catch (Exception e) {
                logger.error("Error getting director birth date: {}", e.getMessage());
            }
            return new SimpleStringProperty("N/A");
        });

        // Set up cell value factory for gender with consistent handling
        directorGenderColumn.setCellValueFactory(cellData -> {
            try {
                Director director = cellData.getValue();
                if (director != null) {
                    try {
                        char gender = director.getGender();
                        if (gender == 'M' || gender == 'm') {
                            return new SimpleStringProperty("Male");
                        } else if (gender == 'F' || gender == 'f') {
                            return new SimpleStringProperty("Female");
                        } else {
                            return new SimpleStringProperty("Other");
                        }
                    } catch (Exception e) {
                        logger.debug("Error getting director gender: {}", e.getMessage());
                    }
                }
            } catch (Exception e) {
                logger.error("Unexpected error getting director gender: {}", e.getMessage(), e);
            }
            return new SimpleStringProperty("N/A");
        });

        // Set up cell value factory for nationality/ethnicity
        directorNationalityColumn.setCellValueFactory(cellData -> {
            try {
                Director director = cellData.getValue();
                if (director != null) {
                    // Get the director's ethnicity
                    Ethnicity ethnicity = director.getEthnicity();
                    if (ethnicity != null) {
                        String label = ethnicity.getLabel();
                        if (label != null && !label.trim().isEmpty()) {
                            return new SimpleStringProperty(label);
                        }
                    }

                    // Try to get nationality from other fields if ethnicity is not set
                    Ethnicity nationality = director.getNationality();
                    if (nationality != null) {
                        String label = nationality.getLabel();
                        if (label != null && !label.trim().isEmpty()) {
                            return new SimpleStringProperty(label);
                        }
                    }

                }
                return new SimpleStringProperty("N/A");
            } catch (Exception e) {
                logger.debug("Error getting director nationality: {}", e.getMessage());
                return new SimpleStringProperty("N/A");
            }
        });

        // Set up cell value factory for notable works with improved formatting and null safety
        directorNotableWorksColumn.setCellValueFactory(cellData -> {
            try {
                Director director = cellData.getValue();
                if (director != null) {
                    try {
                        List<String> notableWorks = director.getNotableWorks();
                        if (notableWorks != null && !notableWorks.isEmpty()) {
                            // Filter out any null or empty strings from notable works
                            List<String> validWorks = notableWorks.stream()
                                .filter(work -> work != null && !work.trim().isEmpty())
                                .collect(Collectors.toList());
                            
                            if (!validWorks.isEmpty()) {
                                // Format the notable works as a comma-separated list, limit to 3 items for display
                                int maxWorks = Math.min(3, validWorks.size());
                                String worksText = String.join(", ", validWorks.subList(0, maxWorks));
                                if (validWorks.size() > 3) {
                                    worksText += "...";
                                }
                                // Log the notable works for the director
                                logger.debug("Notable works for director {} {}: {}",
                                    director.getFirstName(), director.getLastName(), worksText);
                                return new SimpleStringProperty(worksText);
                            }
                        }
                    } catch (Exception e) {
                        logger.warn("Error processing notable works for director {} {}: {}", 
                            director.getFirstName(), director.getLastName(), e.getMessage());
                    }
                }
            } catch (Exception e) {
                logger.error("Unexpected error getting director notable works: {}", e.getMessage(), e);
            }
            return new SimpleStringProperty("No notable works");
        });

        // Set up search functionality
        directorSearchField.textProperty().addListener((observable, oldValue, newValue) -> {
            filterDirectors(newValue);
            // Log the search query
            logger.debug("Search query: {}", newValue);
        });

        // Set the filtered list to the table
        directorsTable.setItems(filteredDirectors);
        // Log the filtered list
        logger.debug("Filtered directors: {}", filteredDirectors);
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
                    assert finalActorList != null;
                    assert finalDirectorList != null;
                    logger.debug("Updating UI with {} actors and {} directors",
                        finalActorList.size(), finalDirectorList.size());

                    // Set the actors and directors to the ObservableLists
                    // Log the actors and directors lists
                    logger.debug("Setting actors list: {}", finalActorList);
                    logger.debug("Setting directors list: {}", finalDirectorList);
                    actors.setAll(finalActorList);
                    directors.setAll(finalDirectorList);
                    
                    // Verify the data was set correctly
                    logger.debug("After setting, actors list has {} items, directors list has {} items", 
                        actors.size(), directors.size());
                    
                    // Reset filters
                    // Log the filters
                    logger.debug("Resetting filters");
                    logger.debug("Setting actors filter: {}", filteredActors);
                    logger.debug("Setting directors filter: {}", filteredDirectors);
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
                // Log the final list
                logger.debug("Final actors list: {}", actors);
                logger.debug("Final directors list: {}", directors);
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
                    logger.error("Error getting director nationality: {}", e.getMessage());
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

    @FXML
    private void handleAddCelebrity() {
        // Create a choice dialog for celebrity type selection
        List<String> choices = Arrays.asList("Actor", "Director");
        ChoiceDialog<String> dialog = new ChoiceDialog<>("Actor", choices);
        dialog.setTitle("Add Celebrity");
        dialog.setHeaderText("Select Celebrity Type");
        dialog.setContentText("Choose celebrity type:");

        // Show the dialog and process the result
        Optional<String> result = dialog.showAndWait();
        
        result.ifPresent(celebrityType -> {
            if ("Actor".equals(celebrityType)) {
                showAddActorDialog();
            } else if ("Director".equals(celebrityType)) {
                showAddDirectorDialog();
            }
        });
    }

    private void showAddActorDialog() {
        // Create a dialog for adding a new actor
        Dialog<Actor> dialog = new Dialog<>();
        dialog.setTitle("Add New Actor");
        dialog.setHeaderText("Enter actor details");

        // Set the button types
        ButtonType saveButtonType = new ButtonType("Save", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(saveButtonType, ButtonType.CANCEL);

        // Create the form
        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 150, 10, 10));

        TextField firstNameField = new TextField();
        firstNameField.setPromptText("First Name");
        TextField lastNameField = new TextField();
        lastNameField.setPromptText("Last Name");
        DatePicker birthDatePicker = new DatePicker();
        birthDatePicker.setPromptText("Birth Date");
        ComboBox<String> genderComboBox = new ComboBox<>(FXCollections.observableArrayList("M", "F", "O"));
        genderComboBox.setPromptText("Gender (M/F/O)");
        
        // Add ethnicity combo box
        ComboBox<Ethnicity> ethnicityComboBox = new ComboBox<>();
        ethnicityComboBox.getItems().addAll(Ethnicity.values());
        ethnicityComboBox.setPromptText("Select Ethnicity");
        
        TextArea notableWorksArea = new TextArea();
        notableWorksArea.setPromptText("Enter notable works, separated by commas");
        notableWorksArea.setPrefRowCount(3);

        grid.add(new Label("First Name:"), 0, 0);
        grid.add(firstNameField, 1, 0);
        grid.add(new Label("Last Name:"), 0, 1);
        grid.add(lastNameField, 1, 1);
        grid.add(new Label("Birth Date:"), 0, 2);
        grid.add(birthDatePicker, 1, 2);
        grid.add(new Label("Gender:"), 0, 3);
        grid.add(genderComboBox, 1, 3);
        grid.add(new Label("Ethnicity:"), 0, 4);
        grid.add(ethnicityComboBox, 1, 4);
        grid.add(new Label("Notable Works:"), 0, 5);
        grid.add(notableWorksArea, 1, 5);

        dialog.getDialogPane().setContent(grid);

        // Request focus on the first name field by default
        Platform.runLater(firstNameField::requestFocus);

        // Convert the result to an Actor when the save button is clicked
        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == saveButtonType) {
                try {
                    String firstName = firstNameField.getText().trim();
                    String lastName = lastNameField.getText().trim();
                    LocalDate birthDate = birthDatePicker.getValue();
                    char gender = genderComboBox.getValue() != null ? genderComboBox.getValue().charAt(0) : 'U';
                    Ethnicity ethnicity = ethnicityComboBox.getValue() != null ? ethnicityComboBox.getValue() : Ethnicity.UNKNOWN;
                    
                    // Parse notable works
                    String notableWorks = Arrays.stream(notableWorksArea.getText().split(","))
                            .map(String::trim)
                            .filter(s -> !s.isEmpty())
                            .collect(Collectors.joining(", "));
                    
                    // Create actor with required fields
                    Actor actor = new Actor(firstName, lastName, birthDate, gender, ethnicity);
                    actor.setNotableWorks(notableWorks);
                    
                    return actor;
                } catch (Exception e) {
                    showError("Error", "Invalid input: " + e.getMessage());
                    return null;
                }
            }
            return null;
        });

        // Show the dialog and process the result
        Optional<Actor> result = dialog.showAndWait();
        result.ifPresent(actor -> {
            try {
                actorService.save(actor);
                actors.add(actor);
                updateStatus("Actor added successfully!");
            } catch (Exception e) {
                showError("Error", "Failed to add actor: " + e.getMessage());
            }
        });
    }

    private void showAddDirectorDialog() {
        // Create a dialog for adding a new director
        Dialog<Director> dialog = new Dialog<>();
        dialog.setTitle("Add New Director");
        dialog.setHeaderText("Enter director details");

        // Set the button types
        ButtonType saveButtonType = new ButtonType("Save", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(saveButtonType, ButtonType.CANCEL);

        // Create the form
        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 150, 10, 10));

        TextField firstNameField = new TextField();
        firstNameField.setPromptText("First Name");
        TextField lastNameField = new TextField();
        lastNameField.setPromptText("Last Name");
        DatePicker birthDatePicker = new DatePicker();
        birthDatePicker.setPromptText("Birth Date");
        ComboBox<String> genderComboBox = new ComboBox<>(FXCollections.observableArrayList("M", "F", "O"));
        genderComboBox.setPromptText("Gender (M/F/O)");
        
        // Add ethnicity combo box
        ComboBox<Ethnicity> ethnicityComboBox = new ComboBox<>();
        ethnicityComboBox.getItems().addAll(Ethnicity.values());
        ethnicityComboBox.setPromptText("Select Ethnicity");
        
        TextArea notableWorksArea = new TextArea();
        notableWorksArea.setPromptText("Enter notable works, separated by commas");
        notableWorksArea.setPrefRowCount(3);

        grid.add(new Label("First Name:"), 0, 0);
        grid.add(firstNameField, 1, 0);
        grid.add(new Label("Last Name:"), 0, 1);
        grid.add(lastNameField, 1, 1);
        grid.add(new Label("Birth Date:"), 0, 2);
        grid.add(birthDatePicker, 1, 2);
        grid.add(new Label("Gender:"), 0, 3);
        grid.add(genderComboBox, 1, 3);
        grid.add(new Label("Ethnicity:"), 0, 4);
        grid.add(ethnicityComboBox, 1, 4);
        grid.add(new Label("Notable Works:"), 0, 5);
        grid.add(notableWorksArea, 1, 5);

        dialog.getDialogPane().setContent(grid);

        // Request focus on the first name field by default
        Platform.runLater(firstNameField::requestFocus);

        // Convert the result to a Director when the save button is clicked
        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == saveButtonType) {
                try {
                    String firstName = firstNameField.getText().trim();
                    String lastName = lastNameField.getText().trim();
                    LocalDate birthDate = birthDatePicker.getValue();
                    char gender = genderComboBox.getValue() != null ? genderComboBox.getValue().charAt(0) : 'U';
                    Ethnicity ethnicity = ethnicityComboBox.getValue() != null ? ethnicityComboBox.getValue() : Ethnicity.UNKNOWN;
                    
                    // Parse notable works
                    String notableWorks = Arrays.stream(notableWorksArea.getText().split(","))
                            .map(String::trim)
                            .filter(s -> !s.isEmpty())
                            .collect(Collectors.joining(", "));
                    
                    // Create director with required fields using factory method
                    Director director = Director.getInstance(firstName, lastName, birthDate, gender, ethnicity);
                    director.setNotableWorks(notableWorks);
                    
                    return director;
                } catch (Exception e) {
                    showError("Error", "Invalid input: " + e.getMessage());
                    return null;
                }
            }
            return null;
        });

        // Show the dialog and process the result
        Optional<Director> result = dialog.showAndWait();
        result.ifPresent(director -> {
            try {
                directorService.save(director);
                directors.add(director);
                updateStatus("Director added successfully!");
            } catch (Exception e) {
                showError("Error", "Failed to add director: " + e.getMessage());
            }
        });
    }
}
