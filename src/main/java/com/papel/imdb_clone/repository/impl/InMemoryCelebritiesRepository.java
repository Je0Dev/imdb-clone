package com.papel.imdb_clone.repository.impl;

import com.papel.imdb_clone.model.people.Celebrity;
import com.papel.imdb_clone.repository.CelebritiesRepository;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

/**
 * In-memory implementation of the CelebritiesRepository interface.
 * This implementation is thread-safe and uses a ConcurrentHashMap for storage.
 */
/**
 * In-memory implementation of the CelebritiesRepository interface.
 * This implementation is thread-safe and uses a ConcurrentHashMap for storage.
 */
/**
 * Creates a new instance of InMemoryCelebritiesRepository.
 */
public class InMemoryCelebritiesRepository implements CelebritiesRepository {
    private final Map<Integer, Celebrity> celebrities = new ConcurrentHashMap<>();
    private final AtomicInteger idGenerator = new AtomicInteger(1);
    private final Object lock = new Object();

    /**
     * Constructs a new InMemoryCelebritiesRepository.
     */
    public InMemoryCelebritiesRepository() {
        // Initialize the repository
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T extends Celebrity> T save(T celebrity) {
        synchronized (lock) {
            if (celebrity.getId() == 0) {
                celebrity.setId(idGenerator.getAndIncrement());
            }
            celebrities.put(celebrity.getId(), celebrity);
            return celebrity;
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T extends Celebrity> Optional<T> findById(int id, Class<T> type) {
        Celebrity celebrity = celebrities.get(id);
        if (celebrity != null && type.isInstance(celebrity)) {
            return Optional.of((T) celebrity);
        }
        return Optional.empty();
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T extends Celebrity> List<T> findAll(Class<T> type) {
        return celebrities.values().stream()
                .filter(type::isInstance)
                .map(celebrity -> (T) celebrity)
                .collect(Collectors.toList());
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T extends Celebrity> List<T> findByNameContaining(String name, Class<T> type) {
        String lowerCaseName = name.toLowerCase();
        return celebrities.values().stream()
                .filter(type::isInstance)
                .filter(celebrity -> celebrity.getFullName().toLowerCase().contains(lowerCaseName))
                .map(celebrity -> (T) celebrity)
                .collect(Collectors.toList());
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T extends Celebrity> Optional<T> findByFullName(String firstName, String lastName, Class<T> type) {
        String searchFirstName = firstName != null ? firstName.trim().toLowerCase() : "";
        String searchLastName = lastName != null ? lastName.trim().toLowerCase() : "";
        
        return (Optional<T>) celebrities.values().stream()
                .filter(type::isInstance)
                .filter(celebrity -> {
                    String celebFirstName = celebrity.getFirstName() != null ? 
                            celebrity.getFirstName().toLowerCase() : "";
                    String celebLastName = celebrity.getLastName() != null ? 
                            celebrity.getLastName().toLowerCase() : "";
                    return celebFirstName.equals(searchFirstName) && 
                           celebLastName.equals(searchLastName);
                })
                .findFirst();
    }

    @Override
    public boolean deleteById(int id) {
        synchronized (lock) {
            return celebrities.remove(id) != null;
        }
    }

    @Override
    public boolean existsById(int id) {
        return celebrities.containsKey(id);
    }

    @Override
    public <T extends Celebrity> long count(Class<T> type) {
        return celebrities.values().stream()
                .filter(type::isInstance)
                .count();
    }

    /**
     * Clears all celebrities from the repository.
     * Primarily used for testing purposes.
     */
    public void clear() {
        synchronized (lock) {
            celebrities.clear();
            idGenerator.set(1);
        }
    }
}
