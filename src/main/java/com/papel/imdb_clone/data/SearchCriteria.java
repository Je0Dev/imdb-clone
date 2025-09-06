package com.papel.imdb_clone.data;

import com.papel.imdb_clone.enums.ContentType;
import com.papel.imdb_clone.enums.Genre;

import java.util.ArrayList;
import java.util.List;

public class SearchCriteria {
    private String query;
    private String title;
    private Double minImdbRating;
    private Double maxImdbRating;
    private Integer minYear;
    private List<Genre> genres;
    private Genre genre;
    private ContentType contentType;
    private final String sortBy; // "title", "year", "rating", "duration"
    private final boolean sortDescending;
    private Integer startYear;
    private Integer endYear;

    public SearchCriteria(String query, Double minImdb, Double maxImdb, Double minUser, Double maxUser, Integer minDuration, Integer maxDuration, List<Genre> selectedGenres, String description, Integer yearFrom, Integer yearTo) {
        this.query = query;
        this.minImdbRating = minImdb;
        this.maxImdbRating = maxImdb;
        this.genres = selectedGenres != null ? new ArrayList<>(selectedGenres) : new ArrayList<>();
        this.minYear = yearFrom;
        this.sortBy = "title";
        this.sortDescending = false;
    }

    public SearchCriteria(String query, String keywords, List<String> contentTypes, Integer yearFromValue, Integer yearToValue) {
        this.query = query != null ? query : "";
        this.title = keywords;
        this.minYear = yearFromValue;
        this.minImdbRating = null;
        this.maxImdbRating = null;
        this.genres = new ArrayList<>();
        this.sortBy = "title";
        this.sortDescending = false;

        // Set content type if provided
        if (contentTypes != null && !contentTypes.isEmpty()) {
            try {
                this.contentType = ContentType.valueOf(contentTypes.getFirst().toUpperCase());
            } catch (IllegalArgumentException e) {
                // If content type is invalid, leave it as null
                this.contentType = null;
            }
        }
    }

    public SearchCriteria(String sortBy, boolean sortDescending) {
        this.sortBy = sortBy;
        this.sortDescending = sortDescending;
    }

    public SearchCriteria(String query) {
        this.query = query;
        this.sortBy = "title";
        this.sortDescending = false;
    }


    public void setTitle(String title) {
        this.title = title;
    }

    public void setMinImdbRating(double rating) {
        this.minImdbRating = rating;
    }

    public void setMaxImdbRating(double rating) {
        this.maxImdbRating = rating;
    }

    public void setMinYear(Integer year) {
        this.minYear = year;
    }



    /**
     * Sets the minimum IMDb rating for filtering content.
     *
     * @param rating The minimum rating (0.0 to 10.0)
     * @throws IllegalArgumentException if rating is not between 0.0 and 10.0
     */
    public void setMinRating(double rating) {
        if (rating < 0.0 || rating > 10.0) {
            throw new IllegalArgumentException("Rating must be between 0.0 and 10.0");
        }
        this.minImdbRating = rating;
    }


    public void setGenre(Genre genre) {
        this.genre = genre;
    }

    public void setContentType(ContentType contentType) {
        this.contentType = contentType;
    }

    public void setQuery(String query) {
        this.query = query;
    }

    // Getters
    public String getQuery() {
        return query;
    }

    public String getTitle() {
        return title;
    }

    public ContentType getContentType() {
        return contentType;
    }

    public Genre getGenre() {
        return genre;
    }

    public Double getMinRating() {
        return minImdbRating;
    }

    public Integer getMinYear() {
        return minYear;
    }

    /**
     * Returns a string representation of this {@code SearchCriteria} object.
     * The string representation includes the values of all fields for debugging purposes.
     *
     * @return A string representation of this {@code SearchCriteria} object.
     */
    @Override
    public String toString() {
        return "SearchCriteria{" +
                "title='" + title + '\'' +
                ", minImdbRating=" + minImdbRating +
                ", maxImdbRating=" + maxImdbRating +
                ", minYear=" + minYear +
                ", genres=" + genres +
                ", sortBy='" + sortBy + '\'' +
                ", sortDescending=" + sortDescending +
                '}';
    }

    public Integer getStartYear() {
        return startYear;
    }

    public Object getMaxRating() {
        return maxImdbRating;
    }

    public void setMaxYear(Integer yearToValue) {
        this.endYear = yearToValue;
    }

    public Integer getEndYear() {
        return endYear;
    }
}