package com.papel.imdb_clone.service.people;

import com.papel.imdb_clone.model.people.Celebrity;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Manages celebrity instances to prevent duplicates.
 * Implements the singleton pattern for global access.
 */
public class CelebrityManager {
    private final Map<String, Celebrity> celebritiesByKey = new ConcurrentHashMap<>();
    private final Map<Integer, Celebrity> celebritiesById = new ConcurrentHashMap<>();
    private static final CelebrityManager instance = new CelebrityManager();
    private final AtomicInteger nextId = new AtomicInteger(1);

    private CelebrityManager() {
        // Private constructor to enforce singleton
    }

    /**
     * Returns the singleton instance of CelebrityManager.
     * @return The singleton instance
     */
    public static CelebrityManager getInstance() {
        return instance;
    }

    /**
     * Checks if a celebrity with the same key already exists.
     * @param celebrity The celebrity to check
     * @return true if a duplicate exists, false otherwise
     */
    public synchronized boolean celebrityExists(Celebrity celebrity) {
        if (celebrity == null) return false;
        String key = generateKey(celebrity);
        return celebritiesByKey.containsKey(key);
    }

    /**
     * Adds a celebrity to the manager if it doesn't already exist.
     * @param celebrity The celebrity to add
     */
    public synchronized void addCelebrity(Celebrity celebrity) {
        if (celebrity == null) {
            return;
        }

        String key = generateKey(celebrity);
        if (celebritiesByKey.containsKey(key)) {
            return;
        }

        int id = nextId.getAndIncrement();
        try {
            // Use reflection to set the ID since it's now protected
            java.lang.reflect.Field idField = Celebrity.class.getDeclaredField("id");
            idField.setAccessible(true);
            idField.set(celebrity, id);

            celebritiesByKey.put(key, celebrity);
            celebritiesById.put(id, celebrity);
        } catch (Exception e) {
            throw new RuntimeException("Failed to add celebrity", e);
        }
    }

    /**
     * Finds an existing celebrity that matches the given one.
     * @param <T> The type of celebrity
     * @param celebrity The celebrity to find
     * @return An Optional containing the existing celebrity, or empty if not found
     */
    @SuppressWarnings("unchecked")
    public <T extends Celebrity> Optional<T> findCelebrity(T celebrity) {
        if (celebrity == null) return Optional.empty();
        String key = generateKey(celebrity);
        return Optional.ofNullable((T) celebritiesByKey.get(key));
    }
    
    /**
     * Finds a celebrity by their ID.
     * @param <T> The type of celebrity to return
     * @param id The ID of the celebrity to find
     * @return An Optional containing the celebrity if found, or empty if not found
     */
    @SuppressWarnings("unchecked")
    public <T extends Celebrity> Optional<T> findById(int id) {
        return Optional.ofNullable((T) celebritiesById.get(id));
    }


    /**
     * Helper method to generate a consistent key for a celebrity
     * @param celebrity The celebrity to generate a key for
     * @return A string key representing the celebrity
     */
    private String generateKey(Celebrity celebrity) {
        if (celebrity == null) return null;
        String firstName = celebrity.getFirstName() != null ? celebrity.getFirstName().toLowerCase() : "";
        String lastName = celebrity.getLastName() != null ? celebrity.getLastName().toLowerCase() : "";
        String birthDate = celebrity.getBirthDate() != null ? celebrity.getBirthDate().toString() : "";
        return String.format("%s|%s|%s", firstName, lastName, birthDate);
    }
    /**
     * Gets the number of unique celebrities currently managed.
     * @return The count of unique celebrities
     */
    public int getCelebrityCount() {
        return celebritiesByKey.size();
    }

}
