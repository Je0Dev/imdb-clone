package com.papel.imdb_clone.model;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;


public class Season {
    private int episodeCount;
    private int id;
    private int year;
    private List<Episode> episodes;
    private int seasonNumber;
    private Object title;

    /**
     * Default constructor for Season.
     * Initializes an empty list of episodes.
     */
    public Season() {
        this.episodes = new ArrayList<>();
    }

    /**
     * Constructor for Season with season number, episode count, and title.
     *
     * @param seasonNumber the season number
     * @param episodeCount the episode count (not used in this implementation)
     * @param title        the title of the season
     */
    public Season(int seasonNumber, int episodeCount, String title) {
        this();
        this.seasonNumber = seasonNumber;
        this.title = title;
        this.episodeCount = episodeCount;
    }

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

    public void addEpisode(Episode episode) {
        if (episode != null) {
            episodes.add(episode);
        }
    }

    public void removeEpisode(Episode episode) {
        if (episode != null) {
            episodes.remove(episode);
        }
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
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        Season season = (Season) obj;
        return id == season.id &&
                year == season.year &&
                Objects.equals(episodes, season.episodes);
    }


    @Override
    public int hashCode() {
        return Objects.hash(id, year, episodes);
    }

    @Override
    public String toString() {
        return "Season{" +
                "id=" + id +
                ", year=" + year +
                ", episodes=" + episodes.size() +
                '}';
    }

    public void setSeriesId(int id) {
        this.id = id;
    }

    public void setTitle(String format) {
        this.title = format;
    }

    public Object getTitle() {
        return title;
    }

    public void setSeries(Series series) {
    }

    public <E> void setEpisodes(List<Episode> episodes) {
        this.episodes = episodes;
    }
}