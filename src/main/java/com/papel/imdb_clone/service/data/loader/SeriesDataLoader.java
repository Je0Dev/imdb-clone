package com.papel.imdb_clone.service.data.loader;

import com.papel.imdb_clone.enums.Genre;
import com.papel.imdb_clone.exceptions.FileParsingException;
import com.papel.imdb_clone.model.Actor;
import com.papel.imdb_clone.model.Director;
import com.papel.imdb_clone.model.Season;
import com.papel.imdb_clone.model.Series;
import com.papel.imdb_clone.service.CelebrityService;
import com.papel.imdb_clone.service.ContentService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
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
                    if (parts.length >= 10) {
                        // Expected format: id,title,startYear,endYear,seasons,rating,description,creator,actors,genres,country,language,imageUrl
                        String id = parts[0].trim();
                        String title = parts[1].trim();

                        // Parse years (handle empty end year)
                        int startYear = 0;
                        Integer endYear = null;
                        try {
                            startYear = Integer.parseInt(parts[2].trim());
                            if (!parts[3].trim().isEmpty()) {
                                endYear = Integer.parseInt(parts[3].trim());
                            }
                        } catch (NumberFormatException e) {
                            logger.warn("Invalid year format at line {}: {}", lineNumber, line);
                            errors++;
                            continue;
                        }

                        int seasonsCount = 0;
                        try {
                            seasonsCount = Integer.parseInt(parts[4].trim());
                        } catch (NumberFormatException e) {
                            logger.warn("Invalid seasons count at line {}: {}", lineNumber, parts[4].trim());
                            seasonsCount = 1; // Default to 1 season if not specified or invalid
                        }

                        double rating = 0.0;
                        try {
                            rating = Double.parseDouble(parts[5].trim());
                        } catch (NumberFormatException e) {
                            logger.warn("Invalid rating format at line {}: {}", lineNumber, parts[5].trim());
                            rating = 0.0; // Default rating if not provided or invalid
                        }

                        String description = parts[6].trim();
                        String creatorName = parts[7].trim();
                        String[] actorNames = parts[8].split(",");
                        String[] genreNames = parts[9].split(",");
                        String country = parts.length > 10 ? parts[10].trim() : "";
                        String language = parts.length > 11 ? parts[11].trim() : "English";
                        String imageUrl = parts.length > 12 ? parts[12].trim() : "";

                        // Check if series already exists by ID
                        if (seriesService.findById(Integer.parseInt(id)).isPresent()) {
                            logger.debug("Series with ID {} already exists: {}", id, title);
                            duplicates++;
                            continue;
                        }

                        try {
                            // Create the series
                            Series series = new Series();
                            series.setId(Integer.parseInt(id));
                            series.setTitle(title);
                            series.setStartYear(startYear);
                            if (endYear != null) {
                                series.setEndYear(endYear);
                            }
                            series.setDescription(description);
                            series.setRating(rating);
                            series.setCountry(country);
                            series.setLanguage(language);

                            // Set genres
                            for (String genreName : genreNames) {
                                try {
                                    if (!genreName.trim().isEmpty()) {
                                        Genre genre = Genre.valueOf(genreName.trim().toUpperCase());
                                        series.setGenre(genre);
                                    }
                                } catch (IllegalArgumentException e) {
                                    logger.warn("Invalid genre '{}' at line {}", genreName, lineNumber);
                                }
                            }

                            // Set creator (director)
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

                                    series.setCreator(String.valueOf(creator));
                                }
                            }

                            // Set actors
                            Set<Actor> actors = new HashSet<>();
                            for (String actorName : actorNames) {
                                if (!actorName.trim().isEmpty()) {
                                    String[] actorNameParts = actorName.trim().split("\\s+", 2);
                                    if (actorNameParts.length >= 2) {
                                        String firstName = actorNameParts[0].trim();
                                        String lastName = actorNameParts[1].trim();

                                        // Find or create actor
                                        Optional<Actor> actorOpt = actorService.findByFullName(firstName, lastName);
                                        if (actorOpt.isPresent()) {
                                            actors.add(actorOpt.get());
                                        } else {
                                            // Create a new actor if not found
                                            Actor newActor = new Actor(
                                                    firstName,
                                                    lastName,
                                                    null, // birth date unknown
                                                    'U',  // gender unknown
                                                    ""    // ethnicity unknown
                                            );
                                            actors.add(actorService.save(newActor));
                                        }
                                    }
                                }
                            }
                            series.setActors((List<Actor>) actors);

                            // Create seasons
                            for (int i = 1; i <= seasonsCount; i++) {
                                Season season = new Season(i, 10, String.format("Season %d", i));
                                season.setSeries(series);
                                series.getSeasons().add(season);
                            }

                            // Add the series using addContent() to preserve pre-defined IDs
                            seriesService.addContent(series);
                            count++;

                            // Log progress every 100 records
                            if (count > 0 && count % 100 == 0) {
                                logger.info("Processed {} series so far...", count);
                            }

                        } catch (Exception e) {
                            logger.error("Error creating series at line {}: {}", lineNumber, e.getMessage(), e);
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
