package com.papel.imdb_clone.controllers;

import com.papel.imdb_clone.controllers.coordinator.UICoordinator;
import com.papel.imdb_clone.data.DataManager;
import com.papel.imdb_clone.model.people.User;
import com.papel.imdb_clone.service.navigation.NavigationService;
import com.papel.imdb_clone.service.search.ServiceLocator;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

import static com.papel.imdb_clone.util.UIUtils.showError;

/**
 * Main controller for the application's primary interface.
 * Handles navigation between different views and manages the application state.
 */
public class MainController extends BorderPane {
    private static final Logger logger = LoggerFactory.getLogger(MainController.class);

    private UICoordinator uiCoordinator;

    // UI Components
    @FXML
    private VBox sidebar;
    @FXML
    private Label statusLabel;
    @FXML
    private Label userLabel;
    @FXML
    private VBox featuredContent;
    private Map<String, Object> data;

    @FXML
    private void showMovies(ActionEvent event) {
        try {
            //Navigate to movies view
            NavigationService navigationService = NavigationService.getInstance();
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            navigationService.navigateTo("/fxml/content/movie-view.fxml", data, stage, "Movies");
        } catch (Exception e) {
            logger.error("Error navigating to movies view", e);
            showError("Navigation Error", "Failed to navigate to movies view: " + e.getMessage());
        }
    }

    //Navigate to advanced search view
    @FXML
    private void showAdvancedSearch(ActionEvent event) {
        try {
            //Navigate to advanced search view
            NavigationService navigationService = NavigationService.getInstance();
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            navigationService.navigateTo("/fxml/search/advanced-search-view.fxml", data, stage, "Advanced Search");
        } catch (Exception e) {
            logger.error("Error navigating to advanced search", e);
            showError("Navigation Error", "Failed to open advanced search: " + e.getMessage());
        }
    }


    //Navigate to TV shows view
    @FXML
    private void showTVShows(ActionEvent event) {
        try {
            NavigationService navigationService = NavigationService.getInstance();
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            navigationService.navigateTo("/fxml/content/series-view.fxml", data, stage, "TV Shows");
        } catch (Exception e) {
            logger.error("Error navigating to TV shows view", e);
            showError("Navigation Error", "Failed to navigate to TV shows: " + e.getMessage());
        }
    }

    // State
    private Stage primaryStage;
    private User currentUser;
    private String sessionToken;
    private String initializationError;
    private boolean isInitializing = false;
    private boolean isInitialized = false;

    /**
     * Default constructor for FXML loader.
     * Note: The initialize() method will be called after FXML injection.
     */
    public MainController() {
        // Initialize services from ServiceLocator in initialize()
    }

    /**
     * Constructor for programmatic creation with user session.
     */
    public MainController(User user, String sessionToken) {
        this();
        setUserSession(user, sessionToken);

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
            // Core services and coordinators
            DataManager dataManager = serviceLocator.getDataManager();
            if (dataManager == null) {
                throw new IllegalStateException("Failed to get DataManager from ServiceLocator");
            }

            logger.info("DataManager initialized successfully");

            // Only load data if it hasn't been loaded yet
            if (!dataManager.isDataLoaded()) {
                try {
                    logger.info("Loading initial data...");
                    dataManager.loadAllData();
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
            //Log error and throw runtime exception
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
        logger.info("Initializing RefactoredMainController...");

        try {
            // Initialize services first
            initializeServices();

            if (uiCoordinator == null) {
                throw new IllegalStateException("UICoordinator not initialized");
            }

            // Always try to load views, regardless of areViewsLoaded()
            logger.info("Loading all views...");
            if (uiCoordinator.loadAndInitializeViews()) {
                logger.warn("Some views failed to load, but continuing with available views");
            }

            // Set initial view
            Platform.runLater(() -> {
                try {

                    // Get home view and set it as center
                    Node homeView = uiCoordinator.getHomeView();
                    if (homeView != null) {
                        setCenter(homeView);
                        updateUserInterface();
                        logger.info("Successfully initialized UI with home view");
                    } else {
                        logger.error("Failed to load home view");
                    }
                } catch (Exception e) {
                    logger.error("Error initializing UI: {}", e.getMessage(), e);
                    showError("Initialization Error", "Failed to initialize UI: " + e.getMessage());
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

        //Initialize coordinators and UI on JavaFX thread
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
                if (uiCoordinator.areViewsLoaded()) {
                    logger.info("Loading views...");
                    if (uiCoordinator.loadAndInitializeViews()) {
                        logger.warn("Some views failed to load, but continuing with available views");
                    }
                }

                // Set initial view to home
                Node homeView = uiCoordinator.getHomeView();
                if (homeView != null) {
                    setCenter(homeView);
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
                //Set isInitializing to false which means initialization is complete which means
                //the application is ready to be used
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
     * Updates UI elements that depend on the current user/session state and refreshes views.
     */
    private void updateUserInterface() {
        try {
            updateUserLabel();
        } catch (Exception e) {
            logger.warn("Failed to update user interface state", e);
        }
    }

    /**
     * Updates the user label in the UI to reflect the current user's information.
     * If no user is logged in, it may clear or hide the user label.
     */
    private void updateUserLabel() {
        if (userLabel != null) {
            if (currentUser != null) {
                String username = currentUser.getUsername();
                logger.debug("Updating user label for user: {}, session token: {}", 
                    username, 
                    sessionToken != null ? "[HIDDEN]" + sessionToken.substring(Math.max(0, sessionToken.length() - 4)) : "null");
                userLabel.setText(username);
            } else {
                logger.debug("No user logged in, setting guest mode");
                userLabel.setText("Guest");
            }
        } else {
            logger.warn("userLabel is not initialized in FXML. Current user: {}, Session token: {}", 
                currentUser != null ? currentUser.getUsername() : "null",
                sessionToken != null ? "[HIDDEN]" + sessionToken.substring(Math.max(0, sessionToken.length() - 4)) : "null");
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
                    // Initialize services
                    initializeServices();
                    if (uiCoordinator != null) {
                        uiCoordinator.setPrimaryStage(primaryStage);
                        initializeCoordinatorsAndUI();
                    }
                    //Set isInitializing to false which means initialization is complete which means
                    //the application is ready to be used
                    isInitializing = false;
                } catch (Exception e) {
                    logger.error("Failed to initialize services after setting primary stage: {}", e.getMessage(), e);
                    showError("Initialization Error", "Failed to initialize application: " + e.getMessage());
                }
            });
        } else if (uiCoordinator != null) {
            uiCoordinator.setPrimaryStage(primaryStage);
        }
    }
    // Navigate to celebrities view
    @FXML
    public void showCelebrities(ActionEvent actionEvent) {
        try {
            if (primaryStage == null) {
                primaryStage = (Stage) ((Node) actionEvent.getSource()).getScene().getWindow();
            }
            //Navigate to celebrities view
            NavigationService.getInstance().showCelebrities(primaryStage);
        } catch (Exception e) {
            logger.error("Failed to navigate to celebrities view: {}", e.getMessage(), e);
            showError("Navigation Error", "Failed to open celebrities view: " + e.getMessage());
        }
    }
}
