package com.papel.imdb_clone.controllers.search;

import com.papel.imdb_clone.controllers.content.ContentDetailsController;
import com.papel.imdb_clone.model.content.Content;
import com.papel.imdb_clone.model.content.Movie;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Controller for handling the results table display and interactions.
 */
public class ResultsTableController extends BaseSearchController {
    private static final Logger logger = LoggerFactory.getLogger(ResultsTableController.class);

    @FXML
    private TableView<Content> resultsTable;
    @FXML
    private TableColumn<Content, String> resultTitleColumn;
    @FXML
    private TableColumn<Content, Integer> resultYearColumn;
    @FXML
    private TableColumn<Content, String> resultTypeColumn;
    @FXML
    private TableColumn<Content, String> resultGenreColumn;
    @FXML
    private TableColumn<Content, String> resultImdbColumn;
    @FXML
    private TableColumn<Content, String> resultDirectorColumn;
    @FXML
    private TableColumn<Content, Integer> resultSeasonsColumn;
    @FXML
    private TableColumn<Content, Integer> resultEpisodesColumn;
    @FXML
    private Label resultsCountLabel;

    private final ObservableList<Content> searchResults = FXCollections.observableArrayList();

    /**
     * Gets the TableView instance.
     *
     * @return The TableView containing search results
     */
    public TableView<Content> getResultsTable() {
        return resultsTable;
    }

    @FXML
    public void initialize() {
        try {
            logger.info("Initializing ResultsTableController...");
            
            // First, set up the table columns
            setupTableColumns();
            
            // Set up the row double-click handler
            setupRowDoubleClickHandler();
            
            // Make sure the table is properly initialized
            if (resultsTable == null) {
                logger.error("Results table is null in initialize()");
                return;
            }
            
            // Set the items in the table
            resultsTable.setItems(searchResults);
            
            // Log the table and column states
            logger.info("Table initialized with columns: " + resultsTable.getColumns());
            logger.info("Table items count: " + (resultsTable.getItems() != null ? resultsTable.getItems().size() : 0));
            
        } catch (Exception e) {
            logger.error("Error initializing ResultsTableController", e);
        }
    }

    /**
     * Sets the search results in the table.
     *
     * @param results The list of content items to display
     */
    public void setResults(ObservableList<Content> results) {
        if (results == null) {
            logger.warn("Attempted to set null results");
            searchResults.clear();
            return;
        }
        searchResults.setAll(results);
        updateResultsCount(results.size());
    }

    /**
     * Updates the table with new search results.
     *
     * @param results The list of content items to display
     */
    public void updateResults(List<Content> results) {
        logger.info("=== Starting updateResults ===");
        logger.info("Received results list: " + (results != null ? results.size() : "null") + " items");
        
        if (results == null || results.isEmpty()) {
            logger.warn("No results to display - results list is " + (results == null ? "null" : "empty"));
            searchResults.clear();
            updateResultsCount(0);
            return;
        }
        
        // Log first few items to verify data
        int itemsToLog = Math.min(3, results.size());
        logger.info("First " + itemsToLog + " items in results:");
        for (int i = 0; i < itemsToLog; i++) {
            Content item = results.get(i);
            logger.info("  " + (i+1) + ". " + item.getTitle() + " (ID: " + item.getId() + ")");
        }
        
        // Clear the current items
        logger.debug("Clearing current search results (current size: " + searchResults.size() + ")");
        searchResults.clear();
        
        // Add all new items
        logger.debug("Adding " + results.size() + " new items to searchResults");
        searchResults.addAll(results);
        
        // Verify the items were added
        logger.info("searchResults size after update: " + searchResults.size());
        
        // Update the results count
        updateResultsCount(searchResults.size());
        
        // Log the table state
        if (resultsTable != null) {
            logger.info("Table state after update:");
            logger.info("  - Table items: " + (resultsTable.getItems() != null ? resultsTable.getItems().size() : "null"));
            logger.info("  - Table columns: " + (resultsTable.getColumns() != null ? resultsTable.getColumns().size() : "null"));
            
            // Log column names
            if (resultsTable.getColumns() != null && !resultsTable.getColumns().isEmpty()) {
                logger.info("  - Column names: " + resultsTable.getColumns().stream()
                    .map(col -> col.getText())
                    .collect(Collectors.joining(", ")));
            }
            
            // Force refresh of the table
            resultsTable.refresh();
        } else {
            logger.error("resultsTable is null in updateResults!");
        }
        
        logger.info("=== Finished updateResults ===");
    }

