package com.papel.imdb_clone.model;

import com.papel.imdb_clone.enums.Genre;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

public class Series extends Content {
    private List<Season> seasons;
    private int startYear;
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
    }

    public Series(String title, String summary, Genre genre, double imdbRating, double userRating, int startYear) {
        super(title, new java.util.Date(), genre, "", new HashMap<>(), imdbRating);
        this.seasons = new ArrayList<>();
        this.actors = new ArrayList<>();
        this.awards = new ArrayList<>();
        this.startYear = startYear;
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


    public int getStartYear() {
        return startYear;
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

    public void setStartYear(int startYear) {
        this.startYear = startYear;
    }

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

    public List<String> setAwards(String awards) {
        this.awards = new ArrayList<>();
        return Arrays.asList(awards.split(","));
    }

}