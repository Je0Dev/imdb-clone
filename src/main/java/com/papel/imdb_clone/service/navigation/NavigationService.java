package com.papel.imdb_clone.service.navigation;

import com.papel.imdb_clone.controllers.content.EditContentController;
import com.papel.imdb_clone.controllers.content.MoviesController;
import com.papel.imdb_clone.controllers.people.CelebritiesController;
import com.papel.imdb_clone.controllers.MainController;
import com.papel.imdb_clone.controllers.content.SeriesController;
import com.papel.imdb_clone.controllers.search.AdvancedSearchController;
import com.papel.imdb_clone.service.content.MoviesService;
import com.papel.imdb_clone.service.content.SeriesService;
import com.papel.imdb_clone.service.search.SearchService;
import com.papel.imdb_clone.service.search.ServiceLocator;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.Stack;

/**
 * Service for handling navigation between views.
 */
public class NavigationService {
    private static final Logger logger = LoggerFactory.getLogger(NavigationService.class);
    private static NavigationService instance;
    private final Stack<Scene> sceneStack = new Stack<>();
    private final Map<String, Object> userData = new HashMap<>();

    /**
     * Private constructor to enforce singleton pattern and module encapsulation.
     */
    private NavigationService() {
        // Private constructor to prevent direct instantiation
    }
    private Object currentController;


    public static synchronized NavigationService getInstance() {
        if (instance == null) {
            instance = new NavigationService();
        }
        return instance;
    }

    /**
     * Navigate to a new view.
     *
     * @param fxmlPath     Path to the FXML file relative to resources
     * @param data         Data to pass to the controller
     * @param currentStage The current stage to update
     * @param title        Title for the new window
     */
    public void navigateTo(String fxmlPath, Map<String, Object> data, Stage currentStage, String title) {
        try {
            if (fxmlPath == null || fxmlPath.trim().isEmpty()) {
                throw new IllegalArgumentException("FXML path cannot be null or empty");
            }

            // Try to get the primary stage if currentStage is null
            if (currentStage == null) {
                currentStage = ServiceLocator.getPrimaryStage();
                if (currentStage == null) {
                    throw new IllegalStateException("No stage provided and primary stage is not set");
                }
            }

            // Load the FXML
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            if (loader.getLocation() == null) {
                throw new IOException("FXML file not found: " + fxmlPath);
            }
            
            Parent root = loader.load();
            
            // Initialize controllers with required services 
            Object controller = loader.getController();
            if (controller != null) {
                switch (controller) {
                    case MainController mainController -> mainController.setPrimaryStage(currentStage);
                    case MoviesController moviesController -> {
                        MoviesService moviesService = MoviesService.getInstance();
                        moviesController.setContentService(moviesService);
                        moviesController.initializeController(0); // Replace with actual user ID
                    }
                    case SeriesController seriesController -> {
                        SeriesService seriesService = SeriesService.getInstance();
                        seriesController.setContentService(seriesService);
                        seriesController.initializeController(0); // Replace with actual user ID
                    }
                    case AdvancedSearchController advancedSearchController -> {
                        // The controller initializes itself, no additional setup needed
                        logger.debug("AdvancedSearchController initialized");
                    }
                    case EditContentController editContentController -> {
                        // The controller initializes itself, no additional setup needed
                        logger.debug("EditContentController initialized");
                    }
                    case CelebritiesController celebritiesController -> {
                        // The controller initializes itself, no additional setup needed
                        logger.debug("CelebritiesController initialized");
                    }
                    default -> {
                        logger.warn("Controller not found for FXML path: {}", fxmlPath);
                        throw new IllegalArgumentException("Controller not found for FXML path: " + fxmlPath);
                    }
                }
            }

            // Set up the scene and show the stage
            Scene scene = new Scene(root);
            currentStage.setScene(scene);
            currentStage.setTitle(title);
            currentStage.show();
            currentStage.centerOnScreen();
            
            // Store the current scene in the stack for back navigation
            sceneStack.push(scene);

        } catch (IOException e) {
            String errorMsg = String.format("Failed to load FXML: %s. Error: %s", fxmlPath, e.getMessage());
            logger.error(errorMsg, e);
            throw new RuntimeException(errorMsg, e);
        } catch (Exception e) {
            String errorMsg = String.format("Unexpected error navigating to %s: %s", fxmlPath, e.getMessage());
            logger.error(errorMsg, e);
            throw new RuntimeException(errorMsg, e);
        }
    }


    //Navigate to celebrities view
    public void showCelebrities(Stage currentStage) {
        try {
            if (currentStage == null) {
                currentStage = ServiceLocator.getPrimaryStage();
                if (currentStage == null) {
                    throw new IllegalStateException("Primary stage is not set. Cannot navigate to Celebrities view.");
                }
            }
            navigateTo("/fxml/celebrities/celebrities-view.fxml", null, currentStage, "Celebrities");
        } catch (Exception e) {
            logger.error("Failed to navigate to celebrities view: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to navigate to celebrities view: " + e.getMessage(), e);
        }
    }

    /**
     * Navigates back to the previous view.
     */
    public void goBack() {
        if (!sceneStack.isEmpty()) {
            Scene previousScene = sceneStack.pop();
            if (previousScene != null) {
                ServiceLocator.getPrimaryStage().setScene(previousScene);
            }
        }
    }

    //show home when click on home button-imdb clone app text
    public void showHome() {
        navigateTo("/fxml/base/home-view.fxml", null, ServiceLocator.getPrimaryStage(), "Imdb Clone");
    }
    
    /**
     * Gets the current controller instance.
     * @return The current controller instance, or null if not available
     */
    public Object getCurrentController() {
        return currentController;
    }

    /**
     * Gets user data for the specified content type.
     * @param contentType The type of content to retrieve data for
     * @return The user data object, or null if not found
     */
    public Object getUserData(String contentType) {
        return userData.get(contentType);
    }
    
    /**
     * Sets user data for the specified content type.
     * @param contentType The type of content to store data for
     * @param data The data to store
     */
    public void setUserData(String contentType, Object data) {
        userData.put(contentType, data);
    }

    public void showLogin(Stage primaryStage) {
        navigateTo("/fxml/authentication//auth-view.fxml", null, primaryStage, "Login");
    }

    public void showRegister(Stage primaryStage) {
        navigateTo("/fxml/authentication//auth-view.fxml", null, primaryStage, "Register");
    }
}
