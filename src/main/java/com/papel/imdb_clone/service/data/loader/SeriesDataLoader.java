package com.papel.imdb_clone.service.data.loader;

import com.papel.imdb_clone.enums.Ethnicity;
import com.papel.imdb_clone.enums.Genre;
import com.papel.imdb_clone.exceptions.FileParsingException;
import com.papel.imdb_clone.model.*;
import com.papel.imdb_clone.service.CelebrityService;
import com.papel.imdb_clone.service.ContentService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashSet;
import java.util.Set;

/**
 * Loads series data from files.
 */
public class SeriesDataLoader extends BaseDataLoader {
    private static final Logger logger = LoggerFactory.getLogger(SeriesDataLoader.class);
    private final ContentService<Series> seriesService;
    private final CelebrityService<Actor> actorService;
    private final CelebrityService<Director> directorService;
    private int seasonNumber;
    private String firstName;
    private String lastName;
    private LocalDate birthDate;
    private char gender;
    private Ethnicity ethnicity;

    public SeriesDataLoader(
            ContentService<Series> seriesService,
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
        logger.info("Loading series from {}", filename);
        int count = 0;
        int errors = 0;
        int duplicates = 0;
        int lineNumber = 0;

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
                    String[] parts = parseCSVLine(line);
                    if (parts.length >= 8) {

                        String title = parts[0].trim();
                        String genreStr = parts[1].trim();

                        int seasonsCount = 1;
                        try {
                            seasonsCount = Integer.parseInt(parts[2].trim());
                        } catch (NumberFormatException e) {
                            logger.warn("Invalid seasons count at line {}: {}", lineNumber, parts[2].trim());
                        }

                        // Parse years (handle empty end year)
                        int startYear = 0;
                        Integer endYear = null;
                        try {
                            startYear = Integer.parseInt(parts[3].trim());
                            logger.debug("Parsed startYear: {} for series: {}", startYear, title);
                        } catch (NumberFormatException e) {
                            logger.warn("Invalid start year format for series: " + title, e);
                        }

                        if (!parts[4].trim().isEmpty()) {
                            try {
                                endYear = Integer.parseInt(parts[4].trim());
                                logger.debug("Parsed endYear: {} for series: {}", endYear, title);
                            } catch (NumberFormatException e) {
                                logger.warn("Invalid end year format for series: {}", title, e);
                            }
                        }

                        int rating = 0;
                        try {
                            rating = Integer.parseInt(parts[5].trim());
                        } catch (NumberFormatException | ArrayIndexOutOfBoundsException e) {
                            logger.warn("Invalid or missing rating at line {}: {}", lineNumber, line);
                            rating = 0; // Default rating if not provided or invalid
                        }

                        String creatorName = parts[6].trim();
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
                                        .replace("'", "");

                                // Handle special cases
                                if (normalizedGenre.equals("SCIFI")) {
                                    normalizedGenre = "SCI_FI";
                                } else if (normalizedGenre.equals("SCIFANTASY")) {
                                    normalizedGenre = "SCIENCE_FANTASY";
                                } else if (normalizedGenre.equals("ACTION")) {
                                    normalizedGenre = "ACTION_ADVENTURE";
                                } else if (normalizedGenre.equals("ADVENTURE")) {
                                    normalizedGenre = "ACTION_ADVENTURE";
                                }

                                // Only add if it's a valid genre
                                try {
                                    Genre genre = Genre.valueOf(normalizedGenre);
                                    genres.add(genre);
                                } catch (IllegalArgumentException e) {
                                    logger.warn("Unknown genre '{}' at line {}: {}", genreItem, lineNumber, line);
                                }
                            } catch (Exception e) {
                                logger.warn("Error processing genre '{}' at line {}: {}", genreItem, lineNumber, e.getMessage());
                            }
                        }

                        String[] actorNames = parts[7].split(";");
                        // Check if series already exists by title and year
                        if (seriesService.findByTitleAndYear(title, startYear).isPresent()) {
                            logger.debug("Series '{}' from {} already exists", title, startYear);
                            duplicates++;
                            continue;
                        }

                        // Create the series
                        Series series = new Series(title);

