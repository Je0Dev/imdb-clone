package com.papel.imdb_clone.repository.impl;

import com.papel.imdb_clone.exceptions.DuplicateEntryException;
import com.papel.imdb_clone.model.User;
import com.papel.imdb_clone.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * In-memory implementation of UserRepository.
 * Thread-safe implementation using CopyOnWriteArrayList and ReentrantReadWriteLock.
 */
public class InMemoryUserRepository implements UserRepository {
    private static final Logger logger = LoggerFactory.getLogger(InMemoryUserRepository.class);

    private final List<User> users = new CopyOnWriteArrayList<>();
    private final AtomicInteger nextId = new AtomicInteger(1);
    private final ReentrantReadWriteLock lock = new ReentrantReadWriteLock();

    @Override
    public Optional<User> findById(int id) {
        lock.readLock().lock();
        try {
            return users.stream()
                    .filter(user -> user.getId() == id)
                    .findFirst();
        } finally {
            lock.readLock().unlock();
        }
    }

    @Override
    public User save(User user) {
        if (user == null) {
            throw new IllegalArgumentException("User cannot be null");
        }

        lock.writeLock().lock();
        try {
            if (user.getId() == 0) {
                // New user - check for duplicate username
                if (existsByUsername(user.getUsername())) {
                    throw new DuplicateEntryException("Username already exists: " + user.getUsername());
                }
                user.setId(nextId.getAndIncrement());
                users.add(user);
                logger.debug("Created new user: {} with ID: {}", user.getUsername(), user.getId());
            } else {
                // Update existing user
                Optional<User> existing = findById(user.getId());
                if (existing.isPresent()) {
                    // Check if username is being changed and if it conflicts
                    if (!existing.get().getUsername().equals(user.getUsername()) &&
                            existsByUsername(user.getUsername())) {
                        throw new DuplicateEntryException("Username already exists: " + user.getUsername());
                    }
                    users.remove(existing.get());
                    users.add(user);
                    logger.debug("Updated user: {} with ID: {}", user.getUsername(), user.getId());
                } else {
                    throw new IllegalArgumentException("User with ID " + user.getId() + " not found");
                }
            }
            return user;
        } finally {
            lock.writeLock().unlock();
        }
    }

    @Override
    public boolean existsByUsername(String username) {
        if (username == null) return false;

        lock.readLock().lock();
        try {
            return users.stream()
                    .anyMatch(user -> username.equals(user.getUsername()));
        } finally {
            lock.readLock().unlock();
        }
    }

    @Override
    public Optional<User> findByUsername(String username) {
        if (username == null) return Optional.empty();

        lock.readLock().lock();
        try {
            return users.stream()
                    .filter(user -> username.equals(user.getUsername()))
                    .findFirst();
        } finally {
            lock.readLock().unlock();
        }
    }

    @Override
    public long count() {
        lock.readLock().lock();
        try {
            return users.size();
        } finally {
            lock.readLock().unlock();
        }
    }

    public List<User> findAll() {
        lock.readLock().lock();
        try {
            return new CopyOnWriteArrayList<>(users);
        } finally {
            lock.readLock().unlock();
        }
    }

    public boolean deleteById(int id) {
        lock.writeLock().lock();
        try {
            return users.removeIf(user -> user.getId() == id);
        } finally {
            lock.writeLock().unlock();
        }
    }

    /**
     * Adds a user directly to the repository (used by data loaders).
     *
     * @param user The user to add
     */
    public void addUser(User user) {
        if (user == null) return;

        lock.writeLock().lock();
        try {
            if (user.getId() > 0) {
                nextId.getAndUpdate(current -> Math.max(current, user.getId() + 1));
            } else {
                user.setId(nextId.getAndIncrement());
            }
            users.add(user);
        } finally {
            lock.writeLock().unlock();
        }
    }

    /**
     * Clears all users (used for testing or data reloading).
     */
    public void clear() {
        lock.writeLock().lock();
        try {
            users.clear();
            nextId.set(1);
            logger.debug("Cleared all users");
        } finally {
            lock.writeLock().unlock();
        }
    }

    public Optional<Object> findByEmail(String email) {
        lock.readLock().lock();
        try {
            return Optional.of(users.stream()
                    .filter(user -> email.equals(user.getEmail()))
                    .findFirst());
        } finally {
            lock.readLock().unlock();
        }
    }
}