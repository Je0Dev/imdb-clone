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
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.stage.Window;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.Objects;

import static com.papel.imdb_clone.util.UIUtils.showError;

/**
 * MainController serves as the primary controller for the application's main window.
 * It manages the overall application state, handles navigation between views, and coordinates
 * between different UI components and services.
 *
 * <p>Key responsibilities include:
 * <ul>
 *     <li>Managing the main application window and its layout</li>
 *     <li>Handling user session state and authentication</li>
 *     <li>Coordinating between different UI components</li>
 *     <li>Providing navigation between different application views</li>
 *     <li>Managing application lifecycle and resource cleanup</li>
 * </ul>
 *
 * <p>This controller is tightly coupled with the main application window's FXML layout
 * and serves as the central hub for application-wide functionality.
 */

public class MainController extends BorderPane {
    private static final Logger logger = LoggerFactory.getLogger(MainController.class);
    private static final String MOVIES_VIEW = "/fxml/content/movie-view.fxml";
    private static final String SERIES_VIEW = "/fxml/content/series-view.fxml";
    private static final String ADVANCED_SEARCH_VIEW = "/fxml/search/advanced-search-view.fxml";

    // Service dependencies
    private final ServiceLocator serviceLocator = ServiceLocator.getInstance();
    private UICoordinator uiCoordinator;

    // UI Components
    @FXML private VBox sidebar;
    @FXML private Label statusLabel;
    @FXML private Label userLabel;
    @FXML private VBox featuredContent;
    @FXML private Button signInButton;
    @FXML private Button registerButton;
    
    // Application state
    private Map<String, Object> data;
    private Stage primaryStage;
    private User currentUser;
    private String sessionToken;
    private boolean isInitialized = false;
    private boolean isInitializing = false;

    // ===== Navigation Methods =====

    /**
     * Handles navigation to the home view.
     *
     * @param event The action event that triggered this navigation
     * @throws IllegalStateException if navigation fails
     */
    @FXML
    private void showHome(ActionEvent event) {
        // Navigate to the home view or refresh the current view
        logger.info("Navigating to home view");
        navigateToView(event, "/fxml/base/home-view.fxml", "Home");
        updateAuthUI();
        updateUserLabel();
    }
    
    /**
     * Handles navigation to the movies view.
     *
     * @param event The action event that triggered this navigation
     * @throws IllegalStateException if navigation fails
     */
    @FXML
    private void showMovies(ActionEvent event) {
        navigateToView(event, MOVIES_VIEW, "Movies");
    }

    /**
     * Handles navigation to the TV shows view.
     *
     * @param event The action event that triggered this navigation
     * @throws IllegalStateException if navigation fails
     */
    @FXML
    private void showTVShows(ActionEvent event) {
        navigateToView(event, SERIES_VIEW, "TV Shows");
    }

    /**
     * Handles navigation to the advanced search view.
     *
     * @param event The action event that triggered this navigation
     * @throws IllegalStateException if navigation fails
     */
    @FXML
    private void showAdvancedSearch(ActionEvent event) {
        navigateToView(event, ADVANCED_SEARCH_VIEW, "Advanced Search");
    }
    
    /**
     * Updates the UI based on the current authentication state
     */
    private void updateAuthUI() {
        boolean isLoggedIn = currentUser != null;
        
        // Update sign in and register buttons visibility
        if (signInButton != null) {
            signInButton.setVisible(!isLoggedIn);
            signInButton.setManaged(!isLoggedIn);
        }
        
        if (registerButton != null) {
            registerButton.setVisible(!isLoggedIn);
            registerButton.setManaged(!isLoggedIn);
        }
        
        // Update user label
        if (userLabel != null) {
            if (isLoggedIn) {
                userLabel.setText("Welcome, " + currentUser.getUsername());
            } else {
                userLabel.setText("Not logged in");
            }
        }
    }
    
    /**
     * Sets the current user and updates the UI accordingly
     * @param user The user to set as current, or null to log out
     */
    public void setCurrentUser(User user) {
        this.currentUser = user;
        updateAuthUI();
    }
    

