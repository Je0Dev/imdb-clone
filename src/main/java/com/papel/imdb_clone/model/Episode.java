package com.papel.imdb_clone.model;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class Episode {
    private int id;
    private int duration;
    private Director director;
    private double imdbRating;
    private List<Actor> actors;
    private String title;
    private int episodeNumber;
    private int number;

    public Episode() {
        this.duration = duration;
        this.director = director;
        this.imdbRating = imdbRating;
        this.actors = actors != null ? new ArrayList<>(actors) : new ArrayList<>();
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

    public int getDuration() {
        return duration;
    }

    public Director getDirector() {
        return director;
    }

    public double getImdbRating() {
        return imdbRating;
    }

    public List<Actor> getActors() {
        return new ArrayList<>(actors);
    }

    public Actor getMainActor() {
        return actors.isEmpty() ? null : actors.getFirst();
    }

    public void setImdbRating(double rating) {
        if (rating >= 0.0 && rating <= 10.0) {
            this.imdbRating = rating;
        }
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

    public void setDuration(Integer duration) {
        if (duration != null) {
            this.duration = duration;
        }
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        Episode episode = (Episode) obj;
        return id == episode.id &&
                duration == episode.duration &&
                Double.compare(episode.imdbRating, imdbRating) == 0 &&
                Objects.equals(director, episode.director) &&
                Objects.equals(actors, episode.actors);
    }

    
    @Override
    public int hashCode() {
        return Objects.hash(id, duration, director, imdbRating, actors);
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

    public void setSeasonId(int id) {
    }

    public void setDescription(String description) {

    }


    public int getNumber() {
        return number;
    }

    public void setNumber(int number) {
        this.number = number;
    }
}