    /**
     * Clears all results from the table.
     */
    public void clearResults() {
        searchResults.clear();
        updateResultsCount(0);
    }

    /**
     * Updates the results count label.
     *
     * @param count The number of results found
     */
    private void updateResultsCount(int count) {
        if (resultsCountLabel != null) {
            resultsCountLabel.setText(String.format("Found %d result%s", count, count != 1 ? "s" : ""));
        } else {
            logger.debug("Results count label is not available");
        }
    }

    /**
     * Sets up the table columns with appropriate cell value factories and cell factories.
     */
    private void setupTableColumns() {
        if (resultsTable == null) {
            logger.error("Results table is not initialized");
            return;
        }

        logger.info("Setting up table columns...");

        try {
            // Clear existing columns to avoid duplicates
            resultsTable.getColumns().clear();

            // Set up title column with tooltip
            if (resultTitleColumn != null) {
                logger.debug("Setting up title column");
                resultTitleColumn.setCellValueFactory(cellData -> {
                    Content content = cellData.getValue();
                    return content != null ?
                            new SimpleStringProperty(content.getTitle()) :
                            new SimpleStringProperty("");
                });

                resultTitleColumn.setCellFactory(column -> new TableCell<>() {
                    private final Tooltip tooltip = new Tooltip();

                    @Override
                    protected void updateItem(String item, boolean empty) {
                        super.updateItem(item, empty);
                        if (empty || item == null) {
                            setText(null);
                            setTooltip(null);
                        } else {
                            setText(item);
                            tooltip.setText(item);
                            tooltip.setStyle("-fx-font-size: 12px; -fx-padding: 5px;");
                            setTooltip(tooltip);
                        }
                    }
                });

                // Make sure the column is added to the table
                if (!resultsTable.getColumns().contains(resultTitleColumn)) {
                    resultsTable.getColumns().add(resultTitleColumn);
                }
            }

            // Set up type column
            if (resultTypeColumn != null) {
                logger.debug("Setting up type column");
                resultTypeColumn.setCellValueFactory(cellData -> {
                    Content content = cellData.getValue();
                    if (content == null) return new SimpleStringProperty("");
                    String type = content instanceof Movie ? "Movie" : "Series";
                    return new SimpleStringProperty(type);
                });
                
                if (!resultsTable.getColumns().contains(resultTypeColumn)) {
                    resultsTable.getColumns().add(resultTypeColumn);
                }
            }

            // Set up year column
            if (resultYearColumn != null) {
                logger.debug("Setting up year column");
                resultYearColumn.setCellValueFactory(cellData -> {
                    Content content = cellData.getValue();
                    return content != null ? 
                           new SimpleIntegerProperty(content.getStartYear()).asObject() : 
                           new SimpleIntegerProperty(0).asObject();
                });
                
                if (!resultsTable.getColumns().contains(resultYearColumn)) {
                    resultsTable.getColumns().add(resultYearColumn);
                }
            }

            // Set up genre column
            if (resultGenreColumn != null) {
                logger.debug("Setting up genre column");
                resultGenreColumn.setCellValueFactory(cellData -> {
                    Content content = cellData.getValue();
                    if (content == null || content.getGenres() == null) {
                        return new SimpleStringProperty("");
                    }

                    String genres = content.getGenres().stream()
                            .map(Enum::name)
                            .map(name -> name.charAt(0) + name.substring(1).toLowerCase())
                            .collect(Collectors.joining(", "));

                    return new SimpleStringProperty(genres);
                });
                
                if (!resultsTable.getColumns().contains(resultGenreColumn)) {
                    resultsTable.getColumns().add(resultGenreColumn);
                }
            }

            // Set up IMDb rating column
            if (resultImdbColumn != null) {
                logger.debug("Setting up IMDb rating column");
                resultImdbColumn.setCellValueFactory(cellData -> {
                    Content content = cellData.getValue();
                    if (content == null || content.getImdbRating() == null || content.getImdbRating() <= 0) {
                        return new SimpleStringProperty("N/A");
                    }
                    return new SimpleStringProperty(String.format("%.1f", content.getImdbRating()));
                });
                
                if (!resultsTable.getColumns().contains(resultImdbColumn)) {
                    resultsTable.getColumns().add(resultImdbColumn);
                }
            }

            // Set up director column
            if (resultDirectorColumn != null) {
                logger.debug("Setting up director column");
                resultDirectorColumn.setCellValueFactory(cellData -> {
                    Content content = cellData.getValue();
                    String director = (content != null && content.getDirector() != null) ?
                            content.getDirector() : "N/A";
                    return new SimpleStringProperty(director);
                });
                
                if (!resultsTable.getColumns().contains(resultDirectorColumn)) {
                    resultsTable.getColumns().add(resultDirectorColumn);
                }
            }
            
            logger.info("Table columns setup completed. Total columns: " + resultsTable.getColumns().size());
            
        } catch (Exception e) {
            logger.error("Error setting up table columns", e);
        }    }

