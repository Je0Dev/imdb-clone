package com.papel.imdb_clone.controllers.authentication;

import com.papel.imdb_clone.controllers.BaseController;
import com.papel.imdb_clone.controllers.MainController;
import com.papel.imdb_clone.exceptions.AuthException;
import com.papel.imdb_clone.model.people.User;
import javafx.scene.Scene;
import javafx.stage.Window;
import javafx.stage.Stage;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.layout.StackPane;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.ToggleGroup;
import javafx.scene.input.KeyEvent;
import javafx.event.ActionEvent;

import java.io.IOException;
import java.io.Serializable;
import java.net.URL;
import java.util.Map;
import java.util.ResourceBundle;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.papel.imdb_clone.exceptions.ValidationException;
import com.papel.imdb_clone.model.people.User;
import com.papel.imdb_clone.service.validation.AuthService;
import com.papel.imdb_clone.service.navigation.NavigationService;
import com.papel.imdb_clone.service.validation.UserInputValidator;
import com.papel.imdb_clone.util.UIUtils;
import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Serializable;
import java.net.URL;
import java.util.*;
import java.util.List;
import java.util.Map;

/**
 * Controller for handling user authentication including login and registration.
 * Manages the authentication UI and coordinates with AuthService for user operations.
 */

public class AuthController extends BaseController implements Initializable {

    // Enhanced logging configuration
    private static final Logger logger = LoggerFactory.getLogger(AuthController.class);

    //transient means that the variable is not serialized which means it is not saved to the database
    private final transient UserInputValidator inputValidator; //user input validator for validationz
    private final transient AuthService authService; //authentication service for authentication
    private final transient NavigationService navigationService; //navigation service for navigation
    private transient String sessionToken; //session token for authentication
    private final transient Map<String, Serializable> data; //data to be passed to the next controller
    private Stage stage;
    private ActionEvent event;

    @FXML
    private Button loginButton;

    
    // Login form fields
    @FXML
    private TextField usernameField;  // Matches FXML fx:id="usernameField" in login-view.fxml
    @FXML
    private PasswordField passwordField;  // Matches FXML fx:id="passwordField" in login-view.fxml
    @FXML
    private Label loginErrorLabel;

    // UI Components - Registration
    @FXML
    private TextField firstNameField;
    @FXML
    private TextField lastNameField;
    // Register form fields
    @FXML
    private TextField registerUsernameField;  // This one is correct
    @FXML
    private ToggleGroup genderToggleGroup;
    @FXML
    private Button registerButton;
    @FXML
    private Label registerErrorLabel;
    @FXML
    private StackPane loginContainer;
    @FXML
    private StackPane registerContainer;
    @FXML
    public TextField registerPasswordVisibleField;  // For password visibility toggle in register form
    @FXML
    private TextField passwordVisibleField;  // For password visibility toggle in login form
    @FXML
    private TextField confirmPasswordVisibleField;  // This one is correct
    private TextField loginPasswordVisibleField;


