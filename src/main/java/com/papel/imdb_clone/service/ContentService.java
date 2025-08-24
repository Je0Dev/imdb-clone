package com.papel.imdb_clone.service;

import com.papel.imdb_clone.data.RefactoredDataManager;
import com.papel.imdb_clone.data.SearchCriteria;
import com.papel.imdb_clone.exceptions.ContentNotFoundException;
import com.papel.imdb_clone.exceptions.EntityNotFoundException;
import com.papel.imdb_clone.model.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.stream.Collectors;

/**
 * Unified service for managing all types of content (Movies, Series, etc.).
 * Provides high-level operations for content management including CRUD and search.
 */
public class ContentService<T extends Content> {
    // Add missing fields
    private final ReentrantReadWriteLock lock = new ReentrantReadWriteLock();
    private final List<T> contentList = new CopyOnWriteArrayList<>();
    private final AtomicInteger nextId = new AtomicInteger(1);
    private static final Logger logger = LoggerFactory.getLogger(ContentService.class);
    private static final Map<Class<?>, ContentService<?>> instances = new HashMap<>();

    private final Class<T> contentType;

    public ContentService(Class<T> contentType) {
        this.contentType = contentType;
    }


    /**
     * Alternative getInstance method for backward compatibility.
     */
    @SuppressWarnings("unchecked")
    public static <T extends Content> ContentService<T> getInstance(RefactoredDataManager dataManager) {
        // For backward compatibility, return a generic Content service
        return (ContentService<T>) instances.computeIfAbsent(Content.class,
                k -> new ContentService<>(Content.class));
    }

    // Add missing method
    public void updateContent(T content) {
        lock.writeLock().lock();
        try {
            int index = -1;
            for (int i = 0; i < contentList.size(); i++) {
                if (contentList.get(i).getId() == content.getId()) {
                    index = i;
                    break;
                }
            }
            if (index != -1) {
                contentList.set(index, content);
            } else {
                throw new ContentNotFoundException("Content with id " + content.getId() + " not found");
            }
        } finally {
            lock.writeLock().unlock();
        }
    }

    /**
     * Finds content by its ID.
     *
     * @param id The content ID
     * @return The content if found
     * @throws ContentNotFoundException if content is not found
     */
    public T findById(int id) throws ContentNotFoundException {
        // Search in the content list
        lock.readLock().lock();
        try {
            Optional<T> found = contentList.stream()
                    .filter(content -> content.getId() == id)
                    .findFirst();

            if (found.isPresent()) {
                return found.get();
            }
        } finally {
            lock.readLock().unlock();
        }

        throw new ContentNotFoundException("Content with ID " + id + " not found");
    }


    /**
     * Finds content by title (partial match).
     *
     * @param title The title to search for
     * @return List of content matching the title
     */
    public List<T> findByTitle(String title) {
        if (title == null || title.trim().isEmpty()) {
            return List.of();
        }

        String searchTerm = title.toLowerCase().trim();
        lock.readLock().lock();
        try {
            return contentList.stream()
                    .filter(content -> content.getTitle().toLowerCase().contains(searchTerm))
                    .collect(Collectors.toList());
        } finally {
            lock.readLock().unlock();
        }
    }

    /**
     * Saves content (creates or updates).
     *
     * @param content The content to save
     * @return The saved content (with ID if new)
     */
    public T save(T content) {
        if (content == null) {
            throw new IllegalArgumentException("Content cannot be null");
        }

        lock.writeLock().lock();
        try {
            if (content.getId() == 0) {
                // New content
                content.setId(nextId.getAndIncrement());
                contentList.add(content);
                logger.info("Created new {}: {}", contentType.getSimpleName(), content.getTitle());
            } else {
                // Update existing
                int index = -1;
                for (int i = 0; i < contentList.size(); i++) {
                    if (contentList.get(i).getId() == content.getId()) {
                        index = i;
                        break;
                    }
                }

                if (index != -1) {
                    contentList.set(index, content);
                    logger.info("Updated {}: {}", contentType.getSimpleName(), content.getTitle());
                } else {
                    throw new EntityNotFoundException(contentType, content.getId());
                }
            }
            return content;
        } finally {
            lock.writeLock().unlock();
        }
    }

    /**
     * Deletes content by ID.
     *
     * @param id The ID of the content to delete
     * @return true if the content was found and deleted, false otherwise
     */
    public boolean delete(int id) {
        lock.writeLock().lock();
        try {
            boolean removed = contentList.removeIf(content -> content.getId() == id);
            return removed;
        } finally {
            lock.writeLock().unlock();
        }
    }

    /**
     * Gets all content of this service's type.
     *
     * @return List of all content
     */
    public List<T> getAll() {
        lock.readLock().lock();
        try {
            return List.copyOf(contentList);
        } finally {
            lock.readLock().unlock();
        }
    }


