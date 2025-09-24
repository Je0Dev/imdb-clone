package com.papel.imdb_clone.model.content;

import com.papel.imdb_clone.enums.Genre;
import com.papel.imdb_clone.model.people.Actor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

/**
 * Content class is an abstract class that represents a content.
 */
public abstract class Content {
    private Date year;
    private String director;
    private int id;
    public String title;
    protected Genre genre;
    private Map<Integer, Integer> userRatings; // userId -> rating
    private Double imdbRating; // IMDb rating
    private Date releaseDate;
    int startYear;
    //Genres and actors
    private List<Genre> genres = new ArrayList<>();
    private List<Actor> actors = new ArrayList<>();
    private static final Logger logger = LoggerFactory.getLogger(Content.class);
    private Object contentType;
    private String series;


    /**
     * Content constructor
     * @param title content title
     * @param year content release year
     * @param genre content genre
     * @param director content director
     * @param userRatings user ratings
     * @param imdbRating imdb rating
     */
    public Content(String title, Date year, Genre genre, String director, Map<Integer, Integer> userRatings, Double imdbRating) {
        this.title = title;
        this.genre = genre;
        this.director = director;
        this.userRatings = new HashMap<>();
        this.imdbRating = imdbRating;

        // Initialize release date and year
        if (year != null) {
            this.year = new Date(year.getTime());
            // Set the release date based on the year
            Calendar cal = Calendar.getInstance();
            cal.setTime(this.year);
            //Set start year and release date
            this.startYear = cal.get(Calendar.YEAR);
            this.releaseDate = new Date(this.year.getTime());
        } else {
            // Default to current date if year is not provided
            this.year = new Date();
            Calendar cal = Calendar.getInstance();
            this.startYear = cal.get(Calendar.YEAR);
            this.releaseDate = new Date();
        }
    }

    //getters
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public Genre getGenre() {
        return genre;
    }

    public List<Genre> getGenres() {
        return new ArrayList<>(genres);
    }

    public String getDirector() {
        return director;
    }

    public Double getImdbRating() {
        return imdbRating;
    }

    public Date getYear() {
        return year;
    }

    public int getReleaseYear() {
        return startYear; // Using startYear as release year
    }

    public Map<Integer, Integer> getUserRatings() {
        return new HashMap<>(userRatings);
    }

    /**
     * Gets the start year of the content.
     *
     * @return the start year, or 0 if not set
     */
    public int getStartYear() {
        // If startYear is not set but releaseDate is, derive it from releaseDate
        if (startYear == 0 && releaseDate != null) {
            Calendar cal = Calendar.getInstance();
            cal.setTime(releaseDate);
            startYear = cal.get(Calendar.YEAR);
        }
        return startYear;
    }

    /**
     * Gets the release date of the content
     *
     * @return the release date
     */
    public Date getReleaseDate() {
        return releaseDate != null ? new Date(releaseDate.getTime()) : null;
    }

    public List<Actor> getActors() {
        return actors != null ? List.copyOf(actors) : List.of(); // Updated to return an unmodifiable list
    }

    /**
     * Gets the year of the content as an integer
     * @return the year as an integer (e.g., 2023)
     */
    public int getYearAsInt() {
        if (this.year != null) {
            //Derive year from date
            Calendar cal = Calendar.getInstance();
            cal.setTime(this.year);
            return cal.get(Calendar.YEAR);
        } else if (this.startYear > 0) {
            return this.startYear;
        } else if (this.releaseDate != null) {
            Calendar cal = Calendar.getInstance();
            cal.setTime(this.releaseDate);
            return cal.get(Calendar.YEAR);
        }
        return 0; // Default if no date is set
    }

    //setters
    public void setGenre(Genre genre) {
        this.genre = genre;
        if (genre != null && !genres.contains(genre)) {
            genres.add(genre);
        }
    }


    public void setDirector(String director) {
        this.director = director;
    }

    public void setYear(Date year) {
        this.year = year;
    }

