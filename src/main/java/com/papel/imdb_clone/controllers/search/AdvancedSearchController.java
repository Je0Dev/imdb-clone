package com.papel.imdb_clone.controllers.search;

import com.papel.imdb_clone.enums.Genre;
import com.papel.imdb_clone.model.content.Content;
import com.papel.imdb_clone.model.content.Movie;
import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import java.util.stream.Collectors;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.concurrent.TimeUnit;

/**
 * Main controller for the advanced search functionality.
 * Coordinates between the search form and results table.
 */
public class AdvancedSearchController extends BaseSearchController {
    private static final Logger logger = LoggerFactory.getLogger(AdvancedSearchController.class);
    public TableView resultsTable;
    public TableColumn resultTitleColumn;
    public TableColumn resultTypeColumn;
    public TableColumn resultYearColumn;
    public TableColumn resultGenreColumn;

    @FXML
    private BorderPane mainContainer;
    @FXML
    private VBox searchFormContainer;
    @FXML
    private SearchFormController searchFormController;
    @FXML
    private ResultsTableController resultsTableController;
    @FXML
    private Button manageDetailsButton;
    @FXML
    private Label resultsCountLabel;

    private Task<ObservableList<Content>> currentSearchTask;
    private final long defaultCacheExpiration = 30;
    private final TimeUnit timeUnit = TimeUnit.MINUTES;
    private Map<String, Object> data;

    /**
     * Constructor for the AdvancedSearchController.
     * Initializes the search service and navigation service.
     */
    public AdvancedSearchController() {
        super(); // Initialize base controller
    }

    /**
     * Initializes the table columns with their respective cell value factories.
     */
    private void initializeTableColumns() {
        try {
            logger.info("Initializing table columns...");
            
            // Clear existing columns to avoid duplicates
            resultsTable.getColumns().clear();
            
            // Title Column
            TableColumn<Content, String> titleColumn = new TableColumn<>("TITLE");
            titleColumn.setCellValueFactory(new PropertyValueFactory<>("title"));
            titleColumn.setPrefWidth(250);
            titleColumn.setStyle("-fx-font-weight: bold; -fx-text-fill: #00E5FF; -fx-font-size: 14;");
            
            // Type Column
            TableColumn<Content, String> typeColumn = new TableColumn<>("TYPE");
            typeColumn.setCellValueFactory(cellData -> {
                Content content = cellData.getValue();
                return new SimpleStringProperty(content instanceof Movie ? "Movie" : "Series");
            });
            typeColumn.setPrefWidth(100);
            typeColumn.setStyle("-fx-text-fill: #40C4FF; -fx-font-weight: bold;");
            
            // Year Column
            TableColumn<Content, Integer> yearColumn = new TableColumn<>("YEAR");
            yearColumn.setCellValueFactory(new PropertyValueFactory<>("year"));
            yearColumn.setStyle("-fx-text-fill: #69F0AE; -fx-font-weight: bold;");
            
            // Genre Column
            TableColumn<Content, String> genreColumn = new TableColumn<>("GENRE");
            genreColumn.setCellValueFactory(cellData -> {
                List<String> genreNames = cellData.getValue().getGenres().stream()
                    .map(Enum::name)
                    .collect(Collectors.toList());
                return new SimpleStringProperty(String.join(", ", genreNames));
            });
            genreColumn.setPrefWidth(200);
            genreColumn.setStyle("-fx-text-fill: #FF8A65; -fx-font-weight: bold;");
            
            // Add columns to the table
            resultsTable.getColumns().addAll(titleColumn, typeColumn, yearColumn, genreColumn);
            
            logger.info("Table columns initialized successfully");
        } catch (Exception e) {
            logger.error("Error initializing table columns", e);
        }
    }
    
    @FXML
    public void initialize() {
        try {
            logger.info("Initializing AdvancedSearchController...");
            
            // Initialize the table
            if (resultsTable != null) {
                logger.info("Initializing results table...");
                
                // Set up the table
                resultsTable.setVisible(true);
                resultsTable.setManaged(true);
                
                // Set up the selection model
                resultsTable.getSelectionModel().setSelectionMode(SelectionMode.SINGLE);
                
                // Update the manage details button state based on selection
                resultsTable.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
                    if (manageDetailsButton != null) {
                        manageDetailsButton.setDisable(newSelection == null);
                    }
                });
                
                // Initialize table columns
                initializeTableColumns();
                
                logger.info("Results table initialized with {} columns", resultsTable.getColumns().size());
            } else {
                logger.error("Results table is not injected");
            }

