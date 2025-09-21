package com.papel.imdb_clone.exceptions;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Exception thrown when authentication or authorization fails.
 * Provides detailed error information through the AuthErrorType enum.
 */
public class AuthException extends ValidationException {
    private static final long serialVersionUID = 2L;
    private static final Logger logger = LoggerFactory.getLogger(AuthException.class);
    
    private final AuthErrorType errorType;
    
    public AuthException(AuthErrorType errorType, String message) {
        this(errorType, message, null, null);
    }
    
    public AuthException(AuthErrorType errorType, String message, Throwable cause) {
        this(errorType, message, null, cause);
    }
    
    public AuthException(AuthErrorType errorType, String message, Map<String, List<String>> fieldErrors) {
        this(errorType, message, fieldErrors, null);
    }
    
    public AuthException(AuthErrorType errorType, String message, 
                        Map<String, List<String>> fieldErrors, Throwable cause) {
        super(message, errorType != null ? errorType.name() : "AUTH_ERROR", 
              fieldErrors != null ? new HashMap<>(fieldErrors) : null, cause);
        this.errorType = errorType != null ? errorType : AuthErrorType.INTERNAL_ERROR;
        addDetail("errorType", this.errorType.name());
        logger.error("Authentication error: {}", message, cause);
    }
    
    public AuthErrorType getErrorType() {
        return errorType;
    }



    /**
     * Builder for AuthException.
     * Provides a fluent API for constructing AuthException instances.
     */
    public static class Builder {
        private AuthErrorType errorType;
        private String message;
        private final Map<String, List<String>> fieldErrors = new HashMap<>();
        private Throwable cause;

        /**
         * Sets the error type.
         */
        public Builder errorType(AuthErrorType errorType) {
            this.errorType = errorType;
            return this;
        }

        /**
         * Sets the error message.
         */
        public Builder message(String message) {
            this.message = message;
            return this;
        }

        /**
         * Adds a field error.
         */
        public Builder fieldError(String field, String error) {
            this.fieldErrors.computeIfAbsent(field, k -> new ArrayList<>()).add(error);
            return this;
        }

        /**
         * Adds multiple field errors.
         */
        public Builder fieldErrors(String field, List<String> errors) {
            if (errors != null && !errors.isEmpty()) {
                this.fieldErrors.computeIfAbsent(field, k -> new ArrayList<>()).addAll(errors);
            }
            return this;
        }

        /**
         * Sets the cause of the exception.
         */
        public Builder cause(Throwable cause) {
            this.cause = cause;
            return this;
        }

        /**
         * Builds the AuthException instance.
         *
         * @return a new AuthException instance
         * @throws IllegalStateException if errorType is not set
         */
        public AuthException build() {
            if (errorType == null) {
                throw new IllegalStateException("errorType is required");
            }
            if (message == null) {
                message = errorType.getDefaultMessage();
            }
            
            if (fieldErrors.isEmpty() && cause == null) {
                return new AuthException(errorType, message);
            } else if (cause == null) {
                return new AuthException(errorType, message, fieldErrors);
            } else {
                AuthException ex = new AuthException(errorType, message, cause);
                fieldErrors.forEach((field, errors) -> 
                    errors.forEach(error -> ex.addFieldError(field, error)));
                return ex;
            }
        }
    }

    private void addFieldError(String field, String error) {
        Map<String, List<String>> currentErrors = new HashMap<>(getFieldErrors());
        currentErrors.computeIfAbsent(field, k -> new ArrayList<>()).add(error);
        addDetail(field, error);
        
        // Create a new AuthException with updated fields
        AuthException updated = new AuthException(
            getErrorType(), 
            getMessage(), 
            currentErrors, 
            getCause()
        );
        
        // Copy all existing details to the new exception
        getFieldErrors().forEach((f, errs) -> 
            errs.forEach(e -> updated.addDetail(f, e)));
        
        throw updated;
    }

    /**
     * Enum representing different types of typical authentication errors
     */
    public enum AuthErrorType {
        INVALID_CREDENTIALS("Invalid username or password"),
        INVALID_PASSWORD("Password does not meet requirements"),
        PASSWORD_MISMATCH("Passwords do not match"),
        USER_ALREADY_EXISTS("User with this email already exists"),
        INTERNAL_ERROR("An internal authentication error occurred"),
        USERNAME_EXISTS("Username already exists"),
        EMAIL_EXISTS("Email already registered"),
        REGISTRATION_FAILED("Registration failed"),
        INVALID_INPUT("Invalid input provided"),
        INVALID_EMAIL("Invalid email format");
        private final String defaultMessage;

        /**
         * Constructor with default error message
         * @param defaultMessage The default error message for this error type
         */
        AuthErrorType(String defaultMessage) {
            this.defaultMessage = defaultMessage;
        }

        /**
         * Gets the default error message for this error type
         * @return The default error message
         */
        public String getDefaultMessage() {
            return defaultMessage;
        }

    }
}
