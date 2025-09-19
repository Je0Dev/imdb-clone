package com.papel.imdb_clone.service.validation;

import com.papel.imdb_clone.exceptions.AuthException;
import com.papel.imdb_clone.model.people.User;
import com.papel.imdb_clone.service.people.UserStorageService;
import com.papel.imdb_clone.util.PasswordHasher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Service responsible for user authentication and session management.
 * Handles user registration, login, logout, and session validation.
 * Uses unified AuthException for all authentication-related errors.
 */
public class AuthService {
    private static final Logger logger = LoggerFactory.getLogger(AuthService.class);
    private static final String USERS_UPDATED_FILE = "src/main/resources/data/people/users_updated.txt";
    private static AuthService instance;
    private User admin;

    // User storage
    private final Map<String, User> usersByUsername = new HashMap<>();
    
    /**
     * Gets the map of usernames to User objects.
     * @return A map of usernames to User objects
     */
    public Map<String, User> getUsersByUsername() {
        return usersByUsername;
    }
    private final Map<String, User> usersByEmail = new HashMap<>();
    private final Map<String, String> userSessions = new ConcurrentHashMap<>();
    private int nextUserId = 1;

    // Dependencies
    private final UserStorageService userStorageService;
    
    private AuthService() {
        this.userStorageService = UserStorageService.getInstance();
        logger.info("Initializing AuthService...");
        loadUsers();
    }

    public static synchronized AuthService getInstance() {
        if (instance == null) {
            instance = new AuthService();
        }
        return instance;
    }

    /**
     * Loads users from the users_updated.txt file.
     */
    private void loadUsersFromTextFile() {
        try (BufferedReader reader = new BufferedReader(new FileReader(USERS_UPDATED_FILE))) {
            String line;
            boolean isFirstLine = true;
            
            while ((line = reader.readLine()) != null) {
                // Skip empty lines and comments
                if (line.trim().isEmpty() || line.trim().startsWith("#")) {
                    continue;
                }
                
                // Skip header line
                if (isFirstLine && line.contains("id,username,email,password,fullName")) {
                    isFirstLine = false;
                    continue;
                }
                
                String[] parts = line.split(",");
                if (parts.length >= 5) {
                    try {
                        int id = Integer.parseInt(parts[0].trim());
                        String username = parts[1].trim();
                        String email = parts[2].trim();
                        String password = parts[3].trim();
                        String[] nameParts = parts[4].trim().split("\\s+", 2);
                        String firstName = nameParts.length > 0 ? nameParts[0] : "";
                        String lastName = nameParts.length > 1 ? nameParts[1] : "";
                        
                        // Create and add user
                        User user = new User(firstName, lastName, username, ' ', email);
                        user.setId(id);
                        // Hash the password when loading from text file
                        user.setPassword(PasswordHasher.hashPassword(password));
                        
                        usersByUsername.put(username, user);
                        usersByEmail.put(email, user);
                        
                        // Update nextUserId if needed
                        if (id >= nextUserId) {
                            nextUserId = id + 1;
                        }
                        
                        logger.info("Loaded user: {} (ID: {})", username, id);
                    } catch (Exception e) {
                        logger.warn("Error parsing user line: {}", line, e);
                    }
                }
            }
            
            logger.info("Loaded {} users from text file", usersByUsername.size());
            
            // Save the loaded users to the serialized file for next time
            if (!usersByUsername.isEmpty()) {
                saveUsers();
            }
            
        } catch (IOException e) {
            logger.error("Error loading users from text file", e);
        }
    }
    
    /**
     * Loads users from the users_updated.txt file.
     */
    private void loadUsers() {
        // Clear existing users
        usersByUsername.clear();
        usersByEmail.clear();
        nextUserId = 1;
        
        // Always load from text file to ensure we have the latest data
        logger.info("Loading users from text file...");
        loadUsersFromTextFile();
        
        // If no users were loaded, create a default admin user
        if (usersByUsername.isEmpty()) {
            logger.info("No users found in text file, creating default admin user");
            createDefaultAdminUser();
        }
        
        logger.info("Successfully loaded {} users", usersByUsername.size());
    }
    
    /**
     * Creates a default admin user if no users exist.
     */
    private void createDefaultAdminUser() {
        try {
            User admin = new User("Admin", "User", "admin", 'M', "admin@imdbclone.com");
            admin.setPassword(PasswordHasher.hashPassword("admin123"));
            admin.setId(nextUserId++);
            
            usersByUsername.put(admin.getUsername(), admin);
            usersByEmail.put(admin.getEmail(), admin);
            
            // Save the new admin user
            saveUsers();
            logger.info("Created default admin user: {}", admin.getUsername());
        } catch (Exception e) {
            logger.error("Failed to create default admin user: {}", e.getMessage(), e);
        }
    }

