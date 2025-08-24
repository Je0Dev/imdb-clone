package com.papel.imdb_clone.services;

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

            Scene scene = new Scene(root);
            currentStage.setScene(scene);
            currentStage.setTitle(title);
            currentStage.show();

            // Center the window on the screen
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
        navigateTo("/fxml/main-refactored.fxml", currentStage, "IMDB Clone App");
    }
}
