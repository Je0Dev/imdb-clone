package com.papel.imdb_clone.util;

import javafx.application.Platform;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.stage.Stage;

import java.util.Optional;
import java.util.logging.Level;
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

    // Standard dialog titles
    
    /**
     * Standard title for information dialogs.
     */
    public static final String TITLE_INFO = "Information";
    
    /**
     * Standard title for error dialogs.
     */
    public static final String TITLE_ERROR = "Error";
    
    /**
     * Standard title for warning dialogs.
     */
    public static final String TITLE_WARNING = "Warning";
    
    /**
     * Standard title for confirmation dialogs.
     */
    public static final String TITLE_CONFIRM = "Confirm";


    /**
     * Displays an information alert dialog with the specified title, header, and content.
     * The dialog is shown on the JavaFX Application Thread.
     *
     * @param title   The title of the dialog (displayed in the window title bar)
     * @param header  The header text (optional, can be null)
     * @param content The main content message to display
     * @throws IllegalArgumentException if title or content is null
     */
    public static void showInfo(String title, String header, String content) {
        Platform.runLater(() -> {
            Alert alert = createAlert(Alert.AlertType.INFORMATION, title, header, content);
            alert.showAndWait();
        });
    }

    /**
     * Displays a success alert with the specified title and message.
     * Uses a consistent styling for success messages.
     *
     * @param title         The title of the dialog
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
     * Displays a simple error message with a default error title.
     * This is a convenience method that uses the default error title.
     *
     * @param message The error message to display
     * @throws IllegalArgumentException if message is null
     */
    public static void showError(String message) {
        showError(TITLE_ERROR, null, message);
    }


    /**
     * Displays a confirmation dialog with the specified title and message.
     * The dialog includes OK and Cancel buttons.
     *
     * @param title   The title of the confirmation dialog
     * @param message The message to display in the dialog
     * @return true if the user clicked OK, false if Cancel was clicked or the dialog was closed
     * @throws IllegalArgumentException if title or message is null
     */
    public static boolean showConfirm(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle(title != null ? title : TITLE_CONFIRM);
        alert.setHeaderText(null);
        alert.setContentText(message);

        Optional<ButtonType> result = alert.showAndWait();
        return result.isPresent() && result.get() == ButtonType.OK;
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
     * Creates a text input dialog with the specified parameters.
     * The dialog is initialized with the application's owner stage if set.
     *
     * @param title       The title of the dialog
     * @param header      The header text (optional, can be null)
     * @param content     The content text to display
     * @param defaultValue The default value for the input field (can be null)
     * @return A configured TextInputDialog instance
     * @throws IllegalArgumentException if title or content is null
     */
    public static TextInputDialog createTextInputDialog(String title, String header, String content, String defaultValue) {
        TextInputDialog dialog = new TextInputDialog(defaultValue);
        dialog.setTitle(title);
        dialog.setHeaderText(header);
        dialog.setContentText(content);

        if (ownerStage != null) {
            dialog.initOwner(ownerStage);
        }

        return dialog;
    }


    /**
     * Creates a styled button with the specified text and action handler.
     * The button includes error handling for the action.
     *
     * @param text   The text to display on the button
     * @param action The action to execute when the button is clicked (can be null)
     * @return A configured Button instance
     * @throws IllegalArgumentException if text is null
     */
    public static Button createButton(String text, Runnable action) {
        Button button = new Button(text);
        button.getStyleClass().add("standard-button");
        if (action != null) {
            button.setOnAction(e -> {
                try {
                    action.run();
                } catch (Exception ex) {
                    logger.log(Level.parse("Button action failed: {}"), ex.getMessage(), ex);
                    showError("Action failed", ex.getMessage());
                }
            });
        }
        return button;
    }

    /**
     * Shows a loading indicator over the specified container
     *
     * @param container The container to show the loading indicator over
     * @param message   Optional message to display with the loading indicator
     */
    public static void showLoadingIndicator(Region container, String message) {
        if (loadingIndicator != null) {
            hideLoadingIndicator();
        }

        // Create loading indicator
        VBox loadingBox = new VBox(10);
        loadingBox.setAlignment(Pos.CENTER);
        loadingBox.setStyle("-fx-background-color: rgba(0, 0, 0, 0.5);");

        ProgressIndicator progress = new ProgressIndicator();
        progress.setMaxSize(50, 50);

        Label messageLabel = new Label(message);
        messageLabel.setTextFill(Color.WHITE);
        messageLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 14px;");

        loadingBox.getChildren().addAll(progress, messageLabel);

        // Create a stack pane to overlay the loading indicator
        loadingContainer = new StackPane(container, loadingBox);
        StackPane.setAlignment(loadingBox, Pos.CENTER);

        // Replace the container in its parent
        if (container.getParent() instanceof Pane parent) {
            int index = parent.getChildren().indexOf(container);
            parent.getChildren().set(index, loadingContainer);
        }

        loadingIndicator = loadingBox;
    }

    /**
     * Hides the currently visible loading indicator
     */
    public static void hideLoadingIndicator() {
        if (loadingIndicator != null && loadingContainer != null) {
            Node container = loadingContainer.getChildren().get(0);
            if (loadingContainer.getParent() instanceof Pane parent) {
                int index = parent.getChildren().indexOf(loadingContainer);
                parent.getChildren().set(index, container);
            }
            loadingIndicator = null;
            loadingContainer = null;
        }
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