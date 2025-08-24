package com.papel.imdb_clone.service;

import com.papel.imdb_clone.data.RefactoredDataManager;
import com.papel.imdb_clone.enums.Genre;
import com.papel.imdb_clone.model.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * Handles loading and initializing content data from various sources.
 * Supports both synchronous and asynchronous loading of content.
 */
public class ContentDataLoader<T extends Content> {
    private static final Logger logger = LoggerFactory.getLogger(ContentDataLoader.class);
    private static ContentDataLoader instance;

    private final RefactoredDataManager dataManager;
    private ContentService<T> contentService;
    private final ExecutorService executorService;
    private TimeUnit timeUnit;
    private long defaultExpirationTime;
    private int endYear;

    private ContentDataLoader(RefactoredDataManager dataManager) {
        this.dataManager = dataManager;
        this.executorService = Executors.newFixedThreadPool(
                Runtime.getRuntime().availableProcessors(),
                r -> {
                    Thread t = new Thread(r);
                    t.setDaemon(true);
                    t.setName("content-loader-" + t.getId());
                    return t;
                }
        );
    }

    public static synchronized ContentDataLoader getInstance(RefactoredDataManager dataManager) {
        if (instance == null) {
            instance = new ContentDataLoader(dataManager);
        }
        return instance;
    }

    /**
     * Initializes the content data by loading from all available sources.
     * This should be called during application startup.
     */
    public void initializeContentData() {
        logger.info("Starting content data initialization...");

        // Load initial content asynchronously
        CompletableFuture.runAsync(() -> {
            try {
                loadInitialMovies();
                loadInitialSeries();
                logger.info("Content data initialization completed successfully");
            } catch (Exception e) {
                logger.error("Failed to initialize content data: {}", e.getMessage(), e);
            }
        }, executorService);
    }

    /**
     * Loads initial movies data.
     */
    /**
     * Creates a new Movie instance with the given parameters.
     */
    private Movie createMovie(String title, int year, Genre genre, String director, String description) {
        Movie movie = new Movie();
        movie.setTitle(title);
        movie.setReleaseYear(year);
        movie.setGenre(genre);
        movie.setDirector(director);
        movie.setSummary(description);
        movie.setId(Math.abs(UUID.randomUUID().hashCode()));
        return movie;
    }

    /**
     * Creates a new Series instance with the given parameters.
     */
    private Series createSeries(String title, int startYear, Genre genre, String creator, String description) {
        Series series = new Series();
        series.setTitle(title);
        series.setStartYear(startYear);
        series.setEndYear(endYear);
        series.setGenre(genre);
        series.setCreator(creator);
        series.setDescription(description);
        series.setId(Integer.parseInt(UUID.randomUUID().toString()));
        return series;
    }

    private void loadInitialMovies() {
        // Check if we already have movies loaded
        if (!dataManager.getAllMovies().isEmpty()) {
            logger.debug("Movies already loaded, skipping initial load");
            return;
        }

        logger.info("Loading initial movies data...");

        try {
            // Example: Load movies from a predefined list or external source
            List<Movie> initialMovies = List.of(
                    createMovie("The Shawshank Redemption", 1994, Genre.DRAMA, "Frank Darabont", "Two imprisoned men bond over a number of years..."),
                    createMovie("The Godfather", 1972, Genre.DRAMA, "Francis Ford Coppola", "The aging patriarch of an organized crime dynasty..."),
                    createMovie("The Dark Knight", 2008, Genre.ACTION, "Christopher Nolan", "When the menace known as the Joker wreaks havoc..."),
                    createMovie("Pulp Fiction", 1994, Genre.CRIME, "Quentin Tarantino", "The lives of two mob hitmen, a boxer, a gangster's wife...")
            );

            // Save movies
            initialMovies.forEach(movie -> {
                try {
                    contentService.saveContent(movie);
                    logger.debug("Loaded movie: {}", movie.getTitle());
                } catch (Exception e) {
                    logger.warn("Failed to load movie {}: {}", movie.getTitle(), e.getMessage());
                }
            });

            logger.info("Loaded {} initial movies", initialMovies.size());

        } catch (Exception e) {
            logger.error("Error loading initial movies: {}", e.getMessage(), e);
        }
    }

    /**
     * Loads initial series data.
     */
    private void loadInitialSeries() {
        // Check if we already have series loaded
        if (!dataManager.getAllSeries().isEmpty()) {
            logger.debug("Series already loaded, skipping initial load");
            return;
        }

        logger.info("Loading initial series data...");

        try {
            //  Create a sample series with seasons and episodes
            Series breakingBad = createSeries(
                    "Breaking Bad",
                    2008,
                    Genre.DRAMA,
                    "Vince Gilligan",
                    "A high school chemistry teacher diagnosed with inoperable lung cancer..."
            );

            // Add seasons and episodes
            Season season1 = createSeason(breakingBad, 1, 2008);
            addEpisode(season1, 1, "Pilot", "Diagnosed with terminal lung cancer...");
            addEpisode(season1, 2, "Cat's in the Bag...", "After their first drug deal goes terribly wrong...");

            // Save the series
            contentService.saveContent(breakingBad);
            logger.info("Loaded initial series: {}", breakingBad.getTitle());

        } catch (Exception e) {
            logger.error("Error loading initial series: {}", e.getMessage(), e);
        }
    }

    /**
     * Creates a new season for a series.
     */
    private Season createSeason(Series series, int seasonNumber, int year) {
        Season season = new Season();
        season.setSeriesId(series.getId());
        season.setSeasonNumber(seasonNumber);
        season.setYear(year);
        season.setTitle(String.format("Season %d", seasonNumber));
        season.setId(Math.abs(UUID.randomUUID().hashCode()));
        return season;
    }

    /**
     * Adds an episode to a season.
     */
    private void addEpisode(Season season, int episodeNumber, String title, String description) {
        Episode episode = new Episode();
        episode.setSeasonId(season.getId());
        episode.setEpisodeNumber(episodeNumber);
        episode.setTitle(title);
        episode.setDescription(description);
        episode.setId(Math.abs(UUID.randomUUID().hashCode()));

        try {
            contentService.saveContent(episode);
            logger.debug("Added episode: {} - {}", season.getTitle(), episode.getTitle());
        } catch (Exception e) {
            logger.warn("Failed to add episode: {}", e.getMessage());
        }
    }
}
