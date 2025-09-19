package com.papel.imdb_clone.service.people;

import com.papel.imdb_clone.exceptions.EntityNotFoundException;
import com.papel.imdb_clone.model.people.Celebrity;
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
    private final CelebrityManager celebrityManager = CelebrityManager.getInstance();
    private final ReentrantReadWriteLock lock = new ReentrantReadWriteLock();
    private final Class<T> celebrityType;


    public CelebrityService(Class<T> celebrityType) {
        this.celebrityType = celebrityType;
    }


    /**
     * Finds celebrities by name (partial match).
     *
     * @param name The name to search for
     * @return List of celebrities matching the name
     */
    public List<?> findByName(String name) {
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
     * Finds a celebrity by their ID.
     *
     * @param id The ID of the celebrity to find
     * @return Optional containing the celebrity if found
     */
    public Optional<T> findById(int id) {
        lock.readLock().lock();
        try {
            // First try to find in our local list
            Optional<T> localCelebrity = celebrities.stream()
                    .filter(celebrity -> celebrity.getId() == id)
                    .findFirst();
                    
            // If not found locally, check with CelebrityManager
            if (localCelebrity.isEmpty()) {
                Optional<Celebrity> celeb = celebrityManager.findById(id);
                if (celeb.isPresent() && celebrityType.isInstance(celeb.get())) {
                    return Optional.of(celebrityType.cast(celeb.get()));
                }
            }
            
            return localCelebrity;
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
            // Let CelebrityManager handle the ID assignment and duplicate checking
            celebrityManager.addCelebrity(celebrity);
            
            // Check if this is a new celebrity or an update
            boolean isNew = true;
            int index = -1;
            
            for (int i = 0; i < celebrities.size(); i++) {
                if (celebrities.get(i).getId() == celebrity.getId()) {
                    isNew = false;
                    index = i;
                    break;
                }
            }
            
            if (isNew) {
                celebrities.add(celebrity);
                logger.info("Created new {}: {}", celebrityType.getSimpleName(), celebrity.getFullName());
            } else if (index != -1) {
                celebrities.set(index, celebrity);
                logger.info("Updated {}: {}", celebrityType.getSimpleName(), celebrity.getFullName());
            } else {
                // This should not happen as CelebrityManager should have handled it
                throw new IllegalStateException("Failed to add or update celebrity: " + celebrity.getFullName());
            }
            
            return celebrity;
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
    public List<T> findAll() {
        lock.readLock().lock();
        try {
            // Return a defensive copy of our local list
            return new java.util.ArrayList<>(celebrities);
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
            return new java.util.ArrayList<>(celebrities);
        } finally {
            lock.readLock().unlock();
        }
    }
}
