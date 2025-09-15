package com.papel.imdb_clone.exceptions;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Exception thrown when an entity fails validation due to invalid data.
 * This is a more specific type of ValidationException for entity validation.
 */
public class InvalidEntityException extends ValidationException {
    private static final String message = "Invalid entity data";
    private final String entityName;
    private Object entityId;

    /**
     * Creates a new InvalidEntityException with entity details.
     */
    public InvalidEntityException(String entityName) {
        super(message, null, null, null);
        this.entityName = entityName;// Add entity details to the details map from parent class
        this.addDetail("entity", entityName);
        this.addDetail("entityId", entityId);
    }

    /**
     * Creates a new InvalidEntityException with entity name, identifier, message, field errors, and cause.
     */
    public InvalidEntityException(String entityName, Object entityId, String message,
                                  Map<String, List<String>> fieldErrors, Throwable cause) {
        //call parent constructor with message, field errors, and cause
        super(message != null ? message : "Invalid entity data", null, fieldErrors, cause);
        this.entityName = entityName;
        this.entityId = entityId;
        this.addDetail("entity", entityName);
        this.addDetail("entityId", entityId);
    }

}
