package com.papel.imdb_clone.exceptions;

import java.util.*;

/**
 * Unified exception for validation errors throughout the application.
 * Supports both simple error messages and field-level validation errors.
 */
public class ValidationException extends RuntimeException {
    //field errors
    public final Map<String, List<String>> fieldErrors = new HashMap<>();
    private final String errorCode;
    private final Map<String, Object> details = new HashMap<>();


    /**
     * Creates a new ValidationException with all possible parameters.
     */
    public ValidationException(String message, String errorCode,
                               Map<String, List<String>> fieldErrors,
                               Throwable cause) {
        super(message, cause);
        //set error code
        this.errorCode = errorCode;
        if (fieldErrors != null) {
            this.fieldErrors.putAll(fieldErrors);
        }
    }

    /**
     * Adds a detail to the exception.
     */
    public void addDetail(String key, Object value) {
        details.put(key, value);
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
     * Builder for ValidationException.
     */
    public static class Builder {
        public String message;
        private String errorCode;
        private final Map<String, List<String>> fieldErrors = new HashMap<>();
        private Throwable cause;

        //add field error to builder
        public Builder fieldError(String field, String error) {
            this.fieldErrors.computeIfAbsent(field, k -> new ArrayList<>()).add(error);
            return this;
        }


        public ValidationException build() {
            return new ValidationException(message, errorCode, fieldErrors, cause);
        }

        //set message
        public Builder message(String validationFailed) {
            this.message = validationFailed;
            return this;
        }
    }
}
