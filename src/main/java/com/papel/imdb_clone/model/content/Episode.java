package com.papel.imdb_clone.model.content;

import com.papel.imdb_clone.model.people.Actor;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Represents an episode of a TV show.
 */
public class Episode {
    private int id;
    private String director;
    private final double imdbRating;
    private List<Actor> actors;
    private String title;
    private int episodeNumber;
    private int number;
    private Object season;

    /**
     * Default constructor for Episode
     */
    public Episode() {
        this.director = null;
        this.imdbRating = 0.0;
        this.actors = new ArrayList<>();
        this.title = "";
        this.episodeNumber = 0;
    }

    // Additional constructor for simple episode creation
    public Episode(String title, int duration) {
        this();
        this.title = title;
    }

    // Constructor for episode with episode number, title, duration, and summary
    public Episode(int episodeNumber, String title, int duration, String summary) {
        this();
        this.episodeNumber = episodeNumber;
        this.title = title;
    }

    public Episode(int episodeNumber, String episodeTitle) {
        this();
        this.episodeNumber = episodeNumber;
        this.title = episodeTitle;
    }

    //getters setters
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getDirector() {
        return director;
    }
    
    public void setDirector(String director) {
        this.director = director;
    }


    public List<Actor> getActors() {
        return new ArrayList<>(actors);
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public int getEpisodeNumber() {
        return episodeNumber;
    }

    public void setEpisodeNumber(int episodeNumber) {
        this.episodeNumber = episodeNumber;
    }


    @Override
    public String toString() {
        return "Episode{" +
                "id=" + id +
                ", director='" + (director != null ? director : "Unknown") + "'" +
                ", imdbRating=" + imdbRating +
                ", actors=" + actors.size() +
                '}';
    }

    public void setSeason(Season newSeason) {
        this.season = season;
    }

    public void setReleaseDate(Date date) {
    }

    //add actors to the list
    public void setActors(List<Actor> actors) {
        this.actors = actors != null ? new ArrayList<>(actors) : new ArrayList<>();
    }
}