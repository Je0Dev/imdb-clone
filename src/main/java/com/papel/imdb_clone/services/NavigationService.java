package com.papel.imdb_clone.services;

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

/**
 * Service for handling navigation between views.
 */
public class NavigationService {
    private static final Logger logger = LoggerFactory.getLogger(NavigationService.class);
    private static NavigationService instance;

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
}
