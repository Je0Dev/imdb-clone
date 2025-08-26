package com.papel.imdb_clone.service.data;

import com.papel.imdb_clone.model.Actor;
import com.papel.imdb_clone.model.Director;
import com.papel.imdb_clone.model.Movie;
import com.papel.imdb_clone.model.Series;
import com.papel.imdb_clone.repository.impl.InMemoryMovieRepository;
import com.papel.imdb_clone.repository.impl.InMemorySeriesRepository;
import com.papel.imdb_clone.repository.impl.InMemoryUserRepository;
import com.papel.imdb_clone.service.CelebrityService;
import com.papel.imdb_clone.service.ContentService;
import com.papel.imdb_clone.service.data.loader.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * File-based implementation of DataLoaderService.
 * Coordinates loading data using specialized loader classes.
 */
public class FileDataLoaderService implements DataLoaderService {
    private static final Logger logger = LoggerFactory.getLogger(FileDataLoaderService.class);
    private final DataLoaderFactory loaderFactory;
    private final String dataDirectory;

    public FileDataLoaderService(
            InMemoryUserRepository userRepository,
            InMemoryMovieRepository movieRepository,
            InMemorySeriesRepository seriesRepository,
            ContentService<Series> seriesService,
            CelebrityService<Actor> actorService,
            CelebrityService<Director> directorService,
            ContentService<Movie> movieService) {

        this.loaderFactory = new DataLoaderFactory(
                userRepository,
                movieRepository,
                seriesRepository,
                movieService,
                seriesService,
                actorService,
                directorService
        );

        // Set up data directory
        this.dataDirectory = findDataDirectory();
        logger.info("Using data directory: {}", dataDirectory);
    }

    private String findDataDirectory() {
        // Check common locations for the data directory
        String[] possiblePaths = {
            "src/main/resources/data",
            "resources/data",
            "data",
            "target/classes/data",
            System.getProperty("user.dir") + "/src/main/resources/data",
            System.getProperty("user.dir") + "/data",
            System.getProperty("user.dir") + "/target/classes/data"
        };

        for (String path : possiblePaths) {
            File dir = new File(path);
            if (dir.exists() && dir.isDirectory()) {
                // Check if it contains any of our data files
                    return dir.getAbsolutePath();
            }
        }
        
        // Default to the standard Maven resources directory
        return "src/main/resources/data";
    }

    @Override
    public void loadAllData() throws IOException {
        long startTime = System.currentTimeMillis();
        logger.info("Starting to load all data...");
        logger.info("Data directory: {}", new File(dataDirectory).getAbsolutePath());

        try {
            // List available files in the data directory
            File dataDir = new File(dataDirectory);
            if (!dataDir.exists() || !dataDir.isDirectory()) {
                throw new IOException("Data directory not found: " + dataDir.getAbsolutePath());
            }

            // Log available files for debugging
            File[] files = dataDir.listFiles((dir, name) -> name.endsWith(".txt"));
            if (files != null && files.length > 0) {
                logger.info("Found {} data files in {}:", files.length, dataDir.getAbsolutePath());
                for (File file : files) {
                    logger.info("  - {} ({} bytes)", file.getName(), file.length());
                }
            } else {
                logger.warn("No data files found in {}", dataDir.getAbsolutePath());
            }

            // Load data in the correct order
            loadActors("actors_updated.txt");
            loadDirectors("directors_updated.txt");
            loadMovies("movies_updated.txt");
            loadSeries("series_updated.txt");
            loadAwardsAndBoxOffice("awards_boxoffice_updated.txt");
            loadUsers("users_updated.txt");

            long endTime = System.currentTimeMillis();
            double duration = (endTime - startTime) / 1000.0;
            logger.info("\n=== Data loading completed in {:.2f} seconds ===\n", duration);

        } catch (Exception e) {
            String errorMsg = String.format(
                "\n!!! ERROR DURING DATA LOADING !!!\n" +
                "Error: %s\n" +
                "Please check that the data files exist in the data directory and are properly formatted.\n" +
                "Data directory: %s\n" +
                "Current working directory: %s",
                e.getMessage(),
                new File(dataDirectory).getAbsolutePath(),
                System.getProperty("user.dir")
            );
            logger.error(errorMsg, e);
            throw new IOException(errorMsg, e);
        }
    }

    @Override
    public void loadUsers(String filename) throws IOException {
        logger.info("Loading users from {}", filename);
        loadDataFile(filename, UserDataLoader.class, "users");
    }

    @Override
    public void loadActors(String filename) throws IOException {
        logger.info("Loading actors from {}", filename);
        loadDataFile(filename, ActorDataLoader.class, "actors");
    }

    @Override
    public void loadDirectors(String filename) throws IOException {
        logger.info("Loading directors from {}", filename);
        loadDataFile(filename, DirectorDataLoader.class, "directors");
    }

    @Override
    public void loadMovies(String filename) throws IOException {
        logger.info("Loading movies from {}", filename);
        loadDataFile(filename, MovieDataLoader.class, "movies");
    }

    @Override
    public void loadSeries(String filename) throws IOException {
        logger.info("Loading series from {}", filename);
        loadDataFile(filename, SeriesDataLoader.class, "series");
    }

    @Override
    public void loadAwardsAndBoxOffice(String filename) throws IOException {
        logger.info("Loading awards and box office data from {}", filename);
        loadDataFile(filename, AwardsDataLoader.class, "awards and box office data");
    }

    /**
     * Helper method to load data from a file using the specified loader class.
     *
     * @param filename the name of the file to load (relative to the data directory)
     * @param loaderClass the loader class to use
     * @param dataType a human-readable description of the data type (for error messages)
     * @param <T> the type of the loader class
     * @throws IOException if there is an error reading the file
     */
    private <T extends BaseDataLoader> void loadDataFile(String filename, Class<T> loaderClass, String dataType) throws IOException {
        long startTime = System.currentTimeMillis();
        
        try {
            logger.info("Loading {} from: {}", dataType, filename);
            
            // Get the loader instance
            T loader = loaderFactory.getLoader(loaderClass);
            
            // Get the load method using reflection to handle different return types
            Method loadMethod = loaderClass.getMethod("load", String.class);
            
            // Try to load the file
            try {
                // Invoke the load method
                Object result = loadMethod.invoke(loader, filename);
                
                // If the load method returns an int (like SeriesDataLoader), log it
                if (result instanceof Integer) {
                    logger.info("Loaded {} {} items", result, dataType);
                }
                
            } catch (InvocationTargetException e) {
                // If the load method throws an exception, unwrap and rethrow it
                Throwable cause = e.getCause();
                if (cause instanceof IOException) {
                    throw (IOException) cause;
                } else if (cause instanceof RuntimeException) {
                    throw (RuntimeException) cause;
                } else {
                    throw new IOException("Failed to load " + dataType, cause);
                }
            }
            
            long endTime = System.currentTimeMillis();
            logger.info("Successfully loaded {} in {} ms", dataType, (endTime - startTime));
            
        } catch (NoSuchMethodException | IllegalAccessException e) {
            String errorMsg = String.format("Error setting up loader for %s: %s", dataType, e.getMessage());
            logger.error(errorMsg, e);
            throw new IOException(errorMsg, e);
        } catch (Exception e) {
            String errorMsg = String.format("Error loading %s from %s: %s", 
                dataType, filename, e.getMessage());
            logger.error(errorMsg);
            if (e.getCause() != null) {
                logger.error("Caused by: ", e.getCause());
            }
            throw new IOException(errorMsg, e);
        }
    }
}