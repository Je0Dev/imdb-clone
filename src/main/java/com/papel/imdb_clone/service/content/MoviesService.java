package com.papel.imdb_clone.service.content;

import com.papel.imdb_clone.enums.Ethnicity;
import com.papel.imdb_clone.enums.Genre;
import com.papel.imdb_clone.model.content.Movie;
import com.papel.imdb_clone.model.people.Actor;
import com.papel.imdb_clone.service.people.CelebrityService;

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
            shawshankActors.add(Actor.getInstance("Tim", "Robbins", LocalDate.of(1958, 10, 16), 'M', Ethnicity.CAUCASIAN));
            shawshankActors.add(Actor.getInstance("Morgan", "Freeman", LocalDate.of(1937, 6, 1), 'M', Ethnicity.AFRICAN));

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
            godfatherActors.add(Actor.getInstance("Marlon", "Brando", LocalDate.of(1924, 4, 3), 'M', Ethnicity.CAUCASIAN));
            godfatherActors.add(Actor.getInstance("Al", "Pacino", LocalDate.of(1940, 4, 25), 'M', Ethnicity.CAUCASIAN));

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
            darkKnightActors.add(Actor.getInstance("Christian", "Bale", LocalDate.of(1974, 1, 30), 'M', Ethnicity.CAUCASIAN));
            darkKnightActors.add(Actor.getInstance("Heath", "Ledger", LocalDate.of(1979, 4, 4), 'M', Ethnicity.CAUCASIAN));

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
            //unlock the write lock when done, which means that other threads can modify the list
            lock.writeLock().unlock();
        }
    }
}
