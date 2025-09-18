package com.papel.imdb_clone.service;

import com.papel.imdb_clone.service.MoviesService;
import com.papel.imdb_clone.service.SeriesService;
import com.papel.imdb_clone.controllers.ContentDetailsController;
import com.papel.imdb_clone.controllers.MoviesController;
import com.papel.imdb_clone.controllers.RefactoredMainController;
import com.papel.imdb_clone.controllers.SeriesController;
import com.papel.imdb_clone.controllers.coordinator.UICoordinator;
import com.papel.imdb_clone.model.Actor;
import com.papel.imdb_clone.model.Movie;
import com.papel.imdb_clone.model.Series;
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
            
            if (controller instanceof RefactoredMainController) {
                ((RefactoredMainController) controller).setPrimaryStage(currentStage);
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

    /**
     * Navigate to the main application view.
     *
     * @param currentStage The current stage to update
     */
    public void navigateToMainApp(Stage currentStage) {
        try {
            // Set the primary stage in ServiceLocator first
            ServiceLocator.setPrimaryStage(currentStage);


            // Navigate to main view
            navigateTo("/fxml/home-view.fxml", currentStage, "IMDb Clone - Home");
        } catch (Exception e) {
            logger.error("Failed to navigate to main app: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to initialize application: " + e.getMessage(), e);
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
            navigateTo("/fxml/celebrities-view.fxml", currentStage, "Celebrities");
        } catch (Exception e) {
            logger.error("Failed to navigate to celebrities view: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to navigate to celebrities view: " + e.getMessage(), e);
        }
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
                                   String boxOffice, List<String> awards, List<Actor> cast) {
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

    //show home when click on home button-imdb clone app text
    public void showHome() {
        navigateTo("/fxml/home-view.fxml", ServiceLocator.getPrimaryStage(), "Imdb Clone");
    }
}
