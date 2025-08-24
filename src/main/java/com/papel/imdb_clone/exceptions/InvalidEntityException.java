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
        super(message != null ? message : "Invalid entity data", null, fieldErrors, cause);
        this.entityName = entityName;
        this.entityId = entityId;
        this.addDetail("entity", entityName);
        this.addDetail("entityId", entityId);
    }

    /**
     * Creates a new InvalidEntityException with a builder pattern.
     *
     * @return A new builder for creating InvalidEntityException instances
     */
    public static Builder invalidEntityBuilder() {
        return new Builder();
    }

    public String getEntityName() {
        return entityName;
    }

    public Object getEntityId() {
        return entityId;
    }

    /**
     * Builder for creating InvalidEntityException instances.
     */
    public static class Builder {
        private String entityName;
        private Object entityId;
        private String message = "Invalid entity data";
        private final Map<String, List<String>> fieldErrors = new HashMap<>();

        public Builder entityName(String entityName) {
            this.entityName = entityName;
            return this;
        }

        public Builder entityId(Object entityId) {
            this.entityId = entityId;
            return this;
        }

        public Builder message(String message) {
            this.message = message;
            return this;
        }

        public Builder fieldError(String field, String error) {
            this.fieldErrors.computeIfAbsent(field, k -> new ArrayList<>()).add(error);
            return this;
        }

        public Builder fieldErrors(Map<String, List<String>> fieldErrors) {
            if (fieldErrors != null) {
                fieldErrors.forEach((field, errors) ->
                        errors.forEach(error -> this.fieldError(field, error))
                );
            }
            return this;
        }

        public InvalidEntityException build() {
            return new InvalidEntityException(entityName);
        }
    }
}