    /**
     * Common method to handle view navigation.
     *
     * @param event The action event that triggered the navigation
     * @param viewPath The FXML path of the view to navigate to
     * @param title The title to display in the window
     * @throws IllegalStateException if navigation fails
     */
    private void navigateToView(ActionEvent event, String viewPath, String title) {
        Objects.requireNonNull(event, "ActionEvent cannot be null");
        Objects.requireNonNull(viewPath, "View path cannot be null");
        
        try {
            Node source = (Node) event.getSource();
            Window window = source.getScene().getWindow();
            if (!(window instanceof Stage)) {
                throw new IllegalStateException("Could not determine the current stage");
            }
            
            NavigationService navigationService = NavigationService.getInstance();
            navigationService.navigateTo(viewPath, data, (Stage) window, title);
            logger.debug("Navigated to {} view", title);
            
        } catch (Exception e) {
            String errorMsg = String.format("Failed to navigate to %s view: %s", title, e.getMessage());
            logger.error(errorMsg, e);
            showError("Navigation Error", errorMsg);
            throw new IllegalStateException(errorMsg, e);
        }
    }

    // ===== Initialization Methods =====

    /**
     * Default constructor for FXML loader.
     * Note: The initialize() method will be called after FXML injection.
     */
    public MainController() {
        // Services will be initialized in the initialize() method
    }

    /**
     * Constructor for programmatic creation with user session.
     *
     * @param user The authenticated user, or null for guest access
     * @param sessionToken The session token for the authenticated user, or null
     * @throws IllegalStateException if service initialization fails
     */
    public MainController(User user, String sessionToken) {
        // Initialize fields directly first
        this.currentUser = user;
        this.sessionToken = Objects.requireNonNull(sessionToken, "Session token cannot be null");
        
        try {
            // Initialize services without using 'this' in a way that could escape
            this.uiCoordinator = ServiceLocator.getUICoordinator(UICoordinator.class);
            
            // Set up the UI coordinator if available
            if (this.uiCoordinator != null) {
                this.uiCoordinator.setUserSession(user, sessionToken);
            }
            
            // Log initialization
            logger.info("MainController initialized for user: {}", 
                user != null ? user.getUsername() : "<guest>");
                
        } catch (Exception e) {
            String errorMsg = String.format("Failed to initialize MainController: %s", e.getMessage());
            logger.error(errorMsg, e);
            throw new IllegalStateException(errorMsg, e);
        }
    }


    /**
     * Initializes the service layer components required by the controller.
     * This includes setting up the data manager and UI coordinator.
     *
     * @throws IllegalStateException if required services cannot be initialized
     */
    private void initializeServices() {
        // Prevent re-entrancy and redundant initialization
        if (isInitialized) {
            logger.debug("Services already initialized, skipping...");
            return;
        }
        if (isInitializing) {
            logger.warn("Service initialization already in progress");
            return;
        }

        isInitializing = true;
        try {
            logger.info("Initializing services...");

            // Initialize core services
            DataManager dataManager = initializeDataManager();
            initializeUICoordinator();
            
            // Load initial data if needed
            loadInitialData(dataManager);

            logger.info("All services initialized successfully");
            isInitialized = true;
            
        } catch (Exception e) {
            String errorMsg = String.format("Failed to initialize services: %s", e.getMessage());
            logger.error(errorMsg, e);
            throw new IllegalStateException(errorMsg, e);
        } finally {
            isInitializing = false;
        }
    }
    
    /**
     * Initializes the DataManager service.
     *
     * @return The initialized DataManager instance
     * @throws IllegalStateException if DataManager cannot be initialized
     */
    private DataManager initializeDataManager() {
        DataManager dataManager = serviceLocator.getDataManager();
        if (dataManager == null) {
            throw new IllegalStateException("Failed to get DataManager from ServiceLocator");
        }
        logger.info("DataManager initialized successfully");
        return dataManager;
    }
    
    /**
     * Initializes the UICoordinator service and sets up the user session.
     *
     * @throws IllegalStateException if UICoordinator cannot be initialized
     */
    private void initializeUICoordinator() {
        this.uiCoordinator = ServiceLocator.getUICoordinator(UICoordinator.class);
        if (this.uiCoordinator == null) {
            throw new IllegalStateException("Failed to get UICoordinator from ServiceLocator");
        }
        
        // Set user session if available
        if (this.currentUser != null && this.sessionToken != null) {
            this.uiCoordinator.setUserSession(this.currentUser, this.sessionToken);
        }
        logger.info("UICoordinator initialized successfully");
    }
    
    /**
     * Loads initial application data if it hasn't been loaded yet.
     *
     * @param dataManager The DataManager instance to use for loading data
     * @throws IllegalStateException if data loading fails
     */
    private void loadInitialData(DataManager dataManager) {
        if (dataManager == null || dataManager.isDataLoaded()) {
            return;
        }
        
        try {
            logger.info("Loading initial application data...");
            dataManager.loadAllData();
            logger.info("Initial data loaded successfully");
        } catch (Exception e) {
            String errorMsg = String.format("Failed to load initial data: %s", e.getMessage());
            logger.error(errorMsg, e);
            throw new IllegalStateException(errorMsg, e);
        }
    }

