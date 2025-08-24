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
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Loads series data from files.
 */
public class SeriesDataLoader extends BaseDataLoader {
    private static final Logger logger = LoggerFactory.getLogger(SeriesDataLoader.class);
    private final ContentService<Series> seriesService;
    private final CelebrityService<Actor> actorService;
    private final CelebrityService<Director> directorService;

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
                    if (parts.length >= 8) {  // Reduced from 10 to 8 to match actual format
                        // Expected format: Title,Genre,Seasons,StartYear,EndYear,Rating,Creator,MainCast
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
                            if (parts.length > 4 && !parts[4].trim().isEmpty()) {
                                endYear = Integer.parseInt(parts[4].trim());
                            }
                        } catch (NumberFormatException e) {
                            logger.warn("Invalid year format at line {}: {}", lineNumber, line);
                            errors++;
                            continue;
                        }

                        double rating = 0.0;
                        try {
                            rating = Double.parseDouble(parts[5].trim());
                        } catch (NumberFormatException | ArrayIndexOutOfBoundsException e) {
                            logger.warn("Invalid or missing rating at line {}: {}", lineNumber, line);
                            rating = 0.0; // Default rating if not provided or invalid
                        }

                        String creatorName = parts[6].trim();
                        String[] actorNames = parts[7].split(";");
                        Set<Genre> genres = new HashSet<>();
                        try {
                            genres.add(Genre.valueOf(genreStr.toUpperCase()));
                        } catch (IllegalArgumentException e) {
                            logger.warn("Unknown genre '{}' at line {}: {}", genreStr, lineNumber, line);
                        }
                        String description = "";
                        String country = "";
                        String language = "English";
                        String imageUrl = "";

                        // Check if series already exists by title and year
                        if (seriesService.findByTitleAndYear(title, startYear).isPresent()) {
                            logger.debug("Series '{}' from {} already exists", title, startYear);
                            duplicates++;
                            continue;
                        }

                        // Create or update the series
                        Series series = new Series();
                        series.setTitle(title);
                        series.setStartYear(startYear);
                        series.setEndYear(endYear);
                        series.setRating(rating);
                        series.setDescription(description);
                        series.setCountry(country);
                        // Convert Set<Genre> to List<Genre>
                        series.setGenres(new ArrayList<>(genres));

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
                                                    creatorFirstName,
                                                    creatorLastName,
                                                    null, // birth date unknown
                                                    'U'   // gender unknown
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
                                        null, // birth date unknown
                                        'U'   // gender unknown
                                );
                                directorService.save(creator);
                                series.setDirector(creator);
                            }
                        }

                        // Add actors
                        for (String actorName : actorNames) {
                            if (!actorName.trim().isEmpty()) {
                                try {
                                    String[] nameParts = actorName.trim().split("\\s+", 2);
                                    if (nameParts.length >= 2) {
                                        String firstName = nameParts[0].trim();
                                        String lastName = nameParts[1].trim();

                                        // Find or create actor
                                        Actor actor = actorService.findByFullName(firstName, lastName)
                                                .orElseGet(() -> {
                                                    Actor newActor = new Actor(
                                                            firstName,
                                                            lastName,
                                                            null, // birth date unknown
                                                            'U',  // gender unknown
                                                            Ethnicity.CAUCASOID // default ethnicity
                                                    );
                                                    return actorService.save(newActor);
                                                });

                                        // Ensure series has seasons list
                                        if (series.getSeasons() == null) {
                                            series.setSeasons(new ArrayList<>());
                                        }

                                        // Ensure series has at least one season
                                        if (series.getSeasons().isEmpty()) {
                                            Season season = new Season(1, 1, "Season 1");
                                            series.getSeasons().add(season);

                                            // Create a new episode with required parameters
                                            Episode episode = new Episode();
                                            episode.setTitle("Pilot");
                                            episode.setDuration(0); // 0 duration for now
                                            episode.setEpisodeNumber(1);

                                            // Add episode to season
                                            season.addEpisode(episode);
                                        }

                                        // Get the first season and its episodes
                                        Season firstSeason = series.getSeasons().get(0);

                                        // Ensure the first season has at least one episode
                                        if (firstSeason.getEpisodes().isEmpty()) {
                                            Episode episode = new Episode();
                                            episode.setTitle("Pilot");
                                            episode.setDuration(0);
                                            episode.setEpisodeNumber(1);
                                            firstSeason.addEpisode(episode);
                                        }

                                        // Add actor to the first episode's cast
                                        Episode firstEpisode = firstSeason.getEpisodes().get(0);
                                        List<Actor> episodeActors = firstEpisode.getActors();
                                        if (episodeActors == null) {
                                            episodeActors = new ArrayList<>();
                                            // Note: We can't directly set the actors list, so we'll add them one by one
                                        }

                                        // Add actor if not already in the list
                                        if (!episodeActors.contains(actor)) {
                                            episodeActors.add(actor);
                                        }
                                    } else {
                                        // If we can't split into first and last name, use full name as last name
                                        // Create actor with empty first name and full name as last name
                                        Actor actor = new Actor(
                                                "",
                                                actorName.trim(),
                                                null, // birth date unknown
                                                'U',  // gender unknown
                                                Ethnicity.CAUCASOID  // default to Caucasoid as fallback
                                        );
                                        actorService.save(actor);
                                        if (series.getActors() == null) {
                                            series.setActors(new ArrayList<>());
                                        }
                                        series.getActors().add(actor);
                                    }
                                } catch (Exception e) {
                                    logger.warn("Error processing actor '{}' at line {}: {}",
                                            actorName, lineNumber, e.getMessage());
                                }
                            }
                        }

                        // Add seasons
                        for (int i = 1; i <= seasonsCount; i++) {
                            try {
                                Season season = new Season();
                                season.setSeasonNumber(i);
                                // Create empty episodes list for the season
                                season.setEpisodes(new ArrayList<>());
                                series.getSeasons().add(season);
                            } catch (Exception e) {
                                logger.warn("Error creating season {} for series '{}' at line {}: {}",
                                        i, title, lineNumber, e.getMessage());
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
