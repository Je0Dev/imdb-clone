package com.papel.imdb_clone.service.navigation;

import com.papel.imdb_clone.controllers.content.ContentDetailsController;
import com.papel.imdb_clone.controllers.content.MoviesController;
import com.papel.imdb_clone.controllers.MainController;
import com.papel.imdb_clone.controllers.content.SeriesController;
import com.papel.imdb_clone.model.people.Actor;
import com.papel.imdb_clone.service.content.MoviesService;
import com.papel.imdb_clone.service.content.SeriesService;
import com.papel.imdb_clone.service.search.ServiceLocator;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.List;
import java.util.Stack;

/**
 * Service for handling navigation between views.
 */
public class NavigationService {
    private static final Logger logger = LoggerFactory.getLogger(NavigationService.class);
    private static NavigationService instance;
    private final Stack<Scene> sceneStack = new Stack<>();


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
            
            // Initialize controllers with required services
            Object controller = loader.getController();
            
            if (controller instanceof MainController) {
                ((MainController) controller).setPrimaryStage(currentStage);
            } else if (controller instanceof MoviesController moviesController) {
                MoviesService moviesService = MoviesService.getInstance();
                moviesController.setContentService(moviesService);
                // Initialize with current user ID (0 for now, replace with actual user ID)
                moviesController.initializeController(0);
            } else if (controller instanceof SeriesController seriesController) {
                SeriesService seriesService = SeriesService.getInstance();
                seriesController.setContentService(seriesService);
                // Initialize with current user ID (0 for now, replace with actual user ID)
                seriesController.initializeController(0);
            }

            Scene scene = new Scene(root);
            currentStage.setScene(scene);
            currentStage.setTitle(title);
            currentStage.show();
            currentStage.centerOnScreen();

        } catch (IOException e) {
            logger.error("Failed to load FXML: {}", fxmlPath, e);
            throw new RuntimeException("Failed to load view: " + e.getMessage(), e);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }


    public void showCelebrities(Stage currentStage) {
        try {
            if (currentStage == null) {
                currentStage = ServiceLocator.getPrimaryStage();
                if (currentStage == null) {
                    throw new IllegalStateException("Primary stage is not set. Cannot navigate to Celebrities view.");
                }
            }
            navigateTo("/fxml/celebrities/celebrities-view.fxml", currentStage, "Celebrities");
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
        navigateTo("/fxml/base/home-view.fxml", ServiceLocator.getPrimaryStage(), "Imdb Clone");
    }
}
