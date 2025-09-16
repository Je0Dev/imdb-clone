package com.papel.imdb_clone.controllers.coordinator;

import com.papel.imdb_clone.config.ApplicationConfig;
import com.papel.imdb_clone.controllers.ContentController;
import com.papel.imdb_clone.data.RefactoredDataManager;
import com.papel.imdb_clone.model.User;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Map;
import java.util.function.Supplier;

/**
 * Coordinates UI components and manages controller lifecycle.
 * Responsible for loading FXML views and initializing controllers.
 * This class serves as the central coordinator for UI navigation and view management.
 */
public class UICoordinator {
    private static final Logger logger = LoggerFactory.getLogger(UICoordinator.class);

    private final RefactoredDataManager dataManager;
    private Stage primaryStage;
    private User currentUser;
    private String sessionToken;

    // Controllers
    private ContentController contentController;

    // Views
    private Node movieView;
    private Node seriesView;
    private Node searchView;
    private Node homeView;

    private boolean isViewLoading = false;

    /**
     * Constructs a new UICoordinator with the specified data manager.
     *
     * @param dataManager The data manager instance to be used for data operations
     * @throws IllegalArgumentException if dataManager is null
     */
    public UICoordinator(RefactoredDataManager dataManager) {
        if (dataManager == null) {
            throw new IllegalArgumentException("DataManager cannot be null");
        }
        this.dataManager = dataManager;
        logger.info("UICoordinator initialized");
    }

    /**
     * Sets the primary stage for the application with proper initialization.
     *
     * @param primaryStage The primary stage instance (must not be null)
     * @throws IllegalArgumentException if primaryStage is null
     */
    public void setPrimaryStage(Stage primaryStage) {
        if (primaryStage == null) {
            logger.error("Attempted to set null primary stage in UICoordinator");
            throw new IllegalArgumentException("Primary stage cannot be null");
        }

        // Only update if this is a new stage
        if (this.primaryStage != primaryStage) {
            logger.info("Setting primary stage in UICoordinator");
            this.primaryStage = primaryStage;

            // Get configuration instance
            ApplicationConfig config = ApplicationConfig.getInstance();

            // Set minimum window size from config
            primaryStage.setMinWidth(config.getMinWidth());
            primaryStage.setMinHeight(config.getMinHeight());

            // Set initial window size from config if not already set by application.properties-ApplicationConfig
            if (primaryStage.getWidth() < config.getMinWidth()) {
                primaryStage.setWidth(config.getMinWidth());
            }
            if (primaryStage.getHeight() < config.getMinHeight()) {
                primaryStage.setHeight(config.getMinHeight());
            }
        }
    }

    /**
     * Sets the current user and session information.
     */
    public void setUserSession(User currentUser, String sessionToken) {
        this.currentUser = currentUser;
        this.sessionToken = sessionToken;
    }


    /**
     * Loads and initializes all views and their controllers.
     *
     * @return true if all views were loaded successfully, false otherwise
     * @throws IllegalArgumentException if primary stage is null
     */
    public boolean loadAndInitializeViews() throws IllegalArgumentException {
        if (primaryStage == null) {
            logger.error("Cannot load views: Primary stage is not set");
            return false;
        }

        logger.info("Loading and initializing all views");
        boolean allViewsLoaded = true;

        // First, ensure the content view is loaded
        if (homeView == null) {
            try {
                loadContentView();
                logger.info("Successfully loaded content view");
            } catch (Exception e) {
                logger.error("Critical: Failed to load content view: {}", e.getMessage(), e);
                return false; // Can't continue without content view-main gui view of the project
            }
        }

        // Define views to load with their names for better error reporting:supplier is a functional interface that returns a value
        Map<String, Supplier<Node>> viewsToLoad = Map.of(
                "movie view", () -> movieView = loadViewSafely("/fxml/movie-view.fxml"),
                "series view", () -> seriesView = loadViewSafely("/fxml/series-view.fxml"),
                "search view", () -> searchView = loadViewSafely("/fxml/advanced-search-view.fxml")
        );

        // Load each view and track failures
        for (Map.Entry<String, Supplier<Node>> entry : viewsToLoad.entrySet()) {
            String viewName = entry.getKey();
            logger.debug("Loading {}...", viewName);

            try {
                // Load the view using the supplier
                Node view = entry.getValue().get();
                if (view == null) {
                    logger.error("Failed to load {}", viewName);
                    allViewsLoaded = false;
                } else {
                    // Log success of loading the view
                    logger.debug("Successfully loaded {}", viewName);
                }
            } catch (IllegalArgumentException e) {
                logger.error("Error loading {}: {}", viewName, e.getMessage(), e);
                allViewsLoaded = false;
            }
        }

        // Log the result of the view loading process
        if (!allViewsLoaded) {
            logger.warn("Some views failed to load, but continuing with available views");
        } else {
            logger.info("All views loaded successfully");
        }

        return allViewsLoaded;
    }

