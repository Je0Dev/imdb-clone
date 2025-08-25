package com.papel.imdb_clone.controllers;

import com.papel.imdb_clone.controllers.coordinator.UICoordinator;
import com.papel.imdb_clone.data.RefactoredDataManager;
import com.papel.imdb_clone.model.User;
import com.papel.imdb_clone.service.ServiceLocator;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

/**
 * Main controller for the application's primary interface.
 * Handles navigation between different views and manages the application state.
 */
public class RefactoredMainController {
    private static final Logger logger = LoggerFactory.getLogger(RefactoredMainController.class);

    // Core services and coordinators
    private RefactoredDataManager dataManager;
    private UICoordinator uiCoordinator;

    // UI Components
    @FXML
    private BorderPane mainBorderPane;
    @FXML
    private VBox sidebar;
    @FXML
    private Label statusLabel;
    @FXML
    private Label userLabel;
    @FXML
    private VBox featuredContent;

    // State
    private Stage primaryStage;
    private User currentUser;
    private String sessionToken;
    private final boolean isSidebarCollapsed = false;
    private String initializationError;
    private String s;


    /**
     * Default constructor for FXML loader.
     * Note: The initialize() method will be called after FXML injection.
     */
    public RefactoredMainController() {
        // Initialize services from ServiceLocator in initialize()
    }

    /**
     * Constructor for programmatic creation with user session.
     */
    public RefactoredMainController(User user, String sessionToken) {
        this();
        this.currentUser = user;
        this.sessionToken = sessionToken;

        // Initialize services from ServiceLocator
        initializeServices();

        if (this.uiCoordinator != null) {
            this.uiCoordinator.setUserSession(user, sessionToken);
        }

        logger.info("RefactoredMainController constructed with user {} and session token",
                user != null ? user.getUsername() : "null");
    }

    /**
     * Initialize services from ServiceLocator.
     */
    /**
     * Initializes the service layer components required by the controller.
     * This includes setting up the data manager and UI coordinator.
     *
     * @throws IllegalStateException if required services cannot be initialized
     */
    private void initializeServices() {
        try {
            // Get or create service locator instance
            ServiceLocator serviceLocator = ServiceLocator.getInstance();

            // Get data manager from service locator
            this.dataManager = serviceLocator.getDataManager();
            if (this.dataManager == null) {
                throw new IllegalStateException("Failed to initialize DataManager");
            }


            // Initialize UI Coordinator
            this.uiCoordinator = new UICoordinator(this.dataManager);
            if (primaryStage != null) {
                this.uiCoordinator.setPrimaryStage(primaryStage);
            }


            // Load data if not already loaded
            if (!this.dataManager.isDataLoaded()) {
                this.dataManager.loadAllData();
            }

            logger.info("RefactoredMainController services initialized successfully");
        } catch (Exception e) {
            String errorMsg = String.format("Failed to initialize services: %s", e.getMessage());
            logger.error(errorMsg, e);
            throw new RuntimeException(errorMsg, e);
        }
    }

    /**
     * Initializes the controller after its root element has been completely processed.
     * This method is automatically called by JavaFX after the FXML file has been loaded.
     * It sets up the UI components, initializes services, and loads initial data.
     */
    @FXML
    public void initialize() {
        try {
            // Initialize services first
            initializeServices();

            // Set the primary stage if it's not already set
            if (primaryStage == null && mainBorderPane != null && mainBorderPane.getScene() != null) {
                primaryStage = (Stage) mainBorderPane.getScene().getWindow();
                logger.info("Primary stage set from scene: {}", primaryStage != null);
            }

            // Initialize coordinators
            boolean coordinatorsInitialized = initializeCoordinators();
            logger.info("Coordinators initialized: {}", coordinatorsInitialized);

            // If we have a user from previous session, set it in the UI coordinator
            if (this.currentUser != null && this.sessionToken != null) {
                logger.info("Setting user session for user: {}", this.currentUser.getUsername());
                if (this.uiCoordinator != null) {
                    this.uiCoordinator.setUserSession(this.currentUser, this.sessionToken);
                }
            }

            if (coordinatorsInitialized) {
                updateUserInterface();
                logger.info("RefactoredMainController initialized successfully");
            } else {
                logger.error("Failed to initialize coordinators");
                showErrorDialog("Initialization Error", "Failed to initialize application components.");
            }
        } catch (Exception e) {
            logger.error("Critical error during controller initialization", e);
            showErrorDialog("Critical Error", "Failed to initialize the application: " + e.getMessage());
        }
    }

    /**
     * Displays an error dialog with the specified title and message.
     *
     * @param title   the title of the error dialog
     * @param message the error message to display
     */
    private void showErrorDialog(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Error");
        alert.setHeaderText(initializationError);
        alert.setContentText(s);
        alert.showAndWait();
    }

    /**
     * Initializes the UI coordinators and sets up the views.
     *
     * @return true if initialization was successful, false otherwise
     */
    private boolean initializeCoordinators() {
        try {
            if (uiCoordinator == null) {
                logger.error("UI Coordinator is not initialized");
                return false;
            }

            logger.info("Setting primary stage in UI Coordinator");
            if (primaryStage == null) {
                logger.error("Cannot initialize coordinators: Primary stage is null");
                return false;
            }

            uiCoordinator.setPrimaryStage(primaryStage);

            if (currentUser != null && sessionToken != null) {
                uiCoordinator.setUserSession(currentUser, sessionToken);
            }

            logger.info("Loading and initializing views...");
            boolean viewsInitialized = uiCoordinator.loadAndInitializeViews();
            if (!viewsInitialized) {
                logger.error("Failed to load and initialize views");
                return false;
            }

            // Set the views in their respective tabs
            setViewsInTabs();

            // Update UI based on current user state
            updateUserInterface();

            return true;
        } catch (Exception e) {
            logger.error("Error initializing coordinators: {}", e.getMessage(), e);
            return false;
        }
    }