    /**
     * Shows the content details for the selected content item.
     *
     * @param content The content to show details for
     */
    protected void showContentDetails(Content content) {
        if (content == null) {
            logger.warn("Cannot show details for null content");
            return;
        }

        try {
            // Create data map with content ID
            Map<String, Object> contentData = new HashMap<>();
            contentData.put("contentId", content.getId());
            contentData.put("contentType", content.getContentType());
            
            // Navigate to content details view
            String fxmlPath = "/fxml/content/content-details-view.fxml";
            navigationService.navigateTo(fxmlPath, contentData, getStage(), content.getTitle());

            // Get the controller and pass the content
            ContentDetailsController controller = (ContentDetailsController) navigationService.getCurrentController();
            if (controller != null) {
                controller.setContent(content);
            }
        } catch (Exception e) {
            logger.error("Error navigating to content details", e);
            showError("Navigation Error", "Could not open content details: " + e.getMessage());
        }
    }

    /**
     * Sets up the row double-click handler for the results table.
     */
    private void setupRowDoubleClickHandler() {
        if (resultsTable == null) {
            logger.warn("Cannot set up row click handler: results table is null");
            return;
        }

        resultsTable.setRowFactory(tv -> {
            TableRow<Content> row = new TableRow<>();

            // Handle double-click on row
            row.setOnMouseClicked(event -> {
                if (event.getClickCount() == 2 && !row.isEmpty()) {
                    Content content = row.getItem();
                    showContentDetails(content);
                }
            });

            // Add hover effect
            row.hoverProperty().addListener((obs, wasHovered, isNowHovered) -> {
                if (isNowHovered && !row.isEmpty()) {
                    row.setStyle("-fx-background-color: #f0f0f0; -fx-cursor: hand;");
                } else {
                    row.setStyle("");
                }
            });

            return row;
        });
    }


    /**
     * Sets up the type column in the results table
     */
    private void setupTypeColumn() {
        if (resultTypeColumn != null) {
            resultTypeColumn.setCellValueFactory(cellData -> {
                Content content = cellData.getValue();
                if (content == null) return new SimpleStringProperty("");
                String type = content instanceof Movie ? "Movie" : "Series";
                return new SimpleStringProperty(type);
            });
        }
    }
    public void handleManageDetails() {
        Content selected = getSelectedContent();
        if (selected != null) {
            showContentDetails(selected);
        } else {
            showError("No Selection", "Please select an item first.");
        }
    }

    public Content getSelectedContent() {
        return resultsTable != null ? resultsTable.getSelectionModel().getSelectedItem() : null;
    }

}
