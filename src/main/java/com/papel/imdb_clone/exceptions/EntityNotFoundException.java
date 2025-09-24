package com.papel.imdb_clone.exceptions;

import java.io.Serializable;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Exception thrown when an entity is not found in the system.
 * This is a specific type of InvalidEntityException for entity not found scenarios.
 */
public class EntityNotFoundException extends InvalidEntityException {
    private static final long serialVersionUID = 1L;

    // Store class name as String for serialization
    private final String entityTypeName;
    private final Serializable identifier; // Changed to Serializable
    private final String entityName;
    private transient Class<?> entityType; // Marked as transient to avoid serialization issues
    private final String serializedEntityType; // Store class name for deserialization
    
    /**
     * Custom serialization method to handle the non-serializable Class object.
     */
    private void writeObject(java.io.ObjectOutputStream out) throws java.io.IOException {
        // Store the class name in the serializedEntityType field
        String className = entityType != null ? entityType.getName() : null;
        try {
            java.lang.reflect.Field field = getClass().getDeclaredField("serializedEntityType");
            field.setAccessible(true);
            field.set(this, className);
        } catch (Exception e) {
            throw new java.io.NotSerializableException("Failed to serialize entity type: " + e.getMessage());
        }
        
        out.defaultWriteObject();
    }
    
    /**
     * Custom deserialization method to handle the non-serializable Class object.
     */
    private void readObject(java.io.ObjectInputStream in) throws java.io.IOException, ClassNotFoundException {
        in.defaultReadObject();
        
        // Restore the class object from the stored name in serializedEntityType
        if (this.serializedEntityType != null) {
            try {
                this.entityType = Class.forName(this.serializedEntityType);
            } catch (ClassNotFoundException e) {
                this.entityType = null;
            }
        }
        
        // Ensure identifier is serializable
        if (this.identifier != null && !(this.identifier instanceof Serializable)) {
            throw new java.io.InvalidObjectException("Identifier must be serializable");
        }
    }


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
        //set entity type, identifier, and entity name
        this.entityType = entityType;
        this.identifier = (Serializable) identifier;
        this.entityName = entityName != null ? entityName :
                (entityType != null ? entityType.getSimpleName() : null);
        this.entityTypeName = entityType != null ? entityType.getName() : null;
        this.serializedEntityType = entityType != null ? entityType.getName() : null;
    }


    //create exception with entity type, identifier, message, and cause
    public EntityNotFoundException(Class<?> entityType, Object identifier, String message, Throwable cause) {
        this(entityType,
                entityType != null ? entityType.getSimpleName() : null,
                identifier,
                message,
                cause);
    }

    /**
     * Creates a standard error message based on entity type and identifier
     */
    private static String createMessage(Class<?> entityType, Object identifier) {
        return String.format("%s not found with identifier: %s",
                entityType != null ? entityType.getSimpleName() : "Entity",
                identifier);
    }

    //create field errors
    private static Map<String, List<String>> createFieldErrors(Class<?> entityType, Object identifier) {
        //create field errors
        Map<String, List<String>> errors = new HashMap<>();
        //get field name from entity type
        String fieldName = entityType != null ?
                String.format("%sId", entityType.getSimpleName().toLowerCase()) : "id";
        errors.put(fieldName, Collections.singletonList("not found: " + identifier));
        return errors;
    }
}