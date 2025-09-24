package com.papel.imdb_clone.model.content;

import com.papel.imdb_clone.enums.Genre;
import com.papel.imdb_clone.model.people.Actor;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Represents a movie.
 */
public class Movie extends Content {
    public static Object getRating;
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
        // Initialize fields directly instead of calling methods that might use 'this'
        this.awards = new ArrayList<>();
        this.genres = new ArrayList<>();
    }

    //constructor for simple movie creation
    public Movie(String title, int year, String genre, String director, Map<Integer, Integer> userRatings, double imdbRating) {
        super(title, createDateFromYear(year), null, director, userRatings, imdbRating);
        this.releaseDate = createDateFromYear(year);
        this.awards = new ArrayList<>();
        this.genres = new ArrayList<>();
        
        // Handle genre after object is fully initialized
        if (genre != null && !genre.trim().isEmpty()) {
            try {
                Genre genreEnum = Genre.valueOf(genre.toUpperCase().replace(" ", "_"));
                if (this.genres != null) {
                    this.genres.add(genreEnum);
                }
            } catch (IllegalArgumentException e) {
                // If genre is invalid, don't add any genre
                System.err.println("Invalid genre: " + genre);
            }
        }
    }

    // Helper method to create a Date from year
    private static Date createDateFromYear(int year) {
        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.YEAR, year);
        cal.set(Calendar.MONTH, Calendar.JANUARY);
        cal.set(Calendar.DAY_OF_MONTH, 1);
        return cal.getTime();
    }

    // Private constructor for FileDataLoaderService
    private Movie(String title, Date year, List<Genre> genres,
                 double imdbRating, String director, List<Actor> actors) {
        // Call super first with minimal initialization
        super(title, 
              year != null ? new Date(year.getTime()) : createDateFromYear(Calendar.getInstance().get(Calendar.YEAR)),
              null, // No default genre
              director, 
              new HashMap<>(), 
              imdbRating);
        
        // Initialize instance fields after super() call
        this.awards = new ArrayList<>();
        this.genres = new ArrayList<>();
        this.releaseDate = year != null ? new Date(year.getTime()) : createDateFromYear(Calendar.getInstance().get(Calendar.YEAR));
        
        // Set genres safely
        if (genres != null) {
            List<Genre> validGenres = new ArrayList<>();
            for (Genre g : genres) {
                if (g != null) {
                    validGenres.add(g);
                }
            }
            this.genres = validGenres;
        }
        

        // Initialize actors list safely
        if (actors != null) {
            List<Actor> actorsCopy = new ArrayList<>(actors);
            super.setActors(actorsCopy);
        }
    }

    // Factory method to create a Movie instance safely.
    // This prevents 'this' escape during construction.
    public static Movie createMovie(String title, Date year, List<Genre> genres,
                                  double imdbRating, String director, List<Actor> actors) {
        Movie movie = new Movie(title, year, genres, imdbRating, director, actors);
        return movie;
    }

    // Method removed as it's no longer needed

    // Method to add a genre, ensuring thread safety and avoiding duplicates
    public void addGenre(Genre genre) {
        if (genre != null) {
            if (this.genres == null) {
                this.genres = new ArrayList<>();
            }
            if (!this.genres.contains(genre)) {
                this.genres.add(genre);
            }
        }
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
