package com.papel.imdb_clone.services;

import com.papel.imdb_clone.controllers.ContentDetailsController;
import com.papel.imdb_clone.controllers.RefactoredMainController;
import com.papel.imdb_clone.controllers.coordinator.UICoordinator;
import com.papel.imdb_clone.service.ServiceLocator;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Stack;

/**
 * Service for handling navigation between views.
 */
public class NavigationService {
    private static final Logger logger = LoggerFactory.getLogger(NavigationService.class);
    private static NavigationService instance;
    private final Stack<Scene> sceneStack = new Stack<>();
    private String posterUrl;

    private NavigationService() {
        // Private constructor to enforce singleton pattern
    }

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
     * @param currentStage The current stage to update
     * @param title        Title for the new window
     */
    public void navigateTo(String fxmlPath, Stage currentStage, String title) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            Parent root = loader.load();

            // If this is the main view, ensure the controller is properly initialized
            if (fxmlPath.equals("/fxml/main-refactored.fxml")) {
                Object controller = loader.getController();
                if (controller instanceof RefactoredMainController) {
                    ((RefactoredMainController) controller).setPrimaryStage(currentStage);
                }
            }

            Scene scene = new Scene(root);
            currentStage.setScene(scene);
            currentStage.setTitle(title);
            currentStage.show();
            currentStage.centerOnScreen();
        } catch (IOException e) {
            logger.error("Failed to load FXML: {}", fxmlPath, e);
            throw new RuntimeException("Failed to load view: " + e.getMessage(), e);
        }
    }

    /**
     * Navigate to the main application view.
     *
     * @param currentStage The current stage to update
     */
    public void navigateToMainApp(Stage currentStage) {
        try {
            // Set the primary stage in ServiceLocator first
            ServiceLocator.setPrimaryStage(currentStage);

            // Get the UICoordinator to ensure it's initialized
            UICoordinator uiCoordinator = ServiceLocator.getInstance().getUICoordinator();

            // Navigate to main view
            navigateTo("/fxml/main-refactored.fxml", currentStage, "IMDB Clone App");
        } catch (Exception e) {
            logger.error("Failed to navigate to main app: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to initialize application: " + e.getMessage(), e);
        }
    }

    public void showCelebrities() {
        navigateTo("/fxml/celebrities-view.fxml", ServiceLocator.getPrimaryStage(), "Celebrities");
    }


    /**
     * Shows the content details view.
     *
     * @param title     The title of the content
     * @param year      The release year(s)
     * @param rating    The content rating
     * @param genre     The content genre
     * @param boxOffice The box office earnings
     * @param awards    The awards received
     * @param cast      The main cast
     */
    public void showContentDetails(String title, String year, String rating, String genre,
                                   String boxOffice, String awards, String cast) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/content-details.fxml"));
            Parent root = loader.load();
            ContentDetailsController controller = loader.getController();
            controller.setContentDetails(title, year, rating, genre, boxOffice, awards, cast);
            Stage currentStage = ServiceLocator.getPrimaryStage();
            sceneStack.push(currentStage.getScene());

            Scene newScene = new Scene(root);
            currentStage.setScene(newScene);
        } catch (IOException e) {
            logger.error("Failed to load content details view", e);
            throw new RuntimeException("Failed to load content details: " + e.getMessage(), e);
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

    public void showHome() {
        navigateTo("/fxml/main-refactored.fxml", ServiceLocator.getPrimaryStage(), "IMDB Clone App");
    }
}
