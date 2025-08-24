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
     * Creates an exception with a custom message
     */
    /**
     * @deprecated Use the builder pattern or constructor with entity details
     */
    @Deprecated
    public EntityNotFoundException(String message) {
        super(null, null, message, null, null);
        this.entityType = null;
        this.identifier = null;
        this.entityName = null;
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

    public static class Builder {
        private Class<?> entityType;
        private String entityName;
        private Object identifier;
        private String message;
        private Throwable cause;

        public Builder entityType(Class<?> entityType) {
            this.entityType = entityType;
            return this;
        }

        public Builder entityName(String entityName) {
            this.entityName = entityName;
            return this;
        }

        public Builder identifier(Object identifier) {
            this.identifier = identifier;
            return this;
        }

        public Builder message(String message) {
            this.message = message;
            return this;
        }

        public Builder cause(Throwable cause) {
            this.cause = cause;
            return this;
        }

        public EntityNotFoundException build() {
            if (entityType == null && entityName == null) {
                throw new IllegalStateException("Either entityType or entityName must be provided");
            }

            String finalEntityName = entityName != null ? entityName :
                    (entityType != null ? entityType.getSimpleName() : null);

            if (cause != null) {
                return new EntityNotFoundException(entityType, identifier, message, cause);
            } else {
                return new EntityNotFoundException(entityType, finalEntityName, identifier, message);
            }
        }
    }

    /**
     * Creates a standard error message based on entity type and identifier
     */
    private String createMessage() {
        return String.format("%s not found with identifier: %s", entityName, identifier);
    }

    // ========== CONVENIENCE FACTORY METHODS ==========

    /**
     * Creates an ActorNotFoundException equivalent
     */
    public static EntityNotFoundException forActor(Object identifier) {
        return new EntityNotFoundException(Object.class, identifier, "Actor not found with identifier: " + identifier);
    }

    /**
     * Creates a DirectorNotFoundException equivalent
     */
    public static EntityNotFoundException forDirector(Object identifier) {
        return new EntityNotFoundException(Object.class, identifier, "Director not found with identifier: " + identifier);
    }

    /**
     * Creates a UserNotFoundException equivalent
     */
    public static EntityNotFoundException forUser(Object identifier) {
        return new EntityNotFoundException(Object.class, identifier, "User not found with identifier: " + identifier);
    }

    /**
     * Creates a MovieNotFoundException equivalent
     */
    public static EntityNotFoundException forMovie(Object identifier) {
        return new EntityNotFoundException(Object.class, identifier, "Movie not found with identifier: " + identifier);
    }

    /**
     * Creates a SeriesNotFoundException equivalent
     */
    public static EntityNotFoundException forSeries(Object identifier) {
        return new EntityNotFoundException(Object.class, identifier, "Series not found with identifier: " + identifier);
    }

    /**
     * Creates a generic entity not found exception
     */
    public static EntityNotFoundException forEntity(Class<?> entityType, Object identifier) {
        return new EntityNotFoundException(entityType, identifier);
    }

    /**
     * Creates a generic entity not found exception with custom message
     */
    public static EntityNotFoundException forEntity(Class<?> entityType, Object identifier, String message) {
        return new EntityNotFoundException(entityType, identifier, message);
    }
}