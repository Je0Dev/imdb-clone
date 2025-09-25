package com.papel.imdb_clone.controllers.content;

import com.papel.imdb_clone.model.content.Content;
import javafx.fxml.FXML;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.time.format.DateTimeFormatter;


/**
 * Controller for displaying detailed information about a specific content (movie or TV show).
 * Handles the UI elements and data binding for the content details view.
 */

public class ContentDetailsController {
    /**
     * Explicit constructor for ContentDetailsController.
     * Required for JavaFX controller initialization.
     */
    public ContentDetailsController() {
        // No initialization needed because it is a controller so it is initialized by JavaFX
    }

    //Logger and date formatter
    private static final Logger logger = LoggerFactory.getLogger(ContentDetailsController.class);

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("MMMM d, yyyy");


    // Data State
    private String contentId;
    private Content content;

    /**
     * Initializes the controller after FXML loading is complete.
     * Sets up initial UI state and event handlers.
     */
    @FXML
    public void initialize() {
        logger.debug("Initializing ContentDetailsController");
        setupEventHandlers();
    }

    
    /**
     * Sets up event handlers for UI components.
     */
    private void setupEventHandlers() {
        //No event handlers needed for now
        logger.debug("Event handlers set up");
    }


    /**
     * Gets the current content ID.
     * 
     * @return the content ID, or null if not set
     */
    public String getContentId() {
        return contentId;
    }

    //sets the content ID
    public void setContentId(String contentId) {
        this.contentId = contentId;
    }
    
    /**
     * Gets the current content object.
     * 
     * @return the content object, or null if not set
     */
    public Content getContent() {
        return content;
    }

    //sets the content object
    public void setContent(Content content) {
        this.content = content;
    }
}
