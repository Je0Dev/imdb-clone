package com.papel.imdb_clone.controllers;

import com.papel.imdb_clone.exceptions.AuthException;
import com.papel.imdb_clone.exceptions.ValidationException;
import com.papel.imdb_clone.model.User;
import com.papel.imdb_clone.service.AuthService;
import com.papel.imdb_clone.service.NavigationService;
import com.papel.imdb_clone.service.validation.UserInputValidator;
import com.papel.imdb_clone.util.UIUtils;
import javafx.beans.binding.Bindings;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URL;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;

public class AuthController extends BaseController {
    // Logger,which is used to log messages for debugging and error tracking
    private static final Logger logger = LoggerFactory.getLogger(AuthController.class);

    // UI Components - Registration Login which is handled by one fxml view auth.fxml
    public Hyperlink registerLink;
    public StackPane registerContainer;
    public Hyperlink loginLink;


    // Services
    private final AuthService authService = AuthService.getInstance();
    private final NavigationService navigationService = NavigationService.getInstance();
    // Validator,which is used to validate user input
    private final UserInputValidator inputValidator = new UserInputValidator();


    // UI Components - Login
    @FXML
    private TextField loginUsernameField;
    @FXML
    private PasswordField loginPasswordField;
    @FXML
    private Button loginButton;
    @FXML
    private Button toggleLoginPassword;

    // Error label,which is used to display error messages
    @FXML
    private Label loginErrorLabel;

    // UI Components - Registration
    @FXML
    private TextField firstNameField;
    @FXML
    private TextField lastNameField;
    @FXML
    private TextField registerUsernameField;
    @FXML
    private TextField emailField;
    @FXML
    private PasswordField passwordField;

    @FXML
    private PasswordField confirmPasswordField;

    // Gender toggle group,which is used to select gender
    @FXML
    private ToggleGroup genderToggleGroup;
    @FXML
    private Button registerButton;
    @FXML
    private Button toggleRegisterPassword;
    @FXML
    private Button toggleConfirmPassword;

    // Error label,which is used to display error messages
    @FXML
    private Label registerErrorLabel;

    // Container panes
    @FXML
    private StackPane loginContainer;

    // Password visibility toggle fields
    @FXML
    public TextField loginPasswordVisibleField;
    @FXML
    private TextField passwordVisibleField;
    @FXML
    private TextField confirmPasswordVisibleField;

    // Stage,which is used to display the authentication window
    private Stage stage;
    private String successMessage;
    // Current user,which is used to store the current user and session token which is used to authenticate the user
    private Object currentUser;
    private String sessionToken;


    // Initialize the controller which is called when the controller is loaded

    /**Initialize AuthController
     *
     * @param currentUserId for current user id
     */
    @Override
    protected void initializeController(int currentUserId) {
        try {
            // Initialize UI bindings and listeners
            setupLoginForm(loginPasswordVisibleField);
            setupRegistrationForm(passwordVisibleField, confirmPasswordVisibleField);

        } catch (Exception e) {
            // Log error and show error message
            logger.error("Error initializing AuthController: {}", e.getMessage(), e);
            UIUtils.showError("Initialization Error", "Failed to initialize authentication system.");
        }
    }

    private void setupLoginForm(TextField loginPasswordVisibleField) {
        this.loginPasswordVisibleField = loginPasswordVisibleField;
        // Bind(means connect state with property) login button state, which is disabled when the username or password is empty
        loginButton.disableProperty().bind(
                loginUsernameField.textProperty().isEmpty()
                        .or(loginPasswordField.textProperty().isEmpty())
        );

    }

    private void setupRegistrationForm(TextField passwordVisibleField, TextField confirmPasswordVisibleField) {
        // Bind register button state with validation
        //means that the button is disabled when the first name, last name, username, email, password, or confirm password is empty or invalid
        registerButton.disableProperty().bind(
                //or means that the button is disabled when the first name is empty or the last name is empty
                firstNameField.textProperty().isEmpty()
                        .or(lastNameField.textProperty().isEmpty())
                        .or(registerUsernameField.textProperty().isEmpty()
                                //or means that the button is disabled when the username is empty or invalid
                                .or(Bindings.createBooleanBinding(() ->
                                                inputValidator.isValidUsername(registerUsernameField.getText()),
                                        registerUsernameField.textProperty())))
                        //or means that the button is disabled when the email is empty or invalid
                        .or(emailField.textProperty().isEmpty()
                                .or(Bindings.createBooleanBinding(() ->
                                                inputValidator.isValidEmail(emailField.getText()),
                                        emailField.textProperty())))
                        .or(passwordField.textProperty().isEmpty()
                                //or means that the button is disabled when the password is empty or the confirm password is empty or the password is not equal to the confirm password
                                .or(Bindings.createBooleanBinding(() ->
                                                !passwordField.getText().equals(confirmPasswordField.getText()),
                                        passwordField.textProperty(),
                                        confirmPasswordField.textProperty())))
        );
        // Handle Enter key press in password field for registration and login
        passwordField.setOnKeyPressed(this::handleRegisterKeyPress);
        confirmPasswordField.setOnKeyPressed(this::handleRegisterKeyPress);


    }

