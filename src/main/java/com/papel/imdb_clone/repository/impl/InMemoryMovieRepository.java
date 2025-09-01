package com.papel.imdb_clone.repository.impl;

import com.papel.imdb_clone.exceptions.DuplicateEntryException;
import com.papel.imdb_clone.model.Movie;
import com.papel.imdb_clone.repository.MovieRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.stream.Collectors;


/**
 * In-memory implementation of MovieRepository.
 * Thread-safe implementation using CopyOnWriteArrayList and ReentrantReadWriteLock.
 */
public class InMemoryMovieRepository implements MovieRepository {
    private static final Logger logger = LoggerFactory.getLogger(InMemoryMovieRepository.class);

    private static final List<Movie> movies = new CopyOnWriteArrayList<>();
    private static final AtomicInteger nextId = new AtomicInteger(1);
    private static final ReentrantReadWriteLock lock = new ReentrantReadWriteLock();

    @Override
    public Optional<Movie> findById(int id) {
        lock.readLock().lock();
        try {
            return movies.stream()
                    .filter(movie -> movie.getId() == id)
                    .findFirst();
        } finally {
            lock.readLock().unlock();
        }
    }

    @Override
    public List<Movie> findByTitle(String title) {
        if (title == null || title.trim().isEmpty()) {
            return new ArrayList<>();
        }
        String searchTitle = title.trim().toLowerCase();
        lock.readLock().lock();
        try {
            return movies.stream()
                    .filter(movie -> movie.getTitle().toLowerCase().contains(searchTitle))
                    .collect(Collectors.toList());
        } finally {
            lock.readLock().unlock();
        }
    }

    @Override
    public Movie save(Movie movie) {
        if (movie == null) {
            throw new IllegalArgumentException("Movie cannot be null");
        }

        lock.writeLock().lock();
        try {
            if (movie.getId() == 0) {
                // New movie - check for duplicate title
                if (existsByTitle(movie.getTitle())) {
                    throw new DuplicateEntryException("Movie already exists: " + movie.getTitle());
                }
                movie.setId(nextId.getAndIncrement());
                movies.add(movie);
                logger.debug("Created new movie: {} with ID: {}", movie.getTitle(), movie.getId());
            } else {
                // Update existing movie
                Optional<Movie> existing = findById(movie.getId());
                if (existing.isPresent()) {
                    // Check if title is being changed and if it conflicts
                    if (!existing.get().getTitle().equals(movie.getTitle()) &&
                            existsByTitle(movie.getTitle())) {
                        throw new DuplicateEntryException("Movie title already exists: " + movie.getTitle());
                    }
                    movies.remove(existing.get());
                    movies.add(movie);
                    logger.debug("Updated movie: {} with ID: {}", movie.getTitle(), movie.getId());
                } else {
                    throw new IllegalArgumentException("Movie with ID " + movie.getId() + " not found");
                }
            }
            return movie;
        } finally {
            lock.writeLock().unlock();
        }
    }

    @Override
    public boolean existsByTitle(String title) {
        if (title == null) return false;

        lock.readLock().lock();
        try {
            return movies.stream()
                    .anyMatch(movie -> title.equals(movie.getTitle()));
        } finally {
            lock.readLock().unlock();
        }
    }

    @Override
    public long count() {
        return movies.size();
    }

    @Override
    public Movie findByTitleAndReleaseYear(String title, int startYear) {
        lock.readLock().lock();
        try {
            return movies.stream()
                    .filter(movie -> movie.getTitle().equalsIgnoreCase(title) &&
                            movie.getStartYear() == startYear)
                    .findFirst()
                    .orElse(null);
        } finally {
            lock.readLock().unlock();
        }
    }

    /**
     * Adds a movie directly to the repository (used by data loaders).
     *
     * @param movie The movie to add
     */
    public static void addMovie(Movie movie) {
        if (movie == null) return;

        lock.writeLock().lock();
        try {
            if (movie.getId() > 0) {
                nextId.getAndUpdate(current -> Math.max(current, movie.getId() + 1));
            } else {
                movie.setId(nextId.getAndIncrement());
            }
            movies.add(movie);
        } finally {
            lock.writeLock().unlock();
        }
    }

}