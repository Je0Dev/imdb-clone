package com.papel.imdb_clone.service;

import com.papel.imdb_clone.data.RefactoredDataManager;
import com.papel.imdb_clone.data.SearchCriteria;
import com.papel.imdb_clone.enums.ContentType;
import com.papel.imdb_clone.model.Actor;
import com.papel.imdb_clone.model.Content;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Service for searching content with filtering capabilities.
 */
public class SearchService {
    private static final Logger logger = LoggerFactory.getLogger(SearchService.class);
    private static SearchService instance;

    private final RefactoredDataManager dataManager;
    private Predicate<? super Actor> nonNull;

    /**
     * Creates a new SearchService.
     *
     * @param dataManager The data manager to use for data access
     */
    public SearchService(RefactoredDataManager dataManager) {
        this.dataManager = dataManager;
        logger.info("SearchService initialized");
    }

    /**
     * Performs a search with the given criteria.
     *
     * @param criteria The search criteria
     * @return List of matching content
     */
    public List<Content> search(SearchCriteria criteria) {
        if (criteria == null || criteria.getQuery() == null || criteria.getQuery().trim().isEmpty()) {
            logger.debug("Empty search query, returning empty results");
            return Collections.emptyList();
        }

        String query = criteria.getQuery().trim();
        logger.debug("Performing search for query: {}", query);

        // Perform search
        return performSearch(criteria);
    }

    /**
     * Performs the actual search operation.
     */
    private List<Content> performSearch(SearchCriteria criteria) {
        String query = criteria.getQuery().toLowerCase();
        ContentType type = criteria.getContentType();

        // Get content based on type
        Stream<Content> contentStream;
        if (type == ContentType.MOVIE) {
            contentStream = dataManager.getAllMovies().stream()
                    .map(m -> m);
        } else if (type == ContentType.SERIES) {
            contentStream = dataManager.getAllSeries().stream()
                    .map(s -> s);
        } else {
            // Search all content types
            contentStream = Stream.concat(
                    dataManager.getAllMovies().stream().map(m -> (Content) m),
                    dataManager.getAllSeries().stream().map(s -> (Content) s)
            );
        }

        // Apply search filters
        List<Predicate<Content>> filters = createSearchFilters(criteria);

        // Apply all filters and collect results
        return contentStream
                .filter(content -> filters.stream().allMatch(filter -> filter.test(content)))
                .collect(Collectors.toList());
    }


    private List<Predicate<Content>> createSearchFilters(SearchCriteria criteria) {
        List<Predicate<Content>> filters = new ArrayList<>();
        if (criteria == null) {
            return filters;
        }

        // Text search filter (title or summary)
        String searchText = criteria.getQuery() != null ? criteria.getQuery().trim().toLowerCase() : "";
        if (!searchText.isEmpty()) {
            filters.add(content -> {
                if (content == null) return false;
                String title = content.getTitle() != null ? content.getTitle().toLowerCase() : "";
                String summary = content.getSummary() != null ? content.getSummary().toLowerCase() : "";
                return title.contains(searchText) || summary.contains(searchText);
            });
        }

        // Actor filter
        if (criteria.getActorName() != null && !criteria.getActorName().trim().isEmpty()) {
            String actorName = criteria.getActorName().trim().toLowerCase();
            filters.add(content -> {
                if (content == null || content.getActors() == null) return false;
                return content.getActors().stream()
                        .filter(nonNull)
                        .anyMatch(actor -> {
                            String firstName = actor.getFirstName() != null ? actor.getFirstName().toLowerCase() : "";
                            String lastName = actor.getLastName() != null ? actor.getLastName().toLowerCase() : "";
                            String fullName = (firstName + " " + lastName).trim();
                            return fullName.contains(actorName) ||
                                    firstName.contains(actorName) ||
                                    lastName.contains(actorName);
                        });
            });
        }

        // Director filter
        if (criteria.getDirectorName() != null && !criteria.getDirectorName().trim().isEmpty()) {
            String directorName = criteria.getDirectorName().trim().toLowerCase();
            filters.add(content -> {
                if (content == null) return false;
                String director = content.getDirector() != null ? content.getDirector().toLowerCase() : "";
                return director.contains(directorName);
            });
        }

        // Genre filter
        if (criteria.getGenre() != null) {
            filters.add(content -> {
                if (content == null || content.getGenres() == null) return false;
                return content.getGenres().contains(criteria.getGenre());
            });
        }

        // Year range filter
        if (criteria.getMinYear() != null && criteria.getMinYear() > 0) {
            filters.add(content -> {
                if (content == null || content.getYear() == null) return false;
                try {
                    Calendar cal = Calendar.getInstance();
                    cal.setTime(content.getYear());
                    return cal.get(Calendar.YEAR) >= criteria.getMinYear();
                } catch (Exception e) {
                    logger.warn("Error processing year for content: " + content.getTitle(), e);
                    return false;
                }
            });
        }

        if (criteria.getMaxYear() != null && criteria.getMaxYear() > 0) {
            filters.add(content -> {
                if (content == null || content.getYear() == null) return false;
                try {
                    Calendar cal = Calendar.getInstance();
                    cal.setTime(content.getYear());
                    return cal.get(Calendar.YEAR) <= criteria.getMaxYear();
                } catch (Exception e) {
                    logger.warn("Error processing year for content: " + content.getTitle(), e);
                    return false;
                }
            });
        }

        // Rating filter
        if (criteria.getMinRating() != null && criteria.getMinRating() > 0) {
            filters.add(content -> {
                if (content == null) return false;
                Double rating = content.getImdbRating();
                return rating != null && rating >= criteria.getMinRating();
            });
        }

        return filters;
    }

    public List<Content> searchContent(SearchCriteria criteria) {
        return search(criteria);
    }
}