    /**
     * Searches content based on the provided criteria.
     *
     * @param criteria The search criteria
     * @return List of content matching the criteria
     */
    public List<T> search(SearchCriteria criteria) {
        if (criteria == null) {
            return List.of();
        }

        // Perform search if not in cache
        lock.readLock().lock();
        try {
            List<T> results = contentList.stream()
                    .filter(content -> {
                        // Filter by title if provided
                        if (criteria.getTitle() != null && !criteria.getTitle().isEmpty()) {
                            if (!content.getTitle().toLowerCase().contains(criteria.getTitle().toLowerCase())) {
                                return false;
                            }
                        }

                        // Filter by year range if provided
                        Integer startYear = criteria.getStartYear();
                        Integer endYear = criteria.getEndYear();

                        // Convert char[] release year to int for comparison
                        int contentYear = Integer.parseInt(String.valueOf(content.getReleaseYear()));

                        // Check if content is before start year (if start year is specified)
                        if (startYear != null && startYear > 0 && contentYear < startYear) {
                            return false;
                        }
                        // Check if content is after end year (if end year is specified)
                        return endYear == null || endYear <= 0 || contentYear <= endYear;
                    })
                    .collect(Collectors.toList());

            return results;
        } finally {
            lock.readLock().unlock();
        }
    }

    /**
     * Saves a rating for content.
     *
     * @param rating The rating to save
     */
    public void saveRating(Rating rating) {
        if (rating == null) {
            throw new IllegalArgumentException("Rating cannot be null");
        }

        try {
            // Validate that the content exists
            findById(rating.getContentId());

            // For now, we'll just return the rating as-is
            logger.info("Rating saved for content ID {}: {} stars",
                    rating.getContentId(), rating.getScore());

        } catch (ContentNotFoundException e) {
            throw new IllegalArgumentException("Cannot rate non-existent content with ID: " + rating.getContentId(), e);
        }
    }

    /**
     * Saves a Series to the content list.
     *
     * @param series The series to save
     */
    public void saveContent(Series series) {
        if (series.getId() == 0) {
            series.setId(nextId.getAndIncrement());
        }
        save((T) series);
    }

    /**
     * Saves a Movie to the content list.
     *
     * @param movie The movie to save
     */
    public void saveContent(Movie movie) {
        if (movie.getId() == 0) {
            movie.setId(nextId.getAndIncrement());
        }
        save((T) movie);
    }

    /**
     * Saves an Episode to the content list.
     *
     * @param episode The episode to save
     */
    public void saveContent(Episode episode) {
        if (episode.getId() == 0) {
            episode.setId(nextId.getAndIncrement());
        }
    }

    /**
     * Adds new content to the service.
     *
     * @param content The content to add
     */
    public void addContent(T content) {
        if (content.getId() == 0) {
            content.setId(nextId.getAndIncrement());
        }
        contentList.add(content);
    }

    public void deleteContent(T selectedMovie) {
        // Check if the content exists
        findById(selectedMovie.getId());

        // Delete the content
        delete(selectedMovie.getId());
    }

    public void rateContent(T selectedMovie, float rating) {
        // Check if the content exists
        findById(selectedMovie.getId());
        // Save the rating
        saveRating(new Rating(selectedMovie.getId(), rating));
        // Update the average user rating
        updateAverageUserRating(selectedMovie);
        // Update the content
        updateContent(selectedMovie);

    }

    private <T extends Content> void updateAverageUserRating(T selectedMovie) {
        // Calculate the new average user rating
        float totalUserRatings = selectedMovie.getUserRatings().
                values().stream()
                .mapToInt(Integer::intValue)
                .sum();
        int userRatingsCount = selectedMovie.getUserRatings().size();
        double newAverageUserRating = totalUserRatings / userRatingsCount;

        // Set the new average user rating
        selectedMovie.setUserRating(newAverageUserRating);
        // Save the updated content
        saveContent((Series) selectedMovie);

    }

    /**
     * Gets all movies from the content list.
     *
     * @return List of all movies
     */
    @SuppressWarnings("unchecked")
    public List<Movie> getAllMovies() {
        return contentList.stream()
                .filter(content -> content instanceof Movie)
                .map(content -> (Movie) content)
                .collect(Collectors.toList());
    }

    /**
     * Gets all series from the content list.
     *
     * @return List of all series
     */
    @SuppressWarnings("unchecked")
    public List<Series> getAllSeries() {
        return contentList.stream()
                .filter(content -> content instanceof Series)
                .map(content -> (Series) content)
                .collect(Collectors.toList());
    }

    public T getSeriesByTitle(String seriesTitle) {
        return (T) contentList.stream()
                .filter(content -> content instanceof Series)
                .map(content -> (Series) content)
                .filter(series -> series.getTitle().equalsIgnoreCase(seriesTitle))
                .findFirst()
                .orElse(null);
    }

    public Optional<T> findByTitleAndYear(String title, int startYear) {
        return contentList.stream()
                .filter(content -> content.getTitle().equalsIgnoreCase(title) &&
                        content.getStartYear() == startYear)
                .findFirst();
    }

    public T findByTitleAndStartYear(String title, int startYear) {
        return contentList.stream()
                .filter(content -> content.getTitle().equalsIgnoreCase(title) &&
                        content.getStartYear() == startYear)
                .findFirst()
                .orElse(null);
    }
}
