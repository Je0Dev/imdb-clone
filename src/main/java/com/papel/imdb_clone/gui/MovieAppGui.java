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

    @Override
    public void start(Stage primaryStage) {

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

            // Get the auth controller
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

            authController.setStage(authStage);

            authStage.show();

        } catch (Exception e) {
            System.err.println("[Login][ERROR] Failed to show login screen: " + e);
            showErrorAndExit(e);
        }
    }


    //show error and exit
    private void showErrorAndExit(Exception e) {
        logger.error("{}: {}", "Failed to show login screen", e.getMessage(), e);

        // Cleanup and graceful shutdown
        cleanup();
        Platform.exit();
    }

    //cleanup resources
    private void cleanup() {
        try {
            if (serviceLocator != null) {
                serviceLocator.shutdown();
            }
            logger.info("Application cleanup completed");
        } catch (Exception e) {
            logger.error("Error during cleanup", e);
        }
    }

    //override stop method
    @Override
    public void stop() throws Exception {
        logger.info("Application stopping...");
        cleanup();
        super.stop();
    }
}