package com.papel.imdb_clone.service.content;

import com.papel.imdb_clone.enums.Ethnicity;
import com.papel.imdb_clone.enums.Genre;
import com.papel.imdb_clone.model.content.Movie;
import com.papel.imdb_clone.model.people.Actor;
import com.papel.imdb_clone.util.FileUtils;

import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

/**
 * Service class for managing Movie content.
 */
public class MoviesService extends BaseContentService<Movie> {
    private static final Logger logger = Logger.getLogger(MoviesService.class.getName());
    private static MoviesService instance;

    //constructor
    private MoviesService() {
        super(Movie.class);
    }

    //get instance of MoviesService
    public static synchronized MoviesService getInstance() {
        if (instance == null) {
            instance = new MoviesService();
        }
        return instance;
    }

    /**
     * Loads movies from the movies_updated.txt file.
     * The expected format is: Title,Year,Genre,Duration,Director,Rating,Actors
     * Actors are separated by semicolons (;)
     */
    @Override
    protected void loadFromFile() {
        String filePath = "src/main/resources/data/content/movies_updated.txt";
        List<String> lines = FileUtils.readLines(filePath);

        if (lines.isEmpty()) {
            logger.warning("No movies found in file: " + filePath);
            return;
        }

        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy");

        for (String line : lines) {
            try {
                String[] parts = line.split(",");
                if (parts.length < 6) {
                    logger.warning("Skipping invalid movie line: " + line);
                    continue;
                }

                String title = parts[0].trim();
                int year = Integer.parseInt(parts[1].trim());
                Genre genre = Genre.valueOf(parts[2].trim());
                int duration = Integer.parseInt(parts[3].trim());
                String director = parts[4].trim();
                double rating = Double.parseDouble(parts[5].trim());

                // Create movie with basic info
                Movie movie = new Movie();
                movie.setTitle(title);
                movie.setYear(dateFormat.parse(String.valueOf(year)));
                movie.setGenre(genre);
                movie.setDuration(duration);
                movie.setDirector(director);
                movie.setImdbRating(rating);

                // Add actors if available
                if (parts.length > 6) {
                    String[] actorNames = parts[6].split(";");
                    for (String actorName : actorNames) {
                        String[] nameParts = actorName.trim().split("\\s+", 2);
                        if (nameParts.length == 2) {
                            Actor actor = Actor.getInstance(nameParts[0], nameParts[1],
                                    LocalDate.now(), ' ', Ethnicity.UNKNOWN);
                            movie.addActor(actor);
                        }
                    }
                }

                save(movie);

            } catch (Exception e) {
                logger.log(Level.WARNING, "Error processing movie line: " + line, e);
            }
        }

        logger.info("Loaded " + contentList.size() + " movies from file");
    }

    /**
     * Escapes commas in strings to prevent CSV parsing issues.
     * @param input The input string to escape
     * @return The escaped string
     */
    private String escapeCommas(String input) {
        if (input == null) {
            return "";
        }
        // If the input contains a comma, wrap it in quotes
        if (input.contains(",")) {
            return "\"" + input.replace("\"", "\"\"") + "\"";
        }
        return input;
    }

    //initialize sample data
    @Override
    protected void initializeSampleData() {
        lock.writeLock().lock();
        try {
            // Clear existing data
            contentList.clear();

            // Load movies from file
            loadFromFile();

            // If no movies were loaded from file, fall back to sample data
            if (contentList.isEmpty()) {
                logger.warning("No movies loaded from file, using sample data");

                // Sample data as fallback
                List<Genre> shawshankGenres = new ArrayList<>();
                shawshankGenres.add(Genre.DRAMA);
                List<Actor> shawshankActors = new ArrayList<>();
                shawshankActors.add(Actor.getInstance("Tim", "Robbins", LocalDate.of(1958, 10, 16), 'M', Ethnicity.CAUCASIAN));
                shawshankActors.add(Actor.getInstance("Morgan", "Freeman", LocalDate.of(1937, 6, 1), 'M', Ethnicity.AFRICAN));

                // Convert genres list to a comma-separated string
                String shawshankGenreStr = shawshankGenres.stream()
                        .map(Enum::name)
                        .collect(Collectors.joining(", "));

                Movie movie1 = new Movie(
                        "The Shawshank Redemption",
                        1994,
                        shawshankGenreStr,
                        "Frank Darabont",
                        new HashMap<>(),
                        9.3);
                movie1.setActors(shawshankActors);
                save(movie1);

                // Movie 2 - The Godfather
                List<Genre> godfatherGenres = new ArrayList<>();
                godfatherGenres.add(Genre.CRIME);
                godfatherGenres.add(Genre.DRAMA);
                List<Actor> godfatherActors = new ArrayList<>();
                godfatherActors.add(Actor.getInstance("Marlon", "Brando", LocalDate.of(1924, 4, 3), 'M', Ethnicity.CAUCASIAN));
                godfatherActors.add(Actor.getInstance("Al", "Pacino", LocalDate.of(1940, 4, 25), 'M', Ethnicity.CAUCASIAN));

                String godfatherGenreStr = godfatherGenres.stream()
                        .map(Enum::name)
                        .collect(Collectors.joining(", "));

                Movie movie2 = new Movie(
                        "The Godfather",
                        1972,
                        godfatherGenreStr,
                        "Francis Ford Coppola",
                        new HashMap<>(),
                        9.2
                );
                movie2.setActors(godfatherActors);
                save(movie2);

                // Movie 3 - The Dark Knight
                List<Genre> darkKnightGenres = new ArrayList<>();
                darkKnightGenres.add(Genre.ACTION);
                darkKnightGenres.add(Genre.CRIME);
                darkKnightGenres.add(Genre.DRAMA);
                List<Actor> darkKnightActors = new ArrayList<>();
                darkKnightActors.add(Actor.getInstance("Christian", "Bale", LocalDate.of(1974, 1, 30), 'M', Ethnicity.CAUCASIAN));
                darkKnightActors.add(Actor.getInstance("Heath", "Ledger", LocalDate.of(1979, 4, 4), 'M', Ethnicity.CAUCASIAN));

                String darkKnightGenreStr = darkKnightGenres.stream()
                        .map(Enum::name)
                        .collect(Collectors.joining(", "));

                Movie movie3 = new Movie(
                        "The Dark Knight",
                        2008,
                        darkKnightGenreStr,
                        "Christopher Nolan",
                        new HashMap<>(),
                        9.0
                );
                movie3.setActors(darkKnightActors);
                save(movie3);

                logger.info("Initialized with " + contentList.size() + " sample movies");
            }
        } finally {
            //unlock the write lock when done, which means that other threads can modify the list
            lock.writeLock().unlock();
        }
        }
    }


