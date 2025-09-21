package com.papel.imdb_clone.service.people;

import com.papel.imdb_clone.model.people.Actor;
import com.papel.imdb_clone.model.people.Celebrity;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Manages celebrity instances to prevent duplicates.
 * Implements the singleton pattern with type-safe generic support.
 * Uses generics to ensure type safety and avoid casting.
 * 
 * @param <T> The type of celebrity this manager handles (e.g., Actor, Director)
 */
public class CelebrityManager<T extends Celebrity> {
    private final Map<String, T> celebritiesByKey = new ConcurrentHashMap<>();
    private final Map<Integer, T> celebritiesById = new ConcurrentHashMap<>();
    private static final Map<Class<?>, CelebrityManager<?>> instances = new ConcurrentHashMap<>();
    private final AtomicInteger nextId = new AtomicInteger(1);

    protected CelebrityManager() {
        // Protected constructor for extension
    }

    /**
     * Returns the singleton instance of CelebrityManager for the specified type.
     * @param <T> The type of celebrity
     * @param clazz The class object of the celebrity type
     * @return The singleton instance for the specified type
     */
    @SuppressWarnings("unchecked")
    public static <T extends Celebrity> CelebrityManager<T> getInstance(Class<T> clazz) {
        return (CelebrityManager<T>) instances.computeIfAbsent(clazz, k -> new CelebrityManager<>());
    }
    
    /**
     * Returns the singleton instance of CelebrityManager for the base Celebrity type.
     * This method is provided for backward compatibility.
     * @param <T> The type of celebrity
     * @return The singleton instance for the base Celebrity type
     */
    @SuppressWarnings("unchecked")
    public static <T extends Celebrity> CelebrityManager<T> getInstance() {
        // Default to base Celebrity type if no specific type is provided
        return (CelebrityManager<T>) getInstance(Celebrity.class);
    }

    /**
     * Generates a unique key for a celebrity based on their name and birth date.
     * @param celebrity The celebrity to generate a key for
     * @return A unique string key
     */
    private String generateKey(Celebrity celebrity) {
        return String.format("%s|%s|%s",
            celebrity.getFirstName() != null ? celebrity.getFirstName().toLowerCase() : "",
            celebrity.getLastName() != null ? celebrity.getLastName().toLowerCase() : "",
            celebrity.getBirthDate() != null ? celebrity.getBirthDate().toString() : ""
        );
    }

    /**
     * Checks if a celebrity with the same key already exists.
     * @param celebrity The celebrity to check
     * @return true if a duplicate exists, false otherwise
     */
    public synchronized boolean celebrityExists(T celebrity) {
        if (celebrity == null) return false;
        String key = generateKey(celebrity);
        return celebritiesByKey.containsKey(key);
    }

    /**
     * Adds a celebrity to the manager if it doesn't already exist.
     * @param celebrity The celebrity to add (must not be null)
     * @return The celebrity that was added or the existing one if it already exists
     * @throws IllegalArgumentException if the celebrity is null
     */
    public synchronized T addCelebrity(T celebrity) {
        Objects.requireNonNull(celebrity, "Celebrity cannot be null");

        String key = generateKey(celebrity);
        
        // Check if a celebrity with the same key already exists
        T existing = celebritiesByKey.get(key);
        if (existing != null) {
            return existing;
        }
        
        // Assign a new ID if needed
        if (celebrity.getId() == 0) {
            int newId = nextId.getAndIncrement();
            celebrity.setId(newId);
        }
        
        // Add to both maps
        celebritiesByKey.put(key, celebrity);
        celebritiesById.put(celebrity.getId(), celebrity);
        return celebrity;
    }

    /**
     * Finds an existing celebrity that matches the given one.
     * @param celebrity The celebrity to find
     * @return An Optional containing the matching celebrity if found, empty otherwise
     */
    public synchronized Optional<T> findExistingCelebrity(T celebrity) {
        if (celebrity == null) return Optional.empty();
        String key = generateKey(celebrity);
        return Optional.ofNullable(celebritiesByKey.get(key));
    }

    /**
     * Gets a celebrity by ID.
     * @param id The ID of the celebrity to retrieve
     * @return An Optional containing the celebrity if found, empty otherwise
     */
    public synchronized Optional<T> getCelebrityById(int id) {
        return Optional.ofNullable(celebritiesById.get(id));
    }

    /**
     * Gets all celebrities managed by this manager.
     * @return A collection of all celebrities
     */
    public synchronized java.util.Collection<T> getAllCelebrities() {
        return new java.util.ArrayList<>(celebritiesById.values());
    }

    /**
     * Removes a celebrity from the manager.
     * @param celebrity The celebrity to remove
     * @return true if the celebrity was removed, false if it wasn't found
     */
    public synchronized boolean removeCelebrity(T celebrity) {
        if (celebrity == null) return false;
        String key = generateKey(celebrity);
        return celebritiesByKey.remove(key) != null && celebritiesById.remove(celebrity.getId()) != null;
    }

    /**
     * Clears all celebrities from the manager and resets the ID counter.
     */
    public synchronized void clear() {
        celebritiesByKey.clear();
        celebritiesById.clear();
        nextId.set(1);
    }
    
    /**
     * Gets the number of celebrities currently managed.
     * @return The number of celebrities
     */
    public synchronized int size() {
        return celebritiesById.size();
    }

    /**
     * Gets the number of unique celebrities currently managed.
     * @return The count of unique celebrities
     */
    public int getCelebrityCount() {
        return celebritiesByKey.size();
    }

    public Optional<Celebrity> findById(int id) {
        return Optional.ofNullable(celebritiesById.get(id));
    }

    public Optional<T> findCelebrity(T celebrity) {
        return Optional.ofNullable(celebritiesByKey.get(generateKey(celebrity)));
    }
}
