package com.papel.imdb_clone.gui;

import com.papel.imdb_clone.config.ApplicationConfig;
import com.papel.imdb_clone.controllers.authentication.AuthController;
import com.papel.imdb_clone.data.DataManager;
import com.papel.imdb_clone.service.search.ServiceLocator;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

/**
 * Improved IMDB Clone Application using FXML and modern JavaFX practices with authentication
 */
public class MovieAppGui extends Application {
    private static final Logger logger = LoggerFactory.getLogger(MovieAppGui.class);


    private static final String HOME_FXML = "/fxml/base/home-view.fxml";
    private static final String AUTH_VIEW = "/fxml/authentication/auth-view.fxml";


    private String currentSessionToken;
    private ApplicationConfig config;
    private ServiceLocator serviceLocator;


    /**
     * Initializes the application
     * @param primaryStage the primary stage for this application, onto which
     * the application scene can be set.
     * Applications may create other stages, if needed, but they will not be
     * primary stages.
     * @throws Exception if the application fails to start
     */
    @Override
    public void start(Stage primaryStage) throws Exception {

        // Set the primary stage in ServiceLocator as early as possible
        ServiceLocator.setPrimaryStage(primaryStage);

        try {
            System.out.println("[Startup] Entering start()...");

            initializeApplication();

            System.out.println("[Startup] initializeApplication() completed");

            // Set window properties
            primaryStage.setTitle(config.getAppTitle());
            primaryStage.setMinWidth(1200);
            primaryStage.setMinHeight(800);
            primaryStage.setWidth(1400);
            primaryStage.setHeight(900);
            primaryStage.setResizable(true);

            logger.info("Application started successfully");

            // Show login screen first
            System.out.println("[Startup] About to show login screen...");
            showLoginScreen();
            System.out.println("[Startup] Login screen shown");

        } catch (Exception e) {
            logger.error("Failed to start application", e);
            Platform.exit();
        }
    }

    /**
     * Initializes the application
     * @throws IOException if the application fails to initialize
     */
    private void initializeApplication() throws IOException {
        logger.info("Initializing IMDB Clone application...");


        // Load configuration
        config = ApplicationConfig.getInstance();

        // Initialize service locator and get data manager
        serviceLocator = ServiceLocator.getInstance();

        // Get data manager using the new getDataManager() method
        DataManager dataManager = serviceLocator.getDataManager();
        dataManager.loadAllData();

        logger.info("Application initialization complete");
    }

    private void showLoginScreen() {
        try {
            //show login screen
            System.out.println("[Login] Creating login screen...");
            FXMLLoader loader = new FXMLLoader(getClass().getResource(AUTH_VIEW));
            Parent authRoot = loader.load();
            AuthController authController = loader.getController();

            // Get the auth controller
            Stage authStage = getStage(loader, authRoot);

            //Show the login screen
            System.out.println("[Login] Showing login screen...");
            authStage.show();

        } catch (Exception e) {
            System.err.println("[Login][ERROR] Failed to show login screen: " + e);
            showErrorAndExit(e);
            Platform.exit();
        }
    }

    /**
     * Gets the stage for the login screen
     * @param loader loader for the login screen
     * @param authRoot authRoot that contains the login screen
     * @return stage for the login screen
     */
    private Stage getStage(FXMLLoader loader, Parent authRoot) {
        AuthController authController = loader.getController();

        // Create auth stage
        Stage authStage = new Stage();
        authStage.setTitle("Login - " + config.getAppTitle());
        Scene scene = new Scene(authRoot, 1200, 800);
        authStage.setScene(scene);
        authStage.setMinWidth(1000);
        authStage.setMinHeight(700);
        authStage.setResizable(true);
        authStage.centerOnScreen();
        System.out.println("[Login] Login stage created");

        //Set the auth controller
        authController.setStage(authStage);
        return authStage;
    }


    /**
     * Shows an error message and exits the application
     * @param e exception that occurred
     */
    private void showErrorAndExit(Exception e) {
        logger.error("{}: {}", "Failed to show login screen", e.getMessage(), e);

        // Cleanup and graceful shutdown
        cleanup();
        System.err.println("[Login][ERROR] Failed to show login screen: " + e);
        Platform.exit();
    }

    /**
     * Cleans up resources and shuts down the application
     */
    private void cleanup() {
        try {
            if (serviceLocator != null) {
                System.out.println("[Login] Shutting down service locator");
                serviceLocator.shutdown();
            }
            System.out.println("[Login] Application cleanup completed");
            logger.info("Application cleanup completed");
        } catch (Exception e) {
            System.err.println("[Login][ERROR] Error during cleanup: " + e);
            logger.error("Error during cleanup", e);
        }
    }

    /**
     * Overrides the stop method to perform cleanup before shutting down the application
     * @throws Exception if an error occurs during cleanup
     */
    @Override
    public void stop() throws Exception {
        logger.info("Application stopping...");
        System.out.println("[Login] Application stopping...");
        cleanup();
        super.stop();
    }
}