    /**
     * Sets the IMDb rating for this content
     *
     * @param imdbRating the IMDb rating to set (0.0 to 10.0)
     * @throws IllegalArgumentException if the rating is not between 0.0 and 10.0
     */
    public void setImdbRating(Double imdbRating) {
        if (imdbRating != null && (imdbRating < 0.0 || imdbRating > 10.0)) {
            throw new IllegalArgumentException("IMDb rating must be between 0.0 and 10.0");
        }
        this.imdbRating = imdbRating;
    }

    public void setUserRatings(Map<Integer, Integer> userRatings) {
        this.userRatings = new HashMap<>(userRatings);
    }


    /**
     * Sets a user's rating for this content and updates the average rating
     * @param userId The ID of the user who is rating
     * @param rating The rating value (0-10)
     * @throws IllegalArgumentException if rating is not between 0 and 10
     */
    public void setUserRating(Integer userId, Integer rating) {
        if (rating < 0 || rating > 10) {
            throw new IllegalArgumentException("Rating must be between 0 and 10");
        }
        
        if (userRatings == null) {
            userRatings = new HashMap<>();
        }
        
        // Store the user's rating
        userRatings.put(userId, rating);
        
        // Calculate new average rating
        double sum = userRatings.values().stream().mapToInt(Integer::intValue).sum();
        this.imdbRating = sum / userRatings.size();
    }

    /**
     * Sets the title of the content
     *
     * @param title the title to set
     */
    public void setTitle(String title) {
        this.title = title;
    }


    /**
     * Adds a genre to the content's genres list
     *
     * @param genre the genre to add
     */
    public void addGenre(Genre genre) {
        if (genre != null && !genres.contains(genre)) {
            genres.add(genre);
            // If no primary genre is set, set it
            if (this.genre == null) {
                this.genre = genre;
            }
        }
    }

    /**
     * Sets all genres for this content
     *
     * @param genres the list of genres to set
     */
    public void setGenres(List<Genre> genres) {
        this.genres = new ArrayList<>(genres);
        if (!genres.isEmpty()) {
            this.genre = genres.getFirst();
        } else {
            this.genre = null;
        }
    }

    /**
     * Sets the release date of the content and updates the start year accordingly.
     *
     * @param releaseDate the release date to set
     */
    public void setReleaseDate(Date releaseDate) {
        this.releaseDate = releaseDate != null ? new Date(releaseDate.getTime()) : null;

        // Update startYear based on the release date
        if (releaseDate != null) {
            //Derive start year from release date
            Calendar cal = Calendar.getInstance();
            cal.setTime(releaseDate);
            this.startYear = cal.get(Calendar.YEAR);
        } else {
            this.startYear = 0;
        }
    }

    /**
     * Sets the actors for this content
     *
     * @param actors the list of actors to set
     */
    public void setActors(List<Actor> actors) {
        this.actors = actors != null ? new ArrayList<>(actors) : new ArrayList<>();
    }

    /**
     * Sets the start year of the content and updates the release date accordingly.
     * If the release date is not set or has a different year, it will be updated.
     *
     * @param startYear the start year to set (must be a 4-digit year)
     */
    public void setStartYear(int startYear) {
        this.startYear = startYear;

        // Update release date if it's not set or has a different year
        if (releaseDate == null || new Calendar.Builder().setInstant(releaseDate).build().get(Calendar.YEAR) != startYear) {
            try {
                Calendar cal = Calendar.getInstance();
                cal.set(Calendar.YEAR, startYear);
                cal.set(Calendar.MONTH, Calendar.JANUARY);
                cal.set(Calendar.DAY_OF_MONTH, 1);
                this.releaseDate = cal.getTime();
            } catch (Exception e) {
                logger.error("Error updating release date for year: {}", startYear, e);
            }
        }
    }

    public void setGenres(String trim) {
        this.genres = new ArrayList<>();
    }

    public Object getContentType() {
        return contentType;
    }

    public String getSeries() {
        return series;
    }

    public double getRating() {
        return imdbRating;
    }
}