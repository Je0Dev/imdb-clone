package com.papel.imdb_clone.model;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;


public class Season {
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
        // Note: episodeCount is not used in this implementation
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

    /**
     * Compares this {@code Season} object with the specified object for equality.
     * Returns {@code true} if the given object is also a {@code Season} and
     * its {@code id}, {@code year}, and {@code episodes} fields are equal.
     * This method uses {@link Objects#equals(Object, Object)} for null-safe comparison
     * of the {@code episodes} collection, assuming its elements (e.g., {@code Episode} objects)
     * correctly implement their own {@code equals} method.
     *
     * @param obj The object to be compared for equality.
     * @return {@code true} if the specified object is equal to this {@code Season}; {@code false} otherwise.
     */
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        Season season = (Season) obj;
        return id == season.id &&
                year == season.year &&
                Objects.equals(episodes, season.episodes);
    }

    /**
     * Generates a hash code for this {@code Season} object.
     * The hash code is computed based on the {@code id}, {@code year}, and {@code episodes} fields.
     * This method is consistent with {@link #equals(Object)}, ensuring that
     * equal {@code Season} objects produce the same hash code.
     * Proper {@code hashCode} implementations are expected for elements within the
     * {@code episodes} collection.
     *
     * @return A hash code value for this object.
     */
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
    }

    public void setTitle(String format) {
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