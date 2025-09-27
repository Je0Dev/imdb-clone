package com.papel.imdb_clone.exceptions;

/**
 * Enumerates the different types of authentication and authorization errors.
 */
public enum AuthErrorType {
    /**
     * Invalid credentials provided (username or password incorrect).
     */
    INVALID_CREDENTIALS("Invalid username or password"),
    
    /**
     * User account is disabled or locked.
     */
    ACCOUNT_DISABLED("Account is disabled"),
    
    /**
     * User account is not verified.
     */
    ACCOUNT_NOT_VERIFIED("Account is not verified"),
    
    /**
     * Session has expired due to inactivity.
     */
    SESSION_EXPIRED("Session has expired"),
    
    /**
     * Invalid or malformed authentication token.
     */
    INVALID_TOKEN("Invalid token"),
    
    /**
     * The provided session is invalid or doesn't exist.
     */
    INVALID_SESSION("Invalid or expired session"),
    
    /**
     * User account not found.
     */
    USER_NOT_FOUND("User not found"),
    
    /**
     * User is not authorized to access the requested resource.
     */
    UNAUTHORIZED("Unauthorized access"),
    
    /**
     * User does not have sufficient permissions.
     */
    INSUFFICIENT_PERMISSIONS("Insufficient permissions"),
    
    /**
     * Authentication request is missing required parameters.
     */
    MISSING_PARAMETERS("Missing required parameters"),
    
    /**
     * Rate limit exceeded for authentication attempts.
     */
    RATE_LIMIT_EXCEEDED("Too many attempts, please try again later"),
    
    /**
     * Internal server error during authentication.
     */
    INTERNAL_ERROR("Internal server error"),
    
    /**
     * Account already exists with the provided credentials.
     */
    ACCOUNT_ALREADY_EXISTS("An account with this email or username already exists"),
    
    /**
     * The provided email address is invalid.
     */
    INVALID_EMAIL("Please provide a valid email address"),
    
    /**
     * Password does not meet complexity requirements.
     */
    PASSWORD_POLICY_VIOLATION("Password does not meet complexity requirements"),
    
    /**
     * The operation is not allowed in the current state.
     */
    ILLEGAL_OPERATION("Operation not allowed"),
    
    /**
     * Error occurred during data serialization/deserialization.
     */
    SERIALIZATION_ERROR("Error processing data"),
    
    /**
     * General input/output error during data operations.
     */
    IO_ERROR("Error accessing storage"),
    
    /**
     * Storage system error (e.g., disk full, permissions).
     */
    STORAGE_ERROR("Storage system error"),
    
    /**
     * Insufficient permissions to access the requested resource.
     */
    PERMISSION_DENIED("Insufficient permissions"),
    
    /**
     * Concurrent modification detected (e.g., another process modified the data).
     */
    CONCURRENT_MODIFICATION("Data was modified by another process, please try again"),
    
    /**
     * The requested data was not found.
     */
    DATA_NOT_FOUND("Requested data not found"), DATA_ACCESS_ERROR("Error accessing data");

    private final String defaultMessage;
    
    AuthErrorType(String defaultMessage) {
        this.defaultMessage = defaultMessage;
    }
    
    public String getDefaultMessage() {
        return defaultMessage;
    }
    
    @Override
    public String toString() {
        return name().toLowerCase();
    }
}