                        // Set the start year (this will also update the release date)
                        if (startYear > 0) {
                            try {
                                // Create a date from the start year (set to January 1st of that year)
                                Calendar cal = Calendar.getInstance();
                                cal.set(Calendar.YEAR, startYear);
                                cal.set(Calendar.MONTH, Calendar.JANUARY);
                                cal.set(Calendar.DAY_OF_MONTH, 1);
                                
                                // Set the release date first (which will also set startYear)
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
                        } else {
                            logger.warn("No valid start year found for series: " + title);
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

                                // Find or create director
                                Director creator = directorService.findByFullName(creatorFirstName, creatorLastName)
                                        .orElseGet(() -> {
                                            Director newDirector = new Director(
                                                    firstName,lastName,birthDate,gender,
                                                    ethnicity
                                            );
                                            return directorService.save(newDirector);
                                        });
                                series.setDirector(creator);
                            } else {
                                // Split the creator name into first and last name
                                String[] nameParts = creatorName.trim().split("\\s+", 2);
                                String firstName = nameParts[0];
                                String lastName = nameParts.length > 1 ? nameParts[1] : "";

                                Director creator = new Director(
                                        firstName,
                                        lastName,
                                        birthDate,
                                        gender,
                                        ethnicity
                                );
                                directorService.save(creator);
                                series.setDirector(creator);
                            }
                        }

                        // Ensure series has seasons list
                        if (series.getSeasons() == null) {
                            series.setSeasons(new ArrayList<>());
                        }

                        // Add seasons
                        for (int i = 1; i <= seasonsCount; i++) {
                            try {
                                Season season = new Season(i, series);
                                season.setSeasonNumber(i);
                                season.setTitle("Season " + i);
                                season.setEpisodes(new ArrayList<>());

                                // Add a default episode to each season
                                Episode episode = new Episode();
                                episode.setTitle("Episode 1");
                                episode.setEpisodeNumber(1);
                                episode.setReleaseDate(new java.util.Date());
                                season.getEpisodes().add(episode);

                                series.getSeasons().add(season);
                            } catch (Exception e) {
                                logger.warn("Error creating season {} for series '{}' at line {}: {}",
                                        i, title, lineNumber, e.getMessage());
                            }
                        }

                        // Add actors
                        for (String actorName : actorNames) {
                            if (!actorName.trim().isEmpty()) {
                                try {
                                    String[] nameParts = actorName.trim().split("\\s+", 2);
                                    String firstName, lastName;

                                    if (nameParts.length >= 2) {
                                        firstName = nameParts[0].trim();
                                        lastName = nameParts[1].trim();
                                    } else {
                                        // If only one name is provided, use it as last name
                                        firstName = "";
                                        lastName = nameParts[0].trim();
                                    }

                                    // Find or create actor
                                    Actor actor = actorService.findByFullName(firstName, lastName)
                                            .orElseGet(() -> {
                                                Actor newActor = new Actor(
                                                        firstName,
                                                        lastName, birthDate, // birthdate unknown
                                                        gender,  // gender unknown
                                                        ethnicity // default ethnicity
                                                );
                                                return actorService.save(newActor);
                                            });

                                    // Add actor to series
                                    if (series.getActors() == null) {
                                        series.setActors(new ArrayList<>());
                                    }
                                    if (!series.getActors().contains(actor)) {
                                        series.getActors().add(actor);
                                    }

                                    // Add actor to first episode of first season
                                    if (!series.getSeasons().isEmpty()) {
                                        Season firstSeason = series.getSeasons().getFirst();
                                        if (firstSeason != null && !firstSeason.getEpisodes().isEmpty()) {
                                            Episode firstEpisode = firstSeason.getEpisodes().getFirst();
                                            if (firstEpisode.getActors() == null) {
                                                firstEpisode.setActors(new ArrayList<>());
                                            }
                                            if (!firstEpisode.getActors().contains(actor)) {
                                                firstEpisode.getActors().add(actor);
                                            }
                                        }
                                    }
                                } catch (Exception e) {
                                    logger.warn("Error processing actor '{}' at line {}: {}",
                                            actorName, lineNumber, e.getMessage());
                                }
                            }
                        }


                        // Save the series
                        try {
                            seriesService.save(series);
                            count++;

                            // Log progress every 10 records
                            if (count > 0 && count % 10 == 0) {
                                logger.info("Processed {} series so far...", count);
                            }

                            logger.debug("Successfully loaded series: {} ({})", title, startYear);
                        } catch (Exception e) {
                            logger.error("Error saving series '{}' at line {}: {}",
                                    title, lineNumber, e.getMessage(), e);
                            errors++;
                        }
                    }
                } catch (Exception e) {
                    logger.error("Error processing line {}: {}", lineNumber, e.getMessage(), e);
                    errors++;
                }
            } // End of while loop for reading lines

            // Log final results
            logger.info("Successfully loaded {} series ({} duplicates, {} errors, {} total lines)",
                    count, duplicates, errors, lineNumber);

            if (errors > 0) {
                throw new FileParsingException("Encountered " + errors + " errors while loading series data");
            }

            return count;

        } catch (IOException e) {
            throw new FileParsingException("Error reading file: " + filename);
        }
    }
}
