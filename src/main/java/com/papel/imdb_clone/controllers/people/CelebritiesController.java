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
    /**
     * Explicit constructor for CelebritiesController.
     * Required for JavaFX controller initialization.
     */
    public CelebritiesController() {
        // No initialization needed
    }

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

    /**
     * Handles navigation to the home view.
     * @param event The mouse event that triggered this action
     */
    @FXML
    public void goToHome(MouseEvent event) {
        try {
            NavigationService navigationService = NavigationService.getInstance();
            navigationService.navigateTo("/fxml/home/home-view.fxml", null, null, "IMDb Clone - Home");
        } catch (Exception e) {
            logger.error("Error navigating to home: ", e);
            showError("Navigation Error", "Failed to navigate to home view.");
        }
    }

    //initialize the controller
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        try {
            logger.info("Initializing CelebritiesController...");
            
            // Initialize services with null checks
            DataManager dataManager = DataManager.getInstance();
            if (dataManager == null) {
                throw new IllegalStateException("DataManager is not properly initialized");
            }
            
            actorService = dataManager.getActorService();
            directorService = dataManager.getDirectorService();
            
            if (actorService == null || directorService == null) {
                throw new IllegalStateException("Failed to initialize required services");
            }

            // Initialize UI components
            try {
                initializeActorTable();
                initializeDirectorTable();
                initializeUnifiedSearch();
            } catch (Exception e) {
                throw new RuntimeException("Failed to initialize UI components: " + e.getMessage(), e);
            }

            // Load initial data in a separate thread to keep UI responsive
            new Thread(this::loadCelebrities).start();
            
            logger.info("CelebritiesController initialized successfully");
            
        } catch (Exception e) {
            String errorMsg = "Failed to initialize CelebritiesController: " + e.getMessage();
            logger.error(errorMsg, e);
            
            // Show error in UI on the JavaFX Application Thread
            Platform.runLater(() -> {
                showError("Initialization Error", 
                    "Failed to initialize the celebrities view. " +
                    "Please restart the application.\n\nError details: " + 
                    e.getMessage());
            });
            
            // If this is a critical error, we might want to disable the UI
            disableUI();
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

                                // If there are valid works, format them
                                if (!validWorks.isEmpty()) {
                                    // Format the notable works as a comma-separated list, limit to 6 items for display
                                    int maxWorks = Math.min(6, validWorks.size());
                                    String worksText = String.join(", ", validWorks.subList(0, maxWorks));
                                    if (validWorks.size() > 6) {
                                        // Add ellipsis if there are more than 6 works
                                        worksText += "...";
                                    }
                                    logger.debug("Notable works for {} {}: {}",
                                            actor.getFirstName(), actor.getLastName(), worksText);
                                    return new SimpleStringProperty(worksText);
                                }
                            }
                            return new SimpleStringProperty("-"); // Use dash for empty works
                        } catch (Exception e) {
                            logger.warn("Error processing notable works for {} {}: {}",
                                    actor.getFirstName(), actor.getLastName(), e.getMessage());
                            return new SimpleStringProperty("Error loading works");
                        }
                    }
                } catch (Exception e) {
                    logger.error("Unexpected error getting actor notable works: {}", e.getMessage(), e);
                }
                return new SimpleStringProperty("-"); // Default fallback
            });

            // Set a tooltip to show all notable works on hover
            actorNotableWorksColumn.setCellFactory(column -> {
                return new TableCell<Actor, String>() {
                    @Override
                    protected void updateItem(String item, boolean empty) {
                        super.updateItem(item, empty);
                        if (item == null || empty) {
                            setText(null);
                            setTooltip(null);
                        } else {
                            setText(item);

                            // Get the list of notable works for the tooltip (max 7)
                            Actor actor = getTableView().getItems().get(getIndex());
                            if (actor != null) {
                                List<String> allWorks = actor.getNotableWorks();
                                if (allWorks != null && !allWorks.isEmpty()) {
                                    // Limit to first 7 works for the tooltip
                                    int maxWorks = Math.min(7, allWorks.size());
                                    List<String> limitedWorks = allWorks.subList(0, maxWorks);
                                    String tooltipText = String.join("\n• ", limitedWorks);
                                    
                                    // Add a note if there are more works than shown
                                    if (allWorks.size() > 7) {
                                        tooltipText += "\n• ... and " + (allWorks.size() - 7) + " more";
                                    }
                                    
                                    setTooltip(new Tooltip("• " + tooltipText));
                                } else {
                                    setTooltip(new Tooltip("No notable works available"));
                                }
                            }
                        }
                    }
                };
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
                    data, (Stage) actorsTable.getScene().getWindow(), "IMDb Clone - Home");
        } catch (Exception e) {
            logger.error("Error navigating to home: {}", e.getMessage(), e);
            showError("Navigation Error", "Failed to navigate to home: " + e.getMessage());
        }
    }

    /**
     * Handles the refresh button action to reload all celebrities.
     * This method clears all search fields and reloads both actors and directors.
     */
    @FXML
    private void handleRefresh() {
        try {
            logger.info("Starting refresh of all celebrity data...");

            // Clear search fields
            Platform.runLater(() -> {
                if (actorSearchField != null) {
                    actorSearchField.clear();
                } else if (directorSearchField != null) {
                    directorSearchField.clear();
                }
                //both for actor and director search
                else if (unifiedSearchField != null) {
                    unifiedSearchField.clear();
                }
            });

            // Clear current data
            actors.clear();
            directors.clear();

            // Reload all celebrities
            loadCelebrities();

            // Update UI with refreshed data
            Platform.runLater(() -> {
                if (actorsTable != null) {
                    actorsTable.refresh();
                } else if (directorsTable != null) {
                    directorsTable.refresh();
                }
            });

            // Show success message
            if (statusLabel != null) {
                String successMessage = String.format("Successfully refreshed %d actors and %d directors",
                        actors.size(), directors.size());
                statusLabel.setText(successMessage);
                logger.info(successMessage);

                // Clear the status message after 3 seconds
                new java.util.Timer().schedule(
                        new java.util.TimerTask() {
                            @Override
                            public void run() {
                                Platform.runLater(() -> {
                                    if (statusLabel != null) {
                                        statusLabel.setText("");
                                        logger.info("Refresh completed");
                                    }
                                });
                            }
                        },
                        3000
                );
            }
        } catch (Exception e) {
            String errorMsg = "Failed to refresh celebrities: " + e.getMessage();
            logger.error(errorMsg, e);

            // Show error in UI on the JavaFX Application Thread
            Platform.runLater(() -> {
                showError("Refresh Error", errorMsg);
                if (statusLabel != null) {
                    statusLabel.setText("Error refreshing data. Please try again.");
                }
            });
        }
    }

    private void initializeDirectorTable() {
        try {
            // Set up cell value factory for director name
            directorNameColumn.setCellValueFactory(cellData -> {
                try {
                    Director director = cellData.getValue();
                    if (director != null) {
                        String firstName = director.getFirstName() != null ? director.getFirstName() : "";
                        String lastName = director.getLastName() != null ? director.getLastName() : "";
                        String fullName = String.format("%s %s", firstName, lastName).trim();
                        logger.debug("Director name: {}", fullName);
                        return new SimpleStringProperty(fullName);
                    }
                } catch (Exception e) {
                    logger.error("Error getting director name: {}", e.getMessage(), e);
                }
                return new SimpleStringProperty("N/A");
            });

            // Set up cell value factory for birth date with proper formatting
            directorBirthDateColumn.setCellValueFactory(cellData -> {
                try {
                    if (cellData.getValue() != null) {
                        LocalDate birthDate = cellData.getValue().getBirthDate();
                        if (birthDate != null) {
                            String formattedDate = birthDate.toString();
                            logger.debug("Director birth date: {}", formattedDate);
                            return new SimpleStringProperty(formattedDate);
                        }
                    }
                } catch (Exception e) {
                    logger.error("Error getting director birth date: {}", e.getMessage(), e);
                }
                return new SimpleStringProperty("N/A");
            });

            // Set up cell value factory for gender
            directorGenderColumn.setCellValueFactory(cellData -> {
                try {
                    if (cellData.getValue() != null) {
                        char gender = cellData.getValue().getGender();
                        String genderStr = "Unknown";
                        if (gender == 'M' || gender == 'm') {
                            genderStr = "Male";
                        } else if (gender == 'F' || gender == 'f') {
                            genderStr = "Female";
                        }
                        logger.debug("Director gender: {}", genderStr);
                        return new SimpleStringProperty(genderStr);
                    }
                } catch (Exception e) {
                    logger.error("Error getting director gender: {}", e.getMessage(), e);
                }
                return new SimpleStringProperty("N/A");
            });

            // Set up cell value factory for nationality/ethnicity
            directorNationalityColumn.setCellValueFactory(cellData -> {
                try {
                    Director director = cellData.getValue();
                    if (director != null) {
                        Ethnicity ethnicity = director.getEthnicity();
                        if (ethnicity != null) {
                            logger.debug("Director {} {} nationality: {}", 
                                director.getFirstName(), director.getLastName(), 
                                ethnicity.getLabel());
                            return new SimpleStringProperty(ethnicity.getLabel());
                        }
                    }
                } catch (Exception e) {
                    logger.error("Error getting director nationality: {}", e.getMessage(), e);
                }
                return new SimpleStringProperty("N/A");
            });

            // Set up cell value factory for notable works
            directorNotableWorksColumn.setCellValueFactory(cellData -> {
                try {
                    Director director = cellData.getValue();
                    if (director != null) {
                        List<String> notableWorks = director.getNotableWorks();
                        if (notableWorks != null && !notableWorks.isEmpty()) {
                            // Filter out any null or empty strings
                            List<String> validWorks = notableWorks.stream()
                                .filter(work -> work != null && !work.trim().isEmpty())
                                .collect(Collectors.toList());

                            if (!validWorks.isEmpty()) {
                                // Limit to 6 works for display
                                int maxWorks = Math.min(6, validWorks.size());
                                String worksText = String.join(", ", validWorks.subList(0, maxWorks));
                                if (validWorks.size() > 6) {
                                    worksText += "...";
                                }
                                logger.debug("Director {} {} notable works (truncated): {}", 
                                    director.getFirstName(), director.getLastName(), worksText);
                                return new SimpleStringProperty(worksText);
                            }
                        }
                    }
                } catch (Exception e) {
                    logger.error("Error getting director notable works: {}", e.getMessage(), e);
                }
                return new SimpleStringProperty("-");
            });

            // Add tooltip for notable works to show full list on hover
            directorNotableWorksColumn.setCellFactory(column -> {
                return new TableCell<Director, String>() {
                    @Override
                    protected void updateItem(String item, boolean empty) {
                        super.updateItem(item, empty);
                        if (item == null || empty) {
                            setText(null);
                            setTooltip(null);
                        } else {
                            setText(item);
                            Director director = getTableView().getItems().get(getIndex());
                            if (director != null) {
                                List<String> allWorks = director.getNotableWorks();
                                if (allWorks != null && !allWorks.isEmpty()) {
                                    // Limit to first 7 works for the tooltip
                                    int maxWorks = Math.min(7, allWorks.size());
                                    List<String> limitedWorks = allWorks.subList(0, maxWorks);
                                    String tooltipText = String.join("\n• ", limitedWorks);
                                    
                                    // Add a note if there are more works than shown
                                    if (allWorks.size() > 7) {
                                        tooltipText += "\n• ... and " + (allWorks.size() - 7) + " more";
                                    }
                                    
                                    setTooltip(new Tooltip("• " + tooltipText));
                                }
                            }
                        }
                    }
                };
            });

            // Set the items to the table
            directorsTable.setItems(filteredDirectors);

            // Enable sorting on all columns
            directorNameColumn.setSortable(true);
            directorBirthDateColumn.setSortable(true);
            directorGenderColumn.setSortable(true);
            directorNationalityColumn.setSortable(true);
            directorNotableWorksColumn.setSortable(true);

            logger.info("Director table initialized successfully");
        } catch (Exception e) {
            logger.error("Error initializing director table: {}", e.getMessage(), e);
            showError("Initialization Error", "Failed to initialize director table: " + e.getMessage());
        }

        // Set up cell value factory for gender with consistent handling
        directorGenderColumn.setCellValueFactory(cellData -> {
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
                logger.error("Error getting director gender: {}", e.getMessage());
            }
            return new SimpleStringProperty("N/A");
        });

        // Set up cell value factory for nationality/ethnicity
        directorNationalityColumn.setCellValueFactory(cellData -> {
            try {
                Director director = cellData.getValue();
                if (director != null) {
                    Ethnicity ethnicity = director.getEthnicity();
                    if (ethnicity != null) {
                        return new SimpleStringProperty(ethnicity.getLabel());
                    } else if (director.getNationality() != null) {
                        return new SimpleStringProperty(director.getNationality().getLabel());
                    }
                }
            } catch (Exception e) {
                logger.error("Error getting director nationality: {}", e.getMessage());
                showError("Error", "Failed to get director nationality: " + e.getMessage());
            }
            return new SimpleStringProperty("N/A");
        });

        // Set up cell value factory for notable works with proper formatting
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
                                // Format the notable works as a comma-separated list, limit to 4 items for display
                                int maxWorks = Math.min(4, validWorks.size());
                                String worksText = String.join(", ", validWorks.subList(0, maxWorks));
                                if (validWorks.size() > 4) {
                                    // Add ellipsis if there are more than 4 works
                                    worksText += "...";
                                }
                                logger.debug("Notable works for director {} {}: {}",
                                        director.getFirstName(), director.getLastName(), worksText);
                                return new SimpleStringProperty(worksText);
                            }
                        }
                        // If we get here, there are no notable works
                        logger.debug("No notable works found for director {} {}",
                                director.getFirstName(), director.getLastName());
                        return new SimpleStringProperty("-"); // Use dash for empty works
                    } catch (Exception e) {
                        logger.warn("Error processing notable works for director {} {}: {}",
                                director.getFirstName(), director.getLastName(), e.getMessage());
                        return new SimpleStringProperty("Error loading works");
                    }
                }
            } catch (Exception e) {
                logger.error("Unexpected error getting director notable works: {}", e.getMessage(), e);
            }
            return new SimpleStringProperty("-"); // Default fallback
        });

        // Set a tooltip to show all notable works on hover
        directorNotableWorksColumn.setCellFactory(column -> {
            return new TableCell<Director, String>() {
                @Override
                protected void updateItem(String item, boolean empty) {
                    super.updateItem(item, empty);
                    if (item == null || empty) {
                        setText(null);
                        setTooltip(null);
                    } else {
                        setText(item);

                        // Get the full list of notable works for the tooltip
                        Director director = getTableView().getItems().get(getIndex());
                        if (director != null) {
                            List<String> allWorks = director.getNotableWorks();
                            if (allWorks != null && !allWorks.isEmpty()) {
                                String tooltipText = String.join("\n• ", allWorks);
                                setTooltip(new Tooltip("• " + tooltipText));
                            } else {
                                setTooltip(new Tooltip("No notable works available"));
                            }
                        }
                    }
                }
            };
        });

        // Set the items to the table
        directorsTable.setItems(filteredDirectors);

        // Enable sorting on all columns
        directorNameColumn.setSortable(true);
        directorBirthDateColumn.setSortable(true);
        directorGenderColumn.setSortable(true);
        directorNationalityColumn.setSortable(true);
        directorNotableWorksColumn.setSortable(true);

        // Set up search functionality for director search field
        if (directorSearchField != null) {
            directorSearchField.textProperty().addListener((observable, oldValue, newValue) -> {
                filterDirectors(newValue);
            });
        }

        // Set up unified search
        if (unifiedSearchField != null) {
            unifiedSearchField.textProperty().addListener((observable, oldValue, newValue) -> {
                filterActors(newValue);
                filterDirectors(newValue);
            });

            }

        // Set up cell value factory for notable works with improved formatting and null safety
        directorNotableWorksColumn.setCellValueFactory(cellData -> {
            try {
                Director director = cellData.getValue();
                if (director != null) {
                    try {
                        // Get the director's notable works
                        List<String> notableWorks = director.getNotableWorks();
                        if (notableWorks != null && !notableWorks.isEmpty()) {
                            // Filter out any null or empty strings from notable works
                            List<String> validWorks = notableWorks.stream()
                                .filter(work -> work != null && !work.trim().isEmpty())
                                .collect(Collectors.toList());
                            
                            if (!validWorks.isEmpty()) {
                                // Format the notable works as a comma-separated list, limit to 6 items for display
                                int maxWorks = Math.min(6, validWorks.size());
                                String worksText = String.join(", ", validWorks.subList(0, maxWorks));
                                if (validWorks.size() > 6) {
                                    worksText += "...";
                                }
                                // Log the notable works for the director
                                logger.debug("Notable works for director {} {}: {}",
                                    director.getFirstName(), director.getLastName(), worksText);
                                return new SimpleStringProperty(worksText);
                            }
                        }
                        // If we get here, there are no notable works
                        logger.debug("No notable works found for director {} {}", 
                            director.getFirstName(), director.getLastName());
                        return new SimpleStringProperty("-"); // Use dash for empty works
                    } catch (Exception e) {
                        logger.warn("Error processing notable works for director {} {}: {}", 
                            director.getFirstName(), director.getLastName(), e.getMessage());
                        return new SimpleStringProperty("Error loading works");
                    }
                }
            } catch (Exception e) {
                logger.error("Unexpected error getting director notable works: {}", e.getMessage(), e);
            }
            return new SimpleStringProperty("-"); // Default fallback
        });
        
        // Add tooltip to show all notable works on hover
        directorNotableWorksColumn.setCellFactory(column -> {
            return new TableCell<Director, String>() {
                @Override
                protected void updateItem(String item, boolean empty) {
                    super.updateItem(item, empty);
                    if (item == null || empty) {
                        setText(null);
                        setTooltip(null);
                    } else {
                        setText(item);
                        
                        // Get the full list of notable works for the tooltip
                        Director director = getTableView().getItems().get(getIndex());
                        if (director != null) {
                            List<String> allWorks = director.getNotableWorks();
                            if (allWorks != null && !allWorks.isEmpty()) {
                                String tooltipText = String.join("\n• ", allWorks);
                                setTooltip(new Tooltip("• " + tooltipText));
                            } else {
                                setTooltip(new Tooltip("No notable works available"));
                            }
                        }
                    }
                }
            };
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
        
        // Initialize empty lists to avoid NPEs in case of errors
        List<Actor> actorList = Collections.emptyList();
        List<Director> directorList = Collections.emptyList();
        
        try {
            // Load actors with notable works
            actorList = actorService.getAll();
            logger.info("Loaded {} actors", actorList.size());
            
            // Log notable works for first few actors
            int actorsToLog = Math.min(5, actorList.size());
            for (int i = 0; i < actorsToLog; i++) {
                Actor actor = actorList.get(i);
                logger.debug("Actor[{}]: {} {}", i, actor.getFirstName(), actor.getLastName());
                logger.debug("  Notable Works: {}", 
                    actor.getNotableWorks() != null && !actor.getNotableWorks().isEmpty() ? 
                    String.join(", ", actor.getNotableWorks()) : "None");
            }

            // Load directors with notable works
            directorList = directorService.getAll();
            logger.info("Loaded {} directors", directorList.size());
            
            // Log notable works for first few directors
            int directorsToLog = Math.min(5, directorList.size());
            for (int i = 0; i < directorsToLog; i++) {
                Director director = directorList.get(i);
                logger.debug("Director[{}]: {} {}", i, director.getFirstName(), director.getLastName());
                logger.debug("  Notable Works: {}", 
                    director.getNotableWorks() != null && !director.getNotableWorks().isEmpty() ? 
                    String.join(", ", director.getNotableWorks()) : "None");
            }
            
            // Update UI on the JavaFX Application Thread
            updateUIWithCelebrities(actorList, directorList);
            
        } catch (Exception e) {
            String errorMsg = "Failed to load celebrities: " + e.getMessage();
            logger.error(errorMsg, e);
            
            // Update UI with empty lists in case of error
            updateUIWithCelebrities(Collections.emptyList(), Collections.emptyList());
            
            // Show error to user
            Platform.runLater(() -> 
                showError("Load Error", "Failed to load celebrities. Please try again later.")
            );
        }
        
        logger.info("Finished loading celebrities");
    }
    
    /**
     * Updates the UI with the loaded celebrities data.
     * This method is called on the JavaFX Application Thread.
     * 
     * @param actorList The list of actors to display
     * @param directorList The list of directors to display
     */
    private void updateUIWithCelebrities(List<Actor> actorList, List<Director> directorList) {
        Platform.runLater(() -> {
            try {
                // Set the actors and directors to the ObservableLists
                actors.setAll(actorList);
                directors.setAll(directorList);
                
                // Reset filters
                filteredActors.setPredicate(actor -> true);
                filteredDirectors.setPredicate(director -> true);
                
                // Update status with success message
                if (statusLabel != null) {
                    String status = String.format("Loaded %d actors and %d directors", 
                        actorList.size(), directorList.size());
                    statusLabel.setText(status);
                    logger.info(status);
                    
                    // Clear the status message after 3 seconds
                    new java.util.Timer().schedule(
                        new java.util.TimerTask() {
                            @Override
                            public void run() {
                                Platform.runLater(() -> {
                                    if (statusLabel != null) {
                                        statusLabel.setText("");
                                    }
                                });
                            }
                        },
                        3000
                    );
                }
            } catch (Exception e) {
                logger.error("Error updating UI with loaded celebrities: {}", e.getMessage(), e);
                showError("UI Update Error", "Failed to update UI with loaded data: " + e.getMessage());
            }
        });
    }

/**
 * Displays an error dialog with the specified title and message.
 * This method is thread-safe and can be called from any thread.
 * 
 * @param title The title of the error dialog
 * @param message The error message to display
 */
private void showError(String title, String message) {
    Platform.runLater(() -> {
        try {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle(title);
            alert.setHeaderText(null);
            alert.setContentText(message);
            
            // Add a copy button to the dialog
            alert.getDialogPane().getButtonTypes().add(ButtonType.CLOSE);
            
            // Show the dialog and wait for it to be closed
            alert.showAndWait();
            
            // Log the error
            logger.error("Error Dialog - {}: {}", title, message);
        } catch (Exception e) {
            // If there's an error showing the dialog, log it to the console
            System.err.println("Failed to show error dialog: " + e.getMessage());
            e.printStackTrace();
        }
    });
}

/**
 * Disables UI components when a critical error occurs.
 * This helps prevent further errors by making it clear the UI is not functional.
 */
private void disableUI() {
    Platform.runLater(() -> {
        try {
            // Disable all interactive components
            if (actorsTable != null) actorsTable.setDisable(true);
            if (directorsTable != null) directorsTable.setDisable(true);
            if (actorSearchField != null) actorSearchField.setDisable(true);
            if (directorSearchField != null) directorSearchField.setDisable(true);
            if (unifiedSearchField != null) unifiedSearchField.setDisable(true);
            
            if (statusLabel != null) {
                statusLabel.setText("An error occurred. Please restart the application.");
                statusLabel.setStyle("-fx-text-fill: red; -fx-font-weight: bold;");
            }
        } catch (Exception e) {
            logger.error("Error disabling UI: {}", e.getMessage(), e);
        }
    });
}

/**
 * Handles the "Add Celebrity" button click event.
 * Shows a dialog to choose between adding an actor, director, or both.
 */
@FXML
private void handleAddCelebrity() {
    try {
        // Create a choice dialog for selecting celebrity type
        List<String> choices = Arrays.asList("Actor", "Director", "Both");
        ChoiceDialog<String> dialog = new ChoiceDialog<>("Actor", choices);
        dialog.setTitle("Add New Celebrity");
        dialog.setHeaderText("Select Celebrity Type");
        dialog.setContentText("Choose type:");
        
        // Customize the dialog buttons
        ButtonType addButton = new ButtonType("Continue", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().setAll(addButton, ButtonType.CANCEL);

        // Show the dialog and process the result
        Optional<String> result = dialog.showAndWait();
        if (result.isPresent()) {
            String choice = result.get();
            switch (choice) {
                case "Actor":
                    showAddActorDialog();
                    break;
                case "Director":
                    showAddDirectorDialog();
                    break;
                case "Both":
                    showAddCelebrityDialog();
                    break;
            }
        }
    } catch (Exception e) {
        logger.error("Error in handleAddCelebrity: {}", e.getMessage(), e);
        showError("Error", "Failed to open add celebrity dialog: " + e.getMessage());
    }
}

/**
 * Shows a dialog for adding a new actor.
 */
private void showAddActorDialog() {
    try {
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
    } catch (Exception e) {
        logger.error("Error in showAddActorDialog: {}", e.getMessage(), e);
        showError("Error", "Failed to open add actor dialog: " + e.getMessage());
    }
}

    /**
     * Shows a dialog for adding a celebrity with both actor and director roles.
     */
    private void showAddCelebrityDialog() {
        try {
            Dialog<Map<String, Object>> dialog = new Dialog<>();
            dialog.setTitle("Add New Celebrity");
            dialog.setHeaderText("Enter celebrity details (will be added as both Actor and Director)");

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

            // Convert the result to a map when the save button is clicked
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
                        
                        // Create a map to return all the values
                        Map<String, Object> result = new HashMap<>();
                        result.put("firstName", firstName);
                        result.put("lastName", lastName);
                        result.put("birthDate", birthDate);
                        result.put("gender", gender);
                        result.put("ethnicity", ethnicity);
                        result.put("notableWorks", notableWorks);
                        
                        return result;
                    } catch (Exception e) {
                        showError("Error", "Invalid input: " + e.getMessage());
                        return null;
                    }
                }
                return null;
            });

            // Show the dialog and process the result
            Optional<Map<String, Object>> result = dialog.showAndWait();
            result.ifPresent(celebrityData -> {
                try {
                    // Extract data from the map
                    String firstName = (String) celebrityData.get("firstName");
                    String lastName = (String) celebrityData.get("lastName");
                    LocalDate birthDate = (LocalDate) celebrityData.get("birthDate");
                    char gender = (char) celebrityData.get("gender");
                    Ethnicity ethnicity = (Ethnicity) celebrityData.get("ethnicity");
                    String notableWorks = (String) celebrityData.get("notableWorks");
                    
                    // Create and save actor
                    Actor actor = new Actor(firstName, lastName, birthDate, gender, ethnicity);
                    actor.setNotableWorks(notableWorks);
                    actorService.save(actor);
                    actors.add(actor);
                    
                    // Create and save director
                    Director director = Director.getInstance(firstName, lastName, birthDate, gender, ethnicity);
                    director.setNotableWorks(notableWorks);
                    directorService.save(director);
                    directors.add(director);
                    
                    updateStatus("Celebrity added successfully as both Actor and Director!");
                } catch (Exception e) {
                    logger.error("Error saving celebrity: {}", e.getMessage(), e);
                    showError("Error", "Failed to save celebrity: " + e.getMessage());
                }
            });
        } catch (Exception e) {
            logger.error("Error in showAddCelebrityDialog: {}", e.getMessage(), e);
            showError("Error", "Failed to open add celebrity dialog: " + e.getMessage());
        }
    }
    
    /**
     * Shows a dialog for adding a new director.
     */
    private void showAddDirectorDialog() {
        try {
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
        } catch (Exception e) {
            logger.error("Error in showAddDirectorDialog: {}", e.getMessage(), e);
            showError("Error", "Failed to open add director dialog: " + e.getMessage());
        }
    }
    
    /**
     * Initializes the unified search functionality for both actors and directors.
     */
    private void initializeUnifiedSearch() {
        if (unifiedSearchField != null) {
            unifiedSearchField.textProperty().addListener((observable, oldValue, newValue) -> {
                filterActors(newValue);
                filterDirectors(newValue);
            });
        }
    }
    
    /**
     * Filters the actors list based on the search query.
     * 
     * @param query The search query
     */
    private void filterActors(String query) {
        if (query == null || query.trim().isEmpty()) {
            actorsTable.setItems(actors);
            return;
        }
        
        String lowerCaseQuery = query.toLowerCase();
        List<Actor> filteredList = actors.stream()
            .filter(actor -> {
                String fullName = (actor.getFirstName() + " " + actor.getLastName()).toLowerCase();
                boolean nameMatches = fullName.contains(lowerCaseQuery);
                
                // Check if any notable work contains the query
                boolean notableWorkMatches = false;
                if (actor.getNotableWorks() != null) {
                    notableWorkMatches = actor.getNotableWorks().stream()
                        .anyMatch(work -> work != null && work.toLowerCase().contains(lowerCaseQuery));
                }
                
                return nameMatches || notableWorkMatches;
            })
            .collect(Collectors.toList());
            
        actorsTable.setItems(FXCollections.observableArrayList(filteredList));
    }
    
    /**
     * Filters the directors list based on the search query.
     * 
     * @param query The search query
     */
    private void filterDirectors(String query) {
        if (query == null || query.trim().isEmpty()) {
            directorsTable.setItems(directors);
            return;
        }
        
        String lowerCaseQuery = query.toLowerCase();
        List<Director> filteredList = directors.stream()
            .filter(director -> {
                String fullName = (director.getFirstName() + " " + director.getLastName()).toLowerCase();
                boolean nameMatches = fullName.contains(lowerCaseQuery);
                
                // Check if any notable work contains the query
                boolean notableWorkMatches = false;
                if (director.getNotableWorks() != null) {
                    notableWorkMatches = director.getNotableWorks().stream()
                        .anyMatch(work -> work != null && work.toLowerCase().contains(lowerCaseQuery));
                }
                
                return nameMatches || notableWorkMatches;
            })
            .collect(Collectors.toList());
            
        directorsTable.setItems(FXCollections.observableArrayList(filteredList));
    }
    
    /**
     * Updates the status label with the given message.
     * 
     * @param message The message to display in the status label
     */
    private void updateStatus(String message) {
        if (statusLabel != null) {
            Platform.runLater(() -> {
                statusLabel.setText(message);
                
                // Clear the status after 5 seconds
                new Thread(() -> {
                    try {
                        Thread.sleep(5000);
                        if (statusLabel.getText().equals(message)) {
                            Platform.runLater(() -> statusLabel.setText(""));
                        }
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                    }
                }).start();
            });
        }
    }
}
