package com.papel.imdb_clone.service;

import com.papel.imdb_clone.data.RefactoredDataManager;
import com.papel.imdb_clone.data.SearchCriteria;
import com.papel.imdb_clone.enums.ContentType;
import com.papel.imdb_clone.model.Content;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Service for searching content with filtering capabilities.
 */
public class SearchService {
    private static final Logger logger = LoggerFactory.getLogger(SearchService.class);
    private static SearchService instance;

    private final ContentService contentService;
    private final ExecutorService executorService;
    private final RefactoredDataManager dataManager;
    private int i;

    /**
     * Creates a new SearchService.
     *
     * @param dataManager The data manager to use for data access
     */
    public SearchService(RefactoredDataManager dataManager) {
        this.dataManager = dataManager;
        this.contentService = new ContentService(Content.class);
        this.executorService = Executors.newFixedThreadPool(
                Runtime.getRuntime().availableProcessors(),
                r -> {
                    Thread t = new Thread(r);
                    t.setDaemon(true);
                    t.setName("search-service-" + t.getId());
                    return t;
                }
        );
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
        List<Content> results = performSearch(criteria);

        return results;
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

    /**
     * Creates search filters based on the search criteria.
     */
    private List<Predicate<Content>> createSearchFilters(SearchCriteria criteria) {
        List<Predicate<Content>> filters = new ArrayList<>();

        // Text search filter
        if (criteria.getQuery() != null && !criteria.getQuery().trim().isEmpty()) {
            String query = criteria.getQuery().toLowerCase();
            filters.add(content ->
                    content.getTitle().toLowerCase().contains(query) ||
                            (content.getSummary() != null && content.getSummary().toLowerCase().contains(query))
            );
        }

        // Genre filter
        if (criteria.getGenre() != null) {
            filters.add(content -> content.getGenre() == criteria.getGenre());
        }

        // Year range filter
        if (criteria.getMinYear() > 0) {
            filters.add(content -> {
                if (content.getYear() == null) return false;
                Calendar cal = Calendar.getInstance();
                cal.setTime(content.getYear());
                return cal.get(Calendar.YEAR) >= criteria.getMinYear();
            });
        }
        if (criteria.getMaxYear() > 0) {
            filters.add(content -> {
                if (content.getYear() == null) return false;
                Calendar cal = Calendar.getInstance();
                cal.setTime(content.getYear());
                return cal.get(Calendar.YEAR) <= criteria.getMaxYear();
            });
        }

        // Rating filter
        if (criteria.getMinRating() > 0) {
            filters.add(content -> content.getImdbRating() >= criteria.getMinRating());
        }

        return filters;
    }

    public List<Content> searchContent(SearchCriteria criteria) {
        return search(criteria);
    }
}
