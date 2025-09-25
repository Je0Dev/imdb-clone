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

                //split the line into parts
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
            // Create a new admin user
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
     * Authenticates a user and creates a new session with detailed logging.
     *
     * @param username The username of the user
     * @param password The plaintext password
     * @return Session token
     * @throws AuthException if authentication fails
     */
    public String login(String username, String password) throws AuthException {
        logger.debug("Attempting login for user: {}", username);
        
        // Input validation
        if (username == null || username.trim().isEmpty()) {
            logger.warn("Login failed: Username is empty");
            throw new AuthException(
                    AuthException.AuthErrorType.INVALID_CREDENTIALS,
                    "Username is required"
            );
        }
        
        if (password == null || password.trim().isEmpty()) {
            logger.warn("Login failed: Password is empty for user: {}", username);
            throw new AuthException(
                    AuthException.AuthErrorType.INVALID_CREDENTIALS,
                    "Password is required"
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
        
        logger.debug("User found, verifying password for: {}", username);
        
        // Verify password using secure hashing
        try {
            String storedPassword = user.getPassword();
            boolean passwordMatches = com.papel.imdb_clone.util.PasswordHasher.verifyPassword(password, storedPassword);
            
            if (!passwordMatches) {
                logger.warn("Login failed: Incorrect password for user - {}", username);
                throw new AuthException(
                        AuthException.AuthErrorType.INVALID_CREDENTIALS,
                        "Invalid username or password"
                );
            }
            // Log the successful password verification
            logger.debug("Password verified successfully for user: {}", username);
        } catch (Exception e) {
            logger.error("Error during password verification for user {}: {}", username, e.getMessage(), e);
            throw new AuthException(
                    AuthException.AuthErrorType.INTERNAL_ERROR,
                    "An error occurred during authentication"
            );
        }

        try {
            // Generate session token which means a unique identifier for the user session
            String sessionToken = UUID.randomUUID().toString();
            userSessions.put(sessionToken, user.getUsername());
            logger.info("User logged in successfully: {}", username);
            return sessionToken;
        } catch (Exception e) {
            // Log the error and throw an AuthException
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
     */
    public void getCurrentUser(String sessionToken) {
        if (sessionToken == null) {
            return;
        }
        String username = userSessions.get(sessionToken);
        if (username != null) {
            usersByUsername.get(username);
        }
    }

    /**
     * Registers a new user with comprehensive validation and logging.
     *
     * @param user The user to register
     * @param password The plaintext password
     * @param confirmPassword The password confirmation
     * @throws AuthException if registration fails
     */
    public void register(User user, String password, String confirmPassword) throws AuthException {
        logger.info("Starting registration for new user: {}", user != null ? user.getUsername() : "null");
        
        // Input validation
        if (user == null) {
            logger.error("Registration failed: User object is null");
            throw new AuthException(
                    AuthException.AuthErrorType.INVALID_INPUT,
                    "User information is required"
            );
        }
        
        if (password == null || password.trim().isEmpty()) {
            logger.warn("Registration failed: Password is empty for user: {}", user.getUsername());
            throw new AuthException(
                    AuthException.AuthErrorType.INVALID_INPUT,
                    "Password is required"
            );
        }

        // Password confirmation check
        if (!password.equals(confirmPassword)) {
            logger.warn("Registration failed: Password confirmation does not match for user: {}", user.getUsername());
            throw new AuthException(
                    AuthException.AuthErrorType.PASSWORD_MISMATCH,
                    "Passwords do not match"
            );
        }
        
        // Password strength validation
        if (password.length() < 8) {
            logger.warn("Registration failed: Password too short for user: {}", user.getUsername());
            throw new AuthException(
                    AuthException.AuthErrorType.INVALID_PASSWORD,
                    "Password must be at least 8 characters long"
            );
        }

        // Check if username already exists
        String username = user.getUsername();
        if (username == null || username.trim().isEmpty()) {
            logger.warn("Registration failed: Username is empty");
            throw new AuthException(
                    AuthException.AuthErrorType.INVALID_INPUT,
                    "Username is required"
            );
        }
        
        if (usersByUsername.containsKey(username)) {
            logger.warn("Registration failed: Username already exists - {}", username);
            throw new AuthException(
                    AuthException.AuthErrorType.USERNAME_EXISTS,
                    "Username already exists"
            );
        }

        // Check if email is valid and not already registered
        String email = user.getEmail();
        if (email == null || email.trim().isEmpty() || !email.matches("^[A-Za-z0-9+_.-]+@(.+)$")) {
            logger.warn("Registration failed: Invalid email format - {}", email);
            throw new AuthException(
                    AuthException.AuthErrorType.INVALID_EMAIL,
                    "Please provide a valid email address"
            );
        }
        
        if (usersByEmail.containsKey(email)) {
            logger.warn("Registration failed: Email already registered - {}", email);
            throw new AuthException(
                    AuthException.AuthErrorType.EMAIL_EXISTS,
                    "Email already registered"
            );
        }

        try {
            logger.debug("All validations passed, creating user account for: {}", username);
            
            // Hash the password before storing
            String hashedPassword = PasswordHasher.hashPassword(password);
            user.setPassword(hashedPassword);
            user.setId(nextUserId);
            
            // Add user to in-memory maps
            usersByUsername.put(username, user);
            usersByEmail.put(email, user);
            
            // Save to persistent storage
            saveUsers();
            
            // Increment ID only after successful save
            nextUserId++;
            
            logger.info("User registered successfully - ID: {}, Username: {}, Email: {}", 
                user.getId(), username, email);
        } catch (Exception e) {
            // Log the error and throw an AuthException
            logger.error("Registration failed for user {}: {}", user.getUsername(), e.getMessage(), e);
            throw new AuthException(
                    AuthException.AuthErrorType.REGISTRATION_FAILED,
                    "Failed to register user: " + e.getMessage(),
                    e
            );
        }
    }
}
