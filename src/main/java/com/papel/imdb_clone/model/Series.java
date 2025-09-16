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
    private String director;  // Changed from Director to String to match parent class
    private List<Actor> actors;
    private String boxOffice;
    private List<String> awards;
    private double rating;
    private String nominations;
    private List<Genre> genres = new ArrayList<>();
    private static final Logger logger = LoggerFactory.getLogger(Series.class);
    
    @Override
    public String getDirector() {
        return director;
    }
    
    /**
     * Sets the director of the series.
     * @param director The director's name as a String
     */
    public void setDirector(String director) {
        this.director = director;
    }

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
        // Set the current year as default
        Calendar cal = Calendar.getInstance();
        cal.setTime(new java.util.Date());
        this.year = cal.getTime();
        this.startYear = cal.get(Calendar.YEAR);
    }

    /**
     * Creates a new Series with the given title, summary, genre, imdbRating, userRating, and startYear.
     * @param title The title of the series
     * @param summary The summary of the series
     * @param genre The genre of the series
     * @param imdbRating The IMDB rating of the series
     * @param userRating The user rating of the series
     * @param startYear The start year of the series
     */
    public Series(String title, String summary, Genre genre, double imdbRating, double userRating, int startYear) {
        super(title, new java.util.Date(), genre, "", new HashMap<>(), imdbRating);
        this.seasons = new ArrayList<>();
        this.actors = new ArrayList<>();
        this.awards = new ArrayList<>();
        this.rating = userRating;
        this.startYear = startYear;
        
        // Set the year based on startYear
        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.YEAR, startYear);
        cal.set(Calendar.MONTH, 0); // January
        cal.set(Calendar.DAY_OF_MONTH, 1);
        this.year = cal.getTime();
    }

    public List<Season> getSeasons() {
        return seasons != null ? new ArrayList<>(seasons) : new ArrayList<>();
    }
    
    public int getTotalSeasons() {
        return seasons != null ? seasons.size() : 0;
    }
    
    public int getTotalEpisodes() {
        if (seasons == null || seasons.isEmpty()) {
            return 0;
        }
        return seasons.stream()
            .filter(Objects::nonNull)
            .mapToInt(season -> season.getEpisodes() != null ? season.getEpisodes().size() : 0)
            .sum();
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


    public void setAwards(String awards) {
        this.awards = new ArrayList<>();
    }

}