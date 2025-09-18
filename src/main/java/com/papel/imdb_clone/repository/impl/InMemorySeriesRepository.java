package com.papel.imdb_clone.repository.impl;

import com.papel.imdb_clone.exceptions.DuplicateEntryException;
import com.papel.imdb_clone.model.Movie;
import com.papel.imdb_clone.model.Series;
import com.papel.imdb_clone.repository.SeriesRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * In-memory implementation of SeriesRepository.
 * Thread-safe implementation using CopyOnWriteArrayList and ReentrantReadWriteLock.
 */
public class InMemorySeriesRepository implements SeriesRepository {
    private static final Logger logger = LoggerFactory.getLogger(InMemorySeriesRepository.class);

    private final List<Series> seriesList = new CopyOnWriteArrayList<>();
    private final AtomicInteger nextId = new AtomicInteger(1);
    private final ReentrantReadWriteLock lock = new ReentrantReadWriteLock();

    /**
     * Finds a series by its ID.
     * @param id The series ID
     * @return
     */
    @Override
    public Optional<Series> findById(int id) {
        lock.readLock().lock();
        try {
            /**
             * Returns the first series that matches the given ID.
             */
            return seriesList.stream()
                    .filter(series -> series.getId() == id)
                    .findFirst();
        } finally {
            lock.readLock().unlock();
        }
    }

    /**
     * Finds a series by its title.
     * @param title The series title to search for
     * @return
     */
    @Override
    public Optional<Series> findByTitle(String title) {
        if (title == null) return Optional.empty();

        lock.readLock().lock();
        try {
            /**
             * Returns the first series that matches the given title.
             */
            return seriesList.stream()
                    .filter(series -> title.equals(series.getTitle()))
                    .findFirst();
        } finally {
            lock.readLock().unlock();
        }
    }

    /**
     * Returns a list of all series.
     * @return
     */
    @Override
    public List<Series> findAll() {
        lock.readLock().lock();
        try {
            return new ArrayList<>(seriesList);
        } finally {
            lock.readLock().unlock();
        }
    }

    /**
     * Saves a series to the repository.
     * @param series The series to save
     * @return
     */
    @Override
    public Series save(Series series) {
        if (series == null) {
            throw new IllegalArgumentException("Series cannot be null");
        }

        lock.writeLock().lock();
        try {
            /**
             * If the series ID is 0, it is a new series and we need to check for duplicate title.
             */
            if (series.getId() == 0) {
                // New series - check for duplicate title
                if (existsByTitle(series.getTitle())) {
                    throw new DuplicateEntryException("Series", series.getId(), "title", series.getTitle());
                }
                series.setId(nextId.getAndIncrement());
                seriesList.add(series);
                logger.debug("Created new series: {} with ID: {}", series.getTitle(), series.getId());
            } else {
                // Existing series - update
                Optional<Series> existing = findById(series.getId());
                if (existing.isPresent()) {
                    // Check if title is being changed to an existing one
                    if (!existing.get().getTitle().equals(series.getTitle()) &&
                            existsByTitle(series.getTitle())) {
                        throw new DuplicateEntryException("Series", series.getId(), "title", series.getTitle());
                    }
                    /**
                     * Remove the existing series and add the updated series.
                     */
                    seriesList.remove(existing.get());
                    seriesList.add(series);
                    logger.debug("Updated series: {} with ID: {}", series.getTitle(), series.getId());
                } else {
                    throw new IllegalArgumentException("Series with ID " + series.getId() + " not found");
                }
            }
            /**
             * Return the updated series.
             */
            return series;
        } finally {
            lock.writeLock().unlock();
        }
    }

    /**
     * Deletes a series by its ID.
     * @param id The ID of the series to delete
     * @return
     */
    @Override
    public boolean deleteById(int id) {
        lock.writeLock().lock();
        try {
            /**
             * Find the series by ID.
             */
            Optional<Series> existing = findById(id);
            if (existing.isPresent()) {
                seriesList.remove(existing.get());
                logger.debug("Deleted series with ID: {}", id);
                return true;
            }
            return false;
        } finally {
            lock.writeLock().unlock();
        }
    }

    /**
     * Checks if a series with the given title exists.
     * @param title The title of the series to check
     * @return true if a series with the given title exists, false otherwise
     */
    @Override
    public boolean existsByTitle(String title) {
        if (title == null) return false;

        lock.readLock().lock();
        try {
            return seriesList.stream()
                    .anyMatch(series -> title.equals(series.getTitle()));
        } finally {
            lock.readLock().unlock();
        }
    }

    //count total series size
    @Override
    public long count() {
        return seriesList.size();
    }

    //delete series by id
    public void delete(int id) {
        deleteById(id);
        logger.debug("Deleted series with ID: {}", id);
    }

    public void remove(Series selected) {
        deleteById(selected.getId());
        seriesList.remove(selected);
        logger.info("Removed series with ID: {}", selected.getId());

    }
}
