package com.papel.imdb_clone.model;

import com.papel.imdb_clone.enums.Genre;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

/**
 * Represents a series of episodes.
 */
public class Series extends Content {
    private List<Season> seasons;
    private Director director;
    private List<Actor> actors;
    private String boxOffice;
    private List<String> awards;
    private double rating;
    private String nominations;
    private List<Genre> genres = new ArrayList<>();
    private static final Logger logger = LoggerFactory.getLogger(Series.class);

    /**
     * Creates a new Series with the given title and summary.
     *
     * @param title The title of the series
     */
    public Series(String title) {
        super(title, new java.util.Date(), Genre.DRAMA, "", new java.util.HashMap<>(), 0.0);
        this.seasons = new ArrayList<>();
        this.actors = new ArrayList<>();
        this.awards = new ArrayList<>();
        // Initialize the year field with current year
        Calendar cal = Calendar.getInstance();
        cal.setTime(new java.util.Date());
        this.year = cal.getTime();
    }

    /**
     * Creates a new Series with the given title, summary, genre, imdbRating, userRating, and startYear.
     * @param title
     * @param summary
     * @param genre
     * @param imdbRating
     * @param userRating
     * @param startYear
     */
    public Series(String title, String summary, Genre genre, double imdbRating, double userRating, int startYear) {
        super(title, new java.util.Date(), genre, "", new HashMap<>(), imdbRating);
        this.seasons = new ArrayList<>();
        this.actors = new ArrayList<>();
        this.awards = new ArrayList<>();
        
        // Set the year based on startYear
        if (startYear > 0) {
            try {
                Calendar cal = Calendar.getInstance();
                cal.set(Calendar.YEAR, startYear);
                cal.set(Calendar.MONTH, Calendar.JANUARY);
                cal.set(Calendar.DAY_OF_MONTH, 1);
                this.year = cal.getTime();
                this.setStartYear(startYear);
            } catch (Exception e) {
                // Fallback to current year if there's an error
                this.year = new java.util.Date();
                this.setStartYear(Calendar.getInstance().get(Calendar.YEAR));
            }
        } else {
            // If no valid start year, use current year
            this.year = new java.util.Date();
            this.setStartYear(Calendar.getInstance().get(Calendar.YEAR));
        }
        
        if (genre != null) {
            this.genres.add(genre);
        }
    }

    public List<Season> getSeasons() {
        return new ArrayList<>(seasons);
    }

    public void addSeason(Season season) {
        if (season != null) {
            season.setSeries(this);
            this.seasons.add(season);
        }
    }

    //getters and setters
    @Override
    public int getStartYear() {
        // If startYear is 0 but we have a year, update startYear from year
        int yearValue = super.getStartYear();
        if (yearValue == 0 && this.year != null) {
            Calendar cal = Calendar.getInstance();
            cal.setTime(this.year);
            yearValue = cal.get(Calendar.YEAR);
            super.setStartYear(yearValue);
        }
        return yearValue;
    }

    /**
     * Sets the director/creator of the series
     *
     * @param director the director to set
     */
    public void setDirector(Director director) {
        this.director = director;
        // Also set the director's name in the parent class
        if (director != null) {
            super.setDirector(director.getFirstName() + " " + director.getLastName());
        } else {
            super.setDirector(null);
        }
    }

    /**
     * Sets the list of actors in the series
     *
     * @param actors the list of actors to set
     */
    public void setActors(List<Actor> actors) {
        this.actors = new ArrayList<>(actors);
    }

    @Override
    public void setStartYear(int startYear) {
        // Update the parent's startYear
        super.setStartYear(startYear);
        
        // Also update the year field to maintain consistency
        if (startYear > 0) {
            try {
                Calendar cal = Calendar.getInstance();
                cal.set(Calendar.YEAR, startYear);
                cal.set(Calendar.MONTH, Calendar.JANUARY);
                cal.set(Calendar.DAY_OF_MONTH, 1);
                this.year = cal.getTime();
            } catch (Exception e) {
                logger.error("Error updating year from startYear: " + startYear, e);
            }
        }
    }

    //set seasons to list of seasons
    public void setSeasons(List<Season> seasons) {
        this.seasons = new ArrayList<>();
        if (seasons != null) {
            for (Season season : seasons) {
                this.addSeason(season);
            }
        }
    }


    public List<String> getAwards() {
        return new ArrayList<>(awards);
    }

    /**
     * Sets the list of genres for this series.
     *
     * @param genres the list of genres to set
     */
    public void setGenres(List<Genre> genres) {
        this.genres = genres != null ? new ArrayList<>(genres) : new ArrayList<>();

        // Set the primary genre to the first one if available
        if (!this.genres.isEmpty()) {
            setGenre(this.genres.getFirst());
        }
    }

    /**
     * Gets all actors from all seasons and episodes.
     *
     * @return List of all actors in the series
     */
    public List<Actor> getActors() {
        List<Actor> allActors = new ArrayList<>();
        for (Season season : seasons) {
            for (Episode episode : season.getEpisodes()) {
                allActors.addAll(episode.getActors());
            }
        }
        return allActors;
    }

    /**
     * Gets all genres for this series
     *
     * @return List of genres
     */
    public List<Genre> getGenres() {
        return new ArrayList<>(genres);
    }


    @Override
    public String toString() {
        return "Series{" +
                "id=" + getId() +
                ", title='" + getTitle() + '\'' +
                ", genre=" + getGenre() +
                ", seasons=" + seasons.size() +
                '}';
    }

    public void setRating(double rating) {
        this.rating = rating;
    }


    public Double getRating() {
        return rating;
    }

    public void setNominations(String nominations) {
        this.nominations = nominations;
    }

    public int getTotalEpisodes() {
        return seasons.stream().mapToInt(Season::getTotalEpisodes).sum();
    }

    public void setAwards(String awards) {
        this.awards = new ArrayList<>();
    }

}