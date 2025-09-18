package com.papel.imdb_clone.service.data.loader;

import com.papel.imdb_clone.enums.Ethnicity;
import com.papel.imdb_clone.enums.Genre;
import com.papel.imdb_clone.exceptions.FileParsingException;
import com.papel.imdb_clone.model.*;
import com.papel.imdb_clone.service.CelebrityService;
import com.papel.imdb_clone.service.SeriesService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Method;
import java.time.LocalDate;
import java.util.*;
import java.util.Random;
import java.util.Calendar;
import java.util.stream.Collectors;

/**
 * Loads series data from files.
 */
public class SeriesDataLoader extends BaseDataLoader {
    private static final Logger logger = LoggerFactory.getLogger(SeriesDataLoader.class);
    private final SeriesService seriesService;
    private final CelebrityService<Actor> actorService;
    private final CelebrityService<Director> directorService;
    private int seasonNumber;
    // Default values for celebrity fields to prevent NullPointerException
    private final LocalDate birthDate = LocalDate.now().minusYears(30); // Default to 30 years ago
    private final char gender = 'U'; // Default to Unknown
    private final Ethnicity ethnicity = Ethnicity.CAUCASOID; // Default ethnicity
    private String lastName;
    private String firstName;
    private String creatorName;
    private Actor actor;

    public SeriesDataLoader(
            SeriesService seriesService,
            CelebrityService<Actor> actorService,
            CelebrityService<Director> directorService) {
        this.seriesService = seriesService;
        this.actorService = actorService;
        this.directorService = directorService;
    }

