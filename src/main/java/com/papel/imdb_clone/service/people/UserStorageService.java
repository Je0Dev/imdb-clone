package com.papel.imdb_clone.service.people;

import com.papel.imdb_clone.model.people.User;

import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
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
     * Saves user data to a file with comprehensive error handling and logging.
     *
     * @param usersByUsername Map of usernames to User objects
     * @param usersByEmail Map of emails to User objects
     * @throws RuntimeException if there's an error saving the data
     */
    public void saveUsers(Map<String, User> usersByUsername, Map<String, User> usersByEmail) {
        if (usersByUsername == null || usersByEmail == null) {
            logger.severe("Cannot save users: users map is null");
            throw new IllegalArgumentException("User maps cannot be null");
        }

        final long startTime = System.currentTimeMillis();
        final int userCount = usersByUsername.size();

        logger.info("Saving user data to file...");
        
        File tempFile = null;
        try {
            // Create a temporary file first for atomic write
            tempFile = new File(USER_DATA_FILE + ".tmp");
            
            // Create parent directories if they don't exist
            File parentDir = tempFile.getParentFile();
            if (parentDir != null && !parentDir.exists()) {
                logger.info("Creating parent directories for user data file");
                if (!parentDir.mkdirs()) {
                    throw new IOException("Failed to create parent directories for " + tempFile.getAbsolutePath());
                }
            }
            
            // Write to temporary file first
            try (ObjectOutputStream oos = new ObjectOutputStream(
                    new FileOutputStream(tempFile))) {
                
                // Create a wrapper object to hold both maps
                List<Map<String, User>> data = new ArrayList<>(2);
                data.add(new HashMap<>(usersByUsername));  // Defensive copy
                data.add(new HashMap<>(usersByEmail));     // Defensive copy

                // Write the data to the temporary file
                oos.writeObject(data);
                oos.flush();
                
                // Ensure all data is written to disk
                oos.flush();

                logger.info("User data successfully written to temporary file");
            }
            
            // Now rename the temp file to the actual file (atomic operation)
            File targetFile = new File(USER_DATA_FILE);
            if (targetFile.exists() && !targetFile.delete()) {
                throw new IOException("Failed to delete existing user data file");
            }
            
            if (!tempFile.renameTo(targetFile)) {
                throw new IOException("Failed to rename temporary file to " + USER_DATA_FILE);
            }
            
            // Calculate and log the duration
            long duration = System.currentTimeMillis() - startTime;
            logger.info(String.format("Successfully saved %d users in %d ms", userCount, duration));
            
        } catch (Exception e) {
            String errorMsg = String.format("Failed to save %d users: %s", userCount, e.getMessage());
            logger.log(Level.SEVERE, errorMsg, e);
            
            // Clean up temporary file if it exists
            if (tempFile != null && tempFile.exists() && !tempFile.delete()) {
                logger.warning("Failed to delete temporary file: " + tempFile.getAbsolutePath());
            }
            
            throw new RuntimeException(errorMsg, e);
        }
    }
}

