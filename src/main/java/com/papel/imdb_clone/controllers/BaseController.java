package com.papel.imdb_clone.controllers;

import com.papel.imdb_clone.data.DataManager;
import com.papel.imdb_clone.service.search.ServiceLocator;
import com.papel.imdb_clone.util.UIUtils;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.Window;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Optional;
import java.util.function.Consumer;

/**
 * Base controller class providing common functionality for all controllers in the application.
 * This abstract class implements the JavaFX {@link Initializable} interface and serves as a foundation
 * for all view controllers, offering common UI utilities, error handling, and lifecycle management.
 *
 * <p>Key features include:
 * <ul>
 *     <li>Standardized controller initialization</li>
 *     <li>Common dialog utilities (alerts, confirmations, errors)</li>
 *     <li>View navigation helpers</li>
 *     <li>Error handling and logging</li>
 *     <li>Data management access</li>
 * </ul>
 *
 * <p>Subclasses must implement {@link #initializeController(int)} for their specific initialization logic.
 */
public abstract class BaseController implements Initializable {
    protected final Logger logger = LoggerFactory.getLogger(getClass());
    protected DataManager dataManager;

    /**
     * Constructs a new BaseController with the specified DataManager.
     *
     * @param dataManager The DataManager instance to be used by this controller
     */
    protected BaseController() {
        this.dataManager = ServiceLocator.getInstance().getDataManager();
        if (this.dataManager == null) {
            throw new IllegalStateException("Failed to initialize DataManager from ServiceLocator");
        }
    }

    /**
     * Initializes the controller with the specified user ID.
     * This method serves as the main entry point for controller initialization.
     *
     * @param currentUserId The ID of the currently authenticated user, or -1 if no user is logged in
     * @throws IllegalStateException if initialization fails
     */
    public final void initialize(int currentUserId) {
        try {
            initializeController(currentUserId);
            logger.debug("{} initialized successfully for user ID: {}", 
                getClass().getSimpleName(), currentUserId);
        } catch (Exception e) {
            String errorMsg = String.format("Failed to initialize %s: %s", 
                getClass().getSimpleName(), e.getMessage());
            logger.error(errorMsg, e);
            showError("Initialization Error", "Failed to initialize the application view.");
            throw new IllegalStateException(errorMsg, e);
        }
    }

    /**
     * Controller-specific initialization logic.
     * Subclasses should override this method to implement their specific initialization code.
     *
     * @param currentUserId The ID of the currently authenticated user
     * @throws Exception if initialization fails
     */
    protected abstract void initializeController(int currentUserId) throws Exception;

    // ===== Dialog Utilities =====

    /**
     * Displays an informational alert dialog.
     *
     * @param title   The dialog title (required)
     * @param message The message to display (required)
     * @throws IllegalArgumentException if either title or message is null or empty
     */
    protected void showAlert(String title, String message) {
        validateDialogParams(title, message);
        UIUtils.showAlert(Alert.AlertType.INFORMATION, title, message);
    }

    /**
     * Displays an error dialog with the specified title and message.
     * Also logs the error at WARN level.
     *
     * @param title   The dialog title (required)
     * @param message The error message to display (required)
     * @throws IllegalArgumentException if either title or message is null or empty
     */
    protected void showError(String title, String message) {
        validateDialogParams(title, message);
        logger.warn("Error dialog shown - Title: {}, Message: {}", title, message);
        UIUtils.showError(title, message);
    }

    /**
     * Displays a success dialog with the specified title and message.
     *
     * @param title   The dialog title (required)
     * @param message The success message to display (required)
     * @throws IllegalArgumentException if either title or message is null or empty
     */
    protected void showSuccess(String title, String message) {
        validateDialogParams(title, message);
        logger.debug("Success dialog shown - {}", message);
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        styleAlert(alert);
        alert.showAndWait();
    }

    /**
     * Displays a confirmation dialog with Yes/No buttons.
     *
     * @param title   The dialog title (required)
     * @param message The confirmation question (required)
     * @return true if the user clicked Yes, false otherwise
     * @throws IllegalArgumentException if either title or message is null or empty
     */
    protected boolean showConfirmationDialog(String title, String message) {
        validateDialogParams(title, message);
        logger.debug("Showing confirmation dialog - {}: {}", title, message);
        
        Alert alert = new Alert(
            Alert.AlertType.CONFIRMATION,
            message,
            ButtonType.YES,
            ButtonType.NO
        );
        alert.setTitle(title);
        alert.setHeaderText(null);
        styleAlert(alert);
        
        Optional<ButtonType> result = alert.showAndWait();
        return result.isPresent() && result.get() == ButtonType.YES;
    }

    // ===== View Navigation =====

    /**
     * Opens a new modal window with the specified content.
     *
     * @param title     The window title
     * @param content   The root node of the content to display
     * @param owner     The owner window (can be null)
     * @param onClose   Callback to execute when the window is closed
     * @return The created Stage instance
     */
    protected Stage showModal(String title, Parent content, Window owner, Runnable onClose) {
        Stage stage = new Stage();
        stage.setTitle(title);
        stage.setScene(new Scene(content));
        stage.initModality(Modality.WINDOW_MODAL);
        
        if (owner != null) {
            stage.initOwner(owner);
            // Center on parent window
            stage.setX(owner.getX() + (owner.getWidth() - content.prefWidth(-1)) / 2);
            stage.setY(owner.getY() + (owner.getHeight() - content.prefHeight(-1)) / 2);
        }
        
        stage.setOnCloseRequest(event -> {
            if (onClose != null) {
                onClose.run();
            }
        });
        
        stage.show();
        return stage;
    }

    // ===== Helper Methods =====

    /**
     * Validates dialog parameters.
     *
     * @param title   The dialog title to validate
     * @param message The dialog message to validate
     * @throws IllegalArgumentException if either parameter is null or empty
     */
    private void validateDialogParams(String title, String message) {
        if (title == null || title.trim().isEmpty()) {
            throw new IllegalArgumentException("Dialog title cannot be null or empty");
        }
        if (message == null || message.trim().isEmpty()) {
            throw new IllegalArgumentException("Dialog message cannot be null or empty");
        }
    }

    /**
     * Applies consistent styling to alert dialogs.
     *
     * @param alert The alert to style
     */
    private void styleAlert(Alert alert) {
        // Apply any global alert styling here
        DialogPane dialogPane = alert.getDialogPane();
        // Removed non-existent CSS reference
        dialogPane.getStyleClass().add("custom-dialog");
    }

    /**
     * Gets the current window from a node.
     *
     * @param node The node to get the window from
     * @return The containing window, or null if not in a window
     */
    protected Window getWindow(Node node) {
        return node.getScene() != null ? node.getScene().getWindow() : null;
    }

}
