package com.papel.imdb_clone.repository.impl;

import com.papel.imdb_clone.exceptions.DuplicateEntryException;
import com.papel.imdb_clone.model.Movie;
import com.papel.imdb_clone.repository.MovieRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.stream.Collectors;

/**
 * A thread means a separate path of execution in a program.
 * The program can have multiple threads running concurrently,
 * each thread can access and modify shared data
 */

/**
 * In-memory implementation of MovieRepository.
 * Thread-safe implementation using CopyOnWriteArrayList and ReentrantReadWriteLock.
 */
public class InMemoryMovieRepository implements MovieRepository {
    private static final Logger logger = LoggerFactory.getLogger(InMemoryMovieRepository.class);

    /**
     * List of movies stored in memory.
     * Thread-safe implementation using CopyOnWriteArrayList.
     */
    private static final List<Movie> movies = new CopyOnWriteArrayList<>();
    private static final AtomicInteger nextId = new AtomicInteger(1);
    private static final ReentrantReadWriteLock lock = new ReentrantReadWriteLock();

    /**
     * Finds a movie by its ID.
     * @param id The movie ID
     * @return
     */
    @Override
    public Optional<Movie> findById(int id) {
        lock.readLock().lock();
        try {
            //return the first movie that matches the id
            return movies.stream()
                    .filter(movie -> movie.getId() == id)
                    .findFirst();
        } finally {
            lock.readLock().unlock();
        }
    }

    /**
     * Finds movies by title.
     * @param title
     * @return
     */
    @Override
    public List<Movie> findByTitle(String title) {
        if (title == null || title.trim().isEmpty()) {
            return new ArrayList<>();
        }
        String searchTitle = title.trim().toLowerCase();
        //Lock the read lock to prevent concurrent modification which means that other threads cannot modify the list while this thread is reading it
        lock.readLock().lock();
        try {
            //return a list of movies that match the search title
            return movies.stream()
                    .filter(movie -> movie.getTitle().toLowerCase().contains(searchTitle))
                    .collect(Collectors.toList());
        } finally {
            lock.readLock().unlock();
        }
    }

    /**
     * Saves a movie to the repository.
     * @param movie The movie to save
     * @return
     */
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
                    throw new DuplicateEntryException("Movie", movie.getId(), "title", movie.getTitle());
                }
                //Assign a unique ID to the new movie
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
                        throw new DuplicateEntryException("Movie", movie.getId(), "title", movie.getTitle());
                    }
                    //Remove the existing movie and add the updated movie
                    movies.remove(existing.get());
                    movies.add(movie);
                    logger.debug("Updated movie: {} with ID: {}", movie.getTitle(), movie.getId());
                } else {
                    throw new IllegalArgumentException("Movie with ID " + movie.getId() + " not found");
                }
            }
            //Return the updated movie
            return movie;
        } finally {
            //Unlock the write lock when done,which means that other threads can modify the list
            lock.writeLock().unlock();
        }
    }

    /**
     * Checks if a movie with the given title exists in the repository.
     * @param title The movie title to check
     * @return
     */
    @Override
    public boolean existsByTitle(String title) {
        if (title == null) return false;

        lock.readLock().lock();
        try {
            /**
             * Check if any movie in the list has the same title as the given title
             */
            return movies.stream()
                    .anyMatch(movie -> title.equals(movie.getTitle()));
        } finally {
            //Unlock the read lock when done,which means that other threads can read the list
            lock.readLock().unlock();
        }
    }

    //count the number of movies in the repository
    @Override
    public long count() {
        return movies.size();
    }

    /**
     * Finds a movie by its title and release year.
     * @param title
     * @param startYear
     * @return
     */
    @Override
    public Movie findByTitleAndReleaseYear(String title, int startYear) {
        lock.readLock().lock();
        try {
            /**
             * Find the first movie in the list that has the same title and release year as the given title and start year
             */
            return movies.stream()
                    .filter(movie -> movie.getTitle().equalsIgnoreCase(title) &&
                            movie.getStartYear() == startYear)
                    .findFirst()
                    .orElse(null);
        }
        catch (Exception e) {
            throw new RuntimeException(e);
        }
        finally {
            //Unlock the read lock when done,which means that other threads can read the list
            lock.readLock().unlock();
        }
    }

    /**
     * Adds a movie to the repository.
     * @param movie
     */
    @Override
    public void add(Movie movie) {
        addMovie(movie);
        logger.info("Added new movie: {} with ID: {}", movie.getTitle(), movie.getId());
    }

    /**
     * Updates a movie in the repository.
     * @param movie
     */
    @Override
    public void update(Movie movie) {
        save(movie);
        logger.info("Updated movie: {} with ID: {}", movie.getTitle(), movie.getId());
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
            /**
             * If the movie has an ID, update the next ID to be the maximum of the current ID and the movie ID plus one
             */
            if (movie.getId() > 0) {
                nextId.getAndUpdate(current -> Math.max(current, movie.getId() + 1));
            } else {
                movie.setId(nextId.getAndIncrement());
            }
            movies.add(movie);
        } finally {
            /**
             * Unlock the write lock when done,which means that other threads can modify the list
             */
            lock.writeLock().unlock();
        }
    }

    //get all movies from the repository
    public List<Movie> getAll() {
        return movies;
    }
    public void delete(int id) {
        Optional<Movie> movieOptional = findById(id);
        if (movieOptional.isPresent()) {
            movies.remove(movieOptional.get());
            logger.info("Deleted movie with ID: {}", id);
        } else {
            logger.warn("Attempted to delete non-existent movie with ID: {}", id);
            throw new NoSuchElementException("Movie with ID " + id + " not found");
        }
    }
}