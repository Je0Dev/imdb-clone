package com.papel.imdb_clone.service.data.loader.content;

import com.papel.imdb_clone.enums.Ethnicity;
import com.papel.imdb_clone.enums.Genre;
import com.papel.imdb_clone.exceptions.FileParsingException;
import com.papel.imdb_clone.model.content.Episode;
import com.papel.imdb_clone.model.content.Season;
import com.papel.imdb_clone.model.content.Series;
import com.papel.imdb_clone.model.people.Actor;
import com.papel.imdb_clone.model.people.Director;
import com.papel.imdb_clone.service.people.CelebrityService;
import com.papel.imdb_clone.service.content.SeriesService;
import com.papel.imdb_clone.service.data.base.BaseDataLoader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.time.LocalDate;
import java.util.*;
import java.util.Random;
import java.util.Calendar;

/**
 * Loads series data from files.
 */
public class SeriesDataLoader extends BaseDataLoader {

    private static final Logger logger = LoggerFactory.getLogger(SeriesDataLoader.class);
    private final SeriesService seriesService;
    private final CelebrityService<Actor> actorService;
    private int seasonNumber;
    // Default values for celebrity fields to prevent NullPointerException
    private final LocalDate birthDate = LocalDate.now().minusYears(30); // Default to 30 years ago
    private final Ethnicity ethnicity = Ethnicity.CAUCASOID; // Default ethnicity
    private String lastName;
    private String firstName;
    private String creatorName;
    private Actor actor;
    private Series series; // Will be initialized with proper parameters

    public SeriesDataLoader(
            SeriesService seriesService,
            CelebrityService<Actor> actorService,
            CelebrityService<Director> directorService) {
        this.seriesService = seriesService;
        this.actorService = actorService;
    }

