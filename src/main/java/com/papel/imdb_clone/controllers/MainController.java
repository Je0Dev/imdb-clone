package com.papel.imdb_clone.controllers;

import com.papel.imdb_clone.controllers.coordinator.UICoordinator;
import com.papel.imdb_clone.data.DataManager;
import com.papel.imdb_clone.gui.MovieAppGui;
import com.papel.imdb_clone.model.people.User;
import com.papel.imdb_clone.service.navigation.NavigationService;
import com.papel.imdb_clone.service.search.ServiceLocator;
import com.papel.imdb_clone.service.validation.AuthService;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.stage.Window;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Map;
import java.util.Objects;

import static com.papel.imdb_clone.util.UIUtils.showError;

/**
 * MainController serves as the primary controller for the application's main window.
 * It manages the overall application state, handles navigation between views, and coordinates
 * between different UI components and services.
 */

public class MainController extends BorderPane {
    private static final Logger logger = LoggerFactory.getLogger(MainController.class);
    private static final String MOVIES_VIEW = "/fxml/content/movie-view.fxml";
    private static final String SERIES_VIEW = "/fxml/content/series-view.fxml";
    private static final String ADVANCED_SEARCH_VIEW = "/fxml/search/advanced-search-view.fxml";
    private static final String RATED_VIEW = "/fxml/content/rated-tab.fxml";

    // Service dependencies
    private final ServiceLocator serviceLocator = ServiceLocator.getInstance();
    private final com.papel.imdb_clone.service.validation.AuthService authService = 
        com.papel.imdb_clone.service.validation.AuthService.getInstance();
    private UICoordinator uiCoordinator;

    // UI Components
    @FXML private VBox sidebar;
    private static final Logger sidebarLogger = LoggerFactory.getLogger("SidebarDebug");
    @FXML private Label statusLabel;
    @FXML private Label userLabel;
    @FXML private VBox featuredContent;
    @FXML private Button signInButton;
    @FXML private Button registerButton;
    @FXML private Button logoutButton;
    @FXML private Button moviesButton;
    @FXML private Button tvShowsButton;
    @FXML private Button celebritiesButton;
    @FXML private Button ratedButton;
    @FXML private Button advancedSearchButton;
    @FXML private Label guestMessage;
    
    // Application state
    private Map<String, Object> data;
    private Stage primaryStage;
    private User currentUser;
    private String sessionToken;
    private boolean isInitialized = false;
    private boolean isInitializing = false;
    private boolean isAdmin;
    private boolean isGuest = true; // Default to guest mode

    // ===== Navigation Methods =====

