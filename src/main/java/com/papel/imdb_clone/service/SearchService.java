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
import java.util.function.Predicate;
import java.util.stream.Collectors;

/**
 * Service for searching content with filtering capabilities.
 */
public class SearchService {
    private static final Logger logger = LoggerFactory.getLogger(SearchService.class);
    private static SearchService instance;
    private final RefactoredDataManager dataManager;
    private int maxYear;

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
        logger.debug("Starting search with criteria: {}", criteria);

        if (criteria == null) {
            logger.debug("Null criteria, returning empty results");
            return Collections.emptyList();
        }

        // Log all criteria for debugging
        logger.debug("Search criteria - Query: '{}', Title: '{}', MinYear: {}, MinRating: {},MaxRating: {}, Genre: {}",
                criteria.getQuery(),
                criteria.getTitle(),
                criteria.getMinYear(),
                criteria.getMinRating(),
                criteria.getMaxRating(),
                criteria.getGenre());

        // Perform search
        List<Content> results = performSearch(criteria);
        logger.debug("Search returned {} results", results.size());
        return results;
    }

    /**
     * Performs the actual search operation.
     */
    private List<Content> performSearch(SearchCriteria criteria) {

        String query = criteria.getQuery() != null ? criteria.getQuery().toLowerCase() : "";
        ContentType type = criteria.getContentType();

        logger.debug("Performing search with type: {}", type);

        // Get content based on type
        List<Content> contentList;
        if (type == ContentType.MOVIE) {
            contentList = new ArrayList<>(dataManager.getAllMovies());
            logger.debug("Found {} movies to search through", contentList.size());
        } else if (type == ContentType.SERIES) {
            contentList = new ArrayList<>(dataManager.getAllSeries());
            logger.debug("Found {} series to search through", contentList.size());
        } else {

            // Search all content types
            List<Content> movies = new ArrayList<>(dataManager.getAllMovies());
            List<Content> series = new ArrayList<>(dataManager.getAllSeries());
            contentList = new ArrayList<>();
            contentList.addAll(movies);
            contentList.addAll(series);
            logger.debug("Found {} total items to search through ({} movies, {} series)",
                    contentList.size(), movies.size(), series.size());
        }

        // Apply search filters
        List<Predicate<Content>> filters = createSearchFilters(criteria);
        logger.debug("Created {} filters to apply", filters.size());

        // Apply all filters and collect results
        List<Content> results = contentList.stream()
                .filter(content -> {
                    boolean matches = filters.stream().allMatch(filter -> {
                        boolean result = filter.test(content);
                        if (!result) {
                            logger.trace("Content '{}' filtered out by a filter",
                                    content != null ? content.getTitle() : "null");
                        }
                        return result;
                    });
                    //log the content that passed all filters
                    if (matches) {
                        logger.trace("Content '{}' passed all filters",
                                content != null ? content.getTitle() : "null");
                    }
                    return matches;
                })
                //collect the results from the stream that passed all filters
                .collect(Collectors.toList());

        logger.debug("After applying filters, found {} matching items", results.size());
        return results;
    }


    /**
     * Creates a list of filters based on the search criteria.
     * @param criteria
     * @return List of filters to apply to the content list
     * Predicate<Content> is a function that takes a Content object as input and returns a boolean value
     */
    private List<Predicate<Content>> createSearchFilters(SearchCriteria criteria) {
        List<Predicate<Content>> filters = new ArrayList<>();
        if (criteria == null) {
            logger.debug("Null criteria in createSearchFilters, returning empty filters");
            return filters;
        }

        // Log all criteria for debugging
        logger.debug("Creating filters with criteria - Title: '{}', Query: '{}', MinYear: {},Max, MinRating: {},MaxRating: {}",
                criteria.getTitle(), criteria.getQuery(), criteria.getMinYear(), criteria.getMinRating(),criteria.getMaxRating() );

        // Text search filter - check both query and title
        String searchText = "";
        if (criteria.getTitle() != null && !criteria.getTitle().trim().isEmpty()) {
            searchText = criteria.getTitle().trim().toLowerCase();
        } else if (criteria.getQuery() != null && !criteria.getQuery().trim().isEmpty()) {
            searchText = criteria.getQuery().trim().toLowerCase();
        }

        /**
         * Text search filter - check both query and title
         */
        if (!searchText.isEmpty()) {
            final String finalSearchText = searchText; // Need final for lambda
            logger.debug("Adding text search filter for: '{}'", finalSearchText);
            filters.add(content -> {
                if (content == null || content.getTitle() == null) {
                    logger.trace("Content or title is null in text search filter");
                    return false;
                }
                String title = content.getTitle().toLowerCase();
                boolean matches = title.contains(finalSearchText);
                logger.trace("Text search filter - Title: '{}', Search: '{}', Match: {}",
                        title, finalSearchText, matches);
                return matches;
            });
        }


        // Genre filter
        if (criteria.getGenre() != null) {
            filters.add(content -> {
                if (content == null || content.getGenres() == null) return false;
                return content.getGenres().contains(criteria.getGenre());
            });
        }

        // Year range filter - handle both min and max years
        if (criteria.getMinYear() != null && criteria.getMinYear() > 0) {
            int minYear = criteria.getMinYear();
            logger.debug("Adding min year filter: {}", minYear);
            filters.add(content -> {
                if (content == null) {
                    logger.trace("Content is null in min year filter");
                    return false;
                }
                try {
                    // First try to get year from startYear field
                    int contentYear = content.getStartYear();
                    
                    // If startYear is 0 (default), try to get from the Date field
                    if (contentYear == 0 && content.getYear() != null) {
                        Calendar cal = Calendar.getInstance();
                        cal.setTime(content.getYear());
                        contentYear = cal.get(Calendar.YEAR);
                    }
                    
                    boolean matches = contentYear >= minYear;
                    logger.debug("Min Year Check - Title: '{}', Content Year: {}, Min Year: {}, Match: {}",
                            content.getTitle(), contentYear, minYear, matches);
                    return matches;
                } catch (Exception e) {
                    logger.warn("Error processing year for content: {}", content.getTitle(), e);
                    return false;
                }
            });
        }



        // Rating filter
        if (criteria.getMinRating() != null && criteria.getMinRating() > 0) {
            double minRating = criteria.getMinRating();
            logger.debug("Adding rating filter for min rating: {}", minRating);
            filters.add(content -> {
                if (content == null) {
                    logger.trace("Content is null in rating filter");
                    return false;
                }
                Double rating = content.getImdbRating();
                boolean matches = rating != null && rating >= minRating;
                logger.trace("Rating filter - Content: '{}', Rating: {}, Min: {}, Match: {}",
                        content.getTitle(), rating, minRating, matches);
                return matches;
            });
        }

        //Max rating filter
        if (criteria.getMaxRating() != null && (int)criteria.getMaxRating() > 0) {
            int maxRating = (int)criteria.getMaxRating();
            logger.debug("Adding rating filter for max rating: {}", maxRating);
            filters.add(content -> {
                if (content == null) {
                    logger.trace("Content is null in rating filter");
                    return false;
                }
                //get the rating from the content
                Double rating = content.getImdbRating();
                boolean matches = rating != null && rating >= maxRating;
                logger.trace("Rating filter - Content: '{}', Rating: {}, Max: {}, Match: {}",
                        content.getTitle(), rating, maxRating, matches);
                return matches;
            });
        }

        return filters;
    }

    //search content
    public List<Content> searchContent(SearchCriteria criteria) {
        return search(criteria);
    }
}
