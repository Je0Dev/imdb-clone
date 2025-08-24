package com.papel.imdb_clone.exceptions;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Unified exception for authentication-related errors.
 * Extends ValidationException to provide detailed error information.
 */
public class AuthException extends ValidationException {
    private AuthErrorType errorType;

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
        this.errorType = errorType != null ? errorType : AuthErrorType.INTERNAL_ERROR;
        super.addDetail("errorType", this.errorType);
    }

    public AuthException(String userAndPasswordAreRequired) {
        super(userAndPasswordAreRequired, "AUTH_ERROR", null, null);
    }

    /**
     * Creates a new builder for AuthException.
     */
    public static ValidationException.Builder builder() {
        return new Builder();
    }


    /**
     * Builder for AuthException.
     */
    public static class Builder extends ValidationException.Builder {
        private AuthErrorType errorType;
        private String message;
        private final Map<String, List<String>> fieldErrors = new HashMap<>();
        private Throwable cause;

        public Builder errorType(AuthErrorType errorType) {
            this.errorType = errorType;
            return this;
        }

        public Builder message(String message) {
            this.message = message;
            return this;
        }

        public Builder fieldError(String field, String error) {
            this.fieldErrors.computeIfAbsent(field, k -> new java.util.ArrayList<>()).add(error);
            return this;
        }

        public Builder cause(Throwable cause) {
            this.cause = cause;
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
        INVALID_EMAIL("Invalid email format"),
        USER_ALREADY_EXISTS("User with this email already exists"),
        WEAK_PASSWORD("Password does not meet requirements"),
        ACCOUNT_LOCKED("Account is locked"),
        ACCOUNT_DISABLED("Account is disabled"),
        CREDENTIALS_EXPIRED("Credentials have expired"),
        SESSION_EXPIRED("Session has expired"),
        INVALID_TOKEN("Invalid or expired token"),
        INSUFFICIENT_PERMISSIONS("Insufficient permissions"),
        INTERNAL_ERROR("An internal authentication error occurred"),
        USERNAME_EXISTS("Username already exists"),
        EMAIL_EXISTS("Email already registered"),
        REGISTRATION_FAILED("Registration failed"),
        INVALID_INPUT("User and password are required"),
        USER_NOT_FOUND("User not found"),
        USER_DISABLED("User is disabled"),
        USER_LOCKED("User is locked"),
        USER_EXPIRED("User's credentials have expired"),
        USER_CREDENTIALS_EXPIRED("User's credentials have expired"),
        USER_SESSION_EXPIRED("User's session has expired"),
        USER_INVALID_TOKEN("User's token is invalid or expired"),
        USER_INSUFFICIENT_PERMISSIONS("User does not have sufficient permissions"),
        USER_ACCOUNT_DISABLED("User's account is disabled"),
        USER_ACCOUNT_LOCKED("User's account is locked"),
        USER_ACCOUNT_EXPIRED("User's account has expired"),
        USER_ACCOUNT_CREDENTIALS_EXPIRED("User's account credentials have expired"),
        USER_ACCOUNT_SESSION_EXPIRED("User's account session has expired"),
        USER_ACCOUNT_INVALID_TOKEN("User's account token is invalid or expired");

        private final String defaultMessage;

        AuthErrorType(String defaultMessage) {
            this.defaultMessage = defaultMessage;
        }

    }
}
