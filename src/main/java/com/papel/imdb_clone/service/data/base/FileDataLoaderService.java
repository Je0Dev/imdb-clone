package com.papel.imdb_clone.service.data.base;

import com.papel.imdb_clone.model.people.Actor;
import com.papel.imdb_clone.model.people.Director;
import com.papel.imdb_clone.repository.impl.InMemoryMovieRepository;
import com.papel.imdb_clone.repository.impl.InMemorySeriesRepository;
import com.papel.imdb_clone.repository.impl.InMemoryUserRepository;
import com.papel.imdb_clone.service.people.CelebrityService;
import com.papel.imdb_clone.service.content.MoviesService;
import com.papel.imdb_clone.service.content.SeriesService;
import com.papel.imdb_clone.service.data.loader.*;
import com.papel.imdb_clone.service.data.loader.people.ActorDataLoader;
import com.papel.imdb_clone.service.data.loader.people.DirectorDataLoader;
import com.papel.imdb_clone.service.data.loader.content.MovieDataLoader;
import com.papel.imdb_clone.service.data.loader.content.SeriesDataLoader;
import com.papel.imdb_clone.service.data.loader.people.UserDataLoader;
import com.papel.imdb_clone.util.DataFileLoader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * File-based implementation of DataLoaderService.
 * Coordinates loading data using specialized loader classes.
 */
public class FileDataLoaderService implements DataLoaderService {
    private static final Logger logger = LoggerFactory.getLogger(FileDataLoaderService.class);
    private final DataLoaderFactory loaderFactory;

    /**
     * Constructor for FileDataLoaderService.
     * @param userRepository
     * @param movieRepository
     * @param seriesRepository
     * @param seriesService
     * @param actorService
     * @param directorService
     * @param moviesService
     */
    public FileDataLoaderService(
            InMemoryUserRepository userRepository,
            InMemoryMovieRepository movieRepository,
            InMemorySeriesRepository seriesRepository,

SeriesService seriesService,
            CelebrityService<Actor> actorService,
            CelebrityService<Director> directorService,
            MoviesService moviesService) {

        this.loaderFactory = new DataLoaderFactory(
                userRepository,
                movieRepository,
                seriesRepository,
                moviesService,
                seriesService,
                actorService,
                directorService
        );

        // Set up data directory
        String dataDirectory = findDataDirectory();
        logger.info("Using data directory: {}", dataDirectory);
    }

