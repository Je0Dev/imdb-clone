package com.papel.imdb_clone.service.data.loader.content;

import com.papel.imdb_clone.enums.Ethnicity;
import com.papel.imdb_clone.enums.Genre;
import com.papel.imdb_clone.exceptions.FileParsingException;
import com.papel.imdb_clone.model.people.Actor;
import com.papel.imdb_clone.model.people.Director;
import com.papel.imdb_clone.service.people.CelebrityManager;
import com.papel.imdb_clone.model.content.Movie;
import com.papel.imdb_clone.repository.impl.InMemoryMovieRepository;
import com.papel.imdb_clone.service.people.CelebrityService;
import com.papel.imdb_clone.service.content.MoviesService;
import com.papel.imdb_clone.service.data.base.BaseDataLoader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.sql.Date;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Optional;

/**
 * Loads movie data from files.
 */
public class MovieDataLoader extends BaseDataLoader {
    private static final Logger logger = LoggerFactory.getLogger(MovieDataLoader.class);
    private final MoviesService movieService;
    private final CelebrityService<Actor> actorService;
    private final CelebrityService<Director> directorService;
    private char gender;
    private String ethnicity;
    private LocalDate birthDate;
    private Ethnicity ethnicityEnum;
    private Genre genre;
    private int duration;
    private String director;
    private double rating;
    private List<String> actors;

    /**
     * Constructor for MovieDataLoader.
     * @param movieService the movie service to use
     * @param actorService the actor service to use
     * @param directorService the director service to use
     */
    public MovieDataLoader(
            MoviesService movieService,
            CelebrityService<Actor> actorService,
            CelebrityService<Director> directorService) {
        if (movieService == null) {
            throw new IllegalArgumentException("movieService cannot be null");
        }
        if (actorService == null) {
            throw new IllegalArgumentException("actorService cannot be null");
        }
        if (directorService == null) {
            throw new IllegalArgumentException("directorService cannot be null");
        }
        this.movieService = movieService;
        this.actorService = actorService;
        this.directorService = directorService;
    }


    /**
     * Normalizes text by fixing common encoding issues and trimming whitespace.
     *
     * @param text the text to normalize
     * @return the normalized text
     */
    private String normalizeText(String text) {
        if (text == null) {
            return "";
        }

        // Fix common encoding issues
        return text.trim()
                .replace("Ac", "é")
                .replace("Aç", "é")
                .replace("Ace", "ée")  // Fix TimothAce -> Timothée
                .replace("Ac", "é");     // Fix ChloAc -> Chloé
    }

