package com.papel.imdb_clone.service;

import com.papel.imdb_clone.data.RefactoredDataManager;
import com.papel.imdb_clone.model.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
}