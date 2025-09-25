package com.papel.imdb_clone.exceptions;

import java.io.Serial;
import java.io.Serializable;
import java.util.*;

/**
 * Unified exception for validation errors throughout the application.
 * Supports both simple error messages and field-level validation errors.
 */
public class ValidationException extends RuntimeException implements Serializable {

    //serial version uid for object serialization
    @Serial
    private static final long serialVersionUID = 1L;
    // Field errors - using HashMap for serialization
    /**
     * Map of field names to their associated error messages.
     */
    private final HashMap<String, List<String>> fieldErrors = new HashMap<>();
    
    /**
     * Map to store additional details about the validation error.
     */
    private final HashMap<String, Serializable> details = new HashMap<>();
    
    /**
     * Error code for the exception.
     */
    private final String errorCode = "VALIDATION_ERROR";


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
        if (errorCode != null) {
            try {
                // Use reflection to set the final field
                java.lang.reflect.Field field = getClass().getDeclaredField("errorCode");
                field.setAccessible(true);
                field.set(this, errorCode);
            } catch (Exception e) {
                // Fallback to default error code
                System.err.println("Failed to set error code: " + e.getMessage());
            }
        }
        
        if (fieldErrors != null) {
            for (Map.Entry<String, List<String>> entry : fieldErrors.entrySet()) {
                if (entry.getKey() != null && entry.getValue() != null) {
                    this.fieldErrors.put(entry.getKey(), new ArrayList<>(entry.getValue()));
                }
            }
        }
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
     * Gets the details map containing additional information about the error.
     * @return an unmodifiable map of details
     */
    public Map<String, Serializable> getDetails() {
        return Collections.unmodifiableMap(details);
    }
    
    /**
     * Custom serialization method to ensure proper serialization of the exception
     */
    @Serial
    private void writeObject(java.io.ObjectOutputStream out) throws java.io.IOException {
        out.defaultWriteObject();
    }
    
    /**
     * Custom deserialization method to ensure proper deserialization of the exception
     */
    @Serial
    private void readObject(java.io.ObjectInputStream in) throws java.io.IOException, ClassNotFoundException {
        in.defaultReadObject();
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

        //serial version uid for object serialization
        @Serial
        private static final long serialVersionUID = 2L;
        private String message;
        private String errorCode;
        private final Map<String, List<String>> fieldErrors = new LinkedHashMap<>();
        private Throwable cause;
        
        /**
         * Constructor for Builder.
         * Required for serialization support.
         */
        public Builder() {
            // Initialize fields if needed
        }

        /**
         * Adds a field error to the builder.
         *
         * @param field the field name
         * @param error the error message
         */
        public void fieldError(String field, String error) {
            if (field != null && error != null) {
                this.fieldErrors.computeIfAbsent(field, k -> new ArrayList<>()).add(error);
            }
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
