package com.papel.imdb_clone.service;

import com.papel.imdb_clone.data.RefactoredDataManager;
import com.papel.imdb_clone.model.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Optional;

/**
 * Service class for user-related operations including authentication, registration,
 * and profile management.
 */
public class UserService {
    private static final Logger logger = LoggerFactory.getLogger(UserService.class);
    private static UserService instance;

    private final RefactoredDataManager dataManager;
    private final EncryptionService encryptionService;
    private User currentUser;
    private User user;
    private boolean authenticated;

    /**
     * Private constructor for singleton pattern.
     *
     * @param dataManager       The data manager instance
     * @param encryptionService The encryption service instance
     */
    private UserService(RefactoredDataManager dataManager, EncryptionService encryptionService) {
        this.dataManager = dataManager;
        this.encryptionService = encryptionService;
    }

    /**
     * @param dataManager       The data manager instance
     * @param encryptionService The encryption service instance
     * @return The instance
     */
    public static synchronized UserService getInstance(RefactoredDataManager dataManager, EncryptionService encryptionService) {
        if (instance == null) {
            instance = new UserService(dataManager, encryptionService);
        }
        return instance;
    }


    /**
     * Authenticates a user with username and password.
     *
     * @param username The username or email
     * @param password The plain text password
     * @return true if authentication successful, false otherwise
     */
    public boolean login(String username, String password) {
        try {
            // First try to find by username
            Optional<Object> userOpt = Optional.ofNullable(dataManager.findUserByUsername(username));

            // If not found by username, try to find by email
            if (userOpt.isEmpty()) {
                // Note: This assumes findUserByEmail exists in dataManager
                // If it doesn't, we'll need to implement it
                userOpt = dataManager.findUserByEmail(username);
            }

            if (userOpt.isEmpty()) {
                return false;
            }

            User user = (User) userOpt.get();
            if (user.getPassword() != null && user.getPassword().equals(encryptionService.hashPassword(password))) {
                this.currentUser = user;
                return true;
            }

            return false;
        } catch (Exception e) {
            logger.error("Login failed for user {}: {}", username, e.getMessage(), e);
            return false;
        }
    }

    /**
     * Gets the currently logged-in user.
     *
     * @return Current user or null if not logged in
     */
    public User getCurrentUser() {
        return currentUser;
    }

    public boolean isAuthenticated() {
        return authenticated;
    }

    public void setAuthenticated(boolean authenticated) {
        this.authenticated = authenticated;
    }
}