    /**
     * Handles navigation to the home view.
     *
     * @param event The action event that triggered this navigation
     * @throws IllegalStateException if navigation fails
     */
    @FXML
    private void showHome(ActionEvent event) {
        try {
            logger.info("Navigating to home view");
            
            // Store a reference to the current sidebar or create a new one if needed
            VBox currentSidebar = this.sidebar != null ? this.sidebar : loadSidebar();
            
            // Ensure sidebar is properly configured
            if (currentSidebar != null) {
                currentSidebar.setDisable(false);
                currentSidebar.setVisible(true);
                currentSidebar.setManaged(true);
            }
            
            // Create a simple home content panel
            VBox homeContent = new VBox(20);
            homeContent.setPadding(new Insets(40));
            homeContent.setAlignment(Pos.CENTER);
            homeContent.setStyle("-fx-background-color: #0f0f0f;");
            
            // Add welcome message
            Label welcomeLabel = new Label("Welcome to IMDb Clone");
            welcomeLabel.setStyle("-fx-text-fill: #f5c518; -fx-font-size: 36px; -fx-font-weight: bold;");
            
            Label subLabel = new Label("Discover and explore your favorite movies and TV shows.");
            subLabel.setStyle("-fx-text-fill: #cccccc; -fx-font-size: 18px;");
            subLabel.setWrapText(true);
            
            // Add navigation buttons
            HBox buttonRow1 = new HBox(20);
            buttonRow1.setAlignment(Pos.CENTER);
            
            Button moviesButton = new Button("Movies");
            moviesButton.setStyle("-fx-background-color: #f5c518; -fx-text-fill: #000000; -fx-font-size: 16px; -fx-pref-width: 150; -fx-cursor: hand;");
            moviesButton.setOnAction(e -> {
                try {
                    showMovies(e);
                } catch (Exception ex) {
                    logger.error("Error in movies navigation: ", ex);
                    showError("Navigation Error", "Failed to navigate to Movies: " + ex.getMessage());
                }
            });
            
            Button tvShowsButton = new Button("TV Shows");
            tvShowsButton.setStyle("-fx-background-color: #f5c518; -fx-text-fill: #000000; -fx-font-size: 16px; -fx-pref-width: 150; -fx-cursor: hand;");
            tvShowsButton.setOnAction(e -> {
                try {
                    showTVShows(e);
                } catch (Exception ex) {
                    logger.error("Error in TV shows navigation: ", ex);
                    showError("Navigation Error", "Failed to navigate to TV Shows: " + ex.getMessage());
                }
            });
            
            buttonRow1.getChildren().addAll(moviesButton, tvShowsButton);
            
            // Add everything to the content
            homeContent.getChildren().addAll(welcomeLabel, subLabel, buttonRow1);
            
            // Update the UI on the JavaFX Application Thread
            Platform.runLater(() -> {
                try {
                    // Get the current user
                    User user = sessionToken != null ? authService.getCurrentUser(sessionToken) : null;
                    // Get the current scene and root
                    Scene currentScene = this.getScene();
                    BorderPane rootPane = null;
                    
                    // If we have a scene and the root is a BorderPane, use it
                    if (currentScene != null && currentScene.getRoot() instanceof BorderPane) {
                        rootPane = (BorderPane) currentScene.getRoot();
                    }
                    
                    // If no valid root pane, create a new one
                    if (rootPane == null) {
                        rootPane = new BorderPane();
                        if (currentScene == null) {
                            Stage stage = primaryStage != null ? primaryStage : new Stage();
                            currentScene = new Scene(rootPane);
                            stage.setScene(currentScene);
                            stage.show();
                        } else {
                            currentScene.setRoot(rootPane);
                        }
                    }
                    
                    // Always update the center content and sidebar
                    rootPane.setCenter(homeContent);
                    
                    if (currentSidebar != null) {
                        rootPane.setLeft(currentSidebar);
                        this.sidebar = currentSidebar; // Update the reference
                    }
                    
                    // Ensure the window is visible
                    if (currentScene.getWindow() != null) {
                        currentScene.getWindow().sizeToScene();
                    }
                    
                    // Request focus on the content
                    homeContent.requestFocus();
                    
                    logger.info("Home view loaded successfully with preserved sidebar");
                    
                } catch (Exception e) {
                    logger.error("Error updating home view: ", e);
                    showError("Navigation Error", "Failed to update home view: " + e.getMessage());
                }
            });
            
        } catch (Exception e) {
            logger.error("Failed to load home view: {}", e.getMessage(), e);
            showError("Navigation Error", "Failed to load home view: " + e.getMessage());
        }
    }

    //load sidebar
    private VBox loadSidebar() {
        try {
            // Load the sidebar FXML
            FXMLLoader loader = new FXMLLoader(getClass().getResource("sidebar-view.fxml"));
            return loader.load();
        } catch (IOException e) {
            logger.error("Failed to load sidebar: {}", e.getMessage(), e);
            showError("Navigation Error", "Failed to load sidebar: " + e.getMessage());
            return null;
        }
    }

    /**
     * Handles navigation to the movies view.
     *
     * @param event The action event that triggered this navigation
     * @throws IllegalStateException if navigation fails
     */
    @FXML
    private void showMovies(ActionEvent event) {
        if (!authService.isAuthenticated()) {
            showError("Authentication Required", "Please sign in to access this feature.");
            return;
        }
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
        if (!authService.isAuthenticated()) {
            showError("Authentication Required", "Please sign in to access this feature.");
            return;
        }
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
        if (!authService.isAuthenticated()) {
            showError("Authentication Required", "Please sign in to access this feature.");
            return;
        }
        navigateToView(event, ADVANCED_SEARCH_VIEW, "Advanced Search");
    }
    
