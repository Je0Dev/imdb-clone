package com.papel.imdb_clone.model;

import com.papel.imdb_clone.enums.Genre;

import java.util.*;

public class Series extends Content {
    private List<Season> seasons;
    private int startYear;
    private Integer endYear;
    private Director director;
    private List<Actor> actors;

    // Rich content fields
    private String boxOffice;
    private List<String> awards;
    private double rating;
    private String nominations;


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

    public Series(String title, String summary, Genre genre, double imdbRating, double userRating) {
        super(title, new java.util.Date(), genre, "", new HashMap<>(), imdbRating);
        this.seasons = new ArrayList<>();
        this.actors = new ArrayList<>();
        this.awards = new ArrayList<>();
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

    public void setAwards(String awards) {
        this.awards = awards != null ? new ArrayList<>(Collections.singleton(awards)) : new ArrayList<>();
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
        this.endYear = endYear;
    }

    public void setCreator(String creator) {
    }


    public void addGenre(String genreName) {

    }

    /**
     * Sets the release year of the series
     * @param year The release year to set
     */
    public void setReleaseYear(int year) {
        this.startYear = year;
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

    public void setNominations(String nominations) {
        this.nominations = nominations;
    }

    public String getNominations() {
        return nominations;
    }

    public int getTotalEpisodes() {
        return seasons.stream().mapToInt(Season::getTotalEpisodes).sum();
    }
}