    /**
     * load filename
     * @param filename the name of the file to load
     * @throws IOException if the file cannot be loaded
     */
    public void load(String filename) throws IOException {
        long startTime = System.currentTimeMillis();
        logger.info("Starting to load movies from: {}", filename);
        //initialize variables
        int count = 0;
        int errors = 0;
        int duplicates = 0;
        int lineNumber = 0;
        logger.debug("Initializing movie data loading process");

        try (InputStream inputStream = getResourceAsStream(filename);
             BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream))) {

            validateInput(inputStream, filename);
            String line;

            /*
             * read file line by line and parse it
             */
            while ((line = reader.readLine()) != null) {
                lineNumber++;
                if (line.trim().isEmpty() || line.trim().startsWith("#")) {
                    continue;
                }

                try {
                    String[] parts = parseCSVLine(line);

                    if (parts.length >= 7) {
                        // Expected format: Title,Year,Genre,Duration,Director,Rating,Actors
                        String title = normalizeText(parts[0]);

                        // Parse year and create release date (using first day of year if only year is provided)
                        int year = 0;
                        Date releaseDate = null;
                        try {
                            String yearStr = parts[1].trim();
                            // First validate the year format and range
                            if (yearStr.matches("^\\d{4}$")) {  // Check if it's exactly 4 digits
                                year = Integer.parseInt(yearStr);
                                // Validate year is within reasonable range (1888 is when first movie was made)
                                int currentYear = Calendar.getInstance().get(Calendar.YEAR);
                                if (year < 1888 || year > currentYear + 2) {
                                    logger.warn("Year {} is out of range (1888-{}) for movie '{}' at line {}. Using current year as fallback.",
                                            year, currentYear + 2, title, lineNumber);
                                    year = currentYear;
                                }
                            } else {
                                throw new NumberFormatException("Invalid year format: " + yearStr);
                            }
                            // create release date
                            Calendar cal = Calendar.getInstance();
                            cal.set(Calendar.YEAR, year);
                            cal.set(Calendar.MONTH, Calendar.JANUARY);
                            cal.set(Calendar.DAY_OF_MONTH, 1);
                            releaseDate = new Date(cal.getTimeInMillis());
                        } catch (NumberFormatException e) {
                            int currentYear = Calendar.getInstance().get(Calendar.YEAR);
                            logger.warn("Invalid year format '{}' for movie '{}' at line {}. Using current year ({}). Error: {}",
                                parts[1].trim(), title, lineNumber, currentYear, e.getMessage());
                            // Set to current year as fallback
                            year = currentYear;
                            Calendar cal = Calendar.getInstance();
                            // set release date to first day of current year
                            cal.set(Calendar.YEAR, year);
                            cal.set(Calendar.MONTH, Calendar.JANUARY);
                            cal.set(Calendar.DAY_OF_MONTH, 1);
                            releaseDate = new Date(cal.getTimeInMillis());
                        }

                        // Parse duration (in minutes)
                        int duration = 0;
                        try {
                            String durationStr = parts[3].trim();
                            if (!durationStr.isEmpty() && !durationStr.equalsIgnoreCase("N/A")) {
                                duration = Integer.parseInt(durationStr);
                                // Validate duration is reasonable (1 minute to 4 hours)
                                if (duration <= 0 || duration > 240) {
                                    logger.warn("Invalid duration '{}' minutes for movie '{}' at line {}. Using default 90 minutes.", 
                                            duration, title, lineNumber);
                                    duration = 90; // Default to 90 minutes if invalid
                                }
                            } else {
                                duration = 90; // Default duration if not specified
                                logger.debug("No duration specified for movie '{}' at line {}. Using default 90 minutes.", 
                                        title, lineNumber);
                            }
                        } catch (NumberFormatException e) {
                            duration = 90; // Default duration if parsing fails
                            logger.warn("Invalid duration format '{}' for movie '{}' at line {}. Using default 90 minutes.", 
                                    parts[3].trim(), title, lineNumber);
                        }

                        // Parse genres (handle both comma and semicolon separated values)
                        String genreField = parts[2].trim();
                        if (genreField.isEmpty() || genreField.equalsIgnoreCase("n/a")) {
                            logger.warn("No genre specified for movie '{}' at line {}. This movie will be skipped.", title, lineNumber);
                            errors++;
                            continue; // Skip this movie if no genre is specified
                        }
                        // split genre field by comma or semicolon
                        String[] genreNames = genreField.split("[,;]");


                        // Parse director name (handle multiple directors, potential quotes, and trim)
                        String directorName = normalizeText(parts[4]).replaceAll("^\"|\"$", "");
                        // Take only the first director if multiple are listed
                        if (directorName.contains(";")) {
                            directorName = directorName.split(";")[0].trim();
                            logger.debug("Multiple directors found, using first one: {}", directorName);
                        }

                        // Parse rating
                        double rating = 0.0;
                        try {
                            rating = Double.parseDouble(parts[5].trim());
                            // Ensure rating is between 0 and 10
                            rating = Math.max(0.0, Math.min(10.0, rating));
                            rating = Math.round(rating * 10.0) / 10.0;
                            logger.debug("Rating for movie '{}' at line {} is {}", title, lineNumber, rating);
                        } catch (NumberFormatException e) {
                            logger.warn("Invalid rating format '{}' for movie '{}' at line {}", parts[5].trim(), title, lineNumber);
                            rating = 0.0; // Default to 0.0 if rating is invalid
                        }

                        // Parse actors (semicolon separated) and normalize names
                        String[] actorNames = normalizeText(parts[6]).split(";");

                        // Create and save the movie
                        try {
                            // Create or find director using factory method
                            String[] directorNameParts = directorName.trim().split("\\s+", 2);
                            String firstName = directorNameParts[0];
                            String lastName = directorNameParts.length > 1 ? directorNameParts[1] : "";

                            // Use Director factory method to get or create instance
                            Director director = Director.getInstance(
                                firstName,
                                lastName,
                                null, // birth date unknown
                                '?',  // gender unknown
                                Ethnicity.UNKNOWN
                            );

                            // Ensure director is saved in the service
                            if (director.getId() == 0) { // Assuming 0 means not saved yet
                                director = directorService.save(director);
                            }

                            // Create new movie with required fields
                            Movie movie = new Movie();
                            movie.setTitle(title);
                            movie.setReleaseDate(releaseDate);
                            movie.setStartYear(year);
                            movie.setDirector(directorName); // This will be updated to use Director object in the future
                            movie.setRating(rating);
                            movie.setDuration(duration); // Set the parsed duration

                            // Set the director object if available
                            if (director != null) {
                                movie.setDirector(director.getFullName());
                            }

                            // Save movie to get an ID
                            movie = movieService.save(movie);

                            // Process actors for the movie
                            for (String actorName : actorNames) {
                                if (actorName == null || actorName.trim().isEmpty()) {
                                    continue;
                                }
                                
                                try {
                                    actorName = actorName.trim();
                                    
                                    // Split name into first and last name
                                    String[] nameParts = actorName.split("\\s+", 2);
                                    String actorFirstName = nameParts[0];
                                    String actorLastName = nameParts.length > 1 ? nameParts[1] : "";
                                    
                                    if (actorFirstName.isEmpty()) {
                                        logger.warn("Empty actor first name at line {}: {}", lineNumber, actorName);
                                        continue;
                                    }

                                    try {
                                        Actor actor = null;
                                        String fullName = actorFirstName + (actorLastName.isEmpty() ? "" : " " + actorLastName);
                                        
                                        try {
                                            // First try to find existing actor by name
                                            List<?> foundActors = actorService.findByName(fullName);
                                            
                                            if (foundActors != null && !foundActors.isEmpty()) {
                                                // Use existing actor
                                                actor = (Actor) foundActors.get(0);
                                                logger.debug("Found existing actor: {}", fullName);
                                            } else {
                                                // Create new actor instance if not found
                                                logger.debug("Creating new actor: {}", fullName);
                                                actor = Actor.getInstance(
                                                    actorFirstName,
                                                    actorLastName,
                                                    null, // birth date unknown
                                                    '?',  // gender unknown
                                                    Ethnicity.UNKNOWN
                                                );
                                                
                                                try {
                                                    actor = actorService.save(actor);
                                                    logger.debug("Successfully created new actor: {}", fullName);
                                                } catch (Exception e) {
                                                    logger.error("Failed to save actor '{}': {}", fullName, e.getMessage(), e);
                                                    continue; // Skip to next actor
                                                }
                                            }
                                        } catch (Exception e) {
                                            logger.error("Error finding actor '{}' in database: {}", fullName, e.getMessage(), e);
                                            continue; // Skip to next actor
                                        }

                                        // Add actor to movie
                                        if (!movie.getActors().contains(actor)) {
                                            movie.addActor(actor);
                                            logger.debug("Added actor {} to movie {}", 
                                                actor.getFullName(), movie.getTitle());
                                        }
                                    } catch (Exception e) {
                                        logger.error("Error processing actor '{}' for movie '{}': {}", 
                                            actorName, movie != null ? movie.getTitle() : "[unknown]", 
                                            e.getMessage(), e);
                                        // Continue with next actor
                                    }
                                    
                                } catch (Exception e) {
                                    logger.warn("Error processing actor '{}' at line {}: {}", 
                                        actorName, lineNumber, e.getMessage());
                                    // Continue with next actor
                                }
                            }

                            // Set genres with improved handling
                            boolean hasValidGenre = false;
                            for (String genreName : genreNames) {
                                try {
                                    if (genreName != null && !genreName.trim().isEmpty()) {
                                        // Normalize genre name: trim, uppercase, and replace special characters
                                        String normalizedGenre = getString(genreName);

                                        try {
                                            Genre genre = Genre.valueOf(normalizedGenre);
                                            movie.addGenre(genre);
                                            hasValidGenre = true;
                                        } catch (IllegalArgumentException e) {
                                            logger.debug("Unknown genre '{}' at line {} (tried as '{}')",
                                                    genreName, lineNumber, normalizedGenre);
                                        }
                                    }
                                } catch (Exception e) {
                                    logger.warn("Error processing genre '{}' at line {}: {}",
                                            genreName, lineNumber, e.getMessage());
                                }
                                // if no valid genre is found, skip this movie
                                if (!hasValidGenre) {
                                    logger.warn("No valid genre found for movie '{}' at line {}", title, lineNumber);
                                    errors++;
                                }
                            }

                            // Set director with improved error handling
                            if (!directorName.trim().isEmpty()) {
                                try {
                                    // Split director name into first and last name (using unique variable name)
                                    String[] dirNameParts = directorName.trim().split("\\s+", 2);
                                    String directorFirstName = dirNameParts[0];
                                    String directorLastName = dirNameParts.length > 1 ? dirNameParts[1] : "";

                                    // Try to find existing director by name
                                    List<?> foundDirectors = directorService.findByName(directorName);

                                    if (!foundDirectors.isEmpty() && foundDirectors.get(0) instanceof Director) {
                                        @SuppressWarnings("unchecked")
                                        List<Director> directorList = (List<Director>) foundDirectors;
                                        movie.setDirector(String.valueOf(directorList.getLast()));
                                    } else {
                                        // Create new director if not found
                                        Director newDirector = Director.getInstance(
                                            directorFirstName,
                                            directorLastName,
                                            null, // birthdate unknown
                                            '?',  // gender unknown
                                            Ethnicity.UNKNOWN
                                        );
                                        newDirector.setFirstName(directorFirstName);
                                        if (!directorLastName.isEmpty()) {
                                            newDirector.setLastName(directorLastName);
                                        }
                                        newDirector = directorService.save(newDirector);
                                        movie.setDirector(String.valueOf(newDirector));
                                        logger.debug("Created new director: {} {}", directorFirstName, directorLastName);
                                    }
                                } catch (Exception e) {
                                    logger.warn("Error setting director '{}' at line {}: {}",
                                            directorName, lineNumber, e.getMessage());
                                    // Continue without director rather than failing the whole movie
                                }
                            }

                            // Save the movie with all actors
                            try {
                                movie = movieService.save(movie);
                                logger.debug("Successfully saved movie: {} with {} actors", 
                                    movie.getTitle(), 
                                    movie.getActors() != null ? movie.getActors().size() : 0);
                            } catch (Exception e) {
                                logger.error("Failed to save movie '{}': {}", movie.getTitle(), e.getMessage());
                                throw e; // Re-throw to be caught by the outer try-catch
                            }


                            try {
                                // save movie
                                InMemoryMovieRepository.addMovie(movie);
                                count++;
                                logger.debug("Successfully loaded movie: {} ({}), ID: {}",
                                        movie.getTitle(), movie.getReleaseYear(), movie.getId());
                            } catch (Exception e) {
                                logger.error("Failed to save movie '{}' at line {}: {}",
                                        title, lineNumber, e.getMessage(), e);
                                errors++;
                            }

                        } catch (Exception e) {
                            logger.error("Error creating movie at line {}: {}", lineNumber, e.getMessage(), e);
                            errors++;
                        }
                    } else {
                        logger.warn("Invalid movie data format at line {}: {}", lineNumber, line);
                        errors++;
                    }
                } catch (Exception e) {
                    logger.error("Error processing line {}: {}", lineNumber, e.getMessage());
                    errors++;
                }
            }

            // Log summary of the loading process
            long endTime = System.currentTimeMillis();
            long duration = (endTime - startTime) / 1000;

            if (errors > 0) {
                logger.warn("Completed loading movies with {} errors. Successfully loaded {} movies ({} duplicates, {} total lines) in {} seconds",
                    errors, count, duplicates, lineNumber, duration);
            } else {
                logger.info("Successfully loaded {} movies ({} duplicates, {} total lines) in {} seconds",
                    count, duplicates, lineNumber, duration);
            }

        } catch (IOException e) {
            // log error for file reading
            logger.error("Error reading movies file: {}", e.getMessage(), e);
            throw new FileParsingException("Error reading movies file: " + e.getMessage());
        } finally {
            logger.debug("Movie data loading process completed");
        }
    }

    //get genre name from string
    private static String getString(String genreName) {
        String normalizedGenre = genreName.trim().toUpperCase()
                .replace("-", "_")
                .replace(" ", "_")
                .replace("&", "AND")
                .replace("/", "_")
                .replace("'", "");

        // Special case handling for common variations
        if (normalizedGenre.equals("SCIFI")) {
            normalizedGenre = "SCI_FI";
        } else if (normalizedGenre.equals("SCIFANTASY")) {
            normalizedGenre = "SCI_FI"; // Map to SCI_FI since SCIENCE_FANTASY doesn't exist
        } else if (normalizedGenre.matches("^DRAMA.*")) {
            normalizedGenre = "DRAMA";
        } else if (normalizedGenre.matches("^COMEDY.*")) {
            normalizedGenre = "COMEDY";
        } else if (normalizedGenre.equals("DOCUMENTARY")) {
            normalizedGenre = "DOCUMENTARY";
        }
        // log normalized genre
        logger.debug("Normalized genre: {}", normalizedGenre);
        return normalizedGenre;
    }
}