    /**
     * Safely loads a view and returns null if loading fails.
     * Errors are logged but not rethrown.
     *
     * @param fxmlPath the path to the FXML file relative to the resources directory
     * @return the loaded JavaFX Node or null if loading fails
     */
    private Node loadViewSafely(String fxmlPath) {
        try {
            // Load the view using the supplier
            return loadView(fxmlPath);
        } catch (Exception e) {
            logger.error("Failed to load view {}: {}", fxmlPath, e.getMessage(), e);
            return null;
        }
    }

    /**
     * Loads an FXML view from the specified path.
     *
     * @param fxmlPath the path to the FXML file relative to the resources directory
     * @return the loaded JavaFX Node
     * @throws IOException if the FXML file cannot be loaded or parsed
     */
    private Node loadView(String fxmlPath) throws IOException {
        logger.debug("Loading view: {}", fxmlPath);
        java.net.URL resourceUrl = null;
        
        try {
            // Get the resource URL for better error reporting
            resourceUrl = getClass().getResource(fxmlPath);
            if (resourceUrl == null) {
                // Try with a leading slash if not found
                if (!fxmlPath.startsWith("/")) {
                    resourceUrl = getClass().getResource("/" + fxmlPath);
                }
                
                if (resourceUrl == null) {
                    String errorMsg = String.format("FXML file not found: %s. Searched in classpath: %s",
                            fxmlPath,
                            System.getProperty("java.class.path"));
                    logger.error(errorMsg);
                    throw new IOException(errorMsg);
                }
            }

            logger.debug("Loading FXML from URL: {}", resourceUrl);
            FXMLLoader loader = new FXMLLoader(resourceUrl);

            // Set controller factory if needed
            if (contentController != null) {
                loader.setController(contentController);
            }

            try {
                Node view = loader.load();
                logger.info("Successfully loaded view: {}", fxmlPath);
                return view;
            } catch (Exception e) {
                String errorMsg = String.format("Failed to load FXML %s: %s", fxmlPath, e.getMessage());
                logger.error(errorMsg, e);
                
                // Check for common FXML loading issues
                if (e.getCause() != null) {
                    logger.error("Root cause: {}", e.getCause().getMessage());
                }
                
                throw new IOException(errorMsg, e);
            }
        } catch (IOException e) {
            logger.error("I/O error loading view {}: {}", fxmlPath, e.getMessage(), e);
            throw e;
        } catch (Exception e) {
            logger.error("Unexpected error loading view {}: {}", fxmlPath, e.getMessage(), e);
            throw new IOException("Failed to load view: " + e.getMessage(), e);
        }
    }

    /**
     * Loads the main content view of the application.
     * This is a special case view that needs to be loaded first as it contains the main layout.
     *
     * @throws IOException if the home view FXML file cannot be loaded
     */
    private synchronized void loadContentView() throws IOException {
        if (homeView != null) {
            return; // Already loaded
        }

        if (isViewLoading) {
            logger.warn("Attempted to load content view while another load is in progress");
            return;
        }

        // Set the loading flag to prevent concurrent loads
        isViewLoading = true;
        try {
            logger.info("Loading main content view...");
            homeView = loadView("/fxml/main-refactored.fxml");
            logger.info("Successfully loaded home view");
        } finally {
            isViewLoading = false;
        }
    }

    /**
     * Gets the movie view component.
     *
     * @return the Node containing the movie view, or null if not loaded
     */
    public Node getMovieView() {
        if (movieView == null) {
            logger.warn("Movie view was not preloaded, attempting to load on demand");
            movieView = loadViewSafely("/fxml/movie-view.fxml");
            if (movieView != null) {
                logger.info("Successfully loaded movie view on demand");
            } else {
                logger.error("Failed to load movie view on demand");
            }
        }
        return movieView;
    }

    /**
     * Gets the series view component.
     *
     * @return the Node containing the series view, or null if not loaded
     */
    public Node getSeriesView() {
        if (seriesView == null) {
            logger.warn("Series view was not preloaded, attempting to load on demand");
            seriesView = loadViewSafely("/fxml/series-view.fxml");
        }
        return seriesView;
    }

    /**
     * Gets the search view component.
     *
     * @return the Node containing the search view, or null if not loaded
     */
    public Node getSearchView() {
        if (searchView == null) {
            logger.warn("Search view was not preloaded, attempting to load on demand");
            searchView = loadViewSafely("/fxml/advanced-search-view.fxml");
        }
        return searchView;
    }

    /**
     * Gets the home view component.
     *
     * @return the Node containing the home view, or null if not loaded
     */
    public Node getHomeView() {
        if (homeView == null) {
            logger.warn("Home view was not preloaded, attempting to load on demand");
            try {
                loadContentView();
                logger.info("Successfully loaded home view on demand");
            } catch (IOException e) {
                logger.error("Failed to load home view on demand: {}", e.getMessage(), e);
                return null;
            }
        }
        return homeView;
    }

        /**
     * Creates an error node to display when view loading fails
     */
    private Node createErrorNode(String message) {
        Label errorLabel = new Label(message);
        errorLabel.setStyle("-fx-text-fill: #ff4444; -fx-padding: 20; -fx-font-size: 14;");
        errorLabel.setWrapText(true);
        return errorLabel;
    }

    /**
     * Checks if all views are loaded
     */
    public boolean areViewsLoaded() {
        return homeView != null && movieView != null && seriesView != null && searchView != null;
    }
}