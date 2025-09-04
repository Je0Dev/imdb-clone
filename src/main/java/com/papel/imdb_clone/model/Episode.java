package com.papel.imdb_clone.model;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class Episode {
    private int id;
    private int duration;
    private Director director;
    private double imdbRating;
    private List<Actor> actors;
    private String title;
    private int episodeNumber;
    private int number;
    private Object season;
    private Date releaseDate;

    public Episode() {
        this.duration = 0;
        this.director = null;
        this.imdbRating = 0.0;
        this.actors = new ArrayList<>();
    }

    // Additional constructor for simple episode creation
    public Episode(String title, int duration) {
        this.title = title;
        this.duration = duration;
        this.director = null;
        this.imdbRating = 0.0;
        this.actors = new ArrayList<>();
    }

    // Constructor for episode with episode number, title, duration, and summary
    public Episode(int episodeNumber, String title, int duration, String summary) {
        this.episodeNumber = episodeNumber;
        this.title = title;
        this.duration = duration;
        this.director = null;
        this.imdbRating = 0.0;
        this.actors = new ArrayList<>();
    }

    public Episode(int episodeNumber, String episodeTitle) {
        this.episodeNumber = episodeNumber;
        this.title = episodeTitle;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public Director getDirector() {
        return director;
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

    public void setDuration(int duration) {
        this.duration = duration;
    }


    @Override
    public String toString() {
        return "Episode{" +
                "id=" + id +
                ", duration=" + duration +
                ", director=" + (director != null ? director.getFullName() : "Unknown") +
                ", imdbRating=" + imdbRating +
                ", actors=" + actors.size() +
                '}';
    }

    public void setDescription(String description) {

    }

    public void setSeason(Season newSeason) {
        this.season = season;
    }

    public void setReleaseDate(Date date) {
        this.releaseDate = date;
    }

    public void setActors(List<Actor> actors) {
        this.actors = actors != null ? new ArrayList<>(actors) : new ArrayList<>();
    }
}