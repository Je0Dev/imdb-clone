package com.papel.imdb_clone.model.content;

import com.papel.imdb_clone.enums.Genre;
import com.papel.imdb_clone.model.people.Actor;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Represents a movie.
 */
public class Movie extends Content {
    private String boxOffice;
    private List<String> awards;
    private List<Genre> genres = new ArrayList<>(); // Multiple genres support
    private Date releaseDate;

    private int startyear;
    private Integer userRating;

    public Date getReleaseDate() {
        return releaseDate != null ? new Date(releaseDate.getTime()) : null;
    }

    public void setReleaseDate(Date releaseDate) {
        this.releaseDate = releaseDate != null ? new Date(releaseDate.getTime()) : null;
    }

    //default constructor
    public Movie() {
        super("", new Date(), null, "Unknown", new HashMap<>(), 0.0);
        initializeRichContentFields();
    }

    //constructor for simple movie creation
    public Movie(String title, int year, String genre, String director, Map<Integer, Integer> userRatings, double imdbRating) {
        super(title, createDateFromYear(year),
                null,
                director, userRatings, imdbRating);
        this.releaseDate = createDateFromYear(year);
        if (genre != null && !genre.trim().isEmpty()) {
            try {
                this.addGenre(Genre.valueOf(genre.toUpperCase().replace(" ", "_")));
            } catch (IllegalArgumentException e) {
                // If genre is invalid, don't add any genre
                System.err.println("Invalid genre: " + genre);
            }
        }
        initializeRichContentFields();
    }

    // Helper method to create a Date from year
    private static Date createDateFromYear(int year) {
        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.YEAR, year);
        cal.set(Calendar.MONTH, Calendar.JANUARY);
        cal.set(Calendar.DAY_OF_MONTH, 1);
        return cal.getTime();
    }

    // Constructor for FileDataLoaderService
    public Movie(String title, Date year, List<Genre> genres,
                 double imdbRating, String director, List<Actor> actors) {
        super(title, year != null ? year : createDateFromYear(Calendar.getInstance().get(Calendar.YEAR)),
                null, // No default genre
                director, new HashMap<>(), imdbRating);
        this.releaseDate = year != null ? new Date(year.getTime()) : createDateFromYear(Calendar.getInstance().get(Calendar.YEAR));
        // Only set genres if the list is not empty and doesn't contain null values
        if (genres != null) {
            this.genres = genres.stream()
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
        } else {
            this.genres = new ArrayList<>();
        }
        if (actors != null) {
            // Use setActors to properly set the actors list
            this.setActors(actors);
        }
        initializeRichContentFields();
    }

    //initialize rich content fields like awards and genres
    private void initializeRichContentFields() {
        this.awards = new ArrayList<>();
        // Don't add parent's genre to avoid duplicates
        // The genres list should only be modified through addGenre or setGenres
    }


    //getters and setters
    @Override
    public List<Actor> getActors() {
        // Return the list from the parent class to ensure we're using a single source of truth
        return super.getActors();
    }

    @Override
    public String getDirector() {
        return super.getDirector();
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


    public void setGenres(List<Genre> genres) {
        this.genres = genres != null ? new ArrayList<>(genres) : new ArrayList<>();
    }

    public void addGenre(Genre genre) {
        if (genre != null && !this.genres.contains(genre)) {
            this.genres.add(genre);
        }
    }
    
    @Override
    public List<Genre> getGenres() {
        return new ArrayList<>(genres);
    }


    @Override
    public String toString() {
        return "Movie{" +
                "id=" + getId() +
                ", title='" + getTitle() + '\'' +
                ", genre=" + getGenre() +
                ", actors=" + getActors().size() +
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

    @Override
    public void setActors(List<Actor> actors) {
        // Create a new ArrayList with the provided actors (or empty list if null)
        List<Actor> newActors = actors != null ? new ArrayList<>(actors) : new ArrayList<>();
        // Use the parent class's setter to update the actors
        super.setActors(newActors);
    }
    
    /**
     * Adds an actor to the movie's cast
     * @param actor The actor to add
     */
    public void addActor(Actor actor) {
        if (actor != null && !getActors().contains(actor)) {
            getActors().add(actor);
        }
    }

    public int getStartYear() {
        return startyear;
    }

    public void setStartYear(int year) {
        this.startyear = year;
    }

    public void setUserRating(int rating) {
        this.userRating=userRating;
    }
}
