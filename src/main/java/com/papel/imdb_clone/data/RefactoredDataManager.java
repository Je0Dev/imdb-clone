package com.papel.imdb_clone.data;

import com.papel.imdb_clone.model.*;
import com.papel.imdb_clone.repository.impl.InMemoryMovieRepository;
import com.papel.imdb_clone.repository.impl.InMemorySeriesRepository;
import com.papel.imdb_clone.repository.impl.InMemoryUserRepository;
import com.papel.imdb_clone.service.CelebrityService;
import com.papel.imdb_clone.service.ContentService;
import com.papel.imdb_clone.service.ServiceLocator;
import com.papel.imdb_clone.service.data.DataLoaderService;
import com.papel.imdb_clone.service.data.FileDataLoaderService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

import java.util.List;

/**
 * Refactored DataManager that delegates to specialized services and repositories.
 * This class serves as a facade for the data layer, providing a unified interface
 * while maintaining separation of concerns.
 */
public class RefactoredDataManager {
    private static RefactoredDataManager instance;
    private boolean dataLoaded;
    private User user;

    public static synchronized RefactoredDataManager getInstance() {
        if (instance == null) {
            // First create instance without service registration to break circular dependency
            instance = new RefactoredDataManager(true);
            // Then register services if ServiceLocator is available
            try {
                ServiceLocator locator = ServiceLocator.getInstance();
                instance.registerServices();
            } catch (Exception e) {
                logger.warn("Could not register services with ServiceLocator: " + e.getMessage());
            }
        }
        return instance;
    }

    private static final Logger logger = LoggerFactory.getLogger(RefactoredDataManager.class);

    // Repositories
    private final InMemoryUserRepository userRepository;
    private final InMemoryMovieRepository movieRepository;
    private InMemorySeriesRepository seriesRepository;

    // Services
    private final ContentService<Movie> movieService;
    private final ContentService<Series> seriesService;
    private final CelebrityService<Actor> actorService;
    private final CelebrityService<Director> directorService;
    private final DataLoaderService dataLoaderService;

    /**
     * Private constructor for singleton pattern
     */
    public RefactoredDataManager() {
        this(false);
    }

    /**
     * Private constructor to enforce singleton pattern.
     *
     * @param skipServiceRegistration If true, skips service registration (for testing or special cases)
     */
    private RefactoredDataManager(boolean skipServiceRegistration) {
        logger.info("Initializing RefactoredDataManager...");

        // Initialize repositories
        this.userRepository = new InMemoryUserRepository();
        this.movieRepository = new InMemoryMovieRepository();
        this.seriesRepository = new InMemorySeriesRepository();

        // Initialize services
        this.movieService = new ContentService<>(Movie.class);
        this.seriesService = new ContentService<>(Series.class);
        this.actorService = new CelebrityService<>(Actor.class);
        this.directorService = new CelebrityService<>(Director.class);

        // Initialize file data loader service as the DataLoaderService implementation
        this.dataLoaderService = new FileDataLoaderService(
                userRepository,
                movieRepository,
                seriesRepository,
                seriesService,
                actorService,
                directorService,
                movieService);

        // Only register services if not skipped
        if (!skipServiceRegistration) {
            registerServices();
        }

        logger.info("RefactoredDataManager initialization complete");
    }

    /**
     * Registers this instance's services with the ServiceLocator.
     * Should be called after construction to complete setup.
     */
    public void registerServices() {
        ServiceLocator locator = ServiceLocator.getInstance();

        // Register content services
        locator.registerService(ContentService.class, movieService, "movie");
        locator.registerService(ContentService.class, seriesService, "series");

        // Register celebrity services
        locator.registerService(CelebrityService.class, actorService, "actor");
        locator.registerService(CelebrityService.class, directorService, "director");

        // Register data loading service
        locator.registerService(DataLoaderService.class, dataLoaderService);

        // Register repositories
        locator.registerService(InMemoryUserRepository.class, userRepository);
        locator.registerService(InMemoryMovieRepository.class, movieRepository);
        locator.registerService(InMemorySeriesRepository.class, seriesRepository);

        // Register self
        locator.registerService(RefactoredDataManager.class, this);

        logger.debug("All services registered with ServiceLocator");
    }

    /**
     * Loads all data from configured sources.
     */
    public void loadAllData() throws IOException {
        logger.info("Loading all data using DataLoaderService");
        dataLoaderService.loadAllData();
        logger.info("Data loading completed");
    }


    // Movie operations
    public List<Movie> getAllMovies() {
        return movieService.getAll();
    }

    // Series operations
    public List<Series> getAllSeries() {
        return seriesService.getAll();
    }


    /**
     * Gets the user repository instance.
     *
     * @return The UserRepository instance
     */
    public InMemoryUserRepository getUserRepository() {
        return userRepository;
    }
    public InMemoryMovieRepository getMovieRepository() {
        return movieRepository;
    }

    public InMemorySeriesRepository getSeriesRepository() {
        return seriesRepository;
    }

    public CelebrityService<Actor> getActorService() {
        return actorService;
    }

    public CelebrityService<Director> getDirectorService() {
        return directorService;
    }

    public boolean isDataLoaded() {
        return dataLoaded;
    }

    public void addMovie(Movie movie) {
        movieService.add(movie);
        movieRepository.add(movie);
        dataLoaded = true;
    }

    public List<Movie> getMovies() {
        return movieRepository.getAll();
    }

    public void updateMovie(Movie movie) {
        movieService.update(movie);
        movieRepository.update(movie);
        dataLoaded = true;
    }

    public void deleteMovie(int id) {
        movieService.delete(id);
        movieRepository.delete(id);
        dataLoaded = true;
    }

    public void deleteSeries(Series selectedSeries) {
        seriesService.delete(selectedSeries.getId());
        seriesRepository.delete(selectedSeries.getId());
        dataLoaded = true;
    }

    public void saveSeries(Series newSeries) {
        seriesService.save(newSeries);
        seriesRepository.save(newSeries);
        dataLoaded = true;
    }


    public void deleteMovie(Movie selectedMovie) {
        movieService.delete(selectedMovie.getId());
        movieRepository.delete(selectedMovie.getId());
        dataLoaded = true;
    }
}