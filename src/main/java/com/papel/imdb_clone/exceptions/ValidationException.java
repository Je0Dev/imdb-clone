package com.papel.imdb_clone.exceptions;

import java.util.*;

/**
 * Unified exception for validation errors throughout the application.
 * Supports both simple error messages and field-level validation errors.
 */
public class ValidationException extends RuntimeException {
    public final Map<String, List<String>> fieldErrors = new HashMap<>();
    private final String errorCode;
    private final Map<String, Object> details = new HashMap<>();

    /**
     * Creates a new ValidationException with a single error message.
     */
    public ValidationException(String message) {
        this(message, null, null, null);
    }

    /**
     * Creates a new ValidationException with a message and error code.
     */
    public ValidationException(String message, String errorCode) {
        this(message, errorCode, null, null);
    }

    /**
     * Creates a new ValidationException with field errors.
     */
    public ValidationException(String message, Map<String, List<String>> fieldErrors) {
        this(message, null, fieldErrors, null);
    }

    /**
     * Creates a new ValidationException with a cause.
     */
    public ValidationException(String message, Throwable cause) {
        this(message, null, null, cause);
    }

    /**
     * Creates a new ValidationException with all possible parameters.
     */
    public ValidationException(String message, String errorCode,
                               Map<String, List<String>> fieldErrors,
                               Throwable cause) {
        super(message, cause);
        this.errorCode = errorCode;
        if (fieldErrors != null) {
            this.fieldErrors.putAll(fieldErrors);
        }
    }

    /**
     * Adds a field error.
     */
    public void addFieldError(String field, String error) {
        fieldErrors.computeIfAbsent(field, k -> new ArrayList<>()).add(error);
    }

    /**
     * Adds multiple errors for a single field.
     */
    public void addFieldErrors(String field, List<String> errors) {
        fieldErrors.computeIfAbsent(field, k -> new ArrayList<>()).addAll(errors);
    }

    /**
     * Adds a detail to the exception.
     */
    public void addDetail(String key, Object value) {
        details.put(key, value);
    }

    /**
     * Gets the error code.
     */
    public String getErrorCode() {
        return errorCode;
    }

    /**
     * Gets all field errors.
     */
    public Map<String, List<String>> getFieldErrors() {
        return Collections.unmodifiableMap(fieldErrors);
    }

    /**
     * Gets all details.
     */
    public Map<String, Object> getDetails() {
        return Collections.unmodifiableMap(details);
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
        private String message;
        private String errorCode;
        private final Map<String, List<String>> fieldErrors = new HashMap<>();
        private Throwable cause;

        public Builder message(String message) {
            this.message = message;
            return this;
        }

        public Builder errorCode(String errorCode) {
            this.errorCode = errorCode;
            return this;
        }

        public Builder fieldError(String field, String error) {
            this.fieldErrors.computeIfAbsent(field, k -> new ArrayList<>()).add(error);
            return this;
        }

        public Builder fieldErrors(String field, List<String> errors) {
            this.fieldErrors.computeIfAbsent(field, k -> new ArrayList<>()).addAll(errors);
            return this;
        }

        public Builder cause(Throwable cause) {
            this.cause = cause;
            return this;
        }

        public ValidationException build() {
            return new ValidationException(message, errorCode, fieldErrors, cause);
        }
    }
}
