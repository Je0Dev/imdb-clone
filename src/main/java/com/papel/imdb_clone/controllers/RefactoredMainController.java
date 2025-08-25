package com.papel.imdb_clone.controllers;

import com.papel.imdb_clone.controllers.coordinator.UICoordinator;
import com.papel.imdb_clone.data.RefactoredDataManager;
import com.papel.imdb_clone.model.User;
import com.papel.imdb_clone.service.ServiceLocator;
import javafx.application.Platform;
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
    private boolean isInitializing = false;
    private boolean isInitialized = false;

    // Static field to track if FXML loading is in progress
    private static final boolean isFxmlLoading = false;

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
     * Initializes the service layer components required by the controller.
     * This includes setting up the data manager and UI coordinator.
     *
     * @throws IllegalStateException if required services cannot be initialized
     */
    private void initializeServices() {
        // Prevent re-entrancy
        if (isInitialized || isInitializing) {
            logger.debug("Services already initialized or initializing, skipping...");
            return;
        }

        isInitializing = true;
        try {
            logger.info("Initializing services...");

            // Get ServiceLocator instance first
            ServiceLocator serviceLocator = ServiceLocator.getInstance();

            // Initialize data manager through ServiceLocator
            this.dataManager = serviceLocator.getDataManager();
            if (this.dataManager == null) {
                throw new IllegalStateException("Failed to get DataManager from ServiceLocator");
            }

            logger.info("DataManager initialized successfully");

            // Only load data if it hasn't been loaded yet
            if (!this.dataManager.isDataLoaded()) {
                try {
                    logger.info("Loading initial data...");
                    this.dataManager.loadAllData();
                    logger.info("Initial data loaded successfully");
                } catch (Exception e) {
                    logger.error("Error loading initial data: {}", e.getMessage(), e);
                    throw new IllegalStateException("Failed to load initial data: " + e.getMessage(), e);
                }
            }

            // Get UICoordinator from ServiceLocator instead of creating a new instance
            this.uiCoordinator = serviceLocator.getUICoordinator();
            if (this.uiCoordinator == null) {
                throw new IllegalStateException("Failed to get UICoordinator from ServiceLocator");
            }

            // Set user session if available
            if (this.currentUser != null && this.sessionToken != null) {
                this.uiCoordinator.setUserSession(this.currentUser, this.sessionToken);
            }

            logger.info("Services initialized successfully");
            isInitialized = true;
        } catch (Exception e) {
            String errorMsg = String.format("Failed to initialize services: %s", e.getMessage());
            logger.error(errorMsg, e);
            throw new RuntimeException(errorMsg, e);
        } finally {
            isInitializing = false;
        }
    }

    /**
     * Initializes the controller after its root element has been completely processed.
     * This method is automatically called by JavaFX after the FXML file has been loaded.
     * It sets up the UI components, initializes services, and loads initial data.
     */
    @FXML
    public void initialize() {
        if (isInitialized || isInitializing) {
            logger.debug("Already initialized or initializing");
            return;
        }

        logger.info("Initializing RefactoredMainController...");

        try {
            // Initialize services first
            initializeServices();

            if (uiCoordinator == null) {
                throw new IllegalStateException("UICoordinator not initialized");
            }

            // Load views if needed
            if (!uiCoordinator.areViewsLoaded()) {
                if (!uiCoordinator.loadAndInitializeViews()) {
                    logger.warn("Some views failed to load");
                }
            }

            // Set initial view
            Platform.runLater(() -> {
                try {
                    Node homeView = uiCoordinator.getHomeView();
                    if (homeView != null) {
                        updateUserInterface();
                        logger.info("Successfully initialized UI");
                    }
                } catch (Exception e) {
                    logger.error("Error initializing UI", e);
                }
            });

        } catch (Exception e) {
            logger.error("Initialization failed", e);
            showError("Initialization Error", e.getMessage());
        }
    }

    /**
     * Initializes coordinators and updates the UI.
     * This can be called from initialize() or setPrimaryStage().
     */
    private void initializeCoordinatorsAndUI() {
        if (primaryStage == null) {
            logger.warn("Primary stage is null in initializeCoordinatorsAndUI");
            return;
        }

        if (mainBorderPane == null) {
            logger.warn("mainBorderPane is null in initializeCoordinatorsAndUI");
            return;
        }

        Platform.runLater(() -> {
            try {
                logger.info("Initializing coordinators and UI...");

                // Ensure services are initialized
                if (uiCoordinator == null) {
                    initializeServices();
                    if (uiCoordinator == null) {
                        throw new IllegalStateException("Failed to initialize UICoordinator");
                    }
                }

                // Initialize coordinators
                boolean coordinatorsInitialized = initializeCoordinators();
                logger.info("Coordinators initialized: {}", coordinatorsInitialized);

                if (!coordinatorsInitialized) {
                    throw new IllegalStateException("Failed to initialize coordinators");
                }

                // Ensure views are loaded
                if (!uiCoordinator.areViewsLoaded()) {
                    logger.info("Loading views...");
                    if (!uiCoordinator.loadAndInitializeViews()) {
                        logger.warn("Some views failed to load, but continuing with available views");
                    }
                }

                // Set initial view to home
                Node homeView = uiCoordinator.getHomeView();
                if (homeView != null) {
                    mainBorderPane.setCenter(homeView);
                    logger.info("Successfully set initial view to home");
                    updateUserInterface();
                } else {
                    throw new IllegalStateException("Failed to load home view");
                }

                logger.info("RefactoredMainController initialized successfully");
                isInitialized = true;

            } catch (Exception e) {
                logger.error("Error initializing coordinators and UI: {}", e.getMessage(), e);
                showError("Initialization Error", "Failed to initialize application: " + e.getMessage());
            } finally {
                isInitializing = false;
            }
        });
    }

    /**
     * Initializes the UI coordinators and sets up the views.
     *
     * @return true if initialization was successful, false otherwise
     */
    private boolean initializeCoordinators() {
        try {
            if (uiCoordinator == null) {
                logger.error("UICoordinator is not initialized");
                return false;
            }

            // Set the primary stage if not already set
            if (primaryStage != null) {
                uiCoordinator.setPrimaryStage(primaryStage);
            }

            // Set user session if available
            if (currentUser != null && sessionToken != null) {
                uiCoordinator.setUserSession(currentUser, sessionToken);
            }

            logger.info("UICoordinator initialized successfully");
            return true;

        } catch (Exception e) {
            logger.error("Failed to initialize coordinators: {}", e.getMessage(), e);
            return false;
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

    @FXML
    private void showMovies(ActionEvent event) {
        try {
            if (uiCoordinator == null) {
                logger.error("UICoordinator is not initialized");
                showError("Navigation Error", "Application not properly initialized. Please restart the application.");
                return;
            }

            Node movieView = uiCoordinator.getMovieView();
            if (movieView != null) {
                mainBorderPane.setCenter(movieView);
            } else {
                logger.error("Failed to load movie view");
                showError("Navigation Error", "Failed to load movies. Please try again later.");
            }
        } catch (Exception e) {
            logger.error("Error showing movies: {}", e.getMessage(), e);
            showError("Navigation Error", "An unexpected error occurred: " + e.getMessage());
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
            if (uiCoordinator == null) {
                logger.error("UICoordinator is not initialized");
                showError("Navigation Error", "Application not properly initialized. Please restart the application.");
                return;
            }

            Node seriesView = uiCoordinator.getSeriesView();
            if (seriesView == null) {
                logger.error("Failed to load TV shows view - view is null");
                showError("Navigation Error", "Failed to load TV shows view. The view could not be initialized.");
                return;
            }

            mainBorderPane.setCenter(seriesView);
            logger.info("Successfully navigated to TV shows view");
        } catch (Exception e) {
            logger.error("Error showing TV shows view: {}", e.getMessage(), e);
            showError("Navigation Error", "An error occurred while loading the TV shows view: " + e.getMessage());
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
            if (uiCoordinator == null) {
                logger.error("UICoordinator is not initialized");
                showError("Navigation Error", "Application not properly initialized. Please restart the application.");
                return;
            }

            Node searchView = uiCoordinator.getSearchView();
            if (searchView == null) {
                logger.error("Failed to load search view - view is null");
                showError("Navigation Error", "Failed to load search view. The view could not be initialized.");
                return;
            }

            mainBorderPane.setCenter(searchView);
            logger.info("Successfully navigated to search view");
        } catch (Exception e) {
            logger.error("Error showing search view: {}", e.getMessage(), e);
            showError("Navigation Error", "An error occurred while loading the search view: " + e.getMessage());
        }
    }

    /**
     * Handles navigation to the home view when the home button is clicked.
     * This method prevents cycles by ensuring we don't try to load the main layout as a view.
     *
     * @param mouseEvent the mouse event that triggered this method
     */
    @FXML
    public void goToHome(MouseEvent mouseEvent) {
        try {
            if (uiCoordinator == null) {
                logger.error("UICoordinator is not initialized");
                showError("Navigation Error", "Application not properly initialized. Please restart the application.");
                return;
            }

            // Get the current center content
            Node currentCenter = mainBorderPane.getCenter();
            
            // If we're already showing the home view (which should be empty for the main view), do nothing
            if (currentCenter == null || currentCenter.getId() == null || !currentCenter.getId().equals("homeContent")) {
                // Clear existing content to prevent memory leaks
                mainBorderPane.setCenter(null);
                
                // Update UI state
                updateUserInterface();
                
                // For the main view, we don't need to set any content in the center
                // as the main layout is already loaded
                logger.info("Navigated to home view");
            }
        } catch (Exception e) {
            String errorMsg = "An error occurred while navigating to home: " + e.getMessage();
            logger.error(errorMsg, e);
            showError("Navigation Error", errorMsg);
        }
    }

    /**
     * Sets the current user's session information.
     *
     * @param user         The current user, or null if no user is logged in
     * @param sessionToken The session token, or null if no session exists
     */
    public void setUserSession(User user, String sessionToken) {
        logger.info("Setting user session for user: {}", user != null ? user.getUsername() : "<none>");
        this.currentUser = user;
        this.sessionToken = sessionToken;

        // Update the UI coordinator if available
        if (uiCoordinator != null) {
            uiCoordinator.setUserSession(user, sessionToken);
        }

        // Update the UI to reflect the current user
        Platform.runLater(() -> {
            updateUserInterface();
            updateUserLabel();
        });
    }

    /**
     * Sets the primary stage for the application and initializes the UI coordinator if needed.
     * This method should be called once during application startup.
     *
     * @param primaryStage The primary stage of the JavaFX application
     */
    public void setPrimaryStage(Stage primaryStage) {
        if (primaryStage == null) {
            logger.warn("Attempted to set null primary stage");
            return;
        }

        if (this.primaryStage != null) {
            logger.warn("Primary stage is already set");
            return;
        }

        this.primaryStage = primaryStage;
        logger.info("Primary stage set in RefactoredMainController");

        // Ensure services are initialized
        if (!isInitialized && !isInitializing) {
            Platform.runLater(() -> {
                try {
                    initializeServices();
                    if (uiCoordinator != null) {
                        uiCoordinator.setPrimaryStage(primaryStage);
                        initializeCoordinatorsAndUI();
                    }
                } catch (Exception e) {
                    logger.error("Failed to initialize services after setting primary stage: {}", e.getMessage(), e);
                    showError("Initialization Error", "Failed to initialize application: " + e.getMessage());
                }
            });
        } else if (uiCoordinator != null) {
            uiCoordinator.setPrimaryStage(primaryStage);
        }
    }
}