    @FXML
    private void showRated(ActionEvent event) {
        if (!authService.isAuthenticated()) {
            showError("Authentication Required", "Please sign in to access this feature.");
            return;
        }
        navigateToView(event, RATED_VIEW, "Your Rated Content");
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
            // Special handling for home view to prevent sidebar recreation
            if (viewPath.endsWith("home-view.fxml")) {
                showHome(event);
                return;
            }
            
            Node source = (Node) event.getSource();
            Window window = source.getScene().getWindow();
            if (!(window instanceof Stage)) {
                throw new IllegalStateException("Could not determine the current stage");
            }
            
            // Store the current sidebar state
            VBox currentSidebar = this.sidebar;
            
            // Navigate to the new view
            NavigationService navigationService = NavigationService.getInstance();
            navigationService.navigateTo(viewPath, data, (Stage) window, title);
            
            // If we have a sidebar reference, ensure it's restored
            if (currentSidebar != null && this.sidebar == null) {
                this.setLeft(currentSidebar);
                this.sidebar = currentSidebar;
            }
            
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
        }
    }

    /**
     * Checks the current authentication state and updates the UI accordingly.
            }
        } catch (Exception e) {
            logger.error("Error checking authentication: {}", e.getMessage(), e);
            this.isGuest = true;
            updateUIForGuestMode();
        }
    }

    /**
     * Sets the current user and updates the UI accordingly
     * @param user The user to set as current, or null to log out
     */
    public void setCurrentUser(User user) {
        this.currentUser = user;
        this.isGuest = (user == null);
        
        if (isGuest) {
            updateUIForGuestMode();
        } else {
            updateUserInterface();
        }
    }
    
    /**
     * Shows the login screen
     * @param event The action event that triggered this method
     */
    @FXML
    private void showLogin(ActionEvent event) {
        if (getScene() != null && getScene().getWindow() != null) {
            Object userData = getScene().getWindow().getUserData();
            if (userData instanceof MovieAppGui) {
                ((MovieAppGui) userData).showLoginScreen();
            }
        }
    }
    
    /**
     * Shows the registration screen
     * @param event The action event that triggered this method
     */
    @FXML
    private void showRegister(ActionEvent event) {
        if (getScene() != null && getScene().getWindow() != null) {
            Object userData = getScene().getWindow().getUserData();
            if (userData instanceof MovieAppGui) {
                ((MovieAppGui) userData).showRegisterScreen();
            }
        }
    }

    /**
     * Handles the logout action.
     * @param event The action event that triggered this method
     */
    @FXML
    private void handleLogout(ActionEvent event) {
        try {
            // Clear authentication state
            if (sessionToken != null) {
                authService.logout(sessionToken);
                sessionToken = null;
            }
            
            // Update UI for guest mode
            updateUIForAuthState(false);
            
            // Clear any user-specific data
            setCurrentUser(null);
            
            // Show login screen
            if (getScene() != null && getScene().getWindow() != null) {
                Object userData = getScene().getWindow().getUserData();
                if (userData instanceof MovieAppGui) {
                    ((MovieAppGui) userData).showLoginScreen();
                }
            }
            
        } catch (Exception e) {
            logger.error("Error during logout: {}", e.getMessage(), e);
            showError("Logout Error", "Failed to log out. Please try again.");
        }
    }

    /**
     * Checks the current authentication state and updates the UI accordingly.
     * This method verifies if the current session token is still valid.
     */
    private void checkAuthentication() {
        try {
            if (sessionToken != null) {
                // Try to get the current user with the session token
                User user = authService.getCurrentUser(sessionToken);
                if (user != null) {
                    // User is authenticated
                    setCurrentUser(user);
                    updateUIForAuthState(true);
                    return;
                }
            }
            // If we get here, user is not authenticated
            setCurrentUser(null);
            updateUIForAuthState(false);
        } catch (Exception e) {
            logger.error("Error checking authentication: {}", e.getMessage(), e);
            // In case of error, assume not authenticated
            setCurrentUser(null);
            updateUIForAuthState(false);
        }
    }
    
    /**
     * Updates the UI for guest users.
     */
    private void updateUIForGuestMode() {
        updateUIForAuthState(false);
        if (userLabel != null) userLabel.setText("Guest");
        if (signInButton != null) signInButton.setVisible(true);
        if (registerButton != null) registerButton.setVisible(true);
        if (logoutButton != null) logoutButton.setVisible(false);
    }

    @FXML
    public void initialize() {
        try {
            logger.info("Initializing MainController");

            // Initialize UI coordinator
            this.uiCoordinator = UICoordinator.getInstance();

            // Initialize UI state based on authentication
            updateUIForAuthState(authService.isAuthenticated());
            
            // Set up button actions
            setupButtonActions();
            
            // Check if we have a logged-in user
            checkAuthentication();

            logger.info("MainController initialized successfully");
            isInitialized = true;
        } catch (Exception e) {
            logger.error("Error initializing MainController: {}", e.getMessage(), e);
            showError("Initialization Error", "Failed to initialize the application.");
        }
    }
    
    /**
     * Sets up button actions for navigation
     */
    private void setupButtonActions() {
        if (moviesButton != null) {
            moviesButton.setOnAction(this::showMovies);
        }
        if (tvShowsButton != null) {
            tvShowsButton.setOnAction(this::showTVShows);
        }
        if (celebritiesButton != null) {
            celebritiesButton.setOnAction(this::showCelebrities);
        }
        if (ratedButton != null) {
            ratedButton.setOnAction(this::showRated);
        }
        if (advancedSearchButton != null) {
            advancedSearchButton.setOnAction(this::showAdvancedSearch);
        }
        if (signInButton != null) {
            signInButton.setOnAction(this::showLogin);
        }
        if (registerButton != null) {
            registerButton.setOnAction(this::showRegister);
        }
        if (logoutButton != null) {
            logoutButton.setOnAction(this::handleLogout);
        }
    }
    
    /**
     * Updates the UI based on authentication state
     * @param isAuthenticated whether the user is authenticated
     */
    public void updateUIForAuthState(boolean isAuthenticated) {
        Platform.runLater(() -> {
            // Update UI elements based on authentication state
            if (logoutButton != null) {
                logoutButton.setVisible(isAuthenticated);
                logoutButton.setManaged(isAuthenticated);
            }
            if (signInButton != null) {
                signInButton.setVisible(!isAuthenticated);
                signInButton.setManaged(!isAuthenticated);
            }
            if (registerButton != null) {
                registerButton.setVisible(!isAuthenticated);
                registerButton.setManaged(!isAuthenticated);
            }
            if (guestMessage != null) {
                guestMessage.setVisible(!isAuthenticated);
            }
            
            // Enable/disable feature buttons based on authentication
            setFeatureButtonsEnabled(isAuthenticated);
            
            // Update user label
            updateUserLabel();
        });
    }
    
    /**
     * Enables or disables feature buttons based on authentication
     * @param enabled whether to enable the buttons
     */
    private void setFeatureButtonsEnabled(boolean enabled) {
        if (moviesButton != null) {
            moviesButton.setDisable(!enabled);
            moviesButton.setOpacity(enabled ? 1.0 : 0.5);
        }
        if (tvShowsButton != null) {
            tvShowsButton.setDisable(!enabled);
            tvShowsButton.setOpacity(enabled ? 1.0 : 0.5);
        }
        if (celebritiesButton != null) {
            celebritiesButton.setDisable(!enabled);
            celebritiesButton.setOpacity(enabled ? 1.0 : 0.5);
        }
        if (ratedButton != null) {
            ratedButton.setDisable(!enabled);
            ratedButton.setOpacity(enabled ? 1.0 : 0.5);
        }
        if (advancedSearchButton != null) {
            advancedSearchButton.setDisable(!enabled);
            advancedSearchButton.setOpacity(enabled ? 1.0 : 0.5);
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

    public void updateUserInterface() {
        updateUserLabel();
        updateSidebarState();
        updateStatusBar();
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
     * Updates the sidebar state based on the current user's permissions.
     */
    public void updateSidebarState() {
        if (sidebar == null) {
            logger.warn("Sidebar component is not initialized");
            return;
        }
        
        // Force enable the sidebar and make it visible
        sidebar.setDisable(false);
        sidebar.setVisible(true);
        sidebar.setManaged(true);
        
        boolean isLoggedIn = currentUser != null;
        isAdmin = isLoggedIn && currentUser.isAdmin();
        
        // Log state changes
        sidebarLogger.info("Updating sidebar state - Logged In: {}, Admin: {}", isLoggedIn, isAdmin);
        
        // Set opacity based on login state
        double targetOpacity = isLoggedIn ? 1.0 : 0.5;
        sidebar.setOpacity(targetOpacity);
        
        // Process all buttons in the sidebar
        Platform.runLater(() -> {
            for (Node node : sidebar.getChildren()) {
                if (node instanceof Button button) {
                    String buttonText = button.getText();
                    if (isLoggedIn) {
                        // For logged-in users, enable all buttons
                        button.setDisable(false);
                        button.setOpacity(1.0);
                        button.setVisible(true);
                        button.setManaged(true);
                        sidebarLogger.info("Enabled button: {}", buttonText);
                    } else {
                        // For guests, only enable Sign In and Register buttons
                        boolean shouldEnable = buttonText != null && 
                            (buttonText.equals("Sign In") || buttonText.equals("Register"));
                        button.setDisable(!shouldEnable);
                        button.setOpacity(shouldEnable ? 1.0 : 0.5);
                        button.setVisible(true);
                        button.setManaged(true);
                        sidebarLogger.info("Button '{}' set to: {}", buttonText, shouldEnable ? "enabled" : "disabled");
                    }
                }
            }
        });
        
        // Add a listener to prevent disabling
        sidebar.disableProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal) {
                sidebarLogger.warn("Preventing sidebar from being disabled");
                Platform.runLater(() -> {
                    sidebar.setDisable(false);
                    sidebar.setVisible(true);
                    sidebar.setManaged(true);
                });
            }
        });
        
        logger.debug("Sidebar state updated - Logged In: {}, Admin: {}", isLoggedIn, isAdmin);
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
    public void updateUserLabel() {
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

    public void setUser(User user) {
        this.currentUser = user;
        updateUserInterface();
    }

    public void setStage(Stage stage) {
        this.primaryStage = stage;

    }
}
