package com.papel.imdb_clone.service;

import com.papel.imdb_clone.exceptions.EntityNotFoundException;
import com.papel.imdb_clone.model.Celebrity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.stream.Collectors;

/**
 * Service for managing celebrities (actors, directors, etc.) in the system.
 * Provides CRUD operations and query capabilities for celebrity data.
 *
 * @param <T> The type of celebrity (e.g., Actor, Director)
 */
public class CelebrityService<T extends Celebrity> {
    private static final Logger logger = LoggerFactory.getLogger(CelebrityService.class);

    private final List<T> celebrities = new CopyOnWriteArrayList<>();
    private final AtomicInteger nextId = new AtomicInteger(1);
    private final ReentrantReadWriteLock lock = new ReentrantReadWriteLock();
    private final Class<T> celebrityType;

    /**
     * Creates a new CelebrityService for the specified celebrity type.
     *
     * @param celebrityType The class object representing the celebrity type (e.g., Actor.class, Director.class)
     */
    public CelebrityService(Class<T> celebrityType) {
        this.celebrityType = celebrityType;
    }

    /**
     * Finds a celebrity by their ID.
     *
     * @param id The celebrity ID
     * @return Optional containing the celebrity if found
     */
    public Optional<T> findById(int id) {
        lock.readLock().lock();
        try {
            return celebrities.stream()
                    .filter(celebrity -> celebrity.getId() == id)
                    .findFirst();
        } finally {
            lock.readLock().unlock();
        }
    }

    /**
     * Finds celebrities by name (partial match).
     *
     * @param name The name to search for
     * @return List of celebrities matching the name
     */
    public List<? extends Object> findByName(String name) {
        if (name == null || name.trim().isEmpty()) {
            return List.of();
        }

        String searchTerm = name.toLowerCase().trim();
        lock.readLock().lock();
        try {
            return celebrities.stream()
                    .filter(celebrity -> celebrity.getFullName().toLowerCase().contains(searchTerm))
                    .collect(Collectors.toList());
        } finally {
            lock.readLock().unlock();
        }
    }

    /**
     * Saves a celebrity (creates or updates).
     *
     * @param celebrity The celebrity to save
     * @return The saved celebrity (with ID if new)
     */
    public T save(T celebrity) {
        if (celebrity == null) {
            throw new IllegalArgumentException("Celebrity cannot be null");
        }

        lock.writeLock().lock();
        try {
            if (celebrity.getId() == 0) {
                // New celebrity
                celebrity.setId(nextId.getAndIncrement());
                celebrities.add(celebrity);
                logger.info("Created new {}: {}", celebrityType.getSimpleName(), celebrity.getFullName());
            } else {
                // Update existing
                int index = -1;
                for (int i = 0; i < celebrities.size(); i++) {
                    if (celebrities.get(i).getId() == celebrity.getId()) {
                        index = i;
                        break;
                    }
                }

                if (index != -1) {
                    celebrities.set(index, celebrity);
                    logger.info("Updated {}: {}", celebrityType.getSimpleName(), celebrity.getFullName());
                } else {
                    throw new EntityNotFoundException(celebrityType, celebrity.getId());
                }
            }
            return celebrity;
        } finally {
            lock.writeLock().unlock();
        }
    }

    /**
     * Deletes a celebrity by ID.
     *
     * @param id The ID of the celebrity to delete
     * @return true if the celebrity was found and deleted, false otherwise
     */
    public boolean delete(int id) {
        lock.writeLock().lock();
        try {
            return celebrities.removeIf(celebrity -> celebrity.getId() == id);
        } finally {
            lock.writeLock().unlock();
        }
    }

    /**
     * Finds a celebrity by their full name (first and last name).
     *
     * @param firstName The first name of the celebrity
     * @param lastName  The last name of the celebrity
     * @return Optional containing the celebrity if found
     */
    public Optional<T> findByFullName(String firstName, String lastName) {
        if (firstName == null || lastName == null) {
            return Optional.empty();
        }

        String searchFirstName = firstName.trim().toLowerCase();
        String searchLastName = lastName.trim().toLowerCase();

        lock.readLock().lock();
        try {
            return celebrities.stream()
                    .filter(celebrity ->
                            celebrity.getFirstName().toLowerCase().equals(searchFirstName) &&
                                    celebrity.getLastName().toLowerCase().equals(searchLastName))
                    .findFirst();
        } finally {
            lock.readLock().unlock();
        }
    }

    /**
     * Gets all celebrities of this service's type.
     *
     * @return List of all celebrities
     */
    public List<T> getAll() {
        lock.readLock().lock();
        try {
            return List.copyOf(celebrities);
        } finally {
            lock.readLock().unlock();
        }
    }
}
