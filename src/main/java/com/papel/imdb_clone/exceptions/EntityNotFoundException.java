package com.papel.imdb_clone.exceptions;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Exception thrown when an entity is not found in the system.
 * This is a specific type of InvalidEntityException for entity not found scenarios.
 */
public class EntityNotFoundException extends InvalidEntityException {

    private final Class<?> entityType;
    private final Object identifier;
    private final String entityName;


    /**
     * Creates an exception with entity type and identifier
     */
    public EntityNotFoundException(Class<?> entityType, Object identifier) {
        this(entityType, identifier, (String) null);
    }

    /**
     * Creates an exception with entity type, identifier, and custom message
     */
    public EntityNotFoundException(Class<?> entityType, Object identifier, String message) {
        this(entityType, entityType != null ? entityType.getSimpleName() : null, identifier, message, null);
    }

    /**
     * Creates an exception with entity type, identifier, and cause
     */
    public EntityNotFoundException(Class<?> entityType, String entityName, Object identifier,
                                   String message, Throwable cause) {
        super(entityName,
                identifier,
                message != null ? message : createMessage(entityType, identifier),
                createFieldErrors(entityType, identifier),
                cause);
        this.entityType = entityType;
        this.identifier = identifier;
        this.entityName = entityName != null ? entityName :
                (entityType != null ? entityType.getSimpleName() : null);
    }

    public EntityNotFoundException(Class<?> entityType, Object identifier, Throwable cause) {
        this(entityType, identifier, null, cause);
    }

    public EntityNotFoundException(Class<?> entityType, Object identifier, String message, Throwable cause) {
        this(entityType,
                entityType != null ? entityType.getSimpleName() : null,
                identifier,
                message,
                cause);
    }

    public EntityNotFoundException(Class<?> entityType, String finalEntityName, Object identifier, String message) {
        super(finalEntityName, identifier, message, createFieldErrors(entityType, identifier), null);
        this.entityType = entityType;
        this.identifier = identifier;
        this.entityName = finalEntityName;
    }

    /**
     * Creates a standard error message based on entity type and identifier
     */
    private static String createMessage(Class<?> entityType, Object identifier) {
        return String.format("%s not found with identifier: %s",
                entityType != null ? entityType.getSimpleName() : "Entity",
                identifier);
    }

    private static Map<String, List<String>> createFieldErrors(Class<?> entityType, Object identifier) {
        Map<String, List<String>> errors = new HashMap<>();
        String fieldName = entityType != null ?
                String.format("%sId", entityType.getSimpleName().toLowerCase()) : "id";
        errors.put(fieldName, Collections.singletonList("not found: " + identifier));
        return errors;
    }
}