    // Handle Enter key press in password field for registration
    private void handleRegisterKeyPress(KeyEvent keyEvent) {
        if (keyEvent.getCode() == KeyCode.ENTER) {
            registerButton.fire();
        }
    }
    // Handle validation error which is used to display the error message
    private void handleValidationError(ValidationException e, Label errorLabel) {
        StringBuilder errorMessage = new StringBuilder();

        if (e.hasFieldErrors()) {
            // Collect all field errors
            Map<String, List<String>> fieldErrors = e.getFieldErrors();
            // Iterate through all field errors and append them to the error message
            fieldErrors.forEach((field, errors) ->
                    errors.forEach(error ->
                            errorMessage.append("• ").append(error).append("\n")
                    )
            );
        } else {
            errorMessage.append(e.getMessage());
        }

        // Set error message and style
        errorLabel.setText(errorMessage.toString());
        errorLabel.setStyle("-fx-text-fill: #d32f2f; -fx-wrap-text: true;");
        errorLabel.setVisible(true);
        errorLabel.setManaged(true);
        logger.warn("Validation error: {}", errorMessage);
    }

    /**
     * Handles authentication errors and updates the UI accordingly
     * @param e The AuthException that occurred
     * @param errorLabel The label to display the error message
     */
    private void handleAuthError(AuthException e, Label errorLabel) {
        String errorMessage = e.getMessage();

        // Use default message if specific message isn't available
        if (errorMessage == null || errorMessage.isEmpty()) {
            errorMessage = e.getErrorType() != null ?
                    e.getErrorType().getDefaultMessage() :
                    "An authentication error occurred";
        }

        errorLabel.setText(errorMessage);
        errorLabel.setStyle("-fx-text-fill: #d32f2f; -fx-font-weight: bold; -fx-wrap-text: true;");
        errorLabel.setVisible(true);
        errorLabel.setManaged(true);

        // Log the error
        logger.warn("Authentication error: {}", errorMessage);
    }

    // Handle unexpected error which is used to display the error message
    private void handleUnexpectedError(String operation, Exception e) {
        String errorMessage = String.format("An unexpected error occurred during %s. Please try again.", operation);
        UIUtils.showError("Error", errorMessage);
        logger.error("Unexpected error during {}: {}", operation, e.getMessage(), e);
    }

    // Set stage which is used to display the authentication window
    public void setStage(Stage stage) {
        this.stage = stage;
        // Set success message if there is one
        if (successMessage != null && !successMessage.isEmpty()) {
            loginErrorLabel.setStyle("-fx-text-fill: #2e7d32;");
            loginErrorLabel.setText(successMessage);
            loginErrorLabel.setVisible(true);
            successMessage = null;
        }
    }

    @FXML
    private void handleLogin() {
        try {
            // Clear previous errors
            loginErrorLabel.setText("");

            String username = loginUsernameField.getText().trim();
            String password = loginPasswordField.getText();

            // Input validation
            if (username.isEmpty() || password.isEmpty()) {
                throw new ValidationException("Username and password are required", "VALIDATION_ERROR", null, null);
            }

            logger.info("Attempting login for user: {}", username);

            // Authenticate user
            String token = authService.login(username, password);

            if (token != null) {
                // Store session token and get current user
                this.sessionToken = token;
                this.currentUser = authService.getCurrentUser(token);

                // Log successful login
                logger.info("User logged in successfully: {}", username);

                // Clear sensitive data
                loginUsernameField.clear();
                loginPasswordField.clear();

                // Navigate to main application
                navigationService.navigateTo("/fxml/home-view.fxml", (Stage) loginButton.getScene().getWindow(), "IMDb Clone");
            }

        } catch (AuthException e) {
            logger.warn("Login failed: {}", e.getMessage());
            handleAuthError(e, loginErrorLabel);
        } catch (ValidationException e) {
            handleValidationError(e, loginErrorLabel);
        } catch (Exception e) {
            logger.error("Unexpected error during login: {}", e.getMessage(), e);
            handleUnexpectedError("login", e);
        }
    }


