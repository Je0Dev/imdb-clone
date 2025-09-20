package com.papel.imdb_clone.service.data.loader.people;

import com.papel.imdb_clone.enums.Ethnicity;
import com.papel.imdb_clone.exceptions.FileParsingException;
import com.papel.imdb_clone.model.people.Actor;
import com.papel.imdb_clone.service.people.CelebrityService;
import com.papel.imdb_clone.service.data.base.BaseDataLoader;
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
        long startTime = System.currentTimeMillis();
        logger.info("Starting to load actors from: {}", filename);
        // Initialize variables
        int count = 0;
        int errors = 0;
        int duplicates = 0;
        int lineNumber = 0;
        logger.debug("Initializing actor data loading process");

        try (InputStream inputStream = getResourceAsStream(filename);
             BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream))) {

            // Validate input
            validateInput(inputStream, filename);
            String line;

            // Read file line by line
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

                        // Parse birth date with better error handling and fallback
                        LocalDate birthDate = null;
                        String birthDateStr = parts[2].trim();
                        
                        if (birthDateStr.isEmpty() || birthDateStr.equalsIgnoreCase("n/a")) {
                            // Generate a reasonable default birth date (30 years ago from now)
                            birthDate = LocalDate.now().minusYears(30 + (count % 20)); // Vary the year slightly based on count
                            logger.warn("No birth date specified for {} {} at line {}. Using default: {}", 
                                firstName, lastName, lineNumber, birthDate);
                        } else {
                            try {
                                birthDate = parseDate(birthDateStr);
                                
                                // Validate birth date is reasonable (not in the future and not too old)
                                LocalDate now = LocalDate.now();
                                LocalDate minBirthDate = now.minusYears(120); // Max age 120 years
                                LocalDate maxBirthDate = now.plusYears(1); // Allow for timezone issues
                                
                                if (birthDate.isBefore(minBirthDate) || birthDate.isAfter(maxBirthDate)) {
                                    logger.warn("Birth date {} for {} {} is out of reasonable range. Adjusting.", 
                                        birthDate, firstName, lastName);
                                    // Set to 30 years ago with some variation
                                    birthDate = now.minusYears(30 + (count % 20));
                                }
                            } catch (Exception e) {
                                logger.warn("Invalid birth date format '{}' for {} {} at line {}. Using default. Error: {}", 
                                    birthDateStr, firstName, lastName, lineNumber, e.getMessage());
                                // Set default birth date with some variation
                                birthDate = LocalDate.now().minusYears(30 + (count % 20));
                            }
                        }

                        // Parse gender (first character, uppercase)
                        char gender = 'U'; // Default to Unknown
                        if (!parts[3].trim().isEmpty()) {
                            String genderStr = parts[3].trim().toUpperCase();
                            gender = genderStr.charAt(0);
                        }

                        // Nationality
                        String nationality = parts[4].trim();
                        Ethnicity ethnicity = null;
                        try {
                            // Set ethnicity if nationality is not empty and not N/A
                            if (!nationality.isEmpty() && !nationality.equalsIgnoreCase("N/A")) {
                                ethnicity = Ethnicity.fromLabel(nationality);
                            }
                        } catch (IllegalArgumentException e) {
                            logger.warn("Unknown ethnicity '{}' for actor {} {} at line {}", nationality, firstName, lastName, lineNumber);
                        }

                        // Notable works (optional)
                        String notableWorks = parts.length > 6 ? parts[6].trim() : "";

                        // Create and save the actor
                        try {
                            // Use factory method to get or create actor instance
                            Actor actor = Actor.getInstance(
                                firstName,
                                lastName,
                                birthDate,
                                gender,
                                ethnicity != null ? ethnicity : Ethnicity.UNKNOWN
                            );
                            
                            // Set notable works if provided
                            if (!notableWorks.isEmpty() && !notableWorks.equalsIgnoreCase("N/A")) {
                                actor.setNotableWorks(notableWorks);
                            }

                            // Check if actor already exists
                            if (actorService.findByFullName(firstName, lastName).isPresent()) {
                                logger.debug("Skipping duplicate actor: {} {}", firstName, lastName);
                                duplicates++;
                                logger.trace("Actor already exists in database: {} {}", firstName, lastName);
                            } else {
                                actorService.save(actor);
                                count++;
                            }
                        } catch (Exception e) {
                            logger.error("Error creating actor '{} {}' at line {}: {}", 
                                firstName, lastName, lineNumber, e.getMessage(), e);
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

            long endTime = System.currentTimeMillis();
            long duration = (endTime - startTime) / 1000;
            
            if (errors > 0) {
                logger.warn("Completed loading actors with {} errors. Successfully loaded {} actors ({} duplicates, {} total lines) in {} seconds", 
                    errors, count, duplicates, lineNumber, duration);
            } else {
                logger.info("Successfully loaded {} actors ({} duplicates, {} total lines) in {} seconds", 
                    count, duplicates, lineNumber, duration);
            }

        } catch (IOException e) {
            logger.error("Error reading actors file: {}", e.getMessage(), e);
            throw new FileParsingException("Error reading actors file: " + e.getMessage());
        } finally {
            logger.debug("Actor data loading process completed");
        }
    }

    /**
     * Parses a date string into a LocalDate object.
     * @param dateStr the date string to parse
     * @return LocalDate object or null if the date string is invalid
     */
    private LocalDate parseDate(String dateStr) {
        if (dateStr == null || dateStr.trim().isEmpty() || dateStr.equalsIgnoreCase("n/a")) {
            return null;
        }
        try {
            // Try parsing with the standard format first
            return LocalDate.parse(dateStr, DATE_FORMATTER);
        } catch (DateTimeParseException e1) {
            try {
                // Try parsing with just year (set to Jan 1st of that year)
                int year = Integer.parseInt(dateStr.trim());
                return LocalDate.of(year, 1, 1);
            } catch (NumberFormatException e2) {
                logger.warn("Invalid date format: {}", dateStr);
                return null;
            }
        }
    }
}
