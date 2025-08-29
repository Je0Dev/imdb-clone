package com.papel.imdb_clone.data;

import com.papel.imdb_clone.enums.ContentType;
import com.papel.imdb_clone.enums.Genre;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class SearchCriteria {
    private String query;
    private String title;
    private String actorName;
    private String directorName;
    private Double minImdbRating;
    private Double maxImdbRating;
    private Double minUserRating;
    private Double maxUserRating;
    private Integer minYear;
    private Integer maxYear;
    private Integer minDuration;
    private Integer maxDuration;
    private List<Genre> genres;
    private Genre genre;
    private ContentType contentType;
    private String description;
    private final String sortBy; // "title", "year", "rating", "duration"
    private final boolean sortDescending;
    private Integer startYear;
    private Integer endYear;

    public SearchCriteria(String query, String actorName, String directorName, Double minImdb, Double maxImdb, Double minUser, Double maxUser, Integer minDuration, Integer maxDuration, List<Genre> selectedGenres, String description, Integer yearFrom, Integer yearTo) {
        this.query = query;
        this.minImdbRating = null;
        this.maxImdbRating = null;
        this.minUserRating = null;
        this.maxUserRating = null;
        this.genres = new ArrayList<>();
        this.sortBy = "title";
        this.sortDescending = false;
    }

    public SearchCriteria(String query, String keywords, List<String> contentTypes, Integer yearFromValue, Integer yearToValue) {
        this.query = query;
        this.minImdbRating = null;
        this.maxImdbRating = null;
        this.minUserRating = null;
        this.maxUserRating = null;
        this.genres = new ArrayList<>();
        this.sortBy = "title";
        this.sortDescending = false;
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

    public void setActorName(String actorName) {
        this.actorName = actorName;
    }

    public void setDirectorName(String directorName) {
        this.directorName = directorName;
    }

    public void setMinImdbRating(double rating) {
        this.minImdbRating = rating;
    }

    public void setMinUserRating(double rating) {
        this.minUserRating = rating;
    }

    public void setMaxImdbRating(double rating) {
        this.maxImdbRating = rating;
    }

    public void setMaxUserRating(double rating) {
        this.maxUserRating = rating;
    }

    public void setMinYear(Integer year) {
        this.minYear = year;
    }


    /**
     * Sets the maximum year for filtering content.
     *
     * @param year The maximum year (inclusive)
     * @return this SearchCriteria instance for method chaining
     */
    public SearchCriteria setMaxYear(Integer year) {
        this.maxYear = year;
        return this;
    }

    /**
     * Sets the genres for filtering content.
     *
     * @param genres List of genres to filter by
     * @return this SearchCriteria instance for method chaining
     */
    public SearchCriteria setGenres(Genre... genres) {
        if (genres != null) {
            this.genres = Arrays.asList(genres);
        } else {
            this.genres = new ArrayList<>();
        }
        return this;
    }

    /**
     * Adds a genre to the list of genres to filter by.
     *
     * @param genre The genre to add
     * @return this SearchCriteria instance for method chaining
     */
    public SearchCriteria addGenre(Genre genre) {
        if (genre != null) {
            if (this.genres == null) {
                this.genres = new ArrayList<>();
            }
            this.genres.add(genre);
        }
        return this;
    }

    /**
     * Sets the minimum IMDb rating for filtering content.
     *
     * @param rating The minimum rating (0.0 to 10.0)
     * @return this SearchCriteria instance for method chaining
     * @throws IllegalArgumentException if rating is not between 0.0 and 10.0
     */
    public SearchCriteria setMinRating(double rating) {
        if (rating < 0.0 || rating > 10.0) {
            throw new IllegalArgumentException("Rating must be between 0.0 and 10.0");
        }
        this.minImdbRating = rating;
        return this;
    }

    /**
     * Sets the maximum IMDb rating for filtering content.
     *
     * @param rating The maximum rating (0.0 to 10.0)
     * @return this SearchCriteria instance for method chaining
     * @throws IllegalArgumentException if rating is not between 0.0 and 10.0
     */
    public SearchCriteria setMaxRating(double rating) {
        if (rating < 0.0 || rating > 10.0) {
            throw new IllegalArgumentException("Rating must be between 0.0 and 10.0");
        }
        this.maxImdbRating = rating;
        return this;
    }

    public void setMinDuration(Integer duration) {
        this.minDuration = duration;
    }

    public void setMaxDuration(Integer duration) {
        this.maxDuration = duration;
    }

    public void setGenres(List<Genre> genres) {
        this.genres = genres != null ? new ArrayList<>(genres) : new ArrayList<>();
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

    public void setDescription(String description) {
        this.description = description;
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

    public Integer getMaxYear() {
        return maxYear;
    }

    public String getDescription() {
        return description;
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
                ", actorName='" + actorName + '\'' +
                ", directorName='" + directorName + '\'' +
                ", minImdbRating=" + minImdbRating +
                ", maxImdbRating=" + maxImdbRating +
                ", minUserRating=" + minUserRating +
                ", maxUserRating=" + maxUserRating +
                ", minYear=" + minYear +
                ", maxYear=" + maxYear +
                ", minDuration=" + minDuration +
                ", maxDuration=" + maxDuration +
                ", genres=" + genres +
                ", description='" + description + '\'' +
                ", sortBy='" + sortBy + '\'' +
                ", sortDescending=" + sortDescending +
                '}';
    }

    public Integer getStartYear() {
        return startYear;
    }

    // Assign the provided startYear to the instance variable
    public Integer getEndYear() {
        return endYear;
    }

    public String getActorName() {
        return actorName;
    }

    public String getDirectorName() {
        return directorName;
    }
}