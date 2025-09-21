package com.papel.imdb_clone.exceptions;

import java.io.Serializable;
import java.io.Serializable;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Unified exception for validation errors throughout the application.
 * Supports both simple error messages and field-level validation errors.
 */
public class ValidationException extends RuntimeException implements Serializable {
    private static final long serialVersionUID = 1L;
    // Field errors - using LinkedHashMap for predictable iteration order
    /**
     * Map of field names to their associated error messages.
     */
    private final Map<String, List<String>> fieldErrors;
    
    /**
     * Map to store additional details about the validation error.
     */
    private final LinkedHashMap<String, Serializable> details;
    
    /**
     * Error code for the exception.
     */
    private final String errorCode;

        /**
     * Creates a new ValidationException with all possible parameters.
     *
     * @param message the detail message
     * @param errorCode the error code
     * @param fieldErrors map of field names to error messages
     * @param cause the cause of the exception
     */
    public ValidationException(String message, String errorCode,
                             Map<String, List<String>> fieldErrors,
                             Throwable cause) {
        super(message, cause);
        this.errorCode = errorCode;
        this.fieldErrors = fieldResults(fieldErrors);
        this.details = new LinkedHashMap<>();
    }
    
    /**
     * Safely creates a new LinkedHashMap from the input field errors.
     *
     * @param fieldErrors input field errors map
     * @return a new LinkedHashMap containing the field errors
     */
    private static Map<String, List<String>> fieldResults(Map<String, List<String>> fieldErrors) {
        if (fieldErrors == null) {
            return new LinkedHashMap<>();
        }
        // Create a defensive copy with serializable values
        Map<String, List<String>> result = new LinkedHashMap<>();
        for (Map.Entry<String, List<String>> entry : fieldErrors.entrySet()) {
            result.put(entry.getKey(), new ArrayList<>(entry.getValue()));
        }
        return result;
    }

    /**
     * Adds a serializable detail to the exception.
     * 
     * @param key the detail key
     * @param value the detail value (must be serializable)
     * @throws IllegalArgumentException if the value is not serializable
     */
    public void addDetail(String key, Object value) {
        if (!(value instanceof Serializable)) {
            throw new IllegalArgumentException("Detail value must be serializable");
        }
        details.put(key, (Serializable) value);
    }


    /**
     * Gets all field errors.
     */
    public Map<String, List<String>> getFieldErrors() {
        return Collections.unmodifiableMap(fieldErrors);
    }


    /**
     * Checks if there are any field errors.
     */
    public boolean hasFieldErrors() {
        return !fieldErrors.isEmpty();
    }

    /**
     * Creates a builder for ValidationException.
     */
    public static Builder builder() {
        return new Builder();
    }

    /**
     * Builder for creating ValidationException instances.
     */
    public static class Builder implements Serializable {
        private static final long serialVersionUID = 2L;
        private String message;
        private String errorCode;
        private final Map<String, List<String>> fieldErrors = new LinkedHashMap<>();
        private Throwable cause;
        
        /**
         * Explicit constructor for serialization support.
         */
        public Builder() {
            // Required for serialization
        }

        /**
         * Adds a field error to the builder.
         *
         * @param field the field name
         * @param error the error message
         * @return this builder instance for method chaining
         */
        public Builder fieldError(String field, String error) {
            if (field != null && error != null) {
                this.fieldErrors.computeIfAbsent(field, k -> new ArrayList<>()).add(error);
            }
            return this;
        }


        public ValidationException build() {
            return new ValidationException(message, errorCode, fieldErrors, cause);
        }

/**
         * Sets the error message.
         *
         * @param message the error message
         * @return this builder instance for method chaining
         */
        public Builder message(String message) {
            this.message = message;
            return this;
        }

        /**
         * Sets the error code.
         *
         * @param errorCode the error code
         * @return this builder instance for method chaining
         */
        public Builder errorCode(String errorCode) {
            this.errorCode = errorCode;
            return this;
        }

        /**
         * Sets the cause of the exception.
         *
         * @param cause the cause
         * @return this builder instance for method chaining
         */
        public Builder cause(Throwable cause) {
            this.cause = cause;
            return this;
        }
    }
}
