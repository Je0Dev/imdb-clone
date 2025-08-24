package com.papel.imdb_clone.controllers;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

/**
 * Controller for the sign-in dialog.
 */
public class SignInDialogController {
    @FXML
    private TextField usernameField;
    
    @FXML
    private PasswordField passwordField;
    
    private Stage dialogStage;
    private boolean signInClicked = false;

    /**
     * Initializes the controller class. This method is automatically called
     * after the fxml file has been loaded.
     */
    @FXML
    private void initialize() {
        // Initialization code (if any)
    }

    /**
     * Sets the stage of this dialog.
     * 
     * @param dialogStage the dialog stage
     */
    public void setDialogStage(Stage dialogStage) {
        this.dialogStage = dialogStage;
    }

    /**
     * Returns true if the user clicked Sign In, false otherwise.
     * 
     * @return true if the user clicked Sign In, false otherwise
     */
    public boolean isSignInClicked() {
        return signInClicked;
    }

    /**
     * Called when the user clicks the Sign In button.
     */
    @FXML
    private void handleSignIn() {
        if (isInputValid()) {
            signInClicked = true;
            dialogStage.close();
        }
    }

    /**
     * Called when the user clicks the Cancel button.
     */
    @FXML
    private void handleCancel() {
        dialogStage.close();
    }

    /**
     * Validates the user input in the text fields.
     * 
     * @return true if the input is valid
     */
    private boolean isInputValid() {
        String errorMessage = "";

        if (usernameField.getText() == null || usernameField.getText().trim().isEmpty()) {
            errorMessage += "No valid username!\\n";
        }
        if (passwordField.getText() == null || passwordField.getText().trim().isEmpty()) {
            errorMessage += "No valid password!\\n";
        }

        if (errorMessage.isEmpty()) {
            return true;
        } else {
            // Show the error message
            // You can use a dialog or a label to show the error message
            return false;
        }
    }
    
    /**
     * Gets the username entered by the user.
     * 
     * @return the username
     */
    public String getUsername() {
        return usernameField.getText();
    }
    
    /**
     * Gets the password entered by the user.
     * 
     * @return the password
     */
    public String getPassword() {
        return passwordField.getText();
    }
}
