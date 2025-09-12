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
    private User currentUser;
    private User user;
    private boolean authenticated;

    /**
     * Private constructor for singleton pattern.
     *
     * @param dataManager       The data manager instance
     */
    private UserService(RefactoredDataManager dataManager) {
        this.dataManager = dataManager;
    }

    /**
     * @param dataManager       The data manager instanc
     * @return The instance
     */
    public static synchronized UserService getInstance(RefactoredDataManager dataManager) {
        if (instance == null) {
            instance = new UserService(dataManager);
        }
        return instance;
    }

}