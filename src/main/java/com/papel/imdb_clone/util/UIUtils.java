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

    private static Stage ownerStage;
    private static Node loadingIndicator;
    private static StackPane loadingContainer;
    private static final Logger logger = Logger.getLogger(UIUtils.class.getName());


    public static final String TITLE_INFO = "Information";
    public static final String TITLE_ERROR = "Error";
    public static final String TITLE_WARNING = "Warning";
    public static final String TITLE_CONFIRM = "Confirm";


    /**
     * Shows an information alert with header and content
     */
    public static void showInfo(String title, String header, String content) {
        Platform.runLater(() -> {
            Alert alert = createAlert(Alert.AlertType.INFORMATION, title, header, content);
            alert.showAndWait();
        });
    }

    /**
     * Shows a success alert with consistent styling
     */
    public static void showSuccess(String title, String successMessage) {
        String message = successMessage != null ? successMessage : "Success";
        showAlert(Alert.AlertType.INFORMATION, title, message);
    }

    /**
     * Shows an error alert with consistent styling
     */
    public static void showError(String title, String message) {
        showError(title, null, message);
    }

    /**
     * Shows a simple error message with default title
     */
    public static void showError(String message) {
        showError(TITLE_ERROR, null, message);
    }


    /**
     * Shows a confirmation dialog
     *
     * @param title   The dialog title
     * @param message The message to display
     * @return true if user clicked OK, false otherwise
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
     * Shows an error alert with header and content
     */
    public static void showError(String title, String header, String content) {
        Platform.runLater(() -> {
            Alert alert = createAlert(Alert.AlertType.ERROR, title, header, content);
            alert.showAndWait();
        });
    }

    /**
     * Shows a warning alert with consistent styling
     */
    public static void showWarning(String title, String message) {
        showAlert(Alert.AlertType.WARNING, title, message);
    }


    /**
     * Creates a text input dialog with validation
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
     * Creates a consistent button with the given text and action
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
     * Creates a standardized alert with consistent styling
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
     * Shows an alert with consistent styling (simplified version)
     */
    private static void showAlert(Alert.AlertType type, String title, String message) {
        Alert alert = createAlert(type, title, null, message);
        alert.showAndWait();
    }
}