    /**
     * Initializes the controller after its root element has been completely processed.
     * This method is automatically called by JavaFX after the FXML file has been loaded.
     * It sets up the UI components, initializes services, and loads initial data.
     * 
     * <p>This method performs the following operations:
     * <ol>
     *   <li>Initializes required services</li>
     *   <li>Loads and initializes all views</li>
     *   <li>Sets up the initial view (home view)</li>
     *   <li>Updates the UI state</li>
     * </ol>
     * 
     * @throws IllegalStateException if any critical initialization step fails
     */
    @FXML
    public void initialize() {
        logger.info("Initializing MainController...");

        try {
            // 1. Initialize required services
            initializeServices();
            validateServiceDependencies();

            // 2. Load and initialize views asynchronously
            Platform.runLater(this::initializeUI);

        } catch (Exception e) {
            String errorMsg = String.format("Failed to initialize MainController: %s", e.getMessage());
            logger.error(errorMsg, e);
            showError("Initialization Error", "Failed to initialize the application: " + e.getMessage());
            throw new IllegalStateException(errorMsg, e);
        }
    }
    
    /**
     * Validates that all required service dependencies are properly initialized.
     * 
     * @throws IllegalStateException if any required service is not available
     */
    private void validateServiceDependencies() {
        if (uiCoordinator == null) {
            throw new IllegalStateException("UICoordinator not initialized");
        }
        if (serviceLocator == null) {
            throw new IllegalStateException("ServiceLocator not available");
        }
    }
    
    /**
     * Initializes the UI components asynchronously.
     * This method is called on the JavaFX Application Thread.
     */
    private void initializeUI() {
        try {
            // 1. Load all views
            logger.info("Loading application views...");
            boolean hasViewLoadingErrors = uiCoordinator.loadAndInitializeViews();
            
            if (hasViewLoadingErrors) {
                logger.warn("Some views failed to load, but continuing with available views");
            }

            // 2. Set up the initial view
            setupInitialView();
            
            // 3. Update UI state
            updateUserInterface();
            
            logger.info("UI initialization completed successfully");
            
        } catch (Exception e) {
            String errorMsg = String.format("Failed to initialize UI: %s", e.getMessage());
            logger.error(errorMsg, e);
            showError("UI Initialization Error", errorMsg);
            throw new IllegalStateException(errorMsg, e);
        }
    }
    
    /**
     * Sets up the initial view (home view) in the center of the main window.
     */
    private void setupInitialView() {
        Node homeView = uiCoordinator.getHomeView();
        if (homeView != null) {
            setCenter(homeView);
            logger.debug("Successfully set home view");
        } else {
            throw new IllegalStateException("Failed to load home view");
        }
    }


    /**
     * Updates UI elements that depend on the current user/session state and refreshes views.
     * This method is called whenever the UI needs to be refreshed to reflect the current state.
     */
    private void updateUserInterface() {
        try {
            // Update UI elements
            updateUserLabel();
            updateSidebarState();
            updateStatusBar();
            logger.debug("UI updated to reflect current application state");
        } catch (Exception e) {
            // Show error to user
            String errorMsg = String.format("Failed to update user interface: %s", e.getMessage());
            logger.warn(errorMsg, e);
            showError("UI Update Error", "Failed to update user interface");
            throw new IllegalStateException(errorMsg, e);
        }
    }
    
    /**
     * Updates the sidebar state based on the current user's permissions.
     */
    private void updateSidebarState() {
        if (sidebar == null) {
            logger.warn("Sidebar component is not initialized");
            return;
        }
        
        // Enable/disable sidebar items based on user permissions
        // Example: Disable admin features for non-admin users
        boolean isAdmin = currentUser != null && currentUser.isAdmin();
        sidebar.setDisable(!isAdmin);
    }
    
    /**
     * Updates the status bar with the current application state.
     */
    private void updateStatusBar() {
        if (statusLabel == null) {
            return;
        }
        
        String status = currentUser != null 
            ? String.format("Logged in as %s", currentUser.getUsername())
            : "Not logged in";
            
        statusLabel.setText(status);
    }

