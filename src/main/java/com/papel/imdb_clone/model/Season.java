package com.papel.imdb_clone.model;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Represents a season of a series.
 */
public class Season {
    private int episodeCount;
    private int id;
    private int year;
    private List<Episode> episodes;
    private int seasonNumber;
    private Object title;
    private Date releaseDate;
    private Series series;

    /**
     * Default constructor for Season.
     * Initializes an empty list of episodes.
     */
    public Season(int seasonNumber, Series series) {
        this.episodes = new ArrayList<>();
    }


    // constructor for season creation
    public Season(int seasonNumber, int episodeCount, String title, Series series) {
        this(seasonNumber, series);
        this.seasonNumber = seasonNumber;
        this.title = title;
        this.episodeCount = episodeCount;
    }

    //getters and setters
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getYear() {
        return year;
    }

    public List<Episode> getEpisodes() {
        return new ArrayList<>(episodes);
    }
    

    public int getSeasonNumber() {
        return seasonNumber;
    }

    public void setSeasonNumber(int seasonNumber) {
        this.seasonNumber = seasonNumber;
    }

    public void setYear(int year) {
        this.year = year;
    }


    @Override
    public String toString() {
        return "Season{" +
                "id=" + id +
                ", year=" + year +
                ", episodes=" + episodes.size() +
                '}';
    }
    

    public void setTitle(String format) {
        this.title = format;
    }

    public Object getTitle() {
        return title;
    }

    public void setSeries(Series series) {
        this.series = series;
    }

    //set episodes type E to a list of episodes
    public <E> void setEpisodes(List<Episode> episodes) {
        this.episodes = episodes;
    }

    public int getTotalEpisodes() {
        return episodes.size();
    }
    
}