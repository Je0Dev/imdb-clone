package com.papel.imdb_clone.service.data.loader;

import com.papel.imdb_clone.enums.Ethnicity;
import com.papel.imdb_clone.enums.Genre;
import com.papel.imdb_clone.exceptions.FileParsingException;
import com.papel.imdb_clone.model.Actor;
import com.papel.imdb_clone.model.Director;
import com.papel.imdb_clone.model.Movie;
import com.papel.imdb_clone.repository.impl.InMemoryMovieRepository;
import com.papel.imdb_clone.service.CelebrityService;
import com.papel.imdb_clone.service.ContentService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.sql.Date;
import java.time.LocalDate;
import java.util.Calendar;
import java.util.List;
import java.util.Optional;

/**
 * Loads movie data from files.
 */
public class MovieDataLoader extends BaseDataLoader {
    private static final Logger logger = LoggerFactory.getLogger(MovieDataLoader.class);
    private final ContentService<Movie> movieService;
    private final CelebrityService<Actor> actorService;
    private final CelebrityService<Director> directorService;
    private char gender;
    private String ethnicity;
    private LocalDate birthDate;
    private Ethnicity Ethnicity;

    /**
     * Constructor for MovieDataLoader.
     * @param movieService
     * @param actorService
     * @param directorService
     */
    public MovieDataLoader(
            ContentService<Movie> movieService,
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
                .replace("Ace", "ée")  // Fix TimothAce -> Timothée
                .replace("Ac", "é");     // Fix ChloAc -> Chloé
    }

    /**
     * load filename
     * @param filename
     * @throws IOException
     */
    public void load(String filename) throws IOException {
        logger.info("Loading movies from {}", filename);
        int count = 0;
        int errors = 0;
        int duplicates = 0;
        int lineNumber = 0;

        try (InputStream inputStream = getResourceAsStream(filename);
             BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream))) {

            validateInput(inputStream, filename);
            String line;

            /**
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
                            year = Integer.parseInt(parts[1].trim());
                            // Validate year is within reasonable range (1888 is when first movie was made)
                            int currentYear = Calendar.getInstance().get(Calendar.YEAR);
                            if (year < 1888 || year > currentYear + 2) {
                                logger.warn("Year {} is out of range (1888-{}). Using current year as fallback.", year, currentYear + 2);
                                year = currentYear;
                            }
                            // create release date
                            Calendar cal = Calendar.getInstance();
                            cal.set(Calendar.YEAR, year);
                            cal.set(Calendar.MONTH, Calendar.JANUARY);
                            cal.set(Calendar.DAY_OF_MONTH, 1);
                            releaseDate = new Date(cal.getTimeInMillis());
                        } catch (Exception e) {
                            logger.warn("Invalid year format '{}' at line {}. Using current year as fallback. Error: {}", 
                                parts[1], lineNumber, e.getMessage());
                            // Set to current year as fallback
                            year = Calendar.getInstance().get(Calendar.YEAR);
                            Calendar cal = Calendar.getInstance();
                            // set release date to first day of current year
                            cal.set(Calendar.YEAR, year);
                            cal.set(Calendar.MONTH, Calendar.JANUARY);
                            cal.set(Calendar.DAY_OF_MONTH, 1);
                            releaseDate = new Date(cal.getTimeInMillis());
                        }

                        // Parse genres (handle both comma and semicolon separated values)
                        String genreField = parts[2].trim();
                        if (genreField.isEmpty() || genreField.equalsIgnoreCase("n/a")) {
                            genreField = "DRAMA"; // Default to DRAMA if no genre specified
                            logger.warn("No genre specified for movie '{}' at line {}. Defaulting to 'DRAMA'.", title, lineNumber);
                        }
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
                        } catch (NumberFormatException e) {
                            logger.warn("Invalid rating format '{}' at line {}", parts[5].trim(), lineNumber);
                            rating = 0.0; // Default to 0.0 if rating is invalid
                        }

                        // Parse actors (semicolon separated) and normalize names
                        String[] actorNames = normalizeText(parts[6]).split(";");

                        // Create and save the movie
                        try {
                            // Create new movie with required fields
                            Movie movie = new Movie();
                            movie.setTitle(title);
                            movie.setReleaseDate(releaseDate);
                            movie.setStartYear(year);  // Set the startYear field
                            movie.setRating(rating); // Set the rating using the setter that accepts double
                            // Save through service only (which will handle repository saving)
                            movie = movieService.save(movie);

                            // Set genres with improved handling
                            boolean hasValidGenre = false;
                            for (String genreName : genreNames) {
                                try {
                                    if (genreName != null && !genreName.trim().isEmpty()) {
                                        // Normalize genre name: trim, uppercase, and replace special characters
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
                            }
                            

                            // Set director with improved error handling
                            if (!directorName.trim().isEmpty()) {
                                try {
                                    // Split director name into first and last name
                                    String[] nameParts = directorName.trim().split("\\s+", 2);
                                    String firstName = nameParts[0];
                                    String lastName = nameParts.length > 1 ? nameParts[1] : "";

                                    // Try to find existing director by name
                                    List<Director> directorOpt = (List<Director>) directorService.findByName(directorName);

                                    if (!directorOpt.isEmpty()) {
                                        movie.setDirector(String.valueOf(directorOpt.getLast()));
                                    } else {
                                        // Create new director if not found
                                        Director newDirector = new Director(firstName, lastName, birthDate, gender,Ethnicity);
                                        newDirector.setFirstName(firstName);
                                        if (!lastName.isEmpty()) {
                                            newDirector.setLastName(lastName);
                                        }
                                        newDirector = directorService.save(newDirector);
                                        movie.setDirector(String.valueOf(newDirector));
                                        logger.debug("Created new director: {} {}", firstName, lastName);
                                    }
                                } catch (Exception e) {
                                    logger.warn("Error setting director '{}' at line {}: {}",
                                            directorName, lineNumber, e.getMessage());
                                    // Continue without director rather than failing the whole movie
                                }
                            }

                            // Set actors with improved error handling
                            for (String actorName : actorNames) {
                                if (actorName != null && !actorName.trim().isEmpty()) {
                                    try {
                                        // Split actor name into first and last name
                                        String[] nameParts = actorName.trim().split("\\s+", 2);
                                        String firstName = nameParts[0];
                                        String lastName = nameParts.length > 1 ? nameParts[1] : "";

                                        // Try to find existing actor by name
                                        Optional<Actor> actorOpt = actorService.findByFullName(firstName, lastName);

                                        if (actorOpt.isPresent()) {
                                            movie.addActor(actorOpt.get());
                                        } else {
                                            // Create new actor if not found
                                            Actor newActor = new Actor(firstName, lastName, birthDate, gender, ethnicity);
                                            newActor.setFirstName(firstName);
                                            if (!lastName.isEmpty()) {
                                                newActor.setLastName(lastName);
                                            }
                                            // save actor
                                            newActor = actorService.save(newActor);
                                            movie.addActor(newActor);
                                            logger.debug("Created new actor: {} {}", firstName, lastName);
                                        }
                                    } catch (Exception e) {
                                        logger.warn("Error processing actor '{}' at line {}: {}",
                                                actorName, lineNumber, e.getMessage());
                                        // Continue with next actor rather than failing
                                    }
                                }
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

            logger.info("Successfully loaded {} movies ({} duplicates, {} errors, {} total lines)",
                    count, duplicates, errors, lineNumber);

        } catch (IOException e) {
            logger.error("Error reading movies file: {}", e.getMessage(), e);
            throw new FileParsingException("Error reading movies file: " + e.getMessage());
        }
    }
}