    /**
     * Updates the user label in the UI to reflect the current user's information.
     * If no user is logged in, it displays "Guest" as the username.
     * 
     * <p>This method handles the following scenarios:
     * <ul>
     *   <li>User is logged in: Displays the username</li>
     *   <li>User is not logged in: Displays "Guest"</li>
     *   <li>userLabel is not initialized: Logs a warning</li>
     * </ul>
     */
    private void updateUserLabel() {
        if (userLabel == null) {
            logger.warn("userLabel is not initialized in FXML. Cannot update user display.");
            return;
        }
        
        if (currentUser != null) {
            String username = currentUser.getUsername();
            // Log only first few and last few characters of session token for security
            String tokenPreview = sessionToken != null 
                ? String.format("%s...%s", 
                    sessionToken.substring(0, Math.min(4, sessionToken.length())),
                    sessionToken.substring(sessionToken.length() - 4))
                : "<no-token>";
                
            logger.debug("Updating user label for user: {}, session: {}", username, tokenPreview);
            userLabel.setText(username);
            userLabel.getStyleClass().remove("guest-label");
            userLabel.getStyleClass().add("user-label");
        } else {
            logger.debug("No user logged in, showing guest mode");
            userLabel.setText("Guest");
            userLabel.getStyleClass().remove("user-label");
            userLabel.getStyleClass().add("guest-label");
        }
    }


    /**
     * Sets the current user's session information and updates the UI accordingly.
     * This method is thread-safe and can be called from any thread.
     *
     * @param user         The current user, or null if logging out
     * @param sessionToken The session token, or null if no session exists
     * @throws IllegalArgumentException if sessionToken is null when user is not null
     */
    public void setUserSession(User user, String sessionToken) {
        // Validate parameters
        if (user != null && sessionToken == null) {
            throw new IllegalArgumentException("Session token cannot be null when user is not null");
        }
        
        // Update state
        this.currentUser = user;
        this.sessionToken = sessionToken;
        
        // Log the session change (without exposing sensitive information)
        logger.info("User session updated - User: {}", 
            user != null ? user.getUsername() : "<none>");

        // Update UI coordinator on the JavaFX Application Thread
        Platform.runLater(() -> {
            try {
                // Update UI coordinator if available
                if (uiCoordinator != null) {
                    uiCoordinator.setUserSession(user, sessionToken);
                }
                
                // Update the UI to reflect the current user
                updateUserInterface();
                
            } catch (Exception e) {
                String errorMsg = String.format("Failed to update UI for user session: %s", e.getMessage());
                logger.error(errorMsg, e);
                showError("Session Error", "Failed to update user interface");
            }
        });
    }

    /**
     * Sets the primary stage for the application.
     * This method must be called exactly once during application startup,
     * before any UI operations are performed.
     *
     * @param primaryStage The primary stage of the JavaFX application (must not be null)
     * @throws IllegalStateException if the primary stage is already set
     * @throws IllegalArgumentException if primaryStage is null
     */
    public void setPrimaryStage(Stage primaryStage) {
        Objects.requireNonNull(primaryStage, "Primary stage cannot be null");

        synchronized (this) {
            if (this.primaryStage != null) {
                String errorMsg = "Primary stage is already set";
                logger.error(errorMsg);
                throw new IllegalStateException(errorMsg);
            }
            
            this.primaryStage = primaryStage;
            logger.info("Primary stage set successfully");
            
            // Configure primary stage properties
            configurePrimaryStage(primaryStage);
        }
    }
    
    /**
     * Configures the primary stage with default settings.
     * 
     * @param stage The primary stage to configure
     */
    private void configurePrimaryStage(Stage stage) {
        stage.setOnCloseRequest(event -> {
            logger.info("Application shutdown requested");
            // Add any cleanup code here if needed
        });
        
        // Set minimum size constraints
        stage.setMinWidth(1024);
        stage.setMinHeight(768);
        
        // Set window title
        stage.setTitle("IMDb Clone");
        
        // Set up window state change listeners
        stage.iconifiedProperty().addListener((obs, wasIconified, isNowIconified) -> {
            logger.debug("Window {}minimized", isNowIconified ? "" : "not ");
        });
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

    public void showLogin(ActionEvent actionEvent) {
        try {
            NavigationService.getInstance().showLogin(primaryStage);
        } catch (Exception e) {
            logger.error("Failed to navigate to login view: {}", e.getMessage(), e);
            showError("Navigation Error", "Failed to open login view: " + e.getMessage());
        }
    }

    public void showRegister(ActionEvent actionEvent) {
        try {
            NavigationService.getInstance().showRegister(primaryStage);
        } catch (Exception e) {
            logger.error("Failed to navigate to register view: {}", e.getMessage(), e);
            showError("Navigation Error", "Failed to open register view: " + e.getMessage());
        }
    }
}
