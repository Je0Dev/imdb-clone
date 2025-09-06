package com.papel.imdb_clone.repository;


import com.papel.imdb_clone.model.Movie;

import java.util.List;
import java.util.Optional;

/**
 * Repository interface for Movie entity operations.
 * Provides abstraction for data access operations.
 */
public interface MovieRepository {

    /**
     * Finds a movie by its unique ID.
     *
     * @param id The movie ID
     * @return Optional containing the movie if found, empty otherwise
     */
    Optional<Movie> findById(int id);


    List<Movie> findByTitle(String title);

    /**
     * Saves a movie (create or update).
     *
     * @param movie The movie to save
     * @return The saved movie with generated ID if new
     */
    Movie save(Movie movie);

    /**
     * Checks if a movie with the given title exists.
     *
     * @param title The movie title to check
     * @return true if exists, false otherwise
     */
    boolean existsByTitle(String title);

    /**
     * Gets the total count of movies.
     *
     * @return The number of movies
     */
    long count();

    Movie findByTitleAndReleaseYear(String title, int startYear);

    void add(Movie movie);
}