package com.papel.imdb_clone.service;

import com.papel.imdb_clone.exceptions.AuthException;
import com.papel.imdb_clone.model.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Service responsible for user authentication and session management.
 * Handles user registration, login, logout, and session validation.
 * Uses unified AuthException for all authentication-related errors.
 */
public class AuthService {
    private static final Logger logger = LoggerFactory.getLogger(AuthService.class);
    private static AuthService instance;
    private User admin;

    // User storage
    private Map<String, User> usersByUsername = new HashMap<>();
    private Map<String, User> usersByEmail = new HashMap<>();
    private final Map<String, String> userSessions = new HashMap<>();

    // Dependencies
    private final UserStorageService userStorageService;
    private final EncryptionService encryptionService;

    private AuthService(User user) {
        this.userStorageService = UserStorageService.getInstance();
        this.encryptionService = EncryptionService.getInstance();
        logger.info("Initializing AuthService...");

        try {
            loadUsers();
            logger.info("Initial user load complete. User count: {}", usersByUsername.size());
            if (user != null) {
                ensureAdminUserExists(user);
            } else if (usersByUsername.isEmpty()) {

                // Create a default admin user if none exists
                User defaultAdmin = new User("Admin", "User", "admin", 'M', "admin@imdbclone.com");
                String hashedPassword = encryptionService.hashPassword("admin123");
                defaultAdmin.setPassword(hashedPassword);
                usersByUsername.put(defaultAdmin.getUsername(), defaultAdmin);
                usersByEmail.put(defaultAdmin.getEmail(), defaultAdmin);
                saveUsers();
                logger.info("Created default admin user");
            }
        } catch (Exception e) {
            logger.error("Failed to initialize AuthService: {}", e.getMessage(), e);
            throw new AuthException(AuthException.AuthErrorType.INTERNAL_ERROR,
                    "Failed to initialize AuthService", e);
        }
    }

    public static synchronized AuthService getInstance() {
        if (instance == null) {
            instance = new AuthService(null);
        }
        return instance;
    }


    /**
     * Registers a new user with the system.
     *
     * @param user     The user to register
     * @param password The plaintext password
     * @throws AuthException if registration fails (username/email exists, invalid input, etc.)
     */
    public void register(User user, String password) {
        if (user == null || password == null || password.trim().isEmpty()) {
            throw new AuthException(
                    AuthException.AuthErrorType.INVALID_INPUT,
                    "User and password are required"
            );
        }

        if (usersByUsername.containsKey(user.getUsername())) {
            throw new AuthException(
                    AuthException.AuthErrorType.USERNAME_EXISTS,
                    "Username already exists"
            );
        }

        if (usersByEmail.containsKey(user.getEmail())) {
            throw new AuthException(
                    AuthException.AuthErrorType.EMAIL_EXISTS,
                    "Email already registered"
            );
        }

        try {
            String hashedPassword = encryptionService.hashPassword(password);
            user.setPassword(hashedPassword);
            usersByUsername.put(user.getUsername(), user);
            usersByEmail.put(user.getEmail(), user);
            saveUsers();
            logger.info("User registered: {}", user.getUsername());
        } catch (Exception e) {
            logger.error("Registration failed for user {}: {}", user.getUsername(), e.getMessage(), e);
            throw new AuthException(
                    AuthException.AuthErrorType.REGISTRATION_FAILED,
                    "Registration failed: " + e.getMessage(), e
            );
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
            throw new AuthException(
                    AuthException.AuthErrorType.INVALID_CREDENTIALS,
                    "Invalid username or password"
            );
        }

        try {
            if (!encryptionService.verifyPassword(password, user.getPassword())) {
                throw new AuthException(
                        AuthException.AuthErrorType.INVALID_CREDENTIALS,
                        "Invalid username or password"
                );
            }

            // Generate session token
            String sessionToken = UUID.randomUUID().toString();
            userSessions.put(sessionToken, user.getUsername());
            logger.info("User logged in: {}", username);
            return sessionToken;
        } catch (Exception e) {
            logger.error("Login failed for user {}: {}", username, e.getMessage(), e);
            throw new AuthException(
                    AuthException.AuthErrorType.INTERNAL_ERROR,
                    "Login failed due to an internal error"
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
     * Ensures that an admin user exists in the system.
     * Creates one with default credentials if none exists.
     */
    private void ensureAdminUserExists(User adminUser) {
        String adminUsername = "admin";
        if (!usersByUsername.containsKey(adminUsername)) {
            try {
                // Use the provided admin user or create a default one
                User admin = (adminUser != null) ? adminUser :
                        new User("Admin", "User", adminUsername, 'M', "admin@imdbclone.com");

                String hashedPassword = encryptionService.hashPassword("admin123");
                admin.setPassword(hashedPassword);
                usersByUsername.put(adminUsername, admin);
                usersByEmail.put(admin.getEmail(), admin);
                saveUsers();
                logger.info("Created admin user: {}", adminUsername);
            } catch (Exception e) {
                logger.error("Failed to create admin user: {}", e.getMessage(), e);
                throw new AuthException(
                        AuthException.AuthErrorType.INTERNAL_ERROR,
                        "Failed to create admin user",
                        e
                );
            }
        }
    }

    /**
     * Loads users from the user storage service.
     */
    private void loadUsers() {
        try {
            Map<String, User>[] userMaps = userStorageService.loadUsers();
            if (userMaps != null && userMaps.length >= 2) {
                usersByUsername = userMaps[0];
                usersByEmail = userMaps[1];
                logger.debug("Loaded {} users from storage", usersByUsername.size());
            } else {
                usersByUsername = new HashMap<>();
                usersByEmail = new HashMap<>();
                logger.debug("No users found in storage, initialized empty user maps");
            }
        } catch (Exception e) {
            logger.error("Failed to load users: {}", e.getMessage(), e);
            throw new AuthException(AuthException.AuthErrorType.INTERNAL_ERROR,
                    "Failed to load user data", e);
        }
    }

    /**
     * Saves all users to persistent storage.
     */
    private void saveUsers() {
        try {
            userStorageService.saveUsers(usersByUsername, usersByEmail);
        } catch (Exception e) {
            logger.error("Failed to save users: {}", e.getMessage(), e);
            throw new AuthException(AuthException.AuthErrorType.INTERNAL_ERROR,
                    "Failed to save user data", e);
        }
    }

    public void initiatePasswordReset(String username) {
        if (username == null || username.trim().isEmpty()) {
            throw new IllegalArgumentException("Username cannot be empty");
        }
        User user = usersByUsername.get(username);
        if (user != null) {
            // Generate and store a password reset token
            String resetToken = UUID.randomUUID().toString();
            logger.info("Password reset token for user {}: {}", username, resetToken);
        }
    }
}