    /**
     * Constructs a new AuthController with the specified dependencies.
     */
    public AuthController() {
        super();
        this.authService = AuthService.getInstance();
        this.navigationService = NavigationService.getInstance();
        this.inputValidator = new UserInputValidator();
        this.sessionToken = null;
        this.data = null;

        // Add shutdown hook to clean up session token when application exits
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            if (this.sessionToken != null) {
                System.out.println("Cleaning up session token on application shutdown...");
                this.sessionToken = null;
                System.out.println("Session token cleared.");
            }
        }));
    }

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        try {
            logger.debug("Initializing AuthController...");

            // Initialize all UI components first
            if (loginContainer != null) {
                logger.info("Login container found, initializing login view...");
                initializeLoginView();
            } else if (registerContainer != null) {
                logger.info("Register container found, initializing register view...");
                initializeRegisterView();
            } else {
                // If no container is found, try to initialize based on available components
                logger.info("No container found, initializing based on available components...");

                boolean hasLoginComponents = loginUsernameField != null && loginPasswordField != null && loginButton != null;
                boolean hasRegisterComponents = registerUsernameField != null && registerPasswordField != null &&
                                              confirmPasswordField != null && registerButton != null;

                if (hasLoginComponents && hasRegisterComponents) {
                    logger.info("Both login and register components found, initializing combined view...");
                    initializeCombinedView();
                } else if (hasLoginComponents) {
                    logger.info("Login components found, initializing login form...");
                    setupLoginForm(loginPasswordVisibleField);
                } else if (hasRegisterComponents) {
                    logger.info("Register components found, initializing registration form...");
                    setupRegistrationForm(registerPasswordVisibleField, confirmPasswordVisibleField);
                } else {
                    logger.error("No valid UI components found for authentication");
                    throw new IllegalStateException("No valid authentication components found in the view");
                }
            }

            logger.debug("AuthController initialization completed successfully");

        } catch (Exception e) {
            String errorMsg = "Error initializing AuthController: " + e.getMessage();
            logger.error(errorMsg, e);
            Platform.runLater(() ->
                showError("Initialization Error", "Failed to initialize the authentication form: " + e.getMessage())
            );
        }
    }

    /**
     * Initializes the login view components
     */
    private void initializeLoginView() {
        try {
            if (loginButton != null && usernameField != null && passwordField != null) {
                // Unbind first to prevent memory leaks
                loginButton.disableProperty().unbind();

                // Set up login button binding
                loginButton.disableProperty().bind(
                    usernameField.textProperty().isEmpty()
                        .or(passwordField.textProperty().isEmpty())
                );

                // Set up password visibility toggle if available
                if (passwordVisibleField != null) {
                    passwordVisibleField.visibleProperty().bind(passwordField.visibleProperty().not());
                    passwordField.visibleProperty().bind(passwordVisibleField.visibleProperty().not());
                    passwordVisibleField.textProperty().bindBidirectional(passwordField.textProperty());
                }

                // Handle Enter key press on password field
                passwordField.setOnKeyPressed(event -> {
                    if (event.getCode() == KeyCode.ENTER) {
                        handleLogin(new ActionEvent(loginButton, null));
                    }
                });

                logger.debug("Login view initialization complete");
            } else {
                logger.warn("Missing required login view components");
                if (loginButton == null) logger.warn("loginButton is null");
                if (usernameField == null) logger.warn("usernameField is null");
                if (passwordField == null) logger.warn("passwordField is null");
            }
        } catch (Exception e) {
            logger.error("Error initializing login view: {}", e.getMessage(), e);
            throw e;
        }
    }

    /**
     * Initializes the register view components
     */
    private void initializeRegisterView() {
        try {
            // Set up registration form if components are available
            if (registerButton != null && registerUsernameField != null &&
                emailField != null && passwordField != null && confirmPasswordField != null) {

                // Unbind first to prevent memory leaks
                registerButton.disableProperty().unbind();

                // Set up password visibility toggle if available
                if (passwordVisibleField != null && confirmPasswordVisibleField != null) {
                    passwordVisibleField.visibleProperty().bind(passwordField.visibleProperty().not());
                    passwordField.visibleProperty().bind(passwordVisibleField.visibleProperty().not());
                    passwordVisibleField.textProperty().bindBidirectional(passwordField.textProperty());

                    confirmPasswordVisibleField.visibleProperty().bind(confirmPasswordField.visibleProperty().not());
                    confirmPasswordField.visibleProperty().bind(confirmPasswordVisibleField.visibleProperty().not());
                    confirmPasswordVisibleField.textProperty().bindBidirectional(confirmPasswordField.textProperty());
                }

                // Set up form validation
                registerButton.disableProperty().bind(
                    (firstNameField != null ? firstNameField.textProperty().isEmpty() : Bindings.createBooleanBinding(() -> false))
                        .or(lastNameField != null ? lastNameField.textProperty().isEmpty() : Bindings.createBooleanBinding(() -> false))
                        .or(registerUsernameField.textProperty().isEmpty()
                            .or(Bindings.createBooleanBinding(() ->
                                !inputValidator.isValidUsername(registerUsernameField.getText()),
                                registerUsernameField.textProperty())))
                        .or(emailField.textProperty().isEmpty()
                            .or(Bindings.createBooleanBinding(() ->
                                !inputValidator.isValidEmail(emailField.getText()),
                                emailField.textProperty())))
                        .or(passwordField.textProperty().isEmpty()
                            .or(Bindings.createBooleanBinding(
                                () -> !passwordField.getText().equals(confirmPasswordField.getText()),
                                passwordField.textProperty(),
                                confirmPasswordField.textProperty())))
                );

                logger.debug("Register view initialization complete");
            } else {
                logger.warn("Missing required register view components");
                if (registerButton == null) logger.warn("registerButton is null");
                if (registerUsernameField == null) logger.warn("registerUsernameField is null");
                if (emailField == null) logger.warn("emailField is null");
                if (passwordField == null) logger.warn("passwordField is null");
                if (confirmPasswordField == null) logger.warn("confirmPasswordField is null");
            }
        } catch (Exception e) {
            logger.error("Error initializing register view: {}", e.getMessage(), e);
            throw e;
        }
    }

    /**
     * Initializes the combined login/register view.
     */
    private void initializeCombinedView() {
        try {
            logger.debug("Initializing combined login/register view");

            // Set initial visibility of containers
            if (loginContainer != null) {
                loginContainer.setVisible(true);
                logger.debug("Login container set to visible");
            } else {
                logger.warn("Login container is null");
            }

            if (registerContainer != null) {
                registerContainer.setVisible(false);
                logger.debug("Register container set to invisible");
            } else {
                logger.warn("Register container is null");
            }

            // Set up login form components
            if (loginButton != null && usernameField != null && passwordField != null) {
                // Unbind first to prevent memory leaks
                loginButton.disableProperty().unbind();

                // Set up login button binding
                loginButton.disableProperty().bind(
                    usernameField.textProperty().isEmpty()
                        .or(passwordField.textProperty().isEmpty())
                );

                // Set up password visibility toggle if available
                if (passwordVisibleField != null) {
                    passwordVisibleField.visibleProperty().bind(passwordField.visibleProperty().not());
                    passwordField.visibleProperty().bind(passwordVisibleField.visibleProperty().not());
                    passwordVisibleField.textProperty().bindBidirectional(passwordField.textProperty());
                }

                logger.debug("Login form components initialized");
            } else {
                logger.warn("Missing required login form components");
                if (loginButton == null) logger.warn("loginButton is null");
                if (usernameField == null) logger.warn("usernameField is null");
                if (passwordField == null) logger.warn("passwordField is null");
            }

            // Set up registration form
            if (registerButton != null && registerUsernameField != null &&
                registerPasswordField != null && confirmPasswordField != null) {

                // Unbind first to prevent memory leaks
                registerButton.disableProperty().unbind();

                // Set up password visibility toggle if available
                if (registerPasswordVisibleField != null && confirmPasswordVisibleField != null) {
                    registerPasswordVisibleField.visibleProperty().bind(registerPasswordField.visibleProperty().not());
                    registerPasswordField.visibleProperty().bind(registerPasswordVisibleField.visibleProperty().not());
                    registerPasswordVisibleField.textProperty().bindBidirectional(registerPasswordField.textProperty());

                    confirmPasswordVisibleField.visibleProperty().bind(confirmPasswordField.visibleProperty().not());
                    confirmPasswordField.visibleProperty().bind(confirmPasswordVisibleField.visibleProperty().not());
                    confirmPasswordVisibleField.textProperty().bindBidirectional(confirmPasswordField.textProperty());
                }

                // Set up form validation
                registerButton.disableProperty().bind(
                    (firstNameField != null ? firstNameField.textProperty().isEmpty() : Bindings.createBooleanBinding(() -> false))
                        .or(lastNameField != null ? lastNameField.textProperty().isEmpty() : Bindings.createBooleanBinding(() -> false))
                        .or(registerUsernameField.textProperty().isEmpty()
                            .or(Bindings.createBooleanBinding(() ->
                                !inputValidator.isValidUsername(registerUsernameField.getText()),
                                registerUsernameField.textProperty())))
                        .or(emailField.textProperty().isEmpty()
                            .or(Bindings.createBooleanBinding(() ->
                                !inputValidator.isValidEmail(emailField.getText()),
                                emailField.textProperty())))
                        .or(passwordField.textProperty().isEmpty()
                            .or(Bindings.createBooleanBinding(
                                () -> !passwordField.getText().equals(confirmPasswordField.getText()),
                                passwordField.textProperty(),
                                confirmPasswordField.textProperty())))
                );

                logger.debug("Register form components initialized");
            } else {
                logger.warn("Missing required register form components");
                if (registerButton == null) logger.warn("registerButton is null");
                if (registerUsernameField == null) logger.warn("registerUsernameField is null");
                if (emailField == null) logger.warn("emailField is null");
                if (registerPasswordField == null) logger.warn("registerPasswordField is null");
                if (confirmPasswordField == null) logger.warn("confirmPasswordField is null");
            }

            // Set up navigation between login and register forms
            if (registerLink != null) {
                registerLink.setOnAction(this::showRegisterForm);
                logger.debug("Register link initialized");
            } else {
                logger.warn("Register link is null");
            }

            if (loginLink != null) {
                loginLink.setOnAction(this::showLoginForm);
                logger.debug("Login link initialized");
            } else {
                logger.warn("Login link is null");
            }

            logger.debug("Combined view initialization complete");

        } catch (Exception e) {
            String errorMsg = "Error initializing combined view: " + e.getMessage();
            logger.error(errorMsg, e);
            throw new RuntimeException(errorMsg, e);
        }
    }

    /**
     * Clears all fields in the registration form.
     */
    private void clearRegistrationForm() {
        if (firstNameField != null) firstNameField.clear();
        if (lastNameField != null) lastNameField.clear();
        if (registerUsernameField != null) registerUsernameField.clear();
        if (emailField != null) emailField.clear();
        if (passwordField != null) passwordField.clear();
        if (confirmPasswordField != null) confirmPasswordField.clear();
        if (passwordVisibleField != null) passwordVisibleField.clear();
        if (confirmPasswordVisibleField != null) confirmPasswordVisibleField.clear();
        if (genderToggleGroup != null) genderToggleGroup.selectToggle(null);
        if (registerErrorLabel != null) registerErrorLabel.setText("");
    }

    /**
     * Shows the login form and hides the registration form.
     *
     * @param event The action event that triggered this method (can be null)
     */
    @FXML
    private void showLoginForm(ActionEvent event) {

        try {
            if (loginContainer != null) {
                loginContainer.setVisible(true);
                if (loginErrorLabel != null) {
                    loginErrorLabel.setText("");
                }
            }
            if (registerContainer != null) {
                registerContainer.setVisible(false);
            }
        } catch (Exception e) {
            logger.error("Error showing login form: {}", e.getMessage(), e);
            showError("UI Error", "Failed to show login form: " + e.getMessage());
        }
    }

    /**
     * Shows the registration form and hides the login form.
     *
     * @param event The action event that triggered this method (can be null)
     */
    @FXML
    private void showRegisterForm(ActionEvent event) {
        try {
            if (registerContainer != null) {
                registerContainer.setVisible(true);
                if (registerErrorLabel != null) {
                    registerErrorLabel.setText("");
                }
            }
            if (loginContainer != null) {
                loginContainer.setVisible(false);
            }
        } catch (Exception e) {
            logger.error("Error showing registration form: {}", e.getMessage(), e);
            showError("UI Error", "Failed to show registration form: " + e.getMessage());
        }
    }

    /**
     * Gets the current session token.
     *
     * @return The current session token, or null if not authenticated
     */
    public String getSessionToken() {
        return sessionToken;
    }

    /**
     * Sets the primary stage for this controller.
     *
     * @param stage The primary stage
     */
    public void setStage(Stage stage) {
        this.stage = stage;
    }


    // UI Components
    public Hyperlink registerLink;
    public Hyperlink loginLink;
    public Label passwordStrengthLabel;


    // UI Components - Login/Register
    @FXML
    private TextField loginUsernameField; // For login form
    @FXML
    private TextField emailField;  // This one is correct
    @FXML
    private PasswordField loginPasswordField;  // For login form
    @FXML
    private PasswordField registerPasswordField;  // For registration form
 // For registration form
    @FXML
    private PasswordField confirmPasswordField;  // This one is correct
    @FXML
    private Label errorLabel;

    /**
     * Navigates to the login view
     */
    @FXML
    public void navigateToLogin() {
        try {
            // Load the login view
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/auth/login-view.fxml"));
            Parent root = loader.load();

            // Get the current stage from any node in the current scene
            Stage stage = (Stage) errorLabel.getScene().getWindow();

            // Set the new scene
            Scene scene = new Scene(root);
            stage.setScene(scene);
            stage.show();
        } catch (IOException e) {
            logger.error("Error navigating to login view: {}", e.getMessage(), e);
            showError("Navigation Error", "Failed to load login view. Please try again.");
        }
    }



    //successMessage and session token
    private String successMessage;


    /**
     * Initializes the controller with the current user ID.
     *
     * @param currentUserId The ID of the currently logged-in user, or -1 if no user is logged in
     */
    @Override
    protected void initializeController(int currentUserId) {
        try {
            //setup login and registration forms
            setupLoginForm(loginPasswordVisibleField);
            setupRegistrationForm(passwordVisibleField, confirmPasswordVisibleField);
        } catch (Exception e) {
            logger.error("Error initializing AuthController: {}", e.getMessage(), e);
            UIUtils.showError("Initialization Error", "Failed to initialize authentication system.");
        }
    }

    /**
     * Sets up the login form with necessary bindings and listeners.
     *
     * @param loginPasswordVisibleField The text field for showing/hiding the password
     */
    private void setupLoginForm(TextField loginPasswordVisibleField) {
        try {
            logger.debug("Setting up login form");

            if (loginPasswordVisibleField == null) {
                logger.warn("loginPasswordVisibleField is null");
                return;
            }

            this.loginPasswordVisibleField = loginPasswordVisibleField;

            // Only set up bindings if we have all required components
            if (loginButton != null && loginUsernameField != null && loginPasswordField != null) {
                // Unbind any existing bindings to prevent memory leaks
                loginButton.disableProperty().unbind();

                // Set up new bindings
                loginButton.disableProperty().bind(
                    loginUsernameField.textProperty().isEmpty()
                        .or(loginPasswordField.textProperty().isEmpty())
                );

                // Set up password visibility toggle if we have the visible field
                if (loginPasswordVisibleField != null) {
                    loginPasswordVisibleField.visibleProperty().bind(loginPasswordField.visibleProperty().not());
                    loginPasswordField.visibleProperty().bind(loginPasswordVisibleField.visibleProperty().not());
                    loginPasswordVisibleField.textProperty().bindBidirectional(loginPasswordField.textProperty());
                }

                logger.debug("Login form setup complete");
            } else {
                logger.warn("Cannot set up login form - missing required components");
                if (loginButton == null) logger.warn("loginButton is null");
                if (loginUsernameField == null) logger.warn("loginUsernameField is null");
                if (loginPasswordField == null) logger.warn("loginPasswordField is null");
            }
        } catch (Exception e) {
            logger.error("Error in setupLoginForm: {}", e.getMessage(), e);
            throw e;
        }
    }

    /**
     * Sets up the registration form with validation and bindings.
     *
     * @param passwordVisibleField        The text field for showing/hiding the password
     * @param confirmPasswordVisibleField The text field for showing/hiding the confirm password
     */
    private void setupRegistrationForm(TextField passwordVisibleField, TextField confirmPasswordVisibleField) {
        try {
            logger.debug("Setting up registration form");

            if (passwordVisibleField == null || confirmPasswordVisibleField == null) {
                logger.warn("Password visibility fields are not properly initialized");
                return;
            }

            this.passwordVisibleField = passwordVisibleField;
            this.confirmPasswordVisibleField = confirmPasswordVisibleField;

            // Check if all required fields are available
            if (registerButton == null || firstNameField == null || lastNameField == null ||
                    registerUsernameField == null || emailField == null ||
                    passwordField == null || confirmPasswordField == null) {

                logger.warn("Cannot set up registration form - missing required components");
                if (registerButton == null) logger.warn("registerButton is null");
                if (firstNameField == null) logger.warn("firstNameField is null");
                if (lastNameField == null) logger.warn("lastNameField is null");
                if (registerUsernameField == null) logger.warn("registerUsernameField is null");
                if (emailField == null) logger.warn("emailField is null");
                if (passwordField == null) logger.warn("passwordField is null");
                if (confirmPasswordField == null) logger.warn("confirmPasswordField is null");
                return;
            }

            // Unbind any existing bindings to prevent memory leaks
            registerButton.disableProperty().unbind();

            // Set up password visibility toggle
            passwordVisibleField.visibleProperty().bind(passwordField.visibleProperty().not());
            passwordField.visibleProperty().bind(passwordVisibleField.visibleProperty().not());
            passwordVisibleField.textProperty().bindBidirectional(passwordField.textProperty());

            confirmPasswordVisibleField.visibleProperty().bind(confirmPasswordField.visibleProperty().not());
            confirmPasswordField.visibleProperty().bind(confirmPasswordVisibleField.visibleProperty().not());
            confirmPasswordVisibleField.textProperty().bindBidirectional(confirmPasswordField.textProperty());

            // Set up form validation
            registerButton.disableProperty().bind(
                    firstNameField.textProperty().isEmpty()
                            .or(lastNameField.textProperty().isEmpty())
                            .or(registerUsernameField.textProperty().isEmpty()
                                    .or(Bindings.createBooleanBinding(() ->
                                                    !inputValidator.isValidUsername(registerUsernameField.getText()),
                                            registerUsernameField.textProperty())))
                            .or(emailField.textProperty().isEmpty()
                                    .or(Bindings.createBooleanBinding(() ->
                                                    !inputValidator.isValidEmail(emailField.getText()),
                                            emailField.textProperty())))
                            .or(registerPasswordField.textProperty().isEmpty()
                                    .or(Bindings.createBooleanBinding(
                                            () -> !registerPasswordField.getText().equals(confirmPasswordField.getText()),
                                            registerPasswordField.textProperty(),
                                            confirmPasswordField.textProperty())))
            );

            // Handle Enter key press in password fields
            registerPasswordField.setOnKeyPressed(this::handleRegisterKeyPress);
            confirmPasswordField.setOnKeyPressed(this::handleRegisterKeyPress);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }



    /**
     * Handles key press events in the registration form.
     * Submits the form when Enter key is pressed.
     *
     * @param keyEvent The key event that was triggered
     */
    private void handleRegisterKeyPress(KeyEvent keyEvent) {
        try {
            if (keyEvent != null && keyEvent.getCode() == KeyCode.ENTER) {
                logger.debug("Enter key pressed in registration form");
                registerButton.fire();
            }
        } catch (Exception e) {
            logger.error("Error handling key press in registration form: {}", e.getMessage(), e);
            // Don't propagate key event errors to avoid disrupting user experience
        }
    }

    /**
     * Handles validation errors by displaying them in the specified label.
     *
     * @param e          The validation exception containing error details
     * @param errorLabel The label to display the error message in
     */
    private void handleValidationError(ValidationException e, Label errorLabel) {
        if (errorLabel == null) {
            logger.error("Error label is null, cannot display validation error");
            return;
        }

        try {
            StringBuilder errorMessage = new StringBuilder();

            // Handle field errors if they exist
            if (e.hasFieldErrors()) {
                e.getFieldErrors().forEach((field, errors) ->
                        errors.forEach(error -> {
                            if (error != null) {
                                errorMessage.append("• ").append(error).append("\n");
                            }
                        })
                );
            } else if (e.getMessage() != null) {
                errorMessage.append(e.getMessage());
            } else {
                errorMessage.append("A validation error occurred.");
            }

            final String finalMessage = errorMessage.toString().trim();

            // Update UI on the JavaFX Application Thread
            Platform.runLater(() -> {
                errorLabel.setText(finalMessage);
                errorLabel.setStyle("-fx-text-fill: #d32f2f; -fx-wrap-text: true; -fx-font-weight: bold;");
                errorLabel.setVisible(true);
                errorLabel.setManaged(true);
            });

            logger.warn("Validation error: {}", finalMessage);
        } catch (Exception ex) {
            logger.error("Error handling validation error: {}", ex.getMessage(), ex);
            // Fallback to simple error display
            Platform.runLater(() -> {
                errorLabel.setText("An error occurred during validation. Please try again.");
                errorLabel.setStyle("-fx-text-fill: #d32f2f; -fx-wrap-text: true;");
                errorLabel.setVisible(true);
                errorLabel.setManaged(true);
            });
        }
    }

    /**
     * Handles authentication errors and updates the UI accordingly.
     *
     * @param e          The authentication exception that occurred
     * @param errorLabel The label to display the error message in
     */
    private void handleAuthError(AuthException e, Label errorLabel) {
        if (errorLabel == null) {
            logger.error("Error label is null, cannot display authentication error");
            return;
        }

        try {
            String errorMessage = e.getMessage();

            // Handle error message
            if (errorMessage == null || errorMessage.trim().isEmpty()) {
                errorMessage = e.getErrorType() != null
                        ? e.getErrorType().getDefaultMessage()
                        : "An authentication error occurred. Please try again.";
            }

            final String finalMessage = errorMessage;

            // Update UI on the JavaFX Application Thread
            Platform.runLater(() -> {
                errorLabel.setText(finalMessage);
                errorLabel.setStyle(
                        "-fx-text-fill: #d32f2f; " +
                                "-fx-font-weight: bold; " +
                                "-fx-wrap-text: true; " +
                                "-fx-padding: 5px;"
                );
                errorLabel.setVisible(true);
                errorLabel.setManaged(true);
            });

            // Log the error with appropriate level
            if (e.getErrorType() == com.papel.imdb_clone.exceptions.AuthErrorType.INVALID_CREDENTIALS) {
                logger.warn("Authentication failed: {}", finalMessage);
            } else {
                logger.error("Authentication error: {}", finalMessage, e);
            }
        } catch (Exception ex) {
            logger.error("Error handling authentication error: {}", ex.getMessage(), ex);
            // Fallback to simple error display
            Platform.runLater(() -> {
                errorLabel.setText("An error occurred during authentication. Please try again.");
                errorLabel.setStyle("-fx-text-fill: #d32f2f; -fx-wrap-text: true;");
                errorLabel.setVisible(true);
                errorLabel.setManaged(true);
            });
        }
    }

    /**
     * Handles unexpected errors by showing an error dialog and logging the details.
     * This method ensures all UI updates happen on the JavaFX Application Thread.
     *
     * @param e       The exception that was thrown
     * @param context Additional context about where the error occurred (e.g., "login", "registration")
     */
    private void handleUnexpectedError(Exception e, String context) {
        if (e == null) {
            e = new Exception("Unknown error occurred");
        }

        String safeContext = (context != null && !context.trim().isEmpty())
                ? context.trim()
                : "operation";

        String errorMessage = String.format(
                "An unexpected error occurred during %s. Please try again.\n\nError: %s",
                safeContext,
                e.getMessage() != null ? e.getMessage() : "Unknown error"
        );

        // Log the full error with stack trace
        logger.error("Unexpected error during {}: {}", safeContext, e.getMessage(), e);

        // Show error dialog on the JavaFX Application Thread
        Platform.runLater(() -> {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Error");
            alert.setHeaderText("An error occurred");
            alert.setContentText(errorMessage);
            alert.showAndWait();
        });
    }

    /**
     * Sets up the form bindings for the registration form
     */
    private void setupFormBindings() {
        if (registerButton != null && usernameField != null && emailField != null &&
                passwordField != null && confirmPasswordField != null) {

            // Disable register button when form is invalid
            registerButton.disableProperty().bind(
                    usernameField.textProperty().isEmpty()
                            .or(emailField.textProperty().isEmpty())
                            .or(passwordField.textProperty().isEmpty())
                            .or(confirmPasswordField.textProperty().isEmpty())
                            .or(Bindings.createBooleanBinding(
                                    () -> !passwordField.getText().equals(confirmPasswordField.getText()),
                                    passwordField.textProperty(),
                                    confirmPasswordField.textProperty()
                            ))
            );

            // Handle Enter key press in password fields
            passwordField.setOnKeyPressed(this::handleRegisterKeyPress);
            confirmPasswordField.setOnKeyPressed(this::handleRegisterKeyPress);
        }
    }

    /**
     * Handles the registration process.
     */
    @FXML
    public void navigateToRegister(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/auth/register-view.fxml"));
            Parent root = loader.load();

            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            Scene scene = new Scene(root);
            stage.setScene(scene);
            stage.show();
        } catch (Exception e) {
            logger.error("Error navigating to register: {}", e.getMessage(), e);
            showError("Navigation Error", "Failed to load registration view. Please try again.");
        }
    }

    @FXML
    private void handleRegister(ActionEvent event) {
        try {
            if (registerUsernameField == null || emailField == null || passwordField == null || confirmPasswordField == null) {
                logger.error("UI components not properly initialized");
                return;
            }

            String username = registerUsernameField.getText().trim();
            String email = emailField.getText().trim();
            String password = passwordField.getText();
            String confirmPassword = confirmPasswordField.getText();

            // Basic validation
            if (username.isEmpty() || email.isEmpty() || password.isEmpty() || confirmPassword.isEmpty()) {
                showError("Validation Error", "All fields are required");
                return;
            }

            if (!password.equals(confirmPassword)) {
                showError("Validation Error", "Passwords do not match");
                return;
            }

            // Create user object
            User newUser = new User();
            newUser.setUsername(username);
            newUser.setEmail(email);
            // Set other required fields as needed

            // Register user
            User registeredUser = authService.register(newUser, password, confirmPassword);

            if (registeredUser != null) {
                navigateToMainView(registeredUser);
            }
        } catch (Exception e) {
            logger.error("Registration error: {}", e.getMessage(), e);
            showError("Registration Error", "An error occurred during registration. Please try again.");
        }
    }

    @FXML
    public void navigateToLogin(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/auth/login-view.fxml"));
            Parent root = loader.load();

            // Get the current stage
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            if (stage == null) {
                stage = new Stage();
                stage.setTitle("Login");
            }

            Scene scene = new Scene(root);
            stage.setScene(scene);
            stage.show();
        } catch (Exception e) {
            logger.error("Error navigating to login: {}", e.getMessage(), e);
            showError("Navigation Error", "Failed to load login view. Please try again.");
        }
    }


    public void showError(String title, String message) {
        Platform.runLater(() -> {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle(title);
            alert.setHeaderText(title);
            alert.setContentText(message);
            alert.showAndWait();
        });
    }

    @FXML
    private void handleLogin(ActionEvent event) {
        try {
            if (usernameField == null || passwordField == null) {
                logger.error("Login form not properly initialized");
                showError("Login Error", "Login form is not properly initialized. Please try again.");
                return;
            }

            String usernameOrEmail = usernameField.getText().trim();
            String password = passwordField.getText();

            if (usernameOrEmail.isEmpty() || password.isEmpty()) {
                if (loginErrorLabel != null) {
                    loginErrorLabel.setText("Please enter both username/email and password");
                    loginErrorLabel.setVisible(true);
                } else {
                    showError("Login Error", "Please enter both username/email and password");
                }
                return;
            }

            // Clear any previous errors
            if (loginErrorLabel != null) {
                loginErrorLabel.setText("");
                loginErrorLabel.setVisible(false);
            }

            // Login the user
            String sessionToken = authService.login(usernameOrEmail, password);
            if (sessionToken != null) {
                // Get the user from the session token
                User user = authService.getUserFromSession(sessionToken);
                if (user != null) {
                    navigateToMainView(user);
                } else {
                    String errorMsg = "Failed to retrieve user information";
                    logger.error(errorMsg);
                    if (loginErrorLabel != null) {
                        loginErrorLabel.setText(errorMsg);
                        loginErrorLabel.setVisible(true);
                    } else {
                        showError("Login Failed", errorMsg);
                    }
                }
            } else {
                String errorMsg = "Invalid username/email or password";
                if (loginErrorLabel != null) {
                    loginErrorLabel.setText(errorMsg);
                    loginErrorLabel.setVisible(true);
                } else {
                    showError("Login Failed", errorMsg);
                }
            }
        } catch (Exception e) {
            String errorMsg = "An error occurred during login. Please try again.";
            logger.error("Login error: {}", e.getMessage(), e);
            if (loginErrorLabel != null) {
                loginErrorLabel.setText(errorMsg);
                loginErrorLabel.setVisible(true);
            } else {
                showError("Login Error", errorMsg);
            }
        }
    }

    private void navigateToMainView(User user) {
        try {
            // Get the current session token
            String sessionToken = authService.getCurrentSessionToken();
            logger.info("Navigating to main view with user: {} and session token: {}", 
                user != null ? user.getUsername() : "null", 
                sessionToken != null ? "[HIDDEN]" : "null");
            
            // Load the main view
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/base/home-view.fxml"));
            Parent root = loader.load();

            // Get the MainController and set the user and session token
            MainController mainController = loader.getController();
            
            // Set the session token and user in a thread-safe way
            Platform.runLater(() -> {
                try {
                    // Set the session token first
                    if (sessionToken != null) {
                        mainController.setSessionToken(sessionToken);
                        logger.debug("Session token set in MainController");
                    }
                    
                    // Set the user and update UI states
                    mainController.setUser(user);
                    mainController.setGuest(false);
                    
                    // Update the UI on the JavaFX Application Thread
                    Platform.runLater(() -> {
                        mainController.updateUIForLoggedInUser(user);
                        mainController.updateUIForAuthState(true);
                        mainController.updateUserInterface();
                        logger.debug("UI updated for logged-in user: {}", user != null ? user.getUsername() : "null");
                    });
                    
                } catch (Exception e) {
                    logger.error("Error updating UI after login: {}", e.getMessage(), e);
                    showError("Navigation Error", "Failed to update UI after login: " + e.getMessage());
                }
            });

            // Get the current stage
            Stage stage = (Stage) (loginButton != null ? loginButton.getScene().getWindow() :
                    registerButton != null ? registerButton.getScene().getWindow() : 
                    usernameField != null ? usernameField.getScene().getWindow() : null);

            if (stage != null) {
                // Set the new scene
                Scene scene = new Scene(root);
                stage.setScene(scene);
                stage.setTitle("IMDb Clone - Home");
                stage.show();
                logger.info("Successfully navigated to main view");
                
                // Force a refresh of the UI
                Platform.runLater(() -> {
                    mainController.updateUIForAuthState(true);
                });
            } else {
                throw new IllegalStateException("Could not determine current stage");
            }
        } catch (Exception e) {
            logger.error("Error navigating to main view: {}", e.getMessage(), e);
            showError("Navigation Error", "Failed to load main application view. Please try again.");
        }
    }

    @FXML
    public void continueAsGuest(ActionEvent actionEvent) {
        try {
            // Load the main view as guest (user ID -1)
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/base/home-view.fxml"));
            Parent root = loader.load();

            MainController mainController = loader.getController();
            mainController.setStage(stage);
            // Remove or update this line based on your MainController's actual method signature
            // mainController.initializeController(-1); // -1 indicates guest user

            // Get the current stage
            Stage stage = (Stage) ((Node) actionEvent.getSource()).getScene().getWindow();

            // Set the scene
            Scene scene = new Scene(root);
            stage.setScene(scene);
            stage.show();
        } catch (Exception e) {
            logger.error("Error continuing as guest: {}", e.getMessage(), e);
            showError("Guest Access Error", "Failed to load application as guest. Please try again.");
        }
    }
}
