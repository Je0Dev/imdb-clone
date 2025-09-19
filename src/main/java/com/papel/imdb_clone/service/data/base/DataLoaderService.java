package com.papel.imdb_clone.service.data.base;

import java.io.IOException;

/**
 * Service interface for loading data from various sources.
 * Provides abstraction for data loading operations.
 */
public interface DataLoaderService {

    /**
     * Loads all data from configured sources.
     * This method should load users, movies, series, actors, directors, etc.
     * @throws IOException if there is an error reading the data files
     */
    void loadAllData() throws IOException;

    /**
     * Loads users from the specified file.
     *
     * @param filename The filename to load from
     * @throws IOException if there is an error reading the file
     */
    void loadUsers(String filename) throws IOException;

    /**
     * Loads movies from the specified file.
     *
     * @param filename The filename to load from
     * @throws IOException if there is an error reading the file
     */
    void loadMovies(String filename) throws IOException;

    /**
     * Loads series from the specified file.
     *
     * @param filename The filename to load from
     * @throws IOException if there is an error reading the file
     */
    void loadSeries(String filename) throws IOException;

    /**
     * Loads actors from the specified file.
     *
     * @param filename The filename to load from
     * @throws IOException if there is an error reading the file
     */
    void loadActors(String filename) throws IOException;

    /**
     * Loads directors from the specified file.
     *
     * @param filename The filename to load from
     * @throws IOException if there is an error reading the file
     */
    void loadDirectors(String filename) throws IOException;

    /**
     * Loads awards and box office data from the specified file.
     *
     * @param filename The filename to load from
     * @throws IOException if there is an error reading the file
     */
    void loadAwardsAndBoxOffice(String filename) throws IOException;
}