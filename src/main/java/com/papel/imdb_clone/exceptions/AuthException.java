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
        AuthErrorType errorType1 = errorType != null ? errorType : AuthErrorType.INTERNAL_ERROR;
        super.addDetail("errorType", errorType1);
    }

    public AuthException(String userAndPasswordAreRequired) {
        super(userAndPasswordAreRequired, "AUTH_ERROR", null, null);
    }


    /**
     * Builder for AuthException.
     */
    public static class Builder extends ValidationException.Builder {
        private AuthErrorType errorType;
        private String message;
        private final Map<String, List<String>> fieldErrors = new HashMap<>();
        private Throwable cause;


        public Builder fieldError(String field, String error) {
            this.fieldErrors.computeIfAbsent(field, k -> new java.util.ArrayList<>()).add(error);
            return this;
        }


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

    public enum AuthErrorType {
        INVALID_CREDENTIALS("Invalid username or password"),
        USER_ALREADY_EXISTS("User with this email already exists"),
        INTERNAL_ERROR("An internal authentication error occurred"),
        USERNAME_EXISTS("Username already exists"),
        EMAIL_EXISTS("Email already registered"),
        REGISTRATION_FAILED("Registration failed"),
        INVALID_INPUT("User and password are required");

        private final String defaultMessage;

        AuthErrorType(String defaultMessage) {
            this.defaultMessage = defaultMessage;
        }

    }
}
