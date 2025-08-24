package com.papel.imdb_clone.exceptions;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Exception thrown when attempting to create a user that already exists in the system.
 * This is a specific type of AuthException for user registration conflicts.
 */
public class UserAlreadyExistsException extends AuthException {
    private static final String DEFAULT_FIELD = "email";

    /**
     * Creates a new UserAlreadyExistsException with the default message.
     */
    public UserAlreadyExistsException() {
        this(null);
    }

    /**
     * Creates a new UserAlreadyExistsException with a custom message.
     *
     * @param message the detail message
     */
    public UserAlreadyExistsException(String message) {
        this(message, null);
    }

    /**
     * Creates a new UserAlreadyExistsException for a specific field and value.
     *
     * @param field the field that caused the conflict (e.g., "email", "username")
     * @param value the value that caused the conflict
     */
    public UserAlreadyExistsException(String field, Object value) {
        this(createDefaultMessage(field, value), field, value);
    }

    /**
     * Creates a new UserAlreadyExistsException with a custom message and field details.
     *
     * @param message the detail message
     * @param field   the field that caused the conflict
     * @param value   the value that caused the conflict
     */
    public UserAlreadyExistsException(String message, String field, Object value) {
        this(message, field, value, null);
    }

    /**
     * Creates a new UserAlreadyExistsException with a custom message, field details, and cause.
     *
     * @param message the detail message
     * @param field   the field that caused the conflict
     * @param value   the value that caused the conflict
     * @param cause   the cause of the exception
     */
    public UserAlreadyExistsException(String message, String field, Object value, Throwable cause) {
        super(
                AuthErrorType.USER_ALREADY_EXISTS,
                message != null ? message : createDefaultMessage(field, value),
                createFieldErrors(field, value),
                cause
        );
    }

    /**
     * Creates a new UserAlreadyExistsException for an email address.
     *
     * @param email the email that already exists
     * @return a new UserAlreadyExistsException instance
     */
    public static UserAlreadyExistsException forEmail(String email) {
        return new UserAlreadyExistsException(
                String.format("User with email '%s' already exists", email),
                "email",
                email
        );
    }

    /**
     * Creates a new UserAlreadyExistsException for a username.
     *
     * @param username the username that already exists
     * @return a new UserAlreadyExistsException instance
     */
    public static UserAlreadyExistsException forUsername(String username) {
        return new UserAlreadyExistsException(
                String.format("Username '%s' is already taken", username),
                "username",
                username
        );
    }

    private static String createDefaultMessage(String field, Object value) {
        String fieldName = field != null ? field : DEFAULT_FIELD;
        return String.format("User with %s '%s' already exists", fieldName, value);
    }

    private static Map<String, List<String>> createFieldErrors(String field, Object value) {
        String fieldName = field != null ? field : DEFAULT_FIELD;
        Map<String, List<String>> errors = new HashMap<>();
        errors.put(fieldName, Collections.singletonList("already exists"));
        return errors;
    }
}
