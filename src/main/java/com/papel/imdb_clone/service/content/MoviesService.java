package com.papel.imdb_clone.service.content;

import com.papel.imdb_clone.enums.Genre;
import com.papel.imdb_clone.model.content.Movie;
import com.papel.imdb_clone.model.people.Actor;

import java.time.LocalDate;
import java.util.*;
import java.util.logging.Logger;

/**
 * Service class for managing Movie content.
 */
public class MoviesService extends BaseContentService<Movie> {
    private static final Logger logger = Logger.getLogger(MoviesService.class.getName());
    private static MoviesService instance;
    
    private MoviesService() {
        super(Movie.class);
    }
    
    public static synchronized MoviesService getInstance() {
        if (instance == null) {
            instance = new MoviesService();
        }
        return instance;
    }
    
    @Override
    protected void initializeSampleData() {
        lock.writeLock().lock();
        try {
            // Clear existing data
            contentList.clear();

            // Movie 1 - The Shawshank Redemption
            List<Genre> shawshankGenres = new ArrayList<>();
            shawshankGenres.add(Genre.DRAMA);
            List<Actor> shawshankActors = new ArrayList<>();
            shawshankActors.add(new Actor("Tim", "Robbins", LocalDate.of(1958, 10, 16), 'M', "White"));
            shawshankActors.add(new Actor("Morgan", "Freeman", LocalDate.of(1937, 6, 1), 'M', "Black"));
            
            Movie movie1 = new Movie(
                "The Shawshank Redemption",
                new GregorianCalendar(1994, Calendar.JANUARY, 1).getTime(),
                shawshankGenres,
                9.3,
                "Frank Darabont",
                shawshankActors
            );
            save(movie1);

            // Movie 2 - The Godfather
            List<Genre> godfatherGenres = new ArrayList<>();
            godfatherGenres.add(Genre.CRIME);
            godfatherGenres.add(Genre.DRAMA);
            List<Actor> godfatherActors = new ArrayList<>();
            godfatherActors.add(new Actor("Marlon", "Brando", LocalDate.of(1924, 4, 3), 'M', "White"));
            godfatherActors.add(new Actor("Al", "Pacino", LocalDate.of(1940, 4, 25), 'M', "White"));
            
            Movie movie2 = new Movie(
                "The Godfather",
                new GregorianCalendar(1972, Calendar.JANUARY, 1).getTime(),
                godfatherGenres,
                9.2,
                "Francis Ford Coppola",
                godfatherActors
            );
            save(movie2);

            // Movie 3 - The Dark Knight
            List<Genre> darkKnightGenres = new ArrayList<>();
            darkKnightGenres.add(Genre.ACTION);
            darkKnightGenres.add(Genre.CRIME);
            darkKnightGenres.add(Genre.DRAMA);
            List<Actor> darkKnightActors = new ArrayList<>();
            darkKnightActors.add(new Actor("Christian", "Bale", LocalDate.of(1974, 1, 30), 'M', "White"));
            darkKnightActors.add(new Actor("Heath", "Ledger", LocalDate.of(1979, 4, 4), 'M', "White"));
            
            Movie movie3 = new Movie(
                "The Dark Knight",
                new GregorianCalendar(2008, Calendar.JANUARY, 1).getTime(),
                darkKnightGenres,
                9.0,
                "Christopher Nolan",
                darkKnightActors
            );
            save(movie3);

            logger.info("Initialized with " + contentList.size() + " sample movies");
        } finally {
            lock.writeLock().unlock();
        }
    }
}
