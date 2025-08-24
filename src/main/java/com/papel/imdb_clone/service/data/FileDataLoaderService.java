package com.papel.imdb_clone.service.data;

import com.papel.imdb_clone.model.Actor;
import com.papel.imdb_clone.model.Director;
import com.papel.imdb_clone.model.Movie;
import com.papel.imdb_clone.model.Series;
import com.papel.imdb_clone.repository.impl.InMemoryMovieRepository;
import com.papel.imdb_clone.repository.impl.InMemoryUserRepository;
import com.papel.imdb_clone.service.CelebrityService;
import com.papel.imdb_clone.service.ContentService;
import com.papel.imdb_clone.service.data.loader.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * File-based implementation of DataLoaderService.
 * Coordinates loading data using specialized loader classes.
 */
public class FileDataLoaderService implements DataLoaderService {
    private static final Logger logger = LoggerFactory.getLogger(FileDataLoaderService.class);

    private final DataLoaderFactory loaderFactory;
    
    private final Map<String, String> dataFiles = new HashMap<>();


    public FileDataLoaderService(
            InMemoryUserRepository userRepository,
            InMemoryMovieRepository movieRepository,
            ContentService<Series> seriesService,
            CelebrityService<Actor> actorService,
            CelebrityService<Director> directorService, ContentService<Movie> movieService) {

        this.loaderFactory = new DataLoaderFactory(
                userRepository,
                movieRepository,
                movieService, seriesService,
                actorService,
                directorService
        );

        // Define data files and their corresponding loader keys
        dataFiles.put("actors_updated.txt", "Actors");
        dataFiles.put("directors_updated.txt", "Directors");
        dataFiles.put("movies_updated.txt", "Movies");  // Updated to use formatted movies file
        dataFiles.put("series_updated.txt", "Series");
        dataFiles.put("awards_boxoffice_updated.txt", "Awards");
        dataFiles.put("users_updated.txt", "Users");    // Updated to use formatted users file
    }

    @Override
    public void loadAllData() throws IOException {
        long startTime = System.currentTimeMillis();
        logger.info("Starting to load all data using specialized loaders...");

        try {
            // Load actors and directors first since they're dependencies for movies and series
            logger.info("=== Loading actors and directors ===");
            loadActors("actors_updated.txt");
            loadDirectors("directors_updated.txt");

            // Then load movies and series which depend on actors and directors
            logger.info("\n=== Loading movies and series ===");
            loadMovies("movies_updated.txt");
            loadSeries("series_updated.txt");

            // Load awards and box office data which depends on movies and series
            logger.info("\n=== Loading awards and box office data ===");
            loadAwardsAndBoxOffice("awards_boxoffice_updated.txt");

            // Finally load users since they might have references to other entities
            logger.info("\n=== Loading users ===");
            loadUsers("users_updated.txt");

            long endTime = System.currentTimeMillis();
            double duration = (endTime - startTime) / 1000.0;
            logger.info("\n=== Data loading completed in {:.2f} seconds ===", duration);

        } catch (Exception e) {
            logger.error("\n!!! ERROR DURING DATA LOADING !!!", e);
            throw new IOException("Failed to load data: " + e.getMessage(), e);
        }
    }

    @Override
    public void loadUsers(String filename) throws IOException {
        logger.info("Loading users from {}", filename);
        long startTime = System.currentTimeMillis();

        try {
            UserDataLoader loader = loaderFactory.getLoader(UserDataLoader.class);
            loader.load(filename);

            long endTime = System.currentTimeMillis();
            logger.info("Successfully loaded users in {} ms", (endTime - startTime));
        } catch (Exception e) {
            logger.error("Error loading users from {}: {}", filename, e.getMessage(), e);
            throw new IOException("Failed to load users: " + e.getMessage(), e);
        }
    }

    @Override
    public void loadActors(String filename) throws IOException {
        logger.info("Loading actors from {}", filename);
        long startTime = System.currentTimeMillis();

        try {
            ActorDataLoader loader = loaderFactory.getLoader(ActorDataLoader.class);
            loader.load(filename);

            long endTime = System.currentTimeMillis();
            logger.info("Successfully loaded actors in {} ms", (endTime - startTime));
        } catch (Exception e) {
            logger.error("Error loading actors from {}: {}", filename, e.getMessage(), e);
            throw new IOException("Failed to load actors: " + e.getMessage(), e);
        }
    }

    @Override
    public void loadDirectors(String filename) throws IOException {
        logger.info("Loading directors from {}", filename);
        long startTime = System.currentTimeMillis();

        try {
            DirectorDataLoader loader = loaderFactory.getLoader(DirectorDataLoader.class);
            loader.load(filename);

            long endTime = System.currentTimeMillis();
            logger.info("Successfully loaded directors in {} ms", (endTime - startTime));
        } catch (Exception e) {
            logger.error("Error loading directors from {}: {}", filename, e.getMessage(), e);
            throw new IOException("Failed to load directors: " + e.getMessage(), e);
        }
    }

    @Override
    public void loadMovies(String filename) throws IOException {
        logger.info("Loading movies from {}", filename);
        long startTime = System.currentTimeMillis();

        try {
            MovieDataLoader loader = loaderFactory.getLoader(MovieDataLoader.class);
            loader.load(filename);

            long endTime = System.currentTimeMillis();
            logger.info("Successfully loaded movies in {} ms", (endTime - startTime));
        } catch (Exception e) {
            logger.error("Error loading movies from {}: {}", filename, e.getMessage(), e);
            throw new IOException("Failed to load movies: " + e.getMessage(), e);
        }
    }

    @Override
    public void loadSeries(String filename) throws IOException {
        logger.info("Loading series from {}", filename);
        long startTime = System.currentTimeMillis();

        try {
            SeriesDataLoader loader = loaderFactory.getLoader(SeriesDataLoader.class);
            loader.load(filename);

            long endTime = System.currentTimeMillis();
            logger.info("Successfully loaded series in {} ms", (endTime - startTime));
        } catch (Exception e) {
            logger.error("Error loading series from {}: {}", filename, e.getMessage(), e);
            throw new IOException("Failed to load series: " + e.getMessage(), e);
        }
    }

    @Override
    public void loadAwardsAndBoxOffice(String filename) throws IOException {
        logger.info("Loading awards and box office data from {}", filename);
        long startTime = System.currentTimeMillis();

        try {
            AwardsDataLoader loader = loaderFactory.getLoader(AwardsDataLoader.class);
            loader.load(filename);

            long endTime = System.currentTimeMillis();
            logger.info("Successfully loaded awards and box office data in {} ms", (endTime - startTime));
        } catch (Exception e) {
            logger.error("Error loading awards and box office data from {}: {}", filename, e.getMessage(), e);
            throw new IOException("Failed to load awards and box office data: " + e.getMessage(), e);
        }
    }

    // Helper methods for parsing CSV and other utilities can be added here if needed
    // ...
}