package com.papel.imdb_clone.exceptions;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Unified exception for authentication-related errors.
 * Extends ValidationException to provide detailed error information.
 */
public class AuthException extends ValidationException {

    /**
     * Creates a new AuthException with error type and message.
     */
    private final AuthErrorType errorType;

    public AuthException(AuthErrorType errorType, String message) {
        this(errorType, message, null, null);
    }

    /**
     * Creates a new AuthException with error type, message, and cause.
     */
    public AuthException(AuthErrorType errorType, String message, Throwable cause) {
        this(errorType, message, null, cause);
    }

    /**
     * Creates a new AuthException with error type, message, field errors, and cause.
     */
    public AuthException(AuthErrorType errorType, String message,
                         Map<String, List<String>> fieldErrors, Throwable cause) {
        super(message, errorType != null ? errorType.name() : "AUTH_ERROR", fieldErrors, cause);
        this.errorType = errorType != null ? errorType : AuthErrorType.INTERNAL_ERROR;
        super.addDetail("errorType", this.errorType);
    }

    public AuthException(String userAndPasswordAreRequired) {
        super(userAndPasswordAreRequired, "AUTH_ERROR", null, null);
        this.errorType = AuthErrorType.INVALID_INPUT;
    }
    
    /**
     * Gets the error type of this exception
     * @return The AuthErrorType associated with this exception
     */
    public AuthErrorType getErrorType() {
        return errorType;
    }


    /**
     * Builder for AuthException.
     */
    public static class Builder extends ValidationException.Builder {
        private AuthErrorType errorType;
        private String message;
        private final Map<String, List<String>> fieldErrors = new HashMap<>();
        private Throwable cause;


        //add field error
        public Builder fieldError(String field, String error) {
            this.fieldErrors.computeIfAbsent(field, k -> new java.util.ArrayList<>()).add(error);
            return this;
        }


        //build AuthException
        public AuthException build() {
            if (errorType == null) {
                throw new IllegalStateException("errorType is required");
            }
            if (message == null) {
                message = errorType.name();
            }
            return new AuthException(errorType, message, fieldErrors, cause);
        }
    }

    /**
     * Enum representing different types of authentication errors
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
        SESSION_EXPIRED("Your session has expired. Please log in again"),
        UNAUTHORIZED_ACCESS("You do not have permission to access this resource"),
        ACCOUNT_LOCKED("Account is locked. Please contact support"),
        TOKEN_EXPIRED("The authentication token has expired"),
        TOKEN_INVALID("The authentication token is invalid");

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
