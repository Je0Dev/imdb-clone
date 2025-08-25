package com.papel.imdb_clone.controllers.coordinator;

import com.papel.imdb_clone.config.ApplicationConfig;
import com.papel.imdb_clone.controllers.ContentController;
import com.papel.imdb_clone.controllers.RefactoredMainController;
import com.papel.imdb_clone.data.RefactoredDataManager;
import com.papel.imdb_clone.model.User;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

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
     * Sets the primary stage for the application.
     *
     * @param primaryStage The primary stage instance
     */
    /**
     * Sets the primary stage for the application with proper initialization.
     *
     * @param primaryStage The primary stage instance (must not be null)
     * @throws IllegalArgumentException if primaryStage is null
     */
    public void setPrimaryStage(Stage primaryStage) {
        if (primaryStage == null) {
            throw new IllegalArgumentException("Primary stage cannot be null");
        }
        this.primaryStage = primaryStage;

        // Get configuration instance
        ApplicationConfig config = ApplicationConfig.getInstance();

        // Set minimum window size from config
        primaryStage.setMinWidth(config.getMinWidth());
        primaryStage.setMinHeight(config.getMinHeight());

        // Set initial window size from config
        primaryStage.setWidth(config.getMinWidth());
        primaryStage.setHeight(config.getMinHeight());
    }

    /**
     * Sets the current user and session information.
     */
    public void setUserSession(User currentUser, String sessionToken) {
        this.currentUser = currentUser;
        this.sessionToken = sessionToken;

        // Update existing controllers if they're already loaded
        updateControllersWithUserSession();
    }

    /**
     * Updates all initialized controllers with the current user session information.
     * This method ensures that all controllers have access to the current user's session data.
     */
    private void updateControllersWithUserSession() {
        if (contentController != null) {
            contentController.setUserSession(currentUser, sessionToken);
        }
    }

    /**
     * Loads and initializes all views and their controllers.
     */
    public boolean loadAndInitializeViews() {
        logger.info("Loading and initializing all views");
        boolean allViewsLoaded = true;

        try {
            // Load content view first
            loadContentView();
            
            // Load other views
            try {
                logger.debug("Loading movie view");
                movieView = loadView("/fxml/movie-view.fxml");
            } catch (Exception e) {
                logger.error("Failed to load movie view: {}", e.getMessage(), e);
                allViewsLoaded = false;
            }
            
            try {
                logger.debug("Loading series view");
                seriesView = loadView("/fxml/series-view.fxml");
            } catch (Exception e) {
                logger.error("Failed to load series view: {}", e.getMessage(), e);
                allViewsLoaded = false;
            }
            
            try {
                logger.debug("Loading search view");
                searchView = loadView("/fxml/search-view.fxml");
            } catch (Exception e) {
                logger.error("Failed to load search view: {}", e.getMessage(), e);
                allViewsLoaded = false;
            }
            
        } catch (Exception e) {
            logger.error("Critical error loading views: {}", e.getMessage(), e);
            return false;
        }

        if (!allViewsLoaded) {
            logger.warn("Some views failed to load, but continuing with available views");
        }
        
        logger.info("View loading completed. Success: {}", allViewsLoaded);
        return allViewsLoaded;
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
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            if (loader.getLocation() == null) {
                throw new IOException("FXML file not found: " + fxmlPath);
            }
            Node view = loader.load();
            
            // Initialize controller if needed
            Object controller = loader.getController();
            if (controller instanceof RefactoredMainController) {
                ((RefactoredMainController) controller).setPrimaryStage(primaryStage);
            }
            
            return view;
        } catch (Exception e) {
            logger.error("Error loading view {}: {}", fxmlPath, e.getMessage(), e);
            throw new IOException("Failed to load view: " + fxmlPath, e);
        }
    }
    
    /**
     * Loads the main content view of the application.
     * This is a special case view that needs to be loaded first as it contains the main layout.
     *
     * @throws IOException if the home view FXML file cannot be loaded
     */
    private void loadContentView() throws IOException {
        logger.debug("Loading home view");
        try {
            homeView = loadView("/fxml/main-refactored.fxml");
        } catch (Exception e) {
            logger.error("Failed to load home view: {}", e.getMessage(), e);
            throw new IOException("Failed to load home view", e);
        }
    }


    /**
     * Gets the movie view component.
     *
     * @return the Node containing the movie view, or null if not loaded
     */
    public Node getMovieView() {
        return movieView;
    }


    /**
     * Gets the series view component.
     *
     * @return the Node containing the series view, or null if not loaded
     */
    public Node getSeriesView() {
        return seriesView;
    }


    /**
     * Gets the search view component.
     *
     * @return the Node containing the search view, or null if not loaded
     */
    public Node getSearchView() {
        return searchView;
    }

    /**
     * Gets the home view component.
     *
     * @return the Node containing the home view, or null if not loaded
     */
    public Node getHomeView() {
        return homeView;
    }
}