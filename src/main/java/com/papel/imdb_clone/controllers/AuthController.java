package com.papel.imdb_clone.controllers;

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
    private static final Logger logger = LoggerFactory.getLogger(AuthController.class);
    public Hyperlink registerLink;
    public StackPane registerContainer;
    public Hyperlink loginLink;


    // Services
    private final AuthService authService = AuthService.getInstance();
    private final NavigationService navigationService = NavigationService.getInstance();
    private final UserInputValidator inputValidator = new UserInputValidator();
    public CheckBox rememberMe;
    public Hyperlink forgotPasswordLink;


    // UI Components - Login
    @FXML
    private TextField loginUsernameField;
    @FXML
    private PasswordField loginPasswordField;
    @FXML
    private Button loginButton;
    @FXML
    private Button toggleLoginPassword;

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

    @FXML
    private ToggleGroup genderToggleGroup;
    @FXML
    private Button registerButton;
    @FXML
    private Button toggleRegisterPassword;
    @FXML
    private Button toggleConfirmPassword;

    @FXML
    private Label registerErrorLabel;
    @FXML
    private Label passwordStrengthLabel;

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


    private Stage stage;
    private String successMessage;
    private Object currentUser;
    private String sessionToken;

    public AuthController() {
        this.confirmPasswordVisibleField = confirmPasswordVisibleField;
    }

    @Override
    protected void initializeController(int currentUserId) {
        try {
            // Initialize UI bindings and listeners
            setupLoginForm(loginPasswordVisibleField);
            setupRegistrationForm(passwordVisibleField, confirmPasswordVisibleField);

        } catch (Exception e) {
            logger.error("Error initializing AuthController: {}", e.getMessage(), e);
            UIUtils.showError("Initialization Error", "Failed to initialize authentication system.");
        }
    }

    private void setupLoginForm(TextField loginPasswordVisibleField) {
        // Bind login button state
        loginButton.disableProperty().bind(
                loginUsernameField.textProperty().isEmpty()
                        .or(loginPasswordField.textProperty().isEmpty())
        );

        // Handle Enter key press in password field
        loginPasswordField.setOnKeyPressed(this::handleLoginKeyPress);
        loginPasswordVisibleField.setOnKeyPressed(this::handleLoginKeyPress);

        // Setup password visibility toggle
        setupPasswordVisibilityToggle(loginPasswordField, loginPasswordVisibleField, toggleLoginPassword);
    }

    private void setupRegistrationForm(TextField passwordVisibleField, TextField confirmPasswordVisibleField) {
        // Bind register button state with validation
        registerButton.disableProperty().bind(
                firstNameField.textProperty().isEmpty()
                        .or(lastNameField.textProperty().isEmpty())
                        .or(registerUsernameField.textProperty().isEmpty()
                                .or(Bindings.createBooleanBinding(() ->
                                                inputValidator.isValidUsername(registerUsernameField.getText()),
                                        registerUsernameField.textProperty())))
                        .or(emailField.textProperty().isEmpty()
                                .or(Bindings.createBooleanBinding(() ->
                                                inputValidator.isValidEmail(emailField.getText()),
                                        emailField.textProperty())))
                        .or(passwordField.textProperty().isEmpty()
                                .or(Bindings.createBooleanBinding(() ->
                                                !passwordField.getText().equals(confirmPasswordField.getText()),
                                        passwordField.textProperty(),
                                        confirmPasswordField.textProperty())))
        );

        // Setup password visibility toggles
        setupPasswordVisibilityToggle(passwordField, passwordVisibleField, toggleRegisterPassword);
        setupPasswordVisibilityToggle(confirmPasswordField, confirmPasswordVisibleField, toggleConfirmPassword);

        // Add password strength indicator
        passwordField.textProperty().addListener((obs, oldVal, newVal) ->
                updatePasswordStrengthIndicator(newVal)
        );
    }

    private void setupPasswordVisibilityToggle(PasswordField passwordField, TextField visibleField, Button toggleButton) {
        visibleField.managedProperty().bind(visibleField.visibleProperty());
        passwordField.managedProperty().bind(visibleField.visibleProperty().not());
        visibleField.visibleProperty().set(false);

        // Bind text between password and visible fields
        passwordField.textProperty().addListener((obs, oldVal, newVal) -> {
            if (passwordField.isFocused()) {
                visibleField.setText(newVal);
            }
        });

        visibleField.textProperty().addListener((obs, oldVal, newVal) -> {
            if (visibleField.isFocused()) {
                passwordField.setText(newVal);
                if (passwordField == this.passwordField) {
                    updatePasswordStrengthIndicator(newVal);
                }
            }
        });

        // Toggle visibility on button click
        toggleButton.setOnAction(e -> {
            boolean isVisible = visibleField.isVisible();
            visibleField.setVisible(!isVisible);
            toggleButton.setText(isVisible ? "Show" : "Hide");
        });
    }

    private void updatePasswordStrengthIndicator(String password) {
        int strength = inputValidator.calculatePasswordStrength(password);

        String[] strengthTexts = {"Very Weak", "Weak", "Moderate", "Strong", "Very Strong"};
        String[] colors = {"#ff4444", "#ff8800", "#ffbb33", "#00C851", "#007E33"};

        if (password == null || password.isEmpty()) {
            passwordStrengthLabel.setText("");
            return;
        }

        int index = Math.min(strength - 1, strengthTexts.length - 1);
        String strengthText = strength > 0 ? strengthTexts[index] : "";
        String color = strength > 0 ? colors[index] : "#b3b3b3";

        passwordStrengthLabel.setText("Password: " + strengthText);
        passwordStrengthLabel.setStyle("-fx-text-fill: " + color + "; -fx-font-size: 11;");
    }

    private void handleValidationError(ValidationException e, Label errorLabel) {
        StringBuilder errorMessage = new StringBuilder();
        
        if (e.hasFieldErrors()) {
            // Collect all field errors
            Map<String, List<String>> fieldErrors = e.getFieldErrors();
            fieldErrors.forEach((field, errors) -> 
                errors.forEach(error -> 
                    errorMessage.append("• ").append(error).append("\n")
                )
            );
        } else {
            errorMessage.append(e.getMessage());
        }
        
        // Remove the last newline if present
        if (errorMessage.length() > 0 && errorMessage.charAt(errorMessage.length() - 1) == '\n') {
            errorMessage.setLength(errorMessage.length() - 1);
        }
        
        errorLabel.setText(errorMessage.toString());
        errorLabel.setStyle("-fx-text-fill: #d32f2f; -fx-wrap-text: true;");
        errorLabel.setVisible(true);
        errorLabel.setManaged(true);
        logger.warn("Validation error: {}", errorMessage);
    }


    private void handleUnexpectedError(String operation, Exception e) {
        String errorMessage = String.format("An unexpected error occurred during %s. Please try again.", operation);
        UIUtils.showError("Error", errorMessage);
        logger.error("Unexpected error during {}: {}", operation, e.getMessage(), e);
    }

    public void setStage(Stage stage) {
        this.stage = stage;
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
            String username = loginUsernameField.getText().trim();
            String password = loginPasswordField.getText();

            // Validate input
            inputValidator.validateLogin(username, password);

            // Attempt login
            authService.login(username, password);

            // Get the current stage from the login button
            Stage currentStage = (Stage) loginButton.getScene().getWindow();

            // Navigate to main app
            try {
                navigationService.navigateToMainApp(currentStage);
            } catch (Exception e) {
                logger.error("Failed to navigate to main app: {}", e.getMessage(), e);
                showLoginError("Failed to load the main application. Please try again.");
            }

        } catch (ValidationException e) {
            handleValidationError(e, loginErrorLabel);
        } catch (Exception e) {
            handleUnexpectedError("login", e);
        }
    }

    @FXML
    private void handleRegister() {
        try {
            // Get form values
            String firstName = firstNameField.getText().trim();
            String lastName = lastNameField.getText().trim();
            String username = registerUsernameField.getText().trim();
            String email = emailField.getText().trim();
            String password = passwordField.getText();
            String confirmPassword = confirmPasswordField.getText();

            // Validate all inputs
            inputValidator.validateRegistration(firstName, lastName, username, email, password, confirmPassword);

            // Create new user
            User newUser = new User(
                    firstName,
                    lastName,
                    username,
                    genderToggleGroup.getSelectedToggle() != null ?
                            ((RadioButton) genderToggleGroup.getSelectedToggle()).getText().charAt(0) : ' ',
                    email
            );

            // Register user
            authService.register(newUser, password);

            // Show success message and switch to login form
            loginErrorLabel.setStyle("-fx-text-fill: #2e7d32;");
            loginErrorLabel.setText("Registration successful! Please log in.");
            loginErrorLabel.setVisible(true);

            // Clear form and switch to login
            clearRegistrationForm();
            showLoginForm(null);

        } catch (ValidationException e) {
            handleValidationError(e, registerErrorLabel);
        } catch (Exception e) {
            registerErrorLabel.setStyle("-fx-text-fill: #d32f2f;");
            registerErrorLabel.setText("Registration failed: " + e.getMessage());
            registerErrorLabel.setVisible(true);
            logger.error("Registration error: {}", e.getMessage(), e);
        }
    }

    private void showLoginError(String message) {
        loginErrorLabel.setStyle("-fx-text-fill: #d32f2f;");
        loginErrorLabel.setText(message);
        loginErrorLabel.setVisible(true);
    }


    private void clearRegistrationForm() {
        // Clear text fields if they're not null
        if (firstNameField != null) firstNameField.clear();
        if (lastNameField != null) lastNameField.clear();
        if (registerUsernameField != null) registerUsernameField.clear();
        if (emailField != null) emailField.clear();
        if (passwordField != null) passwordField.clear();
        if (passwordVisibleField != null) passwordVisibleField.clear();
        if (confirmPasswordField != null) confirmPasswordField.clear();
        if (confirmPasswordVisibleField != null) confirmPasswordVisibleField.clear();
        if (passwordStrengthLabel != null) passwordStrengthLabel.setText("");

        if (genderToggleGroup != null && genderToggleGroup.getSelectedToggle() != null) {
            genderToggleGroup.getSelectedToggle().setSelected(false);
        }
    }

    @FXML
    private void handleLoginKeyPress(KeyEvent event) {
        if (event.getCode() == KeyCode.ENTER) {
            if (loginContainer.isVisible()) {
                handleLogin();
            } else {
                handleRegister();
            }
        }
    }

    @FXML
    public void handleForgotPassword() {
        try {
            String username = loginUsernameField.getText().trim();
            if (username.isEmpty()) {
                showLoginError("Please enter your username to reset password");
                return;
            }

            // Show confirmation dialog
            Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
            alert.setTitle("Password Reset");
            alert.setHeaderText("Reset Password");
            alert.setContentText("A password reset link will be sent to the email associated with your account. Continue?");

            alert.showAndWait().ifPresent(response -> {
                if (response == ButtonType.OK) {
                    try {
                        authService.initiatePasswordReset(username);
                        showLoginError("Password reset instructions have been sent to your email.");
                    } catch (Exception e) {
                        handleUnexpectedError("password reset", e);
                    }
                }
            });

        } catch (Exception e) {
            handleUnexpectedError("password reset", e);
        }
    }


    @FXML
    public void showRegisterForm(ActionEvent event) {
        loginContainer.setVisible(false);
        registerContainer.setVisible(true);
        if (event != null) {
            event.consume();
        }
    }


    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        logger.debug("Initializing AuthController");
        loginContainer.setVisible(true);
    }

    public void showLoginForm(ActionEvent actionEvent) {
        loginContainer.setVisible(true);
        registerContainer.setVisible(false);
        if (actionEvent != null) {
            actionEvent.consume();
        }
        clearRegistrationForm();
        loginErrorLabel.setVisible(false);
        registerErrorLabel.setVisible(false);
        loginUsernameField.requestFocus();
        loginPasswordField.requestFocus();
        loginErrorLabel.setText("");
        registerErrorLabel.setText("");
        loginErrorLabel.setStyle("-fx-text-fill: #d32f2f;");
        registerErrorLabel.setStyle("-fx-text-fill: #d32f2f;");
        loginErrorLabel.setVisible(false);
        registerErrorLabel.setVisible(false);
    }
}