    /**
     * Saves the current user data to storage.
     * This method is public to allow for administrative tasks.
     */
    public void saveUsers() {
        try {
            userStorageService.saveUsers(usersByUsername, usersByEmail);
            logger.info("Saved {} users to storage", usersByUsername.size());
        } catch (Exception e) {
            logger.error("Failed to save users: {}", e.getMessage(), e);
        }
    }

    /**
     * Authenticates a user and creates a new session.
     *
     * @param username The username of the user
     * @param password The plaintext password
     * @return Session token
     * @throws AuthException if authentication fails
     */
    public String login(String username, String password) throws AuthException {
        if (username == null || username.trim().isEmpty() || password == null || password.trim().isEmpty()) {
            throw new AuthException(
                    AuthException.AuthErrorType.INVALID_CREDENTIALS,
                    "Username and password are required"
            );
        }

        User user = usersByUsername.get(username);
        if (user == null) {
            logger.warn("Login failed: User not found - {}", username);
            throw new AuthException(
                    AuthException.AuthErrorType.INVALID_CREDENTIALS,
                    "Invalid username or password"
            );
        }
        
        // Verify password using secure hashing
        String storedPassword = user.getPassword();
        boolean passwordMatches = com.papel.imdb_clone.util.PasswordHasher.verifyPassword(password, storedPassword);
        
        if (!passwordMatches) {
            logger.warn("Login failed: Incorrect password for user - {}", username);
            throw new AuthException(
                    AuthException.AuthErrorType.INVALID_CREDENTIALS,
                    "Invalid username or password"
            );
        }

        try {
            // Generate session token
            String sessionToken = UUID.randomUUID().toString();
            userSessions.put(sessionToken, user.getUsername());
            logger.info("User logged in successfully: {}", username);
            return sessionToken;
        } catch (Exception e) {
            logger.error("Error during login for user {}: {}", username, e.getMessage(), e);
            throw new AuthException(
                    AuthException.AuthErrorType.INTERNAL_ERROR,
                    "An error occurred during login",
                    e
            );
        }
    }

    /**
     * Gets the currently logged-in user for a session token.
     *
     * @param sessionToken The session token
     * @return The User object if the session is valid, null otherwise
     */
    public User getCurrentUser(String sessionToken) {
        if (sessionToken == null) {
            return null;
        }
        String username = userSessions.get(sessionToken);
        return username != null ? usersByUsername.get(username) : null;
    }

    /**
     * Gets the currently logged-in user.
     *
     * @return The User object if the session is valid, null otherwise
     */
    public User getCurrentUser() {
        return getCurrentUser(null);
    }

    /**
     * Registers a new user with the system.
     *
     * @param user The user to register
     * @param password The plaintext password
     * @param confirmPassword The password confirmation
     * @throws AuthException if registration fails
     */
    public void register(User user, String password, String confirmPassword) throws AuthException {
        if (user == null || password == null || password.trim().isEmpty()) {
            throw new AuthException(
                    AuthException.AuthErrorType.INVALID_INPUT,
                    "User and password are required"
            );
        }

        // Password confirmation check
        if (!password.equals(confirmPassword)) {
            throw new AuthException(
                    AuthException.AuthErrorType.PASSWORD_MISMATCH,
                    "Passwords do not match"
            );
        }

        // Check if username already exists
        if (usersByUsername.containsKey(user.getUsername())) {
            throw new AuthException(
                    AuthException.AuthErrorType.USERNAME_EXISTS,
                    "Username already exists"
            );
        }

        // Check if email already exists
        if (usersByEmail.containsKey(user.getEmail())) {
            throw new AuthException(
                    AuthException.AuthErrorType.EMAIL_EXISTS,
                    "Email already registered"
            );
        }

        try {
            // Set the user's password and add to storage
            user.setPassword(password);
            user.setId(nextUserId++);
            usersByUsername.put(user.getUsername(), user);
            usersByEmail.put(user.getEmail(), user);
            
            // Save the updated user list
            saveUsers();
            
            logger.info("User registered successfully: {}", user.getUsername());
        } catch (Exception e) {
            logger.error("Registration failed for user {}: {}", user.getUsername(), e.getMessage(), e);
            throw new AuthException(
                    AuthException.AuthErrorType.REGISTRATION_FAILED,
                    "Failed to register user: " + e.getMessage(),
                    e
            );
        }
    }
}
