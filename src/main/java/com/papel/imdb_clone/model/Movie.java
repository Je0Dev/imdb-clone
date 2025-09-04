package com.papel.imdb_clone.model;

import com.papel.imdb_clone.enums.Genre;

import java.util.*;

public class Movie extends Content {
    private final int duration;
    private final List<Actor> actors;

    private String boxOffice;
    private List<String> awards;
    private List<Genre> genres = new ArrayList<>(); // Multiple genres support
    private Date releaseDate;

    private Arrays directors;
    private int startyear;

    public Date getReleaseDate() {
        return releaseDate != null ? new Date(releaseDate.getTime()) : null;
    }

    public void setReleaseDate(Date releaseDate) {
        this.releaseDate = releaseDate != null ? new Date(releaseDate.getTime()) : null;
    }

    public Movie() {
        super("", new Date(), Genre.ACTION, "Unknown", new HashMap<>(), 0.0);
        this.duration = 0;
        this.actors = new ArrayList<>();
        initializeRichContentFields();
    }

    public Movie(String title, int year, String genre, String director, Map<Integer, Integer> userRatings, double imdbRating) {
        super(title, new Date(year - 1900, Calendar.JANUARY, 1),
                Genre.valueOf(genre.toUpperCase().replace(" ", "_")),
                director, userRatings, imdbRating);
        this.releaseDate = new Date(year - 1900, Calendar.JANUARY, 1);
        this.duration = 0;
        this.actors = new ArrayList<>();
        initializeRichContentFields();
    }

    // Constructor for FileDataLoaderService
    public Movie(String title, Date year, List<Genre> genres, int duration,
                 double imdbRating, String description, String director, List<Actor> actors) {
        super(title, year,
                (genres != null && !genres.isEmpty()) ? genres.get(0) : Genre.DRAMA,
                director, new HashMap<>(), imdbRating);
        this.releaseDate = year != null ? new Date(year.getTime()) : null;
        this.duration = duration;
        this.actors = new ArrayList<>(actors != null ? actors : new ArrayList<>());
        this.genres = genres != null ? new ArrayList<>(genres) : new ArrayList<>();
        initializeRichContentFields();
    }

    private void initializeRichContentFields() {
        this.awards = new ArrayList<>();
        this.genres.add(getGenre()); // Add the primary genre from parent class
    }


    public List<Actor> getActors() {
        return new ArrayList<>(actors);
    }

    public String getDirector() {
        return super.getDirector();
    }


    public String getBoxOffice() {
        return boxOffice;
    }

    public List<String> getAwards() {
        return new ArrayList<>(awards);
    }


    public void setGenres(List<Genre> genres) {
        this.genres = genres != null ? new ArrayList<>(genres) : new ArrayList<>();
    }

    public void addGenre(Genre genre) {
        if (genre != null && !this.genres.contains(genre)) {
            this.genres.add(genre);
        }
    }


    @Override
    public String toString() {
        return "Movie{" +
                "id=" + getId() +
                ", title='" + getTitle() + '\'' +
                ", genre=" + getGenre() +
                ", duration=" + duration +
                ", actors=" + actors.size() +
                ", genres=" + genres.size() +
                '}';
    }

    public void setAwards(String awards) {
        this.awards = Collections.singletonList(awards);
    }

    public void setRating(double rating) {
        this.setImdbRating(rating);
    }

    public static double getRating(Object o) {
        return ((Movie) o).getImdbRating();
    }

    public void addActor(Actor actor) {
        this.actors.add(actor);
    }

    public int getStartYear() {
        return startyear;
    }

    public void setStartYear(int year) {
        this.startyear = year;
    }
}
