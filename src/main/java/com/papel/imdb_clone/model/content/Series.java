package com.papel.imdb_clone.model.content;

import com.papel.imdb_clone.enums.Genre;
import com.papel.imdb_clone.model.people.Actor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

/**
 * Represents a series of episodes.
 */
public class Series extends Content {
    private Date year;
    private List<Season> seasons;
    private String director;  // Changed from Director to String to match parent class
    private List<Actor> actors;
    private String boxOffice;
    private List<String> awards;
    private double rating;
    private List<Genre> genres = new ArrayList<>();
    private static final Logger logger = LoggerFactory.getLogger(Series.class);
    private int endYear;
    
    /**
     * Sets the end year of the series.
     * @param endYear The year the series ended
     */
    public void setEndYear(int endYear) {
        this.endYear = endYear;
    }

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
     * @param genre The genre of the series
     * @param imdbRating The IMDB rating of the series
     * @param userRating The user rating of the series
     * @param startYear The start year of the series
     */
    public Series(String title, Genre genre, double imdbRating, double userRating, int startYear) {
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
        if (seasons == null) {
            seasons = new ArrayList<>();
        }
        return seasons;
    }
    

    
    public int getTotalSeasons() {
        return seasons != null ? seasons.size() : 0;
    }
    
    public int getTotalEpisodes() {
        logger.debug("Calculating total episodes for series: {}", getTitle());
        if (seasons == null || seasons.isEmpty()) {
            logger.debug("No seasons found for series: {}", getTitle());
            return 0;
        }
        
        int total = 0;
        logger.debug("Processing {} seasons for series: {}", seasons.size(), getTitle());
        
        for (int i = 0; i < seasons.size(); i++) {
            Season season = seasons.get(i);
            if (season == null) {
                logger.warn("Season at index {} is null for series: {}", i, getTitle());
                continue;
            }
            
            try {
                // Use reflection to get episodes
                List<Episode> episodes =
                        (List<Episode>) season.getClass()
                    .getMethod("getEpisodes")
                    .invoke(season);
                    
                if (episodes == null) {
                    logger.warn("Episodes list is null for season {} of series: {}", 
                        season.getSeasonNumber(), getTitle());
                    continue;
                }
                
                logger.debug("Season {} has {} episodes", season.getSeasonNumber(), episodes.size());
                total += episodes.size();
                
            } catch (Exception e) {
                logger.warn("Error getting episodes for season {} of series '{}': {}", 
                    season.getSeasonNumber(), getTitle(), e.getMessage(), e);
            }
        }
        
        logger.info("Total episodes for series '{}': {}", getTitle(), total);
        return total;
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
     * Gets all actors from the main cast and all episodes.
     *
     * @return List of all unique actors in the series
     */
    public List<Actor> getActors() {
        Set<Actor> uniqueActors = new LinkedHashSet<>();
        
        // Add main cast first
        if (this.actors != null) {
            uniqueActors.addAll(this.actors);
        }
        
        // Add actors from all episodes
        if (seasons != null) {
            for (Season season : seasons) {
                if (season != null && season.getEpisodes() != null) {
                    for (Episode episode : season.getEpisodes()) {
                        if (episode != null && episode.getActors() != null) {
                            uniqueActors.addAll(episode.getActors());
                        }
                    }
                }
            }
        }
        
        return new ArrayList<>(uniqueActors);
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
        return String.format(
            "%s (%d-%s, %d seasons, %d episodes, %.1f/10)",
            getTitle(),
            getYear() != null ? getYear().getYear() + 1900 : 0,
            endYear > 0 ? String.valueOf(endYear) : "Present",
            getTotalSeasons(),
            getTotalEpisodes(),
            getRating()
        );
    }

    public void setRating(double rating) {
        this.rating = rating;
    }


    public Double getRating() {
        return rating;
    }

    public void setNominations(String nominations) {
    }


    public void setAwards(String awards) {
        this.awards = new ArrayList<>();
    }

    public String getCreator() {
        return director;
    }

    public void setCreator(String creator) {
        this.director = creator;
    }

    public int getEndYear() {
        return endYear;
    }
}