package com.papel.imdb_clone.controllers.coordinator;

import com.papel.imdb_clone.controllers.ContentController;
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
    private Node watchlistView;
    private Node movieView;
    private Node seriesView;
    private Node searchView;
    private Node homeView;

    public UICoordinator(RefactoredDataManager dataManager) {
        this.dataManager = dataManager;
        logger.info("UICoordinator initialized");
    }

    /**
     * Sets the primary stage for the application.
     */
    public void setPrimaryStage(Stage primaryStage) {
        this.primaryStage = primaryStage;
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

    private void updateControllersWithUserSession() {
        if (contentController != null) {
            contentController.setUserSession(currentUser, sessionToken);
        }
    }

    /**
     * Loads and initializes all views and their controllers.
     */
    public boolean loadAndInitializeViews() throws IOException {
        logger.info("Loading and initializing all views");

        try {
            loadContentView();

        } catch (Exception e) {
            logger.error("Error loading views: {}", e.getMessage(), e);
            throw e;
        }

        logger.info("All views loaded and initialized successfully");
        return true;
    }

    private void loadContentView() throws IOException {
        logger.debug("Loading movie view");
        
        // Create and configure the movie loader
        FXMLLoader movieLoader = new FXMLLoader(getClass().getResource("/fxml/movie-view.fxml"));
        
        // Create and initialize the controller
        contentController = new ContentController();
        try {
            contentController.initializeServices();
            if (currentUser != null) {
                contentController.setUserSession(currentUser, sessionToken);
            }
        } catch (Exception e) {
            logger.error("Failed to initialize ContentController: {}", e.getMessage(), e);
            throw new IOException("Failed to initialize ContentController", e);
        }
        
        // Set the controller on the loader
        movieLoader.setController(contentController);
        
        // Load the view
        movieView = movieLoader.load();

        logger.debug("Loading series view");
        FXMLLoader seriesLoader = new FXMLLoader(getClass().getResource("/fxml/series-view.fxml"));
        seriesView = seriesLoader.load();

        logger.debug("Movie and series views loaded successfully");
    }


    public Node getMovieView() {
        return movieView;
    }


    public Node getSeriesView() {
        return seriesView;
    }


    public Node getSearchView() {
        return searchView;
    }

    public Node getHomeView() {
        return homeView;
    }
}