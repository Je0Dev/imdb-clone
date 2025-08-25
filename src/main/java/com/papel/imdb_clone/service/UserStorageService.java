package com.papel.imdb_clone.service;

import com.papel.imdb_clone.model.User;

import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class UserStorageService {
    private static final String USER_DATA_FILE = "user_data.ser";
    private static UserStorageService instance;

    private UserStorageService() {
        // Private constructor to enforce singleton pattern
    }

    public static synchronized UserStorageService getInstance() {
        if (instance == null) {
            instance = new UserStorageService();
        }
        return instance;
    }

    public void saveUsers(Map<String, User> usersByUsername, Map<String, User> usersByEmail) {
        try (ObjectOutputStream oos = new ObjectOutputStream(
                new FileOutputStream(USER_DATA_FILE))) {

            // Create a wrapper object to hold both maps
            List<Map<String, User>> data = new ArrayList<>();
            data.add(usersByUsername);
            data.add(usersByEmail);

            oos.writeObject(data);

        } catch (IOException e) {
            System.err.println("Error saving user data: " + e.getMessage());
        }
    }

    @SuppressWarnings("unchecked")
    public Map<String, User>[] loadUsers() {
        File file = new File(USER_DATA_FILE);
        if (!file.exists()) {
            // Return empty maps if the file doesn't exist yet
            return new Map[]{new HashMap<String, User>(), new HashMap<String, User>()};
        }

        try (ObjectInputStream ois = new ObjectInputStream(
                new FileInputStream(USER_DATA_FILE))) {

            List<Map<String, User>> data = (List<Map<String, User>>) ois.readObject();
            return new Map[]{data.get(0), data.get(1)};

        } catch (IOException | ClassNotFoundException e) {
            System.err.println("Error loading user data: " + e.getMessage());
            return new Map[]{new HashMap<String, User>(), new HashMap<String, User>()};
        }
    }

}
