package com.papel.imdb_clone.gui;

import com.papel.imdb_clone.config.ApplicationConfig;
import com.papel.imdb_clone.controllers.AuthController;
import com.papel.imdb_clone.controllers.RefactoredMainController;
import com.papel.imdb_clone.data.RefactoredDataManager;
import com.papel.imdb_clone.service.ServiceLocator;
import com.papel.imdb_clone.util.AppStateManager;
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
public class ImprovedMovieApp extends Application {
    private static final Logger logger = LoggerFactory.getLogger(ImprovedMovieApp.class);

    private static final String MAIN_FXML = "/fxml/main-refactored.fxml";
    private static final String AUTH_VIEW = "/fxml/auth-view.fxml";

    private Stage primaryStage;
    private String currentSessionToken;
    private ApplicationConfig config;
    private ServiceLocator serviceLocator;

    @Override
    public void start(Stage primaryStage) {
        this.primaryStage = primaryStage;

        try {
            System.out.println("[Startup] Entering start()...");

            initializeApplication();

            System.out.println("[Startup] initializeApplication() completed");

            // Set window title from config
            this.primaryStage.setTitle(config.getAppTitle());

            logger.info("Application started successfully");

            // Show login screen first
            System.out.println("[Startup] About to show login screen...");
            showLoginScreen();
            System.out.println("[Startup] Login screen shown");

        } catch (Exception e) {
            logger.error("Failed to start application", e);
            System.err.println("[Startup][ERROR] Exception in start(): " + e);
            e.printStackTrace();
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
        RefactoredDataManager dataManager = serviceLocator.getDataManager();
        dataManager.loadAllData();

        logger.info("Application initialization complete");
    }

    private void showLoginScreen() {
        try {
            System.out.println("[Login] Creating login screen...");
            FXMLLoader loader = new FXMLLoader(getClass().getResource(AUTH_VIEW));
            Parent authRoot = loader.load();

            // Get the auth controller
            AuthController authController = loader.getController();

            // Create auth stage
            Stage authStage = new Stage();
            authStage.setTitle("Login - " + config.getAppTitle());
            authStage.setScene(new Scene(authRoot));
            authStage.setResizable(false);
            authStage.centerOnScreen();

            authController.setStage(authStage);


            // Add guest access button functionality
            addGuestAccessToLogin(authStage, authRoot);

            authStage.show();

        } catch (Exception e) {
            System.err.println("[Login][ERROR] Failed to show login screen: " + e);
            e.printStackTrace();
            showErrorAndExit("Failed to show login screen", e);
        }
    }

    private void addGuestAccessToLogin(Stage authStage, Parent authRoot) {
        try {
            // Add a "Continue as Guest" button to the login screen
            if (authRoot instanceof javafx.scene.layout.AnchorPane anchorPane) {
                javafx.scene.control.Button guestButton = new javafx.scene.control.Button("Continue as Guest");
                guestButton.setStyle("-fx-background-color: #6c757d; -fx-text-fill: white; -fx-font-size: 12px;");
                guestButton.setPrefWidth(150);
                guestButton.setLayoutX(325);
                guestButton.setLayoutY(450);

                guestButton.setOnAction(e -> {
                    System.out.println("[Login] Guest access selected");
                    currentSessionToken = null; // No session for guest
                    authStage.close();
                    loadMainApplication();
                });

                anchorPane.getChildren().add(guestButton);
            }
        } catch (Exception e) {
            logger.warn("Failed to add guest access button", e);
        }
    }

    private void loadMainApplication() {
        try {
            System.out.println("[Startup] Enter loadMainApplication()");
            
            // Ensure we have a primary stage
            if (primaryStage == null) {
                primaryStage = new Stage();
                System.out.println("[Startup] Created new primary stage");
            }

            // Get the current user
            com.papel.imdb_clone.service.AuthService authService = 
                    com.papel.imdb_clone.service.AuthService.getInstance();
            com.papel.imdb_clone.model.User currentUser = authService.getCurrentUser(currentSessionToken);
            System.out.println("[Startup] Current user: " + (currentUser != null ? currentUser.getUsername() : "<none>"));

            // Load the FXML first to let it create the controller
            FXMLLoader loader = new FXMLLoader(getClass().getResource(MAIN_FXML));
            System.out.println("[Startup] Loading FXML: " + MAIN_FXML);
            
            // Load the main view
            Parent root = loader.load();
            System.out.println("[Startup] FXML loaded successfully");
            
            // Get the controller that was created by the FXML loader
            RefactoredMainController controller = loader.getController();
            System.out.println("[Startup] Controller obtained from FXML loader");
            
            // Initialize the controller with the user and session
            if (currentUser != null) {
                controller.setUserSession(currentUser, currentSessionToken);
            }
            
            // Set the primary stage in the controller
            controller.setPrimaryStage(primaryStage);
            System.out.println("[Startup] Primary stage set in controller");

            // Set up the scene
            Scene scene = new Scene(root, config.getMinWidth(), config.getMinHeight());
            primaryStage.setScene(scene);
            primaryStage.setTitle(config.getAppTitle() + (currentUser != null ? " - " + currentUser.getUsername() : ""));
            primaryStage.setMinWidth(config.getMinWidth());
            primaryStage.setMinHeight(config.getMinHeight());
            primaryStage.centerOnScreen();

            // Publish session globally
            try {
                AppStateManager.getInstance().setSession(currentUser, currentSessionToken);
                System.out.println("[Startup] Session published");
            } catch (Exception e) {
                logger.warn("Failed to publish session via AppStateManager", e);
                System.err.println("[Startup][WARN] Failed to publish session: " + e);
            }

            // Show the main application
            primaryStage.show();
            System.out.println("[Startup] Stage shown");

        } catch (Exception e) {
            System.err.println("[Startup][ERROR] loadMainApplication() failed: " + e);
            e.printStackTrace();
            showErrorAndExit("Failed to load main application", e);
        }
    }

    // Apply theme and animations

    // Theme selection now handled by ThemeManager
    private String getThemeStylesheet(String themeKey) {
        return null;
    }

    private void showErrorAndExit(String message, Exception e) {
        logger.error("{}: {}", message, e.getMessage(), e);

        // Cleanup and graceful shutdown
        cleanup();
        Platform.exit();
    }

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

    @Override
    public void stop() throws Exception {
        logger.info("Application stopping...");
        cleanup();
        super.stop();
    }

    public static void main(String[] args) {
        launch(args);
    }
}