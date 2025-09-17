package com.papel.imdb_clone.service;

import com.papel.imdb_clone.model.User;

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
     * Save users to a file which means serialize the users and write them to a file.
     * @param usersByUsername
     * @param usersByEmail
     */
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

            oos.writeObject(data);
            
            long duration = System.currentTimeMillis() - startTime;
            logger.info(String.format("Successfully saved %d users in %d ms", 
                usersByUsername.size(), duration));
                
        } catch (IOException e) {
            String errorMsg = "Error saving user data: " + e.getMessage();
            logger.log(Level.SEVERE, errorMsg, e);
            throw new RuntimeException(errorMsg, e);
        }
    }

    //load users from a file which means deserialize the users and read them from a file.
    public Map<String, User>[] loadUsers() {
        try (ObjectInputStream ois = new ObjectInputStream(
                new FileInputStream(USER_DATA_FILE))) {
            List<Map<String, User>> data = (List<Map<String, User>>) ois.readObject();
            return data.toArray(new Map[0]);
        } catch (IOException | ClassNotFoundException e) {
            logger.log(Level.SEVERE, "Error loading user data", e);
            // Return an array of two empty maps if loading fails
            return new Map[] { new HashMap<>(), new HashMap<>() };
        }
    }
}
