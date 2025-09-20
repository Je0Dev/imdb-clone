package com.papel.imdb_clone.data;

import com.papel.imdb_clone.model.content.Movie;
import com.papel.imdb_clone.model.content.Series;
import com.papel.imdb_clone.model.people.Actor;
import com.papel.imdb_clone.model.people.Director;
import com.papel.imdb_clone.model.people.User;
import com.papel.imdb_clone.repository.impl.InMemoryMovieRepository;
import com.papel.imdb_clone.repository.impl.InMemorySeriesRepository;
import com.papel.imdb_clone.repository.impl.InMemoryUserRepository;
import com.papel.imdb_clone.service.people.CelebrityService;
import com.papel.imdb_clone.service.content.MoviesService;
import com.papel.imdb_clone.service.content.SeriesService;
import com.papel.imdb_clone.service.search.ServiceLocator;
import com.papel.imdb_clone.service.data.base.DataLoaderService;
import com.papel.imdb_clone.service.data.base.FileDataLoaderService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

import java.util.List;

/**
 * DataManager that delegates to specialized services and repositories.
 * This class serves as a facade for the data layer, providing a unified interface
 * while maintaining separation of concerns.
 */
public class DataManager {
    private static DataManager instance;
    private boolean dataLoaded;
    private User user;

    public static synchronized DataManager getInstance() {
        if (instance == null) {
            // First create instance without service registration to break circular dependency,which means
            // that the instance is created first and then the services are registered.
            instance = new DataManager(true);
            // Then register services if ServiceLocator is available
            try {
                ServiceLocator locator = ServiceLocator.getInstance();
                instance.registerServices();
            } catch (Exception e) {
                logger.warn("Could not register services with ServiceLocator: {}", e.getMessage());
            }
        }
        return instance;
    }

    private static final Logger logger = LoggerFactory.getLogger(DataManager.class);

    // Repositories
    private final InMemoryUserRepository userRepository;
    private final InMemoryMovieRepository movieRepository;
    private final InMemorySeriesRepository seriesRepository;

    // Services
    private final MoviesService moviesService;
    private final SeriesService seriesService;
    private final CelebrityService<Actor> actorService;
    private final CelebrityService<Director> directorService;
    private final DataLoaderService dataLoaderService;

    /**
     * Private constructor for singleton pattern
     */
    public DataManager() {
        this(false);
    }

    /**
     * Private constructor to enforce singleton pattern.
     *
     * @param skipServiceRegistration If true, skips service registration (for testing or special cases)
     */
    private DataManager(boolean skipServiceRegistration) {
        logger.info("Initializing RefactoredDataManager...");

        // Initialize repositories
        this.userRepository = new InMemoryUserRepository();
        this.movieRepository = new InMemoryMovieRepository();
        this.seriesRepository = new InMemorySeriesRepository();

        // Initialize services
        this.moviesService = MoviesService.getInstance();
        this.seriesService = SeriesService.getInstance();
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
                moviesService);

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

        // Register services
        locator.registerService(MoviesService.class, moviesService);
        locator.registerService(SeriesService.class, seriesService);

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
        locator.registerService(DataManager.class, this);

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
        return moviesService.getAll();
    }

    // Get movies service
    public MoviesService getMoviesService() {
        return moviesService;
    }

    // Get series service
    public SeriesService getSeriesService() {
        return seriesService;
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
    /**
     * Gets the movie repository instance.
     *
     * @return The MovieRepository instance
     */
    public InMemoryMovieRepository getMovieRepository() {
        return movieRepository;
    }
/**
     * Gets the series repository instance.
     *
     * @return The SeriesRepository instance
     */
    public InMemorySeriesRepository getSeriesRepository() {
        return seriesRepository;
    }

    //get actor service
    public CelebrityService<Actor> getActorService() {
        return actorService;
    }

    //get director service
    public CelebrityService<Director> getDirectorService() {
        return directorService;
    }

    //get data loader service
    public DataLoaderService getDataLoaderService() {
        return dataLoaderService;
    }

    //get data loaded status
    public boolean isDataLoaded() {
        return dataLoaded;
    }

    //get all movies
    public List<Movie> getMovies() {
        return moviesService.getAll();
    }

}