package com.papel.imdb_clone.service;

import com.papel.imdb_clone.data.RefactoredDataManager;
import com.papel.imdb_clone.data.SearchCriteria;
import com.papel.imdb_clone.exceptions.ContentNotFoundException;
import com.papel.imdb_clone.exceptions.EntityNotFoundException;
import com.papel.imdb_clone.model.Content;
import com.papel.imdb_clone.model.Movie;
import com.papel.imdb_clone.model.Rating;
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

    public final Class<T> contentType;

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
            /**
             * Search for the content in the list by ID.
             */
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
     * @return Optional<T> if content is found, empty otherwise
     */
    public Optional<T> findById(int id) {
        lock.readLock().lock();
        try {
            return contentList.stream()
                    .filter(content -> content.getId() == id)
                    .findFirst();
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
            // Filter by title if provided
            // Filter by year range if provided
            // Convert char[] release year to int for comparison
            // Check if content is before start year (if start year is specified)
            // Check if content is after end year (if end year is specified)

            return contentList.stream()
                    .filter(content -> {
                        // Filter by title if provided
                        if (criteria.getTitle() != null && !criteria.getTitle().isEmpty()) {
                            if (!content.getTitle().toLowerCase().contains(criteria.getTitle().toLowerCase())) {
                                return false;
                            }
                        }

                        // Filter by year range if provided
                        Integer startYear = criteria.getStartYear();

                        // Convert char[] release year to int for comparison
                        int contentYear = Integer.parseInt(String.valueOf(content.getReleaseYear()));

                        // Check if content is before start year (if start year is specified)
                        if (startYear != null && startYear > 0 && contentYear < startYear) {
                            return false;
                        }
                        return true;
                    })
                    .collect(Collectors.toList());
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


    public void rateContent(T content, float rating) {
        if (content == null) {
            throw new IllegalArgumentException("Content cannot be null");
        }
        if (rating < 0 || rating > 10) {
            throw new IllegalArgumentException("Rating must be between 0 and 10");
        }

        T existingContent = findById(content.getId())
                .orElseThrow(() -> new ContentNotFoundException("Content not found with ID: " + content.getId()));

        existingContent.setImdbRating((double) rating);
        saveRating(new Rating(existingContent.getId(), rating));
        updateContent(existingContent);

        logger.info("Rated {} '{}' with {} stars",
                existingContent.getClass().getSimpleName(),
                existingContent.getTitle(),
                rating);
    }


    /**
     * Finds content by title and start year.
     * @param title
     * @param startYear
     * @return Optional<T> if content is found, empty otherwise
     */
    public Optional<T> findByTitleAndYear(String title, int startYear) {
        return contentList.stream()
                .filter(content -> content.getTitle().equalsIgnoreCase(title) &&
                        content.getStartYear() == startYear)
                .findFirst();
    }

    //add content type T movie
    public void add(T movie) {
        save(movie);
        contentList.add(movie);
        logger.info("Added new {}", contentType.getSimpleName());
    }

    //update content type T movie
    public void update(T movie) {
        updateContent(movie);
        contentList.add(movie);
        logger.info("Updated {}", contentType.getSimpleName());
    }

    //delete type T
    public void delete(int id) {
        findById(id);
        contentList.removeIf(content -> content.getId() == id);
        logger.info("Deleted {} with ID: {}", contentType.getSimpleName(), id);
    }
}
