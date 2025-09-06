package com.papel.imdb_clone.service.data.loader;

import com.papel.imdb_clone.enums.Ethnicity;
import com.papel.imdb_clone.exceptions.FileParsingException;
import com.papel.imdb_clone.model.Director;
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
 * Loads director data from files.
 */
public class DirectorDataLoader extends BaseDataLoader {
    private static final Logger logger = LoggerFactory.getLogger(DirectorDataLoader.class);
    private final CelebrityService<Director> directorService;
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    private Ethnicity Ethnicity;

    public DirectorDataLoader(CelebrityService<Director> directorService) {
        this.directorService = directorService;
    }

    /**
     * Loads directors from the specified file.
     *
     * @param filename the name of the file to load
     * @throws IOException if there is an error reading the file
     */
    public void load(String filename) throws IOException {
        logger.info("Loading directors from {}", filename);
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
                        // Expected format: FirstName,LastName,BirthDate,Gender,Nationality,NotableWorks,ActiveYears
                        String firstName = parts[0].trim();
                        String lastName = parts[1].trim();

                        // Parse birthdate
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

                        // Nationality
                        String nationality = parts.length > 4 ? parts[4].trim() : "";

                        // Notable works (optional)
                        String notableWorks = parts.length > 5 ? parts[5].trim() : "";

                        // Active years (optional)
                        String activeYears = parts.length > 6 ? parts[6].trim() : "";

                        // Create and save the director
                        try {
                            Director director = new Director(firstName, lastName, birthDate, gender,Ethnicity);
                            
                            // Check if director already exists
                            if (directorService.findByFullName(firstName, lastName).isPresent()) {
                                logger.debug("Director already exists: {} {}", firstName, lastName);
                                duplicates++;
                            } else {
                                directorService.save(director);
                                count++;
                            }
                        } catch (Exception e) {
                            logger.error("Error creating director at line {}: {}", lineNumber, e.getMessage());
                            errors++;
                        }
                    } else {
                        logger.warn("Invalid director data format at line {}: {}", lineNumber, line);
                        errors++;
                    }
                } catch (Exception e) {
                    logger.error("Error processing line {}: {}", lineNumber, e.getMessage());
                    errors++;
                }
            }

            logger.info("Successfully loaded {} directors ({} duplicates, {} errors, {} total lines)",
                    count, duplicates, errors, lineNumber);

        } catch (IOException e) {
            logger.error("Error reading directors file: {}", e.getMessage(), e);
            throw new FileParsingException("Error reading directors file: " + e.getMessage());
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
