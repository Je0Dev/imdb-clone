package com.papel.imdb_clone.service.validation;

import com.papel.imdb_clone.exceptions.ValidationException;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

/**
 * Service for validating user input with detailed error messages.
 */
public class UserInputValidator {
    // Constants for validation patterns
    private static final Pattern EMAIL_PATTERN = Pattern.compile("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.\\w+$");
    private static final Pattern USERNAME_PATTERN = Pattern.compile("^[a-zA-Z0-9_.]{3,20}$");
    private static final Pattern PASSWORD_PATTERN = Pattern.compile("^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=.*[@#$%^&+=])(?=\\S+$).{8,}$");


    /**
     * Validates user registration input.
     */
    public void validateRegistration(String firstName, String lastName, String username,
                                     String email, String password, String confirmPassword) {
        Map<String, String> errors = new HashMap<>();

        // Basic field presence validation
        if (firstName == null || firstName.trim().isEmpty()) {
            errors.put("firstName", "First name is required");
        }

        if (lastName == null || lastName.trim().isEmpty()) {
            errors.put("lastName", "Last name is required");
        }

        // Username validation
        if (username == null || username.trim().isEmpty()) {
            errors.put("username", "Username is required");
        } else if (isValidUsername(username)) {
            errors.put("username", "Username must be 3-20 characters long and can only contain letters, numbers, dots and underscores");
        }

        // Email validation
        if (email == null || email.trim().isEmpty()) {
            errors.put("email", "Email is required");
        } else if (isValidEmail(email)) {
            errors.put("email", "Please enter a valid email address");
        }

        // Password validation
        if (password == null || password.isEmpty()) {
            errors.put("password", "Password is required");
        } else if (!isValidPassword(password)) {
            errors.put("password", "Password must be at least 8 characters long and include uppercase, lowercase, number and special character");
        }

        // Confirm password validation
        assert password != null;
        if (!password.equals(confirmPassword)) {
            errors.put("confirmPassword", "Passwords do not match");
        }

        if (!errors.isEmpty()) {
            ValidationException.Builder builder = ValidationException.builder()
                    .message("Validation failed");
            
            // Add all field errors to the exception
            errors.forEach(builder::fieldError);
            
            throw builder.build();
        }
    }

    /**
     * Validates user login input.
     */
    public void validateLogin(String usernameOrEmail, String password) {
        Map<String, String> errors = new HashMap<>();

        if (usernameOrEmail == null || usernameOrEmail.trim().isEmpty()) {
            errors.put("usernameOrEmail", "Username or email is required");
        }

        if (password == null || password.isEmpty()) {
            errors.put("password", "Password is required");
        }

        if (!errors.isEmpty()) {
            ValidationException.Builder builder = ValidationException.builder()
                    .message("Login validation failed");
            
            // Add all field errors to the exception
            errors.forEach(builder::fieldError);
            
            throw builder.build();
        }
    }

    public boolean isValidUsername(String username) {
        return username != null && USERNAME_PATTERN.matcher(username).matches();
    }

    public boolean isValidEmail(String email) {
        return email != null && EMAIL_PATTERN.matcher(email).matches();
    }

    public boolean isValidPassword(String password) {
        return password != null && PASSWORD_PATTERN.matcher(password).matches();
    }

    public int calculatePasswordStrength(String password) {
        if (password == null || password.isEmpty()) {
            return 0;
        }

        int strength = 0;
        if (password.length() >= 8) strength++;
        if (password.matches(".*[A-Z].*")) strength++;
        if (password.matches(".*[a-z].*")) strength++;
        if (password.matches(".*[0-9].*")) strength++;
        if (password.matches(".*[^A-Za-z0-9].*")) strength++;

        return strength;
    }
}
