package com.papel.imdb_clone.controllers.base;

import com.papel.imdb_clone.data.DataManager;
import com.papel.imdb_clone.service.search.ServiceLocator;
import com.papel.imdb_clone.util.UIUtils;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Optional;

/**
 * Base controller class providing common functionality for all controllers.
 * This class implements the Initializable interface and provides common
 * functionality for UI components, dialogs, and data management.
 */
public abstract class BaseController implements Initializable {
    protected final Logger logger = LoggerFactory.getLogger(getClass());
    protected DataManager dataManager;

    /**
     * Initialize the controller with the specified user ID.
     *
     * @param currentUserId The ID of the current user
     */
    public void initialize(int currentUserId) {
        try {
            this.dataManager = ServiceLocator.getInstance().getDataManager();
            initializeController(currentUserId);
            logger.debug("{} initialized successfully", getClass().getSimpleName());
        } catch (Exception e) {
            logger.error("Error initializing {}", getClass().getSimpleName(), e);
            showError("Initialization Error", "Failed to initialize " + getClass().getSimpleName());
            throw new RuntimeException("Failed to initialize " + getClass().getSimpleName(), e);
        }
    }

    /**
     * Initialize controller-specific components.
     * This method is called during controller initialization.
     *
     * @param currentUserId The ID of the current user
     */
    protected abstract void initializeController(int currentUserId) throws Exception;

    /**
     * Show an alert dialog with the specified title and message.
     *
     * @param title   The title of the alert
     * @param message The message to display
     */
    protected void showAlert(String title, String message) {
        UIUtils.showAlert(Alert.AlertType.INFORMATION, title, message);
    }

    /**
     * Show an error dialog with the specified title and message.
     *
     * @param title   The title of the error dialog
     * @param message The error message to display
     */
    protected void showError(String title, String message) {
        logger.warn("Showing error: {} - {}", title, message);
        UIUtils.showError(title, message);
    }

    /**
     * Show a success dialog with the specified title and message.
     *
     * @param title   The title of the success dialog
     * @param message The success message to display
     */
    protected void showSuccess(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    /**
     * Show a confirmation dialog with the specified message.
     *
     * @param message The confirmation message to display
     * @return true if the user clicked OK, false otherwise
     */
    protected boolean showConfirmationDialog(String message) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Confirmation");
        alert.setHeaderText(null);
        alert.setContentText(message);

        Optional<ButtonType> result = alert.showAndWait();
        return result.isPresent() && result.get() == ButtonType.OK;
    }

}