    private String findDataDirectory() {
        // Check common locations for the data directory
        String[] possiblePaths = {
                "src/main/resources/data",
                "target/classes/data",
                System.getProperty("user.dir") + "/src/main/resources/data",
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

    /**
     * Verifies that all required data files exist and are accessible.
     *
     * @return true if all files exist, false otherwise
     */
    private boolean verifyDataFiles(String[][] loadTasks) {
        boolean allFilesExist = true;
        logger.info("\n=== Verifying data files ===");

        for (String[] task : loadTasks) {
            String dataType = task[0];
            String filename = task[1];

            try (InputStream is = DataFileLoader.getResourceAsStream(filename)) {
                logger.info("✓ Found {}: {}", dataType, filename);
            } catch (Exception e) {
                logger.error("✗ Missing {} file: {}", dataType, filename);
                allFilesExist = false;
            }
        }

        // Log a warning if any files are missing
        if (!allFilesExist) {
            logger.warn("\nSome data files are missing. The application may not function correctly.");
            logger.info("Please ensure all data files are in one of these locations:");
            logger.info("- src/main/resources/data/");
            logger.info("- target/classes/data/");
            logger.info("- data/ (in the project root)");
        } else {
            logger.info("\n✓ All data files verified successfully");
        }

        return allFilesExist;
    }

    @Override
    public void loadAllData() throws IOException {
        long startTime = System.currentTimeMillis();
        logger.info("=== Starting Data Loading Process ===");

        // Define the data loading tasks in the correct order
        String[][] loadTasks = {
                {"Users", "users_updated.txt"},
                {"Actors", "actors_updated.txt"},
                {"Directors", "directors_updated.txt"},
                {"Movies", "movies_updated.txt"},
                {"Series", "series_updated.txt"},
                {"Awards and Box Office", "awards_boxoffice_updated.txt"}
        };

        // Verify all data files exist before starting
        boolean filesVerified = verifyDataFiles(loadTasks);
        if (!filesVerified) {
            logger.warn("Proceeding with data loading despite missing files. Some features may not work as expected.");
        }

        int successCount = 0;
        int failureCount = 0;

        for (String[] task : loadTasks) {
            String dataType = task[0];
            String filename = task[1];

            logger.info("\n=== Loading {} from {} ===", dataType, filename);
            long taskStartTime = System.currentTimeMillis();

            try {
                switch (dataType) {
                    case "Users":
                        loadUsers(filename);
                        break;
                    case "Actors":
                        loadActors(filename);
                        break;
                    case "Directors":
                        loadDirectors(filename);
                        break;
                    case "Movies":
                        loadMovies(filename);
                        break;
                    case "Series":
                        loadSeries(filename);
                        break;
                    case "Awards and Box Office":
                        loadAwardsAndBoxOffice(filename);
                        break;
                }
                successCount++;
                long taskTime = System.currentTimeMillis() - taskStartTime;
                logger.info("✓ Successfully loaded {} in {} ms", dataType, taskTime);

            } catch (Exception e) {
                failureCount++;
                logger.error("✗ Failed to load {}: {}", dataType, e.getMessage());
                logger.debug("Stack trace:", e);

                // Continue with next task instead of failing completely
                continue;
            }
        }

        long totalTime = System.currentTimeMillis() - startTime;
        logger.info("\n=== Data Loading Summary ===");
        logger.info("Successfully loaded: {}/{} data sources", successCount, loadTasks.length);
        logger.info("Total time: {} ms", totalTime);

        if (failureCount > 0) {
            logger.warn("Warning: {} data source(s) failed to load. Check the logs for details.", failureCount);
        }
    }

    //load users from file
    @Override
    public void loadUsers(String filename) throws IOException {
        logger.info("Loading users from {}", filename);
        loadDataFile(filename, UserDataLoader.class, "users");
    }

    //load actors from file
    @Override
    public void loadActors(String filename) throws IOException {
        logger.info("Loading actors from {}", filename);
        loadDataFile(filename, ActorDataLoader.class, "actors");
    }

    //load directors from file
    @Override
    public void loadDirectors(String filename) throws IOException {
        logger.info("Loading directors from {}", filename);
        loadDataFile(filename, DirectorDataLoader.class, "directors");
    }

    //load movies from file
    @Override
    public void loadMovies(String filename) throws IOException {
        logger.info("Loading movies from {}", filename);
        loadDataFile(filename, MovieDataLoader.class, "movies");
    }

    //load series from file
    @Override
    public void loadSeries(String filename) throws IOException {
        logger.info("Loading series from {}", filename);
        loadDataFile(filename, SeriesDataLoader.class, "series");
    }

    //load awards and box office from file
    @Override
    public void loadAwardsAndBoxOffice(String filename) throws IOException {
        logger.info("Loading awards and box office data from {}", filename);
        loadDataFile(filename, AwardsDataLoader.class, "awards and box office data");
    }

    /**
     * Helper method to load data from a file using the specified loader class.
     * This method provides detailed error handling and logging to help diagnose issues.
     *
     * @param filename    the name of the file to load (relative to the data directory)
     * @param loaderClass the loader class to use
     * @param dataType    a human-readable description of the data type (for error messages)
     * @param <T>         the type of the loader class
     * @throws IOException if there is an error reading the file
     */
    private <T extends BaseDataLoader> void loadDataFile(String filename, Class<T> loaderClass, String dataType) throws IOException {
        long startTime = System.currentTimeMillis();
        String fullPath = "";
        
        try {
            // Try to get the full path for better error reporting
            try {
                fullPath = DataFileLoader.getResourcePath(filename);
                logger.debug("Found {} data file at: {}", dataType, fullPath);
            } catch (Exception e) {
                logger.warn("Could not determine full path for {}: {}", filename, e.getMessage());
            }
            
            logger.info("Loading {} data from: {}", dataType, fullPath.isEmpty() ? filename : fullPath);

            // Get the loader instance
            T loader = loaderFactory.getLoader(loaderClass);

            // Get the load method using reflection to handle different return types
            Method loadMethod = loaderClass.getMethod("load", String.class);

            // Try to load the file
            try {
                logger.debug("Invoking load method for {} data", dataType);
                Object result = loadMethod.invoke(loader, filename);
                
                // Log success with timing information
                long duration = System.currentTimeMillis() - startTime;
                logger.info("✓ Successfully loaded {} data in {} ms", dataType, duration);
                
                // If the loader returned a count, log it
                if (result instanceof Integer) {
                    logger.info("Processed {} {} records", result, dataType);
                }
            } catch (InvocationTargetException e) {
                // Unwrap the underlying exception
                Throwable cause = e.getCause();
                String errorMsg = String.format("✗ Error loading %s: %s", dataType, 
                    cause != null ? cause.getMessage() : e.getMessage());
                    
                logger.error(errorMsg);
                
                // Provide more context for common issues
                if (cause != null) {
                    logger.debug("Root cause:", cause);
                    
                    // Special handling for file not found
                    if (cause instanceof FileNotFoundException) {
                        logger.error("The data file could not be found. Please check if the file exists at: {}", 
                            fullPath.isEmpty() ? filename : fullPath);
                        logger.info("Searched in the following locations:");
                        logger.info("1. src/main/resources/data/{}", filename);
                        logger.info("2. target/classes/data/{}", filename);
                        logger.info("3. data/{}", filename);
                    }
                    
                    // Special handling for parsing errors
                    if (cause.getMessage() != null && 
                        (cause.getMessage().contains("parsing") || 
                         cause.getMessage().contains("format"))) {
                        logger.error("There appears to be an issue with the data file format. Please verify the file is correctly formatted.");
                    }
                    
                    // Special handling for null pointer exceptions
                    if (cause instanceof NullPointerException) {
                        logger.error("A null pointer occurred while processing the data. This might indicate missing or malformed data.");
                    }
                }
                
                throw new IOException(errorMsg, cause != null ? cause : e);
            }
        } catch (NoSuchMethodException | IllegalAccessException e) {
            // Log error and rethrow
            String errorMsg = String.format("✗ Error setting up loader for %s: %s", dataType, e.getMessage());
            logger.error(errorMsg);
            logger.debug("Stack trace:", e);
            throw new IOException(errorMsg, e);
        } catch (Exception e) {
            // Log error and rethrow
            String errorMsg = String.format("✗ Unexpected error loading %s: %s", dataType, e.getMessage());
            logger.error(errorMsg);
            logger.debug("Stack trace:", e);
            
            // Provide additional context for common issues
            if (e.getCause() != null) {
                logger.error("Caused by: {}", e.getCause().getMessage());
                logger.debug("Root cause:", e.getCause());
            }
            
            throw new IOException(errorMsg, e);
        }
    }
}