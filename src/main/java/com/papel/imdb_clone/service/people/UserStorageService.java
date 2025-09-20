package com.papel.imdb_clone.service.people;

import com.papel.imdb_clone.model.people.User;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.logging.ConsoleHandler;
import java.util.logging.Level;
import java.util.logging.Logger;

public class UserStorageService {
    private static final String USER_DATA_FILE = "user_data.ser";
    private static UserStorageService instance;
    private final Logger logger;

    private UserStorageService() {
        // Private constructor to enforce singleton pattern
        logger = Logger.getLogger(UserStorageService.class.getName());
        logger.setLevel(Level.INFO);
        logger.setUseParentHandlers(false);
        logger.addHandler(new ConsoleHandler());
        logger.setUseParentHandlers(false);

    }

    /**
     * UserStorageService singleton instance
     * @return UserStorageService instance which means the object that implements the UserStorageService interface
     */
    public static synchronized UserStorageService getInstance() {
        if (instance == null) {
            instance = new UserStorageService();
        }
        return instance;
    }

    /**
     * Saves user data to a file and logs the operation
     * @param usersByUsername Map of usernames to User objects
     * @param usersByEmail Map of emails to User objects
     */
    public void saveUsers(Map<String, User> usersByUsername, Map<String, User> usersByEmail) {
        long startTime = System.currentTimeMillis();
        logger.info("Starting to save user data...");
        
        try (ObjectOutputStream oos = new ObjectOutputStream(
                new FileOutputStream(USER_DATA_FILE))) {
            
            // Create a wrapper object to hold both maps
            List<Map<String, User>> data = new ArrayList<>();
            data.add(usersByUsername);
            data.add(usersByEmail);

            // Write the data to the file
            oos.writeObject(data);

            // Calculate and log the duration
            long duration = System.currentTimeMillis() - startTime;
            logger.info(String.format("Successfully saved %d users in %d ms", 
                usersByUsername.size(), duration));
                
        } catch (IOException e) {
            // Log error and rethrow
            String errorMsg = "Error saving user data: " + e.getMessage();
            logger.log(Level.SEVERE, errorMsg, e);
            throw new RuntimeException(errorMsg, e);
        }
    }

}
