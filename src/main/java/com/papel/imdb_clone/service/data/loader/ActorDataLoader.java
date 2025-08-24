package com.papel.imdb_clone.service.data.loader;

import com.papel.imdb_clone.exceptions.FileParsingException;
import com.papel.imdb_clone.model.Actor;
import com.papel.imdb_clone.service.CelebrityService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

/**
 * Loads actor data from files.
 */
public class ActorDataLoader extends BaseDataLoader {
    private static final Logger logger = LoggerFactory.getLogger(ActorDataLoader.class);
    private final CelebrityService<Actor> actorService;
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    public ActorDataLoader(CelebrityService<Actor> actorService) {
        this.actorService = actorService;
    }

    /**
     * Loads actors from the specified file.
     *
     * @param filename the name of the file to load
     * @throws IOException if there is an error reading the file
     */
    public void load(String filename) throws IOException {
        logger.info("Loading actors from {}", filename);
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
                    if (parts.length >= 5) {
                        // Expected format: FirstName,LastName,BirthDate,Gender,Nationality,Biography,NotableWorks
                        String firstName = parts[0].trim();
                        String lastName = parts[1].trim();
                        
                        // Parse birth date
                        LocalDate birthDate = null;
                        try {
                            birthDate = parseDate(parts[2].trim());
                        } catch (Exception e) {
                            logger.warn("Invalid birth date format '{}' at line {}: {}", parts[2], lineNumber, e.getMessage());
                            errors++;
                            continue;
                        }
                        
                        // Parse gender (first character, uppercase)
                        char gender = 'U'; // Default to Unknown
                        if (parts.length > 3 && !parts[3].trim().isEmpty()) {
                            String genderStr = parts[3].trim().toUpperCase();
                            gender = genderStr.charAt(0);
                        }
                        
                        // Nationality (ethnicity in the model)
                        String nationality = parts.length > 4 ? parts[4].trim() : "";
                        
                        // Biography (optional)
                        String biography = parts.length > 5 ? parts[5].trim() : "";
                        
                        // Notable works (optional)
                        String notableWorks = parts.length > 6 ? parts[6].trim() : "";

                        // Create and save the actor
                        try {
                            Actor actor = new Actor(firstName, lastName, birthDate, gender, nationality);
                            if (biography != null && !biography.isEmpty()) {
                                actor.setBiography(biography);
                            }
                            if (notableWorks != null && !notableWorks.isEmpty()) {
                                actor.setNotableWorks(notableWorks);
                            }

                            // Check if actor already exists
                            if (actorService.findByFullName(firstName, lastName).isPresent()) {
                                logger.debug("Actor already exists: {} {}", firstName, lastName);
                                duplicates++;
                            } else {
                                actorService.save(actor);
                                count++;
                            }
                        } catch (Exception e) {
                            logger.error("Error creating actor at line {}: {}", lineNumber, e.getMessage());
                            errors++;
                        }
                    } else {
                        logger.warn("Invalid actor data format at line {}: {}", lineNumber, line);
                        errors++;
                    }
                } catch (Exception e) {
                    logger.error("Error processing line {}: {}", lineNumber, e.getMessage());
                    errors++;
                }
            }

            logger.info("Successfully loaded {} actors ({} duplicates, {} errors, {} total lines)",
                    count, duplicates, errors, lineNumber);

        } catch (IOException e) {
            logger.error("Error reading actors file: {}", e.getMessage(), e);
            throw new FileParsingException("Error reading actors file: " + e.getMessage());
        }
    }

    private LocalDate parseDate(String dateStr) {
        if (dateStr == null || dateStr.trim().isEmpty()) {
            return null;
        }
        try {
            return LocalDate.parse(dateStr, DATE_FORMATTER);
        } catch (DateTimeParseException e) {
            logger.warn("Invalid date format: {}", dateStr);
            return null;
        }
    }
}