    /**
     * Loads series from the specified file.
     *
     * @param filename the name of the file to load
     * @return the number of series loaded
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
                    // Parse CSV line using a more reliable CSV parsing approach
                    List<String> partsList = getStrings(line);

                    String[] parts = partsList.toArray(new String[0]);
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

                        // Create the Series with just the title first
                        // We'll set other values after parsing them
                        this.series = new Series(title);
                        this.series.setGenres(new ArrayList<>()); // Initialize empty genres list
                        
                        int startYear = 0;
                        int currentYear = Calendar.getInstance().get(Calendar.YEAR);

                        // Parse and validate years
                        try {
                            // Parse start year (index 3)
                            String startYearStr = parts[3].trim();
                            logger.debug("Parsing start year from '{}' for series: {}", startYearStr, title);
                            startYear = Integer.parseInt(startYearStr);
                            logger.debug("Parsed start year: {}", startYear);
                            
                            // Validate start year
                            if (startYear < 1928 || startYear > currentYear + 1) {
                                logger.warn("Start year {} for series '{}' is out of range. Using current year.", startYear, title);
                                startYear = currentYear;
                            }
                            this.series.setStartYear(startYear);
                            
                            // Parse end year (index 4) - handle dash or empty string
                            String endYearStr = parts[4].trim();
                            logger.debug("Processing end year for '{}': '{}' (raw: '{}')", title, endYearStr, parts[4]);

                            if (endYearStr.isEmpty() || endYearStr.equals("-") || endYearStr.equalsIgnoreCase("N/A")) {
                                logger.debug("No end year provided for series: {}. Marking as ongoing (0).", title);
                                this.series.setEndYear(0);
                            } else {
                                try {
                                    int endYear = Integer.parseInt(endYearStr);
                                    logger.debug("Parsed end year for '{}': {}", title, endYear);

                                    if (endYear >= startYear) {
                                        logger.debug("Setting end year to {} for series: {}", endYear, title);
                                        this.series.setEndYear(endYear);
                                        logger.info("Set end year to {} for series: {}", endYear, title);
                                    } else {
                                        logger.warn("End year {} is before start year {} for series '{}'. Marking as ongoing (0).",
                                            endYear, startYear, title);
                                        this.series.setEndYear(0);
                                    }
                                } catch (NumberFormatException e) {
                                    logger.warn("Invalid end year format '{}' for series '{}'. Marking as ongoing (0).",
                                        endYearStr, title);
                                    this.series.setEndYear(0);
                                }
                            }
                        } catch (NumberFormatException | ArrayIndexOutOfBoundsException e) {
                            logger.warn("Invalid year format '{}' at line {}. Using current year.", 
                                parts.length > 3 ? parts[3].trim() : "", lineNumber);
                            startYear = currentYear;
                            this.series.setStartYear(startYear);
                        }

                        // Parse rating
                        double rating = 0.0;
                        int ratingIndex = 5; // Rating is at index 5 (after title, genre, seasons, startYear, endYear)
                        try {
                            if (parts.length > ratingIndex) {
                                String ratingStr = parts[ratingIndex].trim();
                                if (!ratingStr.isEmpty() && !ratingStr.equalsIgnoreCase("N/A")) {
                                    try {
                                        rating = Double.parseDouble(ratingStr);
                                        // Ensure rating is between 0 and 10
                                        rating = Math.max(0, Math.min(10.0, rating));
                                        rating = Math.round(rating * 10.0) / 10.0;
                                        logger.debug("Rating for series '{}' at line {} is {}", title, lineNumber, rating);
                                    } catch (NumberFormatException e) {
                                        logger.warn("Invalid rating format '{}' at line {}. Using default value 0.0", 
                                            ratingStr, lineNumber);
                                    }
                                } else {
                                    logger.warn("Missing rating at line {}. Using default value 0.0", lineNumber);
                                }
                            } else {
                                logger.warn("Rating field missing at line {}. Using default value 0.0", lineNumber);
                            }
                        } catch (Exception e) {
                            logger.warn("Error parsing rating at line {}: {}", lineNumber, e.getMessage());
                        }

                        // Parse director(s) - now at index 6
                        String directorName = "";
                        try {
                            directorName = parts.length > 6 ? parts[6].trim() : "";
                            creatorName = directorName; // Set creatorName from director
                        } catch (ArrayIndexOutOfBoundsException e) {
                            logger.warn("Missing director at line {}. Using empty string: {}", lineNumber, line);
                        }

                        String[] genreArray = parts[1].trim().split(",");
                        Set<Genre> genres = new HashSet<>();

                        for (String genreItem : genreArray) {
                            try {
                                // Normalize the genre string
                                String normalizedGenre = getString(genreItem);

                                // Only add if it's a valid genre
                                try {
                                    // log normalized genre
                                    logger.debug("Normalized genre: {}", normalizedGenre);
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
                        int actorsIndex = 7; // Changed from 6 to 7 to skip the director field and get the actors-main cast column on ui
                        String[] actorNames = new String[0];
                        try {
                            actorNames = parts[actorsIndex].trim().split(";");
                            // log actor names
                            logger.debug("Actor names: {}", (Object) actorNames);
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

                        try {
                            // Set start year (which will also set the release date)
                            series.setStartYear(startYear);
                            
                            // If end year is not set, it will remain null (indicating ongoing series)
                            logger.debug("Set series years - start: {}, end: {}", 
                                    series.getStartYear(), series.getEndYear());
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
                            //get director name from creator name
                            String directorFullName = getString();

                            // Set director name as a string
                            series.setDirector(directorFullName);
                            logger.debug("Set director '{}' for series '{}'", directorFullName, title);
                        }

                        // Ensure all series has seasons list
                        series.setSeasons(0); // Initialize with 0 seasons, they'll be added later

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
                                    Episode episode = new Episode();
                                    episode.setTitle("Episode " + j);
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

                                        // Create or get actor using factory method
                                        // Default to Unknown
                                        char gender = 'U';
                                        Actor actor = Actor.getInstance(
                                            firstName,
                                            lastName,
                                            birthDate,
                                            gender,
                                            ethnicity
                                        );
                                        
                                        // Ensure actor is saved in the service
                                        if (actor.getId() == 0) { // Assuming 0 means not saved yet
                                            actor = actorService.save(actor);
                                        }

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
                                            String updatedWorks = getString(currentWorks, series);

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
                            Series savedSeries = seriesService.save(series);
                            if (savedSeries != null) {
                                count++;
                                logger.debug("Successfully loaded series: {} ({} - {})", 
                                    title, startYear, series.getEndYear() != 0 ? series.getEndYear() : "Present");
                            } else {
                                logger.warn("Failed to save series: {} (duplicate or invalid data)", title);
                                duplicates++;
                            }
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

            // Calculate duration
            long endTime = System.currentTimeMillis();
            long duration = (endTime - startTime) / 1000;
            
            if (errors > 0) {
                logger.warn("Completed loading series with {} errors. Successfully loaded {} series ({} duplicates, {} total lines) in {} seconds", 
                    errors, count, duplicates, lineNumber, duration);
                throw new FileParsingException("Encountered " + errors + " errors while loading series data");
            } else {
                logger.info("Series data loading completed in {} ms. Successfully loaded {} series ({} duplicates, {} errors, {} lines processed)",
                    (endTime - startTime), count, duplicates, errors, lineNumber);
            }

            // Return the count of successfully loaded series
            return count;
        } catch (IOException e) {
            logger.error("Error reading series file: {}", e.getMessage(), e);
            throw new FileParsingException("Error reading file: " + filename + ": " + e.getMessage());
        } finally {
            logger.debug("Series data loading process completed");
        }}

    private static List<String> getStrings(String line) {
        List<String> partsList = new ArrayList<>();
        boolean inQuotes = false;
        StringBuilder current = new StringBuilder();

        for (int i = 0; i < line.length(); i++) {
            char c = line.charAt(i);
            if (c == '"') {
                inQuotes = !inQuotes;
            } else if (c == ',' && !inQuotes) {
                partsList.add(current.toString().trim());
                current = new StringBuilder();
            } else {
                current.append(c);
            }
        }
        partsList.add(current.toString().trim()); // Add the last field
        return partsList;
    }

    private String getString() {
        String[] creatorNameParts = creatorName.trim().split("\\s+", 2);
        String directorFullName;

        if (creatorNameParts.length >= 2) {
            String creatorFirstName = creatorNameParts[0].trim();
            String creatorLastName = creatorNameParts[1].trim();
            directorFullName = creatorFirstName + (creatorLastName.isEmpty() ? "" : " " + creatorLastName);
        } else {
            directorFullName = creatorName.trim();
        }
        return directorFullName;
    }

    // Helper method to normalize genre names
    private static String getString(String genreItem) {
        String normalizedGenre = genreItem.trim().toUpperCase()
                .replace("-", "_")
                .replace(" ", "_")
                .replace("&", "AND")
                .replace("/", "_")
                .replace("'", "")
                .replace("SCI-FI", "SCI_FI"); // Handle hyphenated SCI-FI

        // Handle special cases and normalize genre names
        // Map war to history
        // Add more mappings as needed
        normalizedGenre = switch (normalizedGenre) {
            case "SCIFI" -> "SCI_FI";
            case "SCIFANTASY" -> "SCI_FI"; // Map to SCI_FI since SCIENCE_FANTASY doesn't exist
            case "ACTION", "ADVENTURE" -> "ACTION"; // Both map to ACTION
            case "ROMANCE", "MUSICAL" -> "ROMANCE"; // Map musicals to romance
            case "WAR", "HISTORY" -> "HISTORY";
            default -> normalizedGenre;
        };
        return normalizedGenre;
    }

    // Helper method to update works string
    private static String getString(String currentWorks, Series series) {
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
        // log updated works
        logger.debug("Updated works: {}", updatedWorks);
        return updatedWorks;
    }
}
