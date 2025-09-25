package com.papel.imdb_clone.service.people;

import com.papel.imdb_clone.exceptions.EntityNotFoundException;
import com.papel.imdb_clone.model.people.Celebrity;
import com.papel.imdb_clone.repository.CelebritiesRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Optional;
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

    private final CelebritiesRepository celebritiesRepository;
    private final CelebrityManager<T> celebrityManager;
    private final ReentrantReadWriteLock lock = new ReentrantReadWriteLock();
    private final Class<T> celebrityType;


    public CelebrityService(Class<T> celebrityType, CelebritiesRepository celebritiesRepository) {
        this.celebrityType = celebrityType;
        this.celebritiesRepository = celebritiesRepository;
        this.celebrityManager = CelebrityManager.getInstance(celebrityType);
    }


    /**
     * Finds celebrities by name (partial match).
     *
     * @param name The name to search for
     * @return List of celebrities matching the name
     */
    public List<T> findByName(String name) {
        if (name == null || name.trim().isEmpty()) {
            return List.of();
        }

        lock.readLock().lock();
        try {
            return celebritiesRepository.findByNameContaining(name.trim(), celebrityType);
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
            // First try to find in the repository
            Optional<T> celebrity = celebritiesRepository.findById(id, celebrityType);
            
            // If not found in repository, check with CelebrityManager
            if (celebrity.isEmpty()) {
                Optional<T> celeb = celebrityManager.getCelebrityById(id);
                if (celeb.isPresent() && celebrityType.isInstance(celeb.get())) {
                    T managedCeleb = celebrityType.cast(celeb.get());
                    // Save the celebrity from manager to repository
                    celebritiesRepository.save(managedCeleb);
                    return Optional.of(managedCeleb);
                }
            }
            
            return celebrity;
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
            
            // Save to repository
            T savedCelebrity = celebritiesRepository.save(celebrity);
            
            if (savedCelebrity.getId() == celebrity.getId()) {
                logger.info("Updated {}: {}", celebrityType.getSimpleName(), celebrity.getFullName());
            } else {
                logger.info("Created new {}: {}", celebrityType.getSimpleName(), celebrity.getFullName());
            }
            
            return savedCelebrity;
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

        lock.readLock().lock();
        try {
            return celebritiesRepository.findByFullName(
                firstName.trim(), 
                lastName.trim(), 
                celebrityType
            );
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
            return celebritiesRepository.findAll(celebrityType);
        } finally {
            lock.readLock().unlock();
        }
    }
    
    /**
     * Deletes a celebrity by ID.
     * @param id The ID of the celebrity to delete
     * @return true if a celebrity was deleted, false otherwise
     */
    public boolean deleteById(int id) {
        lock.writeLock().lock();
        try {
            return celebritiesRepository.deleteById(id);
        } finally {
            lock.writeLock().unlock();
        }
    }
    
    /**
     * Checks if a celebrity with the given ID exists.
     * @param id The ID to check
     * @return true if a celebrity with the ID exists, false otherwise
     */
    public boolean existsById(int id) {
        lock.readLock().lock();
        try {
            return celebritiesRepository.existsById(id);
        } finally {
            lock.readLock().unlock();
        }
    }
    
    /**
     * Returns the total count of celebrities of this service's type.
     * @return The count of celebrities
     */
    public long count() {
        lock.readLock().lock();
        try {
            return celebritiesRepository.count(celebrityType);
        } finally {
            lock.readLock().unlock();
        }
    }
}
