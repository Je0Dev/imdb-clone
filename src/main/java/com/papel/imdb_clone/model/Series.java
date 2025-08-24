package com.papel.imdb_clone.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;

public class Series extends Content {
    private List<Season> seasons;
    private int startYear;
    private Integer endYear;
    private Director director;
    private List<Actor> actors;

    // Rich content fields
    private String boxOffice;
    private List<String> awards;
    private String country;
    private String language;
    private String status;
    private double rating;

    /**
     * Creates a new Series with default values.
     */
    public Series() {
        this("", "");
    }

    /**
     * Creates a new Series with the given title and summary.
     *
     * @param title   The title of the series
     * @param summary A brief summary/description of the series
     */
    public Series(String title, String summary) {
        super(title, new java.util.Date(), com.papel.imdb_clone.enums.Genre.DRAMA, "", new java.util.HashMap<>(), 0.0);
        this.seasons = new ArrayList<>();
        this.actors = new ArrayList<>();
        this.awards = new ArrayList<>();
        setSummary(summary);
    }

    public Series(String title, String summary, com.papel.imdb_clone.enums.Genre genre, double imdbRating, double userRating) {
        super(title, new java.util.Date(), genre, "", new HashMap<>(), imdbRating);
        this.seasons = new ArrayList<>();
        this.actors = new ArrayList<>();
        this.awards = new ArrayList<>();
        setSummary(summary);
        // The userRating would be set through a different mechanism, so we don't set it here.
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

    public void removeSeason(Season season) {
        if (season != null) {
            this.seasons.remove(season);
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
     * Gets the director/creator of the series
     *
     * @return the director of the series
     */
    public Director getSeriesDirector() {
        return director;
    }

    /**
     * Adds an actor to the series
     *
     * @param actor the actor to add
     */
    public void addActor(Actor actor) {
        if (actor != null && !actors.contains(actor)) {
            actors.add(actor);
        }
    }

    /**
     * Removes an actor from the series
     *
     * @param actor the actor to remove
     * @return true if the actor was removed, false otherwise
     */
    public boolean removeActor(Actor actor) {
        return actors.remove(actor);
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

    public Integer getEndYear() {
        return endYear;
    }

    public void setEndYear(Integer endYear) {
        this.endYear = endYear;
    }

    public void setSeasons(List<Season> seasons) {
        this.seasons = new ArrayList<>();
        if (seasons != null) {
            for (Season season : seasons) {
                this.addSeason(season);
            }
        }
    }

    public String getBoxOffice() {
        return boxOffice;
    }

    public void setBoxOffice(String boxOffice) {
        this.boxOffice = boxOffice;
    }

    public List<String> getAwards() {
        return new ArrayList<>(awards);
    }

    public void setAwards(List<String> awards) {
        this.awards = awards != null ? new ArrayList<>(awards) : new ArrayList<>();
    }

    public void addAward(String award) {
        if (award != null && !award.trim().isEmpty()) {
            this.awards.add(award);
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
     * Gets the description/summary of the series.
     *
     * @return The series description
     */
    public String getDescription() {
        return getSummary();
    }

    /**
     * Compares this {@code Series} object with the specified object for equality.
     * Returns {@code true} if the given object is also a {@code Series} and
     * all inherited fields, as well as the {@code seasons} field, are equal.
     * This method relies on the superclass's {@code equals} implementation
     * and uses {@link java.util.Objects#equals(Object, Object)} for null-safe comparison
     * of the {@code seasons} field, assuming proper {@code equals} implementation for
     * elements within the {@code seasons} collection.
     *
     * @param obj The object to be compared for equality.
     * @return {@code true} if the specified object is equal to this {@code Series}; {@code false} otherwise.
     */

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        if (!super.equals(obj)) return false;
        Series series = (Series) obj;
        return Objects.equals(seasons, series.seasons) &&
                Objects.equals(boxOffice, series.boxOffice) &&
                Objects.equals(awards, series.awards);
    }

    /**
     * Generates a hash code for this {@code Series} object.
     * The hash code is computed based on the superclass's hash code and the
     * hash of the {@code seasons} field.
     * This method is consistent with {@link #equals(Object)}, ensuring that
     * equal {@code Series} objects produce the same hash code.
     * Proper {@code hashCode} implementations are expected for both the superclass
     * and the elements within the {@code seasons} collection.
     *
     * @return A hash code value for this object.
     */
    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), seasons, boxOffice, awards);
    }

    @Override
    public String toString() {
        return "Series{" +
                "id=" + getId() +
                ", title='" + getTitle() + '\'' +
                ", genre=" + getGenre() +
                ", summary='" + (getSummary() != null ? getSummary().substring(0, Math.min(20, getSummary().length())) + "..." : "") + '\'' +
                ", seasons=" + seasons.size() +
                '}';
    }


    public void setEndYear(int endYear) {
    }

    public void setCreator(String creator) {
    }

    public void setDescription(String description) {
    }

    public void setReleaseYear(int i) {
    }

    public void addGenre(String genreName) {

    }

    public void setYear(int year) {
        this.setReleaseYear(year);
    }

    public void setCountry(String country) {
        this.country = country;
    }

    public void setLanguage(String language) {
        this.language = language;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public void setRating(double rating) {
        this.rating = rating;
    }

    public boolean isPresent() {
        return true;
    }

    public Double getRating() {
        return rating;
    }
}