    /**
     * Loads series from the specified file.
     *
     * @param filename the name of the file to load
     * @return
     * @throws IOException if there is an error reading the file
     */
    public int load(String filename) throws IOException {
        long startTime = System.currentTimeMillis();
        logger.info("Starting to load series from: {}", filename);
        int count = 0;
        int errors = 0;
        int duplicates = 0;
        int lineNumber = 0;
        logger.debug("Initializing series data loading process");

        try (InputStream inputStream = getResourceAsStream(filename);
             BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream))) {

            validateInput(inputStream, filename);
            String line;

            while ((line = reader.readLine()) != null) {
                lineNumber++;
                if (line.trim().isEmpty() || line.trim().startsWith("#")) {
                    continue;
                }

                try {
                    // Parse CSV line
                    String[] parts = parseCSVLine(line);
                    if (parts.length >= 7) {

                        String title = parts[0].trim();
                        // Handle missing or invalid genre
                        String genreStr = parts[1].trim();
                        if (genreStr.isEmpty() || genreStr.equalsIgnoreCase("n/a")) {
                            genreStr = "DRAMA"; // Default to DRAMA if no genre specified
                            logger.warn("No genre specified for series '{}' at line {}. Defaulting to 'DRAMA'.", title, lineNumber);
                        }

                        int seasonsCount = 1;
                        try {
                            // Parse seasons count
                            seasonsCount = Integer.parseInt(parts[2].trim());
                            if (seasonsCount < 1) {
                                logger.warn("Invalid seasons count {} at line {}. Using default value 1.", seasonsCount, lineNumber);
                                seasonsCount = 1;
                            }
                        } catch (NumberFormatException e) {
                            logger.warn("Invalid seasons count '{}' at line {}. Using default value 1.", parts[2].trim(), lineNumber);
                        }

                        int startYear = 0;
                        int currentYear = Calendar.getInstance().get(Calendar.YEAR);

                        // Parse the year (now just a single year instead of a range)
                        String yearStr = parts[3].trim();
                        try {
                            startYear = Integer.parseInt(yearStr);
                            // Validate start year (1928 is when first TV broadcast happened)
                            if (startYear < 1928 || startYear > currentYear + 2) {
                                logger.warn("Start year {} for series '{}' is out of range (1928-{}). Using current year as fallback.",
                                        startYear, title, currentYear + 2);
                                startYear = currentYear;
                            }
                            logger.debug("Parsed startYear: {} for series: {}", startYear, title);
                        } catch (NumberFormatException e) {
                            logger.warn("Invalid year format '{}' at line {}. Using current year.", yearStr, lineNumber);
                            startYear = currentYear;
                        }

                        // Parse rating (now at index 4 since we removed end year)
                        double rating = 0.0;
                        try {
                            String ratingStr = parts[4].trim();
                            if (!ratingStr.isEmpty()) {
                                rating = Double.parseDouble(ratingStr);
                                // Ensure rating is between 0 and 10
                                rating = Math.max(0, Math.min(10.0, rating));
                            } else {
                                logger.warn("Missing rating at line {}. Using default value 0.0: {}", lineNumber, line);
                            }
                        } catch (NumberFormatException | ArrayIndexOutOfBoundsException e) {
                            logger.warn("Invalid rating format at line {}. Using default value 0.0: {}", lineNumber, line);
                        }

                        // Parse director(s) - now at index 5
                        String directorName = "";
                        try {
                            directorName = parts[5].trim();
                            creatorName = directorName; // Set creatorName from director
                        } catch (ArrayIndexOutOfBoundsException e) {
                            logger.warn("Missing director at line {}. Using empty string: {}", lineNumber, line);
                        }

                        String[] genreArray = parts[1].trim().split(",");
                        Set<Genre> genres = new HashSet<>();

                        for (String genreItem : genreArray) {
                            try {
                                // Normalize the genre string
                                String normalizedGenre = genreItem.trim().toUpperCase()
                                        .replace("-", "_")
                                        .replace(" ", "_")
                                        .replace("&", "AND")
                                        .replace("/", "_")
                                        .replace("'", "")
                                        .replace("SCI-FI", "SCI_FI"); // Handle hyphenated SCI-FI

                                // Handle special cases and normalize genre names
                                switch (normalizedGenre) {
                                    case "SCIFI":
                                        normalizedGenre = "SCI_FI";
                                        break;
                                    case "SCIFANTASY":
                                        normalizedGenre = "SCI_FI"; // Map to SCI_FI since SCIENCE_FANTASY doesn't exist
                                        break;
                                    case "ACTION":
                                    case "ADVENTURE":
                                        normalizedGenre = "ACTION"; // Both map to ACTION
                                        break;
                                    case "ROMANCE":
                                    case "MUSICAL":
                                        normalizedGenre = "ROMANCE"; // Map musicals to romance
                                        break;
                                    case "WAR":
                                    case "HISTORY":
                                        normalizedGenre = "HISTORY"; // Map war to history
                                        break;
                                    // Add more mappings as needed
                                }

                                // Only add if it's a valid genre
                                try {
                                    Genre genre = Genre.valueOf(normalizedGenre);
                                    genres.add(genre);
                                } catch (IllegalArgumentException e) {
                                    logger.debug("Unknown genre '{}' for series '{}' at line {}", genreItem, title, lineNumber);
                                }
                            } catch (Exception e) {
                                logger.warn("Error processing genre '{}' for series '{}' at line {}: {}", genreItem, title, lineNumber, e.getMessage());
                            }
                        }

                        // Parse actors (semicolon separated) - adjust index based on the format
                        int actorsIndex = 6;
                        String[] actorNames = new String[0];
                        try {
                            actorNames = parts[actorsIndex].trim().split(";");
                        } catch (ArrayIndexOutOfBoundsException e) {
                            logger.warn("Missing actors at line {}. Using empty array: {}", lineNumber, line);
                        }

                        // Check if series already exists by title and year
                        if (seriesService.findByTitleAndYear(title, startYear).isPresent()) {
                            logger.debug("Series '{}' from {} already exists", title, startYear);
                            duplicates++;
                            continue;
                        }

                        // Create the series
                        Series series = new Series(title);

                        // When creating the series, we only set the start year
                        try {
                            Calendar cal = Calendar.getInstance();
                            cal.set(Calendar.YEAR, startYear);
                            cal.set(Calendar.MONTH, 0); // January
                            cal.set(Calendar.DAY_OF_MONTH, 1);

                            // Set the release date (which will also set startYear)
                            series.setReleaseDate(cal.getTime());
                            logger.debug("Set release date to {}-01-01 for series: {}", startYear, title);

                            // Explicitly set startYear as well to ensure consistency
                            series.setStartYear(startYear);
                            logger.debug("Set startYear to {} for series: {}", startYear, title);
                        } catch (Exception e) {
                            logger.error("Error setting release date for series: {}", title, e);
                            // Fall back to just setting the year if date creation fails
                            series.setStartYear(startYear);
                        }

                        series.setRating(rating);
                        logger.debug("Final series object: {}", series);

                        // Set genres - this will also set the primary genre to the first one
                        if (!genres.isEmpty()) {
                            series.setGenres(new ArrayList<>(genres));
                            // Log the created series for debugging
                            logger.debug("Created series: {} (startYear: {})",
                                    series.getTitle(),
                                    series.getStartYear());
                        } else {
                            logger.warn("No valid genres found for series: {}", title);
                        }

                        // Add creator as director if not empty
                        if (!creatorName.trim().isEmpty()) {
                            String[] creatorNameParts = creatorName.trim().split("\\s+", 2);
                            if (creatorNameParts.length >= 2) {
                                String creatorFirstName = creatorNameParts[0].trim();
                                String creatorLastName = creatorNameParts[1].trim();

                                // Set director name as a string
                                String directorFullName = creatorFirstName + (creatorLastName.isEmpty() ? "" : " " + creatorLastName);
                                series.setDirector(directorFullName);
                            } else {
                                // Split the creator name into first and last name
                                String[] nameParts = creatorName.trim().split("\\s+", 2);
                                String firstName = nameParts[0];
                                String lastName = nameParts.length > 1 ? nameParts[1] : "";

                                // Set director name as a string
                                String directorFullName = firstName + (lastName.isEmpty() ? "" : " " + lastName);
                                series.setDirector(directorFullName);
                            }
                        }

                        // Ensure all series has seasons list
                        series.setSeasons(new ArrayList<>());

                        // Add seasons
                        int minEpisodesPerSeason = 8;  // Minimum episodes per season
                        int maxEpisodesPerSeason = 16; // Maximum episodes per season

                        // Create a list to store all actors for the series
                        List<Actor> mainCast = new ArrayList<>();

                        for (int i = 1; i <= seasonsCount; i++) {
                            try {
                                Season season = new Season(i, series);
                                season.setSeasonNumber(i);
                                season.setTitle("Season " + i);

                                // Create a random number of episodes between min and max
                                Random random = new Random();
                                int episodesCount = random.nextInt(maxEpisodesPerSeason - minEpisodesPerSeason + 1) + minEpisodesPerSeason;

                                List<Episode> episodes = new ArrayList<>();
                                for (int j = 1; j <= episodesCount; j++) {
                                    Episode episode = new Episode("Episode " + j, 45);
                                    episode.setEpisodeNumber(j);

                                    // Set a random release date within the series year
                                    Calendar cal = Calendar.getInstance();
                                    cal.set(Calendar.YEAR, startYear);
                                    cal.set(Calendar.MONTH, random.nextInt(12));
                                    cal.set(Calendar.DAY_OF_MONTH, 1 + random.nextInt(28));
                                    episode.setReleaseDate(cal.getTime());

                                    // Initialize empty actors list for the episode
                                    episode.setActors(new ArrayList<>());
                                    episodes.add(episode);
                                }

                                // Set episodes to season using reflection
                                try {
                                    season.getClass().getMethod("setEpisodes", List.class).invoke(season, episodes);
                                } catch (Exception e) {
                                    logger.warn("Could not set episodes for season: {}", e.getMessage());
                                }

                                // Add season to series
                                series.getSeasons().add(season);

                                logger.debug("Added season {} with {} episodes to series '{}'",
                                        i, episodes.size(), title);
                            } catch (Exception e) {
                                logger.warn("Error creating season {} for series '{}' at line {}: {}",
                                        i, title, lineNumber, e.getMessage(), e);
                            }
                        }

                        // Process actors and add them to the main cast
                        if (actorNames != null && actorNames.length > 0) {
                            for (String actorName : actorNames) {
                                String trimmedName = actorName.trim();
                                if (!trimmedName.isEmpty()) {
                                    try {
                                        // Split name into first and last name
                                        String[] nameParts = trimmedName.split("\\s+", 2);
                                        String firstName = nameParts[0];
                                        String lastName = nameParts.length > 1 ? nameParts[1] : "";

                                        // Create or get actor
                                        Actor actor = new Actor(firstName, lastName, birthDate, gender, ethnicity);
                                        actor = actorService.save(actor);

                                        // Create final variables for use in lambda
                                        final String actorFirstName = actor.getFirstName();
                                        final String actorLastName = actor.getLastName();
                                        
                                        // Add actor to main cast if not already present
                                        if (mainCast.stream().noneMatch(a ->
                                                a.getFirstName().equals(actorFirstName) &&
                                                        a.getLastName().equals(actorLastName))) {
                                            mainCast.add(actor);
                                            logger.debug("Added actor {} {} to main cast of series '{}'",
                                                    actor.getFirstName(), actor.getLastName(), title);
                                        }

                                        // Add series to actor's notable works
                                        String currentWorks = actor.getNotableWorks() != null ?
                                                actor.getNotableWorks().toString() : "";

                                        if (!currentWorks.contains(series.getTitle())) {
                                            String updatedWorks = currentWorks.isEmpty() ?
                                                    series.getTitle() :
                                                    currentWorks + ", " + series.getTitle();

                                            // Clean up the works string
                                            updatedWorks = updatedWorks.replace(", ,", ",")
                                                    .replaceAll("\\s*,\\s*", ", ")
                                                    .replaceAll("^\\s*,\\s*|\\s*\\.\\s*$|\\.\\s*(?=,|$)", "")
                                                    .trim();

                                            if (updatedWorks.endsWith(",")) {
                                                updatedWorks = updatedWorks.substring(0, updatedWorks.length() - 1).trim();
                                            }

                                            actor.setNotableWorks(updatedWorks);
                                            actorService.save(actor);
                                        }
                                    } catch (Exception e) {
                                        logger.warn("Error processing actor '{}' for series '{}' at line {}: {}",
                                                actorName, title, lineNumber, e.getMessage(), e);
                                    }
                                }
                            }

                            // Set the main cast for the series
                            series.setActors(mainCast);
                            logger.info("Set main cast of {} actors for series '{}'", mainCast.size(), title);
                        }
                        // Save the series
                        try {
                            seriesService.save(series);
                            count++;
                            logger.debug("Successfully loaded series: {} ({})", title, startYear);
                        } catch (Exception e) {
                            logger.error("Error saving series '{}' at line {}: {}",
                                    title, lineNumber, e.getMessage(), e);
                            logger.debug("Series object that failed to save: {}", series);
                            errors++;
                        }
                    } else {
                        logger.warn("Invalid line format at line {}: {}", lineNumber, line);
                        errors++;
                    }
                } catch (Exception e) {
                    errors++;
                    logger.error("Unexpected error processing line {}: {}", lineNumber, line, e);
                }
            } // End of while loop

            long endTime = System.currentTimeMillis();
            long duration = (endTime - startTime) / 1000;
            
            if (errors > 0) {
                logger.warn("Completed loading series with {} errors. Successfully loaded {} series ({} duplicates, {} total lines) in {} seconds", 
                    errors, count, duplicates, lineNumber, duration);
                throw new FileParsingException("Encountered " + errors + " errors while loading series data");
            } else {
                logger.info("Successfully loaded {} series ({} duplicates, {} total lines) in {} seconds", 
                    count, duplicates, lineNumber, duration);
            }

            return count;
        } catch (IOException e) {
            logger.error("Error reading series file: {}", e.getMessage(), e);
            throw new FileParsingException("Error reading file: " + filename + ": " + e.getMessage());
        } finally {
            logger.debug("Series data loading process completed");
        }}
    }
