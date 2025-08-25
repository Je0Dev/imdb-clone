package com.papel.imdb_clone.util;

import javafx.application.Platform;
import javafx.scene.Node;
import javafx.scene.control.Alert;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;

import java.util.logging.Logger;

/**
 * Unified utility class for all UI operations including dialogs, layouts, and basic validation.
 * Combines functionality from DialogFactory, DialogUtils, and UI-related ValidationUtils methods.
 */
public class UIUtils {

    /**
     * The primary stage of the application, used for dialog positioning.
     */
    private static Stage ownerStage;

    /**
     * The currently displayed loading indicator node.
     */
    private static Node loadingIndicator;

    /**
     * Container for the loading indicator overlay.
     */
    private static StackPane loadingContainer;

    /**
     * Logger instance for this class.
     */
    private static final Logger logger = Logger.getLogger(UIUtils.class.getName());


    /**
     * Displays a success alert with the specified title and message.
     * Uses a consistent styling for success messages.
     *
     * @param title          The title of the dialog
     * @param successMessage The success message to display (if null, "Success" is used)
     * @throws IllegalArgumentException if title is null
     */
    public static void showSuccess(String title, String successMessage) {
        String message = successMessage != null ? successMessage : "Success";
        showAlert(Alert.AlertType.INFORMATION, title, message);
    }

    /**
     * Displays an error alert with the specified title and message.
     * Uses a consistent styling for error messages.
     *
     * @param title   The title of the error dialog
     * @param message The error message to display
     * @throws IllegalArgumentException if title or message is null
     */
    public static void showError(String title, String message) {
        showError(title, null, message);
    }


    /**
     * Displays an error alert with the specified title, header, and content.
     * The dialog is shown on the JavaFX Application Thread.
     *
     * @param title   The title of the error dialog
     * @param header  The header text (optional, can be null)
     * @param content The detailed error message
     * @throws IllegalArgumentException if title or content is null
     */
    public static void showError(String title, String header, String content) {
        Platform.runLater(() -> {
            Alert alert = createAlert(Alert.AlertType.ERROR, title, header, content);
            alert.showAndWait();
        });
    }

    /**
     * Displays a warning alert with the specified title and message.
     * Uses a consistent styling for warning messages.
     *
     * @param title   The title of the warning dialog
     * @param message The warning message to display
     * @throws IllegalArgumentException if title or message is null
     */
    public static void showWarning(String title, String message) {
        showAlert(Alert.AlertType.WARNING, title, message);
    }


    /**
     * Creates a standardized alert dialog with consistent styling.
     * The alert is configured with the specified type, title, header, and content.
     *
     * @param type    The type of alert (e.g., ERROR, WARNING, INFORMATION)
     * @param title   The title of the alert
     * @param header  The header text (optional, can be null)
     * @param content The content message
     * @return A configured Alert instance
     * @throws IllegalArgumentException if type, title, or content is null
     */
    private static Alert createAlert(Alert.AlertType type, String title, String header, String content) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(header);
        alert.setContentText(content);

        if (ownerStage != null) {
            alert.initOwner(ownerStage);
        }

        // Add consistent styling
        alert.getDialogPane().setPrefWidth(400);
        alert.getDialogPane().setPrefHeight(200);

        return alert;
    }

    /**
     * Displays a simple alert with the specified type, title, and message.
     * This is a convenience method that creates and shows an alert in one call.
     *
     * @param type    The type of alert (e.g., ERROR, WARNING, INFORMATION)
     * @param title   The title of the alert
     * @param message The message to display
     * @throws IllegalArgumentException if any parameter is null
     */
    private static void showAlert(Alert.AlertType type, String title, String message) {
        Alert alert = createAlert(type, title, null, message);
        alert.showAndWait();
    }
}