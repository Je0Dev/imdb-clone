package com.papel.imdb_clone.controllers.authentication;

import com.papel.imdb_clone.controllers.BaseController;
import com.papel.imdb_clone.exceptions.AuthException;
import com.papel.imdb_clone.exceptions.ValidationException;
import com.papel.imdb_clone.model.people.User;
import com.papel.imdb_clone.service.validation.AuthService;
import com.papel.imdb_clone.service.navigation.NavigationService;
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
import java.util.Map;
import java.util.ResourceBundle;

/**
 * Controller for handling user authentication including login and registration.
 * Manages the authentication UI and coordinates with AuthService for user operations.
 */

public class AuthController extends BaseController {

    //logger
    private static final Logger logger = LoggerFactory.getLogger(AuthController.class);
    //transient means that the variable is not serialized which means it is not saved to the database
    private final transient UserInputValidator inputValidator;
    private final transient AuthService authService;
    private final transient NavigationService navigationService;
    private transient String sessionToken;
    private final transient Map<String, Object> data;

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
    }


    // UI Components
    public Hyperlink registerLink;
    public StackPane registerContainer;
    public Hyperlink loginLink;
    public Label passwordStrengthLabel;


    // UI Components - Login
    @FXML
    private TextField loginUsernameField;
    @FXML
    private PasswordField loginPasswordField;
    @FXML
    private Button loginButton;

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
        this.loginPasswordVisibleField = loginPasswordVisibleField;
        
        // Disable login button when username or password is empty
        loginButton.disableProperty().bind(
            loginUsernameField.textProperty().isEmpty()
                .or(loginPasswordField.textProperty().isEmpty())
        );
    }

    /**
     * Sets up the registration form with validation and bindings.
     * 
     * @param passwordVisibleField The text field for showing/hiding the password
     * @param confirmPasswordVisibleField The text field for showing/hiding the confirm password
     */
    private void setupRegistrationForm(TextField passwordVisibleField, TextField confirmPasswordVisibleField) {
        this.passwordVisibleField = passwordVisibleField;
        this.confirmPasswordVisibleField = confirmPasswordVisibleField;
        
        // Disable register button when form is invalid
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
                .or(passwordField.textProperty().isEmpty()
                    .or(Bindings.createBooleanBinding(
                        () -> !passwordField.getText().equals(confirmPasswordField.getText()),
                        passwordField.textProperty(),
                        confirmPasswordField.textProperty())))
        );
        
        // Handle Enter key press in password fields
        passwordField.setOnKeyPressed(this::handleRegisterKeyPress);
        confirmPasswordField.setOnKeyPressed(this::handleRegisterKeyPress);
    }

    /**
     * Handles key press events in the registration form.
     * Submits the form when Enter key is pressed.
     * 
     * @param keyEvent The key event that was triggered
     */
    private void handleRegisterKeyPress(KeyEvent keyEvent) {
        if (keyEvent.getCode() == KeyCode.ENTER) {
            registerButton.fire();
        }
    }
    /**
     * Handles validation errors by displaying them in the specified label.
     * 
     * @param e The validation exception containing error details
     * @param errorLabel The label to display the error message in
     */
    private void handleValidationError(ValidationException e, Label errorLabel) {
        StringBuilder errorMessage = new StringBuilder();

        if (e.hasFieldErrors()) {
            e.getFieldErrors().forEach((field, errors) ->
                errors.forEach(error ->
                    errorMessage.append("• ").append(error).append("\n")
                )
            );
        } else {
            errorMessage.append(e.getMessage());
        }

        errorLabel.setText(errorMessage.toString());
        errorLabel.setStyle("-fx-text-fill: #d32f2f; -fx-wrap-text: true;");
        errorLabel.setVisible(true);
        errorLabel.setManaged(true);
        logger.warn("Validation error: {}", errorMessage);
    }

    /**
     * Handles authentication errors and updates the UI accordingly.
     * 
     * @param e The authentication exception that occurred
     * @param errorLabel The label to display the error message in
     */
    private void handleAuthError(AuthException e, Label errorLabel) {
        String errorMessage = e.getMessage();

        if (errorMessage == null || errorMessage.isEmpty()) {
            errorMessage = e.getErrorType() != null
                ? e.getErrorType().getDefaultMessage()
                : "An authentication error occurred";
        }

        errorLabel.setText(errorMessage);
        errorLabel.setStyle("-fx-text-fill: #d32f2f; -fx-font-weight: bold; -fx-wrap-text: true;");
        errorLabel.setVisible(true);
        errorLabel.setManaged(true);
        logger.warn("Authentication error: {}", errorMessage);
    }

    /**
     * Handles unexpected errors by showing an error dialog and logging the details.
     *
     * @param e The exception that was thrown
     */
    private void handleUnexpectedError(Exception e) {
        String errorMessage = String.format("An unexpected error occurred during %s. Please try again.", "login");
        UIUtils.showError("Error", errorMessage);
        logger.error("Unexpected error during {}: {}", "login", e.getMessage(), e);
    }

    /**
     * Sets the stage for this controller and displays any success messages.
     * 
     * @param stage The JavaFX stage to set for this controller
     */
    public void setStage(Stage stage) {
        if (successMessage != null && !successMessage.isEmpty()) {
            loginErrorLabel.setStyle("-fx-text-fill: #2e7d32;");
            loginErrorLabel.setText(successMessage);
            loginErrorLabel.setVisible(true);
            successMessage = null;
        }
    }

    /**
     * Handles the login button action.
     * Validates input and attempts to authenticate the user.
     */
    @FXML
    private void handleLogin() {
        try {
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
                this.sessionToken = token;
                authService.getCurrentUser(token);
                logger.info("User logged in successfully: {}", username);

                // Clear sensitive data
                loginUsernameField.clear();
                loginPasswordField.clear();

                // Navigate to main application
                navigationService.navigateTo(
                    "/fxml/base/home-view.fxml", 
                    data, 
                    (Stage) loginButton.getScene().getWindow(), 
                    "IMDb Clone"
                );
            }
            //handle unexpected errors
        } catch (AuthException e) {
            logger.warn("Login failed: {}", e.getMessage());
            handleAuthError(e, loginErrorLabel);
        } catch (ValidationException e) {
            handleValidationError(e, loginErrorLabel);
        } catch (Exception e) {
            logger.error("Unexpected error during login: {}", e.getMessage(), e);
            handleUnexpectedError(e);
        }
    }


    /**
     * Handles the registration form submission.
     * Validates input and creates a new user account.
     */
    @FXML
    private void handleRegister() {
        try {
            registerErrorLabel.setText("");

            // Get and validate input
            String firstName = firstNameField.getText().trim();
            String lastName = lastNameField.getText().trim();
            String username = registerUsernameField.getText().trim();
            String email = emailField.getText().trim();
            String password = passwordField.getText();
            String confirmPassword = confirmPasswordField.getText();

            logger.info("Starting registration process for user: {}", username);

            // Input validation
            if (firstName.isEmpty() || lastName.isEmpty() || username.isEmpty() ||
                email.isEmpty() || password.isEmpty() || confirmPassword.isEmpty()) {
                throw new ValidationException("All fields are required", "VALIDATION_ERROR", null, null);
            }

            if (!email.matches("^[A-Za-z0-9+_.-]+@(.+)$")) {
                throw new ValidationException("Please enter a valid email address", "VALIDATION_ERROR", null, null);
            }

            if (!password.equals(confirmPassword)) {
                throw new ValidationException("Passwords do not match", "VALIDATION_ERROR", null, null);
            }

            // Create and register new user
            logger.debug("Creating new user object for: {}", username);
            User newUser = new User(firstName, lastName, username, 'M', email);
            
            authService.register(newUser, password, confirmPassword);
            
            // Show success message and switch to login form
            loginErrorLabel.setStyle("-fx-text-fill: #2e7d32;");
            loginErrorLabel.setText("Registration successful! Please log in.");
            loginErrorLabel.setVisible(true);

            clearRegistrationForm();
            showLoginForm(null);

        } catch (AuthException e) {
            logger.warn("Registration failed: {}", e.getMessage());
            handleAuthError(e, registerErrorLabel);
        } catch (ValidationException e) {
            handleValidationError(e, registerErrorLabel);
        } catch (Exception e) {
            registerErrorLabel.setStyle("-fx-text-fill: #d32f2f;");
            registerErrorLabel.setText("Registration failed: " + e.getMessage());
            registerErrorLabel.setVisible(true);
            logger.error("Unexpected registration error: {}", e.getMessage(), e);
        }
    }

    /**
     * Clears all fields in the registration form.
     */
    private void clearRegistrationForm() {
        // Clear text fields
        if (firstNameField != null) firstNameField.clear();
        if (lastNameField != null) lastNameField.clear();
        if (registerUsernameField != null) registerUsernameField.clear();
        if (emailField != null) emailField.clear();
        
        // Clear password fields
        if (passwordField != null) passwordField.clear();
        if (passwordVisibleField != null) passwordVisibleField.clear();
        if (confirmPasswordField != null) confirmPasswordField.clear();
        if (confirmPasswordVisibleField != null) confirmPasswordVisibleField.clear();

        // Clear gender selection
        if (genderToggleGroup != null && genderToggleGroup.getSelectedToggle() != null) {
            genderToggleGroup.getSelectedToggle().setSelected(false);
        }
    }



    /**
     * Switches the view to show the registration form.
     * 
     * @param event The action event that triggered this method
     */
    @FXML
    public void showRegisterForm(ActionEvent event) {
        loginContainer.setVisible(false);
        registerContainer.setVisible(true);
        
        if (event != null) {
            event.consume();
        }
    }

    /**
     * Initializes the controller after its root element has been processed.
     * 
     * @param url The location used to resolve relative paths for the root object, or null if unknown
     * @param resourceBundle The resources used to localize the root object, or null if none
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

    //get session token
    public String getSessionToken() {
        return sessionToken;
    }

    //set session token
    public void setSessionToken(String sessionToken) {
        this.sessionToken = sessionToken;
    }
}