    @FXML
    private void handleRegister() {
        try {
            // Clear previous errors
            registerErrorLabel.setText("");

            // Get and validate input
            String firstName = firstNameField.getText().trim();
            String lastName = lastNameField.getText().trim();
            String username = registerUsernameField.getText().trim();
            String email = emailField.getText().trim();
            String password = passwordField.getText();
            String confirmPassword = confirmPasswordField.getText();

            logger.info("Starting registration process for user: {}", username);

            // Basic input validation
            if (firstName.isEmpty() || lastName.isEmpty() || username.isEmpty() ||
                    email.isEmpty() || password.isEmpty() || confirmPassword.isEmpty()) {
                throw new ValidationException("All fields are required", "VALIDATION_ERROR", null, null);
            }

            // Email format validation
            if (!email.matches("^[A-Za-z0-9+_.-]+@(.+)$")) {
                throw new ValidationException("Please enter a valid email address", "VALIDATION_ERROR", null, null);
            }

            // Password confirmation check
            if (!password.equals(confirmPassword)) {
                throw new ValidationException("Passwords do not match", "VALIDATION_ERROR", null, null);
            }

            // Create and register new user
            logger.debug("Creating new user object for: {}", username);
            User newUser = new User(firstName, lastName, username, 'M', email);
            
            // Register the user
            authService.register(newUser, password, confirmPassword);
            
            // Show success message and switch to login form
            loginErrorLabel.setStyle("-fx-text-fill: #2e7d32;");
            loginErrorLabel.setText("Registration successful! Please log in.");
            loginErrorLabel.setVisible(true);

            // Clear form and switch to login
            clearRegistrationForm();
            showLoginForm(null);

        } catch (AuthException e) {
            logger.warn("Registration failed: {}", e.getMessage());
            handleAuthError(e, registerErrorLabel);
        } catch (ValidationException e) {
            handleValidationError(e, registerErrorLabel);
        } catch (Exception e) {
            // Handle unexpected error with style
            registerErrorLabel.setStyle("-fx-text-fill: #d32f2f;");
            registerErrorLabel.setText("Registration failed: " + e.getMessage());
            registerErrorLabel.setVisible(true);
            logger.error("Unexpected registration error: {}", e.getMessage(), e);
        }
    }

    /**Show login error
     *
     * @param message
     */
    private void showLoginError(String message) {
        loginErrorLabel.setStyle("-fx-text-fill: #d32f2f;");
        loginErrorLabel.setText(message);
        loginErrorLabel.setVisible(true);
    }


    /**Clear registration form
     */
    private void clearRegistrationForm() {

        // Clear text fields if they're not null
        if (firstNameField != null) firstNameField.clear();
        if (lastNameField != null) lastNameField.clear();
        if (registerUsernameField != null) registerUsernameField.clear();
        if (emailField != null) emailField.clear();
        // Clear password fields if they're not null
        if (passwordField != null) passwordField.clear();
        if (passwordVisibleField != null) passwordVisibleField.clear();
        if (confirmPasswordField != null) confirmPasswordField.clear();
        if (confirmPasswordVisibleField != null) confirmPasswordVisibleField.clear();

        // Clear gender toggle group if it's not null
        if (genderToggleGroup != null && genderToggleGroup.getSelectedToggle() != null) {
            genderToggleGroup.getSelectedToggle().setSelected(false);
        }
    }



    /**Show register form
     *
     * @param event for action event
     */
    @FXML
    public void showRegisterForm(ActionEvent event) {
        // Hide login form and show register form
        loginContainer.setVisible(false);
        registerContainer.setVisible(true);
        // Consume event if it's not null which means it's a mouse click that triggered this method
        if (event != null) {
            event.consume();
        }
    }

    /**Initialize AuthController
     *
     * @param url for corresponding FXML file
     * @param resourceBundle that contains the FXML file that this controller is associated with f
     */
    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        logger.debug("Initializing AuthController");
        loginContainer.setVisible(true);
    }

    /**Show login form
     *
     * @param actionEvent that triggered this method
     */
    public void showLoginForm(ActionEvent actionEvent) {
        // Show login form and hide register form
        loginContainer.setVisible(true);
        registerContainer.setVisible(false);
        if (actionEvent != null) {
            actionEvent.consume();
        }
        // Clear registration form
        clearRegistrationForm();
        // Clear error labels for login and register forms
        loginErrorLabel.setVisible(false);
        registerErrorLabel.setVisible(false);
        loginUsernameField.requestFocus();
        loginPasswordField.requestFocus();
        loginErrorLabel.setText("");
        registerErrorLabel.setText("");
        // Set error label styles and make them invisible
        loginErrorLabel.setStyle("-fx-text-fill: #d32f2f;");
        registerErrorLabel.setStyle("-fx-text-fill: #d32f2f;");
        loginErrorLabel.setVisible(false);
        registerErrorLabel.setVisible(false);
    }
}
