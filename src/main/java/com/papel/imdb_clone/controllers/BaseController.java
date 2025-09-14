package com.papel.imdb_clone.controllers;

import com.papel.imdb_clone.util.UIUtils;
import javafx.fxml.Initializable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * Base controller class providing common functionality for all controllers.
 * This class implements the Initializable interface and provides a common
 * initialization method for all controllers.
 * It also provides a common error handling method for all controllers.
 */
public abstract class BaseController implements Initializable {
    protected final Logger logger = LoggerFactory.getLogger(getClass());

    //initialize the controller with the specified user ID
    public void initialize(int currentUserId) {
        try {
            initializeController(currentUserId);
            logger.debug("{} initialized successfully", getClass().getSimpleName());
        } catch (Exception e) {
            // Log error and show error message
            logger.error("Error initializing {}", getClass().getSimpleName(), e);
            showError("Initialization Error", "Failed to initialize " + getClass().getSimpleName());
            throw new RuntimeException("Failed to initialize " + getClass().getSimpleName(), e);
        }
    }

    /**
     * Initialize controller-specific components.
     * This method is called during controller initialization.
     */
    protected abstract void initializeController(int currentUserId) throws Exception;

    /**
     * Show an error dialog with the specified title and message.
     */
    protected void showError(String title, String message) {
        logger.warn("Showing error: {} - {}", title, message);
        UIUtils.showError(title, message);
    }


}