    /**
     * Sets up the views in their respective tabs or containers.
     * This method is kept for backward compatibility but is not used in the current UI.
     */
    private void setViewsInTabs() {
        // No longer using tabs in the new UI
        // This method is kept for backward compatibility but does nothing
    }


    /**
     * Updates UI elements that depend on the current user/session state and refreshes views.
     */
    private void updateUserInterface() {
        try {
            updateUserLabel();
            // Initialize featured content if needed
            if (featuredContent != null && featuredContent.getChildren().isEmpty()) {
                initializeFeaturedContent();
            }
        } catch (Exception e) {
            logger.warn("Failed to update user interface state", e);
        }
    }

    /**
     * Updates the user label in the UI to reflect the current user's information.
     * If no user is logged in, it may clear or hide the user label.
     */
    private void updateUserLabel() {
    }

    /**
     * Initializes and populates the featured content section of the UI.
     * This typically includes loading and displaying featured movies or TV shows.
     */
    private void initializeFeaturedContent() {
        // Add some placeholder content
        Label title = new Label("Featured Content");
        title.setStyle("-fx-text-fill: white; -fx-font-size: 24px; -fx-font-weight: bold;");

        Label subtitle = new Label("Check out our latest additions");
        subtitle.setStyle("-fx-text-fill: #999; -fx-font-size: 14px;");

        VBox content = new VBox(10, title, subtitle);
        content.setAlignment(Pos.CENTER);
        content.setPadding(new Insets(20));

        featuredContent.getChildren().add(content);
    }


    /**
     * Displays an error dialog with the given title and message.
     *
     * @param title   The title of the error dialog
     * @param message The error message to display
     */
    private void showError(String title, String message) {
        if (primaryStage != null) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.initOwner(primaryStage);
            alert.setTitle(title);
            alert.setHeaderText(null);
            alert.setContentText(message);
            alert.showAndWait();
        } else {
            logger.error("{}: {}", title, message);
        }
    }

    /**
     * Sets the primary stage for this controller and initializes necessary components.
     *
     * @param primaryStage the primary stage of the application
     * @throws IOException              if there is an error during initialization
     * @throws IllegalArgumentException if primaryStage is null
     */
    public void setPrimaryStage(Stage primaryStage) throws IOException {
        if (primaryStage == null) {
            throw new IllegalArgumentException("Primary stage cannot be null");
        }

        this.primaryStage = primaryStage;

        // Initialize the UI coordinator if it hasn't been initialized yet
        if (uiCoordinator == null) {
            initializeServices();
        }

        if (uiCoordinator != null) {
            uiCoordinator.setPrimaryStage(primaryStage);
        }

        // Data loading is now handled internally by the data manager
        // No need to explicitly call loadInitialData()
    }

    /**
     * Handles the action event for showing the movies view.
     *
     * @param actionEvent the event that triggered this method
     */
    @FXML
    public void showMovies(ActionEvent actionEvent) {
        try {
            if (uiCoordinator != null) {
                Node moviesView = uiCoordinator.getMovieView();
                if (moviesView != null) {
                    mainBorderPane.setCenter(moviesView);
                    return;
                }
            }
            showError("Navigation Error", "Failed to load movies view.");
        } catch (Exception e) {
            logger.error("Error in showMovies: {}", e.getMessage(), e);
            showError("Error", "An error occurred while loading movies.");
        }
    }

    /**
     * Handles the action event for showing the TV shows view.
     *
     * @param actionEvent the event that triggered this method
     */
    @FXML
    public void showTVShows(ActionEvent actionEvent) {
        try {
            if (uiCoordinator != null) {
                Node tvShowsView = uiCoordinator.getSeriesView();
                if (tvShowsView != null) {
                    mainBorderPane.setCenter(tvShowsView);
                    return;
                }
            }
            showError("Navigation Error", "Failed to load TV shows view.");
        } catch (Exception e) {
            logger.error("Error in showTVShows: {}", e.getMessage(), e);
            showError("Error", "An error occurred while loading TV shows.");
        }
    }

    /**
     * Handles the action event for showing the advanced search view.
     *
     * @param actionEvent the event that triggered this method
     */
    @FXML
    public void showAdvancedSearch(ActionEvent actionEvent) {
        try {
            if (uiCoordinator != null) {
                Node searchView = uiCoordinator.getSearchView();
                if (searchView != null) {
                    mainBorderPane.setCenter(searchView);
                    return;
                }
            }
            showError("Navigation Error", "Failed to load search view.");
        } catch (Exception e) {
            logger.error("Error in showAdvancedSearch: {}", e.getMessage(), e);
            showError("Error", "An error occurred while loading search.");
        }
    }

    /**
     * Handles the mouse event for navigating to the home view.
     *
     * @param mouseEvent the mouse event that triggered this method
     */
    @FXML
    public void goToHome(MouseEvent mouseEvent) {
        try {
            if (uiCoordinator != null) {
                Node homeView = uiCoordinator.getHomeView();
                if (homeView != null) {
                    mainBorderPane.setCenter(homeView);
                    initializeFeaturedContent();
                    return;
                }
            }
            showError("Navigation Error", "Failed to load home view.");
        } catch (Exception e) {
            logger.error("Error in goToHome: {}", e.getMessage(), e);
            showError("Error", "An error occurred while loading the home page.");
        }
    }

    
}