            // Initialize the search form controller if available
            if (searchFormController != null) {
                logger.info("Initializing SearchFormController...");
                // Initialize the form with default values
                searchFormController.initializeForm();

                // Set up the search form listener
                searchFormController.setSearchFormListener(new SearchFormController.SearchFormListener() {
                    @Override
                    public void onSearchCriteriaChanged(SearchCriteria criteria) {
                        // Update UI based on search criteria changes if needed
                        if (hasSearchCriteria(criteria)) {
                            updateStatus("Ready to search");
                        } else {
                            updateStatus("Enter search criteria");
                        }
                    }

                    @Override
                    public void onSearchRequested(SearchCriteria criteria) {
                        logger.info("Search requested with criteria: {}", criteria);
                        // If criteria is null, create a new one with default sorting
                        if (criteria == null) {
                            criteria = new SearchCriteria("title", false);
                        }
                        performSearch(criteria);
                    }
                });
            } else {
                logger.error("Search form controller is not initialized");
            }

            // Initialize the results table and manage details button
            if (resultsTableController != null) {
                try {
                    TableView<Content> table = resultsTableController.getResultsTable();
                    if (table != null) {
                        // Set up selection model
                        TableView.TableViewSelectionModel<Content> selectionModel = table.getSelectionModel();
                        selectionModel.setSelectionMode(SelectionMode.SINGLE);

                        // Initialize the manage details button state
                        if (manageDetailsButton != null) {
                            // Initial state
                            manageDetailsButton.setDisable(true);

                            // Update button state when selection changes
                            selectionModel.selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
                                manageDetailsButton.setDisable(newSelection == null);

                                // Update button style based on state
                                if (newSelection == null) {
                                    manageDetailsButton.setStyle(
                                        "-fx-background-color: transparent; " +
                                        "-fx-text-fill: #9E9E9E; " +
                                        "-fx-border-color: #9E9E9E; " +
                                        "-fx-border-width: 1.5; " +
                                        "-fx-border-radius: 4; " +
                                        "-fx-padding: 5 10; " +
                                        "-fx-font-weight: bold;"
                                    );
                                } else {
                                    manageDetailsButton.setStyle(
                                        "-fx-background-color: transparent; " +
                                        "-fx-text-fill: #4CAF50; " +
                                        "-fx-border-color: #4CAF50; " +
                                        "-fx-border-width: 1.5; " +
                                        "-fx-border-radius: 4; " +
                                        "-fx-padding: 5 10; " +
                                        "-fx-font-weight: bold; " +
                                        "-fx-cursor: hand; " +
                                        "-fx-effect: dropshadow(gaussian, rgba(76,175,80,0.3), 5, 0, 0, 1);"
                                    );
                                }
                            });
                        }
                    }

                    // Set up the results table controller
                    if (resultsTableController != null) {
                        // Set up the table with the controller's table view
                        resultsTable = resultsTableController.getResultsTable();

                        // Set up double-click handler for table rows
                        resultsTable.setOnMouseClicked(event -> {
                            if (event.getClickCount() == 2) {
                                handleTableClick();
                            }
                        });

                        // Bind the manage details button's disable property to the table's selection model
                        resultsTable.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
                            if (newSelection != null) {
                                // Enable the manage details button when an item is selected
                                // This is handled by the FXML binding, but we keep this for any additional logic
                            }
                        });
                    }

                    updateStatus("Enter search criteria");
                } catch (Exception e) {
                    logger.error("Error initializing table components", e);
                    showError("Initialization Error", "Failed to initialize table components: " + e.getMessage());
                }
            }
        } catch (Exception e) {
            logger.error("Error initializing AdvancedSearchController", e);
            showError("Initialization Error", "Failed to initialize search interface: " + e.getMessage());
        }
    }

    /**
     * Checks if the search criteria contains any search terms.
     *
     * @param criteria The search criteria to check
     * @return true if the criteria contains search terms, false otherwise
     */
    private boolean hasSearchCriteria(SearchCriteria criteria) {
        if (criteria == null) {
            return false;
        }
        
        // Check for search terms in title or query (not empty and not just whitespace)
        if ((criteria.getTitle() != null && !criteria.getTitle().trim().isEmpty()) ||
            (criteria.getQuery() != null && !criteria.getQuery().trim().isEmpty())) {
            return true;
        }
        
        // Check for valid year range (at least one year specified)
        if (criteria.getMinYear() != null || criteria.getEndYear() != null) {
            // If either year is specified, it's a valid search
            return true;
        }
        
        // Check for rating (greater than 0 or less than 10)
        if ((criteria.getMinImdbRating() != null && criteria.getMinImdbRating() > 0) || 
            (criteria.getMaxImdbRating() != null && criteria.getMaxImdbRating() < 10)) {
            return true;
        }
        
        // Check for content type (both checkboxes can't be unchecked)
        if (criteria.getContentType() != null) {
            return true;
        }
        
        // Check for genres (at least one genre selected)
        return criteria.getGenres() != null && !criteria.getGenres().isEmpty();
        
        // No valid search criteria found
    }

    /**
     * Navigates back to the home view.
     */
    @FXML
    public void goToHome() {
        try {
            navigationService.navigateTo(
                "/fxml/base/home-view.fxml",
                    data, getStage(),
                "IMDb Clone - Home"
            );
        } catch (Exception e) {
            logger.error("Error navigating to home view", e);
            showError("Navigation Error", "Failed to navigate to home view: " + e.getMessage());
        }
    }

    /**
     * Performs a search with the given criteria.
     *
     * @param criteria The search criteria to use
     */
    private void performSearch(SearchCriteria criteria) {
        if (criteria == null) {
            logger.warn("Search criteria is null");
            showInfo("Search", "No search criteria provided");
            return;
        }

        // Log the search criteria for debugging
        logger.info("Performing search with criteria: {}", criteria);

        // Cancel any ongoing search
        if (currentSearchTask != null && currentSearchTask.isRunning()) {
            logger.info("Cancelling previous search task");
            currentSearchTask.cancel();
        }

        // Make sure the results table is available
        if (resultsTable == null) {
            logger.error("Results table is not available");
            showError("Error", "Search results cannot be displayed");
            return;
        }

        // Clear previous results
        resultsTable.getItems().clear();
        
        // Update status
        updateStatus("Searching...");
        // Make sure the table is visible and managed
        resultsTable.setVisible(true);
        resultsTable.setManaged(true);
        
        // Set up the table's selection model if not already done
        resultsTable.getSelectionModel().setSelectionMode(SelectionMode.SINGLE);
        
        // Log table state before search
        logger.info("Table state before search - Items: {}, Columns: {}", 
            resultsTable.getItems().size(), 
            resultsTable.getColumns().size());

        // Update status
        updateStatus("Searching...");

        // Create a new search task
        currentSearchTask = new Task<ObservableList<Content>>() {
            @Override
            protected ObservableList<Content> call() throws Exception {
                try {
                    // Perform the search using the search service
                    List<Content> results = searchService.search(criteria);
                    logger.info("Found {} results for search criteria: {}", results.size(), criteria);
                    
                    // Update the UI on the JavaFX Application Thread
                    Platform.runLater(() -> {
                        try {
                            // Update the table with search results
                            ObservableList<Content> observableResults = FXCollections.observableArrayList(results);
                            resultsTable.setItems(observableResults);
                            
                            // Update the results count label
                            if (resultsCountLabel != null) {
                                resultsCountLabel.setText(String.format("Results: %d", results.size()));
                            }
                            
                            // Update status
                            updateStatus(String.format("Found %d results", results.size()));
                            
                            // Log the update
                            logger.info("Updated table with {} items", results.size());
                            
                            // Force a refresh of the table
                            resultsTable.refresh();
                            
                        } catch (Exception e) {
                            logger.error("Error updating table with search results", e);
                            showError("Error", "Failed to display search results: " + e.getMessage());
                        }
                    });

                    //Return the results
                    return FXCollections.observableArrayList(results);
                } catch (Exception e) {
                    logger.error("Error during search: {}", e.getMessage(), e);
                    Platform.runLater(() -> {
                        showError("Search Error", "Failed to perform search: " + e.getMessage());
                        updateStatus("Search failed");
                        
                        // Clear the results table on error
                        if (resultsTableController != null) {
                            resultsTableController.clearResults();
                        }
                        
                        // Clear the results count label
                        if (resultsCountLabel != null) {
                            resultsCountLabel.setText("");
                        }
                        updateStatus("Search failed");
                    });
                    throw e;
                }
            }
        };

        // Handle successful search completion
        currentSearchTask.setOnSucceeded(event -> {
            try {
                // Results are already handled in the call() method through Platform.runLater
                // This is a fallback in case the Platform.runLater in call() didn't execute
                ObservableList<Content> results = currentSearchTask.getValue();
                if (results != null && resultsTableController != null) {
                    resultsTableController.setResults(results);
                    
                    // Update the results count label if not already updated
                    if (resultsCountLabel != null) {
                        resultsCountLabel.setText(String.format("Results: %d", results.size()));
                    }
                    
                    updateStatus(String.format("Found %d results", results.size()));
                }
                //Assert that results are not null which means the search was successful
                assert results != null;
                logger.info("Search completed. Found {} results.", results.size());

                // Log the first few results for debugging
                int maxResultsToLog = Math.min(5, results.size());
                for (int i = 0; i < maxResultsToLog; i++) {
                    Content content = results.get(i);
                    logger.debug("Result {}: {} (ID: {})", i + 1, content.getTitle(), content.getId());
                }

                if (resultsTableController != null) {
                    // Make sure the table is visible
                    if (resultsTableController.getResultsTable() != null) {
                        resultsTableController.getResultsTable().setVisible(true);
                        logger.debug("Table visibility set to: {}", resultsTableController.getResultsTable().isVisible());
                    }

                    // Update the results using the controller's method
                    resultsTableController.updateResults(results);
                    logger.debug("Results passed to resultsTableController.updateResults()");

                    // Update the results count label
                    if (resultsCountLabel != null) {
                        String countText = String.format("Found %d result%s", results.size(), results.size() != 1 ? "s" : "");
                        resultsCountLabel.setText(countText);
                        logger.debug("Results count label updated to: {}", countText);
                    }

                    // Make sure the Manage Details button is properly set up
                    logger.debug("Table has {} items", resultsTable.getItems() != null ? resultsTable.getItems().size() : 0);
                    logger.debug("Table columns: {}", resultsTable.getColumns());

                    if (manageDetailsButton != null) {
                        // Update the button state based on current selection
                        manageDetailsButton.setDisable(resultsTable.getSelectionModel().getSelectedItem() == null);
                        logger.debug("Manage Details button disabled state: {}", manageDetailsButton.isDisabled());
                    }
                    updateStatus(String.format("Found %d results", results.size()));
                }
            } catch (Exception e){
                logger.error("Error performing search", e);
                showError("Search Error", "Failed to perform search: " + e.getMessage());
                updateStatus("Search failed");
            }
        });

        // Handle search errors
        currentSearchTask.setOnFailed(event -> {
            Throwable e = currentSearchTask.getException();
            String errorMsg = "Search failed: " + (e != null ? e.getMessage() : "Unknown error");
            logger.error(errorMsg, e);
            showError("Search Error", errorMsg);
            updateStatus("Search failed");
        });

        // Run the search in a background thread
        Thread searchThread = new Thread(currentSearchTask);
        searchThread.setDaemon(true);
        searchThread.start();
    }

    /**
     * Navigates to the content details view for the specified content.
     *
     * @param content The content to view details for
     */
    private void navigateToContentDetails(Content content) {
        if (content == null) {
            logger.warn("Attempted to navigate to null content");
            return;
        }

        try {
            // TODO: Implement navigation to content details view
            // For now, just show an info dialog
            showInfo("Content Details", "Viewing details for: " + content.getTitle());
        } catch (Exception e) {
            logger.error("Error navigating to content details", e);
            showError("Navigation Error", "Could not open content details: " + e.getMessage());
        }
    }

    /**
     * Handles the manage details button click event.
     * Opens the edit view for the selected content item.
     */
    @FXML
    public void handleManageDetails() {
        try {
            // Get the selected content from the table
            Content selectedContent = null;
            if (resultsTableController != null && resultsTableController.getResultsTable() != null) {
                selectedContent = resultsTableController.getResultsTable().getSelectionModel().getSelectedItem();
            } else if (resultsTable != null && resultsTable.getSelectionModel() != null) {
                selectedContent = (Content) resultsTable.getSelectionModel().getSelectedItem();
            }
            
            if (selectedContent == null) {
                showInfo("No Selection", "Please select a valid item to manage details.");
                return;
            }
            
            logger.info("Opening edit view for content: {}", selectedContent.getTitle());
            
            // Create a map to pass data to the edit view
            Map<String, Object> data = new HashMap<>();
            data.put("contentId", selectedContent.getId());
            data.put("contentType", selectedContent.getContentType());
            
            // Navigate to the edit view with the content ID
            navigationService.navigateTo(
                "/fxml/content/edit-content-view.fxml",
                data,
                getStage(),
                "Edit " + selectedContent.getTitle()
            );
            
            logger.info("Successfully navigated to edit view for content ID: {}", selectedContent.getId());
            
        } catch (Exception e) {
            logger.error("Error navigating to edit content view: {}", e.getMessage(), e);
            showError("Error", "Failed to open edit view. Please try again.");
        }
    }

    
    /**
     * Handles the table click event.
     * If an item is selected, it will navigate to the content details.
     */
    @FXML
    public void handleTableClick() {
        if (resultsTable != null) {
            @SuppressWarnings("unchecked")
            TableView<Content> tableView = (TableView<Content>) resultsTable;
            Content selectedContent = tableView.getSelectionModel().getSelectedItem();
            if (selectedContent != null) {
                navigateToContentDetails(selectedContent);
            }
        }
    }
}
