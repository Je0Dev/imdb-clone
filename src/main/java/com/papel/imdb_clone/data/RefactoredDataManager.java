package com.papel.imdb_clone.data;

import com.papel.imdb_clone.exceptions.ContentNotFoundException;
import com.papel.imdb_clone.model.*;
import com.papel.imdb_clone.repository.MovieRepository;
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
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

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

        // Initialize services with repositories
        this.movieService = new ContentService<>(Movie.class);
        this.seriesService = new ContentService<>(Series.class);
        this.actorService = new CelebrityService<>(Actor.class);
        this.directorService = new CelebrityService<>(Director.class);
        // Initialize file data loader service as the DataLoaderService implementation
        this.dataLoaderService = new FileDataLoaderService(
                userRepository,
                movieRepository,
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

    // User operations
    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    public Optional<User> findUserById(int id) {
        return userRepository.findById(id);
    }

    public Optional<User> findUserByUsername(String username) {
        return userRepository.findByUsername(username);
    }

    /**
     * Finds a user by their email address.
     *
     * @param email The email address to search for (case-insensitive)
     * @return An Optional containing the user if found, or empty if not found
     */
    public Optional<Object> findUserByEmail(String email) {
        if (email == null || email.trim().isEmpty()) {
            return Optional.empty();
        }
        // Search through all users to find a matching email (case-insensitive)
        return userRepository.findByEmail(email.toLowerCase());
    }

    public User saveUser(User user) {
        if (user == null) {
            throw new IllegalArgumentException("User cannot be null");
        }
        return userRepository.save(user);
    }

    // Movie operations
    public List<Movie> getAllMovies() {
        return movieService.getAll();
    }


    public Optional<Movie> findMovieByTitle(String title) {
        List<Movie> results = movieService.findByTitle(title);
        return results.isEmpty() ? Optional.empty() : Optional.of(results.get(0));
    }

    public List<Movie> searchMovies(SearchCriteria criteria) {
        return movieService.search(criteria);
    }


    public Movie saveMovie(Movie movie) {
        return movieService.save(movie);
    }

    public boolean deleteMovie(int id) {
        return movieService.delete(id);
    }

    public boolean rateMovie(int movieId, int userId, double rating) {
        // For now, just return true as rating functionality needs to be implemented
        logger.info("Rating movie {} with {} stars by user {}", movieId, rating, userId);
        return true;
    }

    public List<Movie> getTopRatedMovies(int limit) {
        // For now, return all movies (would need rating implementation)
        List<Movie> allMovies = movieService.getAll();
        return allMovies.stream().limit(limit).collect(java.util.stream.Collectors.toList());
    }

    public List<Movie> getRecentMovies(int limit) {
        // For now, return all movies (would need timestamp implementation)
        List<Movie> allMovies = movieService.getAll();
        return allMovies.stream().limit(limit).collect(java.util.stream.Collectors.toList());
    }

    public long getMovieCount() {
        return movieService.getAll().size();
    }

    // Series operations
    public List<Series> getAllSeries() {
        return seriesService.getAll();
    }

    public Optional<Series> findSeriesById(int id) {
        try {
            return Optional.of(seriesService.findById(id));
        } catch (Exception e) {
            return Optional.empty();
        }
    }

    public Optional<Series> findSeriesByTitle(String title) {
        List<Series> results = seriesService.findByTitle(title);
        return results.isEmpty() ? Optional.empty() : Optional.of(results.get(0));
    }

    public List<Series> searchSeries(SearchCriteria criteria) {
        return seriesService.search(criteria);
    }

    public List<Series> searchSeries(String query) {
        if (query == null || query.trim().isEmpty()) {
            return new ArrayList<>();
        }
        return seriesService.findByTitle(query);
    }

    public Series saveSeries(Series series) {
        return seriesService.save(series);
    }

    public boolean deleteSeries(int id) {
        return seriesService.delete(id);
    }

    public long getSeriesCount() {
        return seriesService.getAll().size();
    }


    /**
     * Finds all episodes for a given series ID.
     *
     * @param seriesId The ID of the series
     * @return List of episodes for the series, or empty list if series not found
     */
    public List<Episode> findEpisodesBySeriesId(int seriesId) {
        try {
            Series series = seriesService.findById(seriesId);
            return series.getSeasons().stream()
                    .flatMap(season -> season.getEpisodes().stream())
                    .collect(Collectors.toList());
        } catch (ContentNotFoundException e) {
            return Collections.emptyList();
        }
    }

    // Actor operations
    public List<Actor> getAllActors() {
        return actorService.getAll();
    }

    public Optional<Actor> findActorById(int id) {
        return actorService.findById(id);
    }


    public Actor saveActor(Actor actor) {
        return actorService.save(actor);
    }

    public boolean deleteActor(int id) {
        return actorService.delete(id);
    }

    public long getActorCount() {
        return actorService.getAll().size();
    }

    // Director operations
    public List<Director> getAllDirectors() {
        return directorService.getAll();
    }

    public Optional<Director> findDirectorById(int id) {
        return directorService.findById(id);
    }

    public List<Director> findDirectorsByName(String name) {
        return (List<Director>) directorService.findByName(name);
    }

    public Director saveDirector(Director director) {
        return directorService.save(director);
    }

    public boolean deleteDirector(int id) {
        return directorService.delete(id);
    }

    public long getDirectorCount() {
        return directorService.getAll().size();
    }


    // Utility methods for backward compatibility
    public void clearAllData() {
        logger.info("Clearing all data from services");

        userRepository.clear();
        movieRepository.clear();
        // Clear services by removing all items
        movieService.getAll().forEach(movie -> movieService.delete(movie.getId()));
        seriesService.getAll().forEach(series -> seriesService.delete(series.getId()));
        actorService.getAll().forEach(actor -> actorService.delete(actor.getId()));
        directorService.getAll().forEach(director -> directorService.delete(director.getId()));


        logger.info("All data cleared");
    }

    // Getters for services (for advanced usage)
    public ContentService<Movie> getMovieService() {
        return movieService;
    }

    public ContentService<Series> getSeriesService() {
        return seriesService;
    }

    public CelebrityService<Actor> getActorService() {
        return actorService;
    }

    public CelebrityService<Director> getDirectorService() {
        return directorService;
    }


    /**
     * Gets the user repository instance.
     *
     * @return The UserRepository instance
     */
    public InMemoryUserRepository getUserRepository() {
        return userRepository;
    }

    public MovieRepository getMovieRepository() {
        return movieRepository;
    }

    // Backward compatibility methods for controllers
    public List<Movie> getMovies() {
        return getAllMovies();
    }

    public List<Series> getSeries() {
        return getAllSeries();
    }

    public void addMovie(Movie movie) {
        saveMovie(movie);
    }

    public void addSeries(Series series) {
        saveSeries(series);
    }

    public void removeMovie(Movie movie) {
        deleteMovie(movie.getId());
    }

    public void removeSeries(Series series) {
        deleteSeries(series.getId());
    }


    public List<Content> searchContent(SearchCriteria criteria) {
        List<Content> results = new ArrayList<>();
        results.addAll(searchMovies(criteria));
        results.addAll(searchSeries(criteria));
        return results;
    }

    // Convenience overload used by some controllers
    public boolean rateMovie(Movie movie, double rating) {
        if (movie == null) return false;
        return rateMovie(movie.getId(), -1, rating);
    }

    public boolean isDataLoaded() {
        return dataLoaded;
    }

    /**
     * Authenticates a user with the given username and password.
     *
     * @param username the username
     * @param password the plain text password
     * @return the authenticated user if successful, null otherwise
     */
    public User authenticateUser(String username, String password) {
        if (userRepository == null) {
            logger.error("UserRepository is not initialized");
            return null;
        }

        try {
            User user = userRepository.findByUsername(username)
                    .filter(u -> u.getPassword() != null && u.getPassword().equals(password))
                    .orElse(null);

            if (user != null) {
                this.user = user; // Store the authenticated user
            }

            return user;
        } catch (Exception e) {
            logger.error("Error during user authentication", e);
            return null;
        }
    }
}