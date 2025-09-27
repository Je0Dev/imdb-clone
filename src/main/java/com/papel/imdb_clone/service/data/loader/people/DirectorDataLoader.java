package com.papel.imdb_clone.service.data.loader.people;

import com.papel.imdb_clone.enums.Ethnicity;
import com.papel.imdb_clone.exceptions.FileParsingException;
import com.papel.imdb_clone.model.people.Director;
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
 * Loads director data from files.
 */
public class DirectorDataLoader extends BaseDataLoader {
    private static final Logger logger = LoggerFactory.getLogger(DirectorDataLoader.class);
    private final CelebrityService<Director> directorService;
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");

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
        long startTime = System.currentTimeMillis();
        logger.info("Starting to load directors from: {}", filename);

        // Counters to track loading progress
        int count = 0;
        int errors = 0;
        int duplicates = 0;
        int lineNumber = 0;
        logger.debug("Initializing director data loading process");

        try (InputStream inputStream = getResourceAsStream(filename);
             BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream))) {

            validateInput(inputStream, filename);
            String line;

            // Read each line of the file
            while ((line = reader.readLine()) != null) {
                lineNumber++;
                if (line.trim().isEmpty() || line.trim().startsWith("#")) {
                    continue;
                }

                try {
                    String[] parts = parseCSVLine(line);
                    if (parts.length < 5) {
                        logger.warn("Incomplete data at line {}: {}", lineNumber, line);
                        errors++;
                        continue;
                    }

                    // Expected format: FirstName,LastName,BirthDate,Gender,Nationality,NotableWorks
                    String firstName = parts[0].trim();
                    String lastName = parts[1].trim();

                    // Parse birth date with better error handling and fallback
                    LocalDate birthDate = parseBirthDate(parts[2].trim(), firstName, lastName, count, lineNumber);
                    if (birthDate == null) {
                        errors++;
                        continue;
                    }

                    // Parse gender (M/F/Other)
                    char gender = parseGender(parts);

                    // Parse nationality/ethnicity
                    Ethnicity ethnicity = parseEthnicity(parts, firstName, lastName, lineNumber);

                    // Parse notable works
                    String notableWorks = parseNotableWorks(parts, firstName, lastName);

                    // Create and save the director
                    if (createAndSaveDirector(firstName, lastName, birthDate, gender, ethnicity, 
                            notableWorks, lineNumber)) {
                        count++;
                    } else {
                        errors++;
                    }
                } catch (Exception e) {
                    logger.error("Error processing line {}: {}", lineNumber, e.getMessage(), e);
                    errors++;
                }
            }

            // Log summary of the loading process
            logLoadingSummary(startTime, count, duplicates, lineNumber, errors);

        } catch (IOException e) {
            logger.error("Error reading directors file: {}", e.getMessage(), e);
            throw new FileParsingException("Error reading directors file: " + e.getMessage());
        } finally {
            logger.debug("Director data loading process completed");
        }
    }

    private LocalDate parseBirthDate(String birthDateStr, String firstName, String lastName, int count, int lineNumber) {
        if (birthDateStr.isEmpty() || birthDateStr.equalsIgnoreCase("n/a")) {
            // Generate a unique default birth date with more variation
            int nameHash = Math.abs((firstName + lastName).hashCode());
            int yearVariation = (count + nameHash) % 50; // 0-49 years variation
            int monthVariation = (count + nameHash) % 12 + 1; // 1-12 months
            int dayVariation = ((count + nameHash) % 28) + 1; // 1-28 days
            
            LocalDate birthDate = LocalDate.now()
                .minusYears(35 + yearVariation) // 35-84 years old
                .minusMonths(monthVariation)
                .minusDays(dayVariation);
                
            logger.debug("No birth date specified for director {} {} at line {}. Generated default: {}", 
                firstName, lastName, lineNumber, birthDate);
            return birthDate;
        }

        try {
            LocalDate birthDate = parseDate(birthDateStr);
            
            // Validate birth date is reasonable (not in the future and not too old)
            LocalDate now = LocalDate.now();
            LocalDate minBirthDate = now.minusYears(120); // Max age 120 years
            LocalDate maxBirthDate = now.plusYears(1); // Allow for timezone issues
            
            if (birthDate.isBefore(minBirthDate) || birthDate.isAfter(maxBirthDate)) {
                logger.warn("Birth date {} for director {} {} is out of reasonable range. Adjusting.", 
                    birthDate, firstName, lastName);
                // Set to 35 years ago with some variation
                return now.minusYears(35 + (count % 20));
            }
            return birthDate;
        } catch (Exception e) {
            logger.error("Error parsing birth date '{}' for director {} {} at line {}: {}", 
                birthDateStr, firstName, lastName, lineNumber, e.getMessage());
            return null;
        }
    }

    private char parseGender(String[] parts) {
        // Default to Unknown
        char gender = 'U';
        if (parts.length > 3 && !parts[3].trim().isEmpty()) {
            String genderStr = parts[3].trim().toUpperCase();
            if (genderStr.startsWith("M") || genderStr.startsWith("F")) {
                gender = genderStr.charAt(0);
            } else if (genderStr.startsWith("MALE")) {
                gender = 'M';
            } else if (genderStr.startsWith("FEMALE")) {
                gender = 'F';
            }
        }
        return gender;
    }

    private Ethnicity parseEthnicity(String[] parts, String firstName, String lastName, int lineNumber) {
        Ethnicity ethnicity = Ethnicity.UNKNOWN;
        if (parts.length > 4 && !parts[4].trim().isEmpty()) {
            String nationality = parts[4].trim().toUpperCase();
            try {
                // Try to match nationality with Ethnicity enum
                ethnicity = Ethnicity.valueOf(nationality);
            } catch (IllegalArgumentException e) {
                // Try to find a matching ethnicity
                for (Ethnicity eValue : Ethnicity.values()) {
                    if (eValue.name().contains(nationality) || 
                        nationality.contains(eValue.name()) ||
                        eValue.toString().equalsIgnoreCase(nationality)) {
                        ethnicity = eValue;
                        break;
                    }
                }
                if (ethnicity == Ethnicity.UNKNOWN) {
                    logger.debug("Could not map nationality '{}' for director {} {} at line {}", 
                        parts[4], firstName, lastName, lineNumber);
                }
            }
        }
        return ethnicity;
    }

    private String parseNotableWorks(String[] parts, String firstName, String lastName) {
        String notableWorks = "";
        if (parts.length > 5 && !parts[5].trim().isEmpty()) {
            notableWorks = parts[5].trim()
                .replace("  ", " ")  // replace double spaces with single space
                .trim();
            
            logger.debug("Processed notable works for director {} {}: {}", 
                firstName, lastName, notableWorks);
        } else {
            // Generate some default works based on director's name
            String defaultWork1 = String.format("Directed \"The %s Story\"", lastName);
            String defaultWork2 = String.format("Directed \"%s's Vision\"", lastName);
            notableWorks = defaultWork1 + ", " + defaultWork2;
            logger.debug("Using default notable works for director {} {}: {}", 
                firstName, lastName, notableWorks);
        }
        return notableWorks;
    }

    private boolean createAndSaveDirector(String firstName, String lastName, LocalDate birthDate, 
                                        char gender, Ethnicity ethnicity, String notableWorks, 
                                        int lineNumber) {
        try {
            // Use factory method to get or create director instance
            Director director = Director.getInstance(
                firstName,
                lastName,
                birthDate,
                gender,
                ethnicity
            );
            
            // Set notable works if provided
            if (!notableWorks.equalsIgnoreCase("N/A")) {
                director.setNotableWorks(notableWorks);
            }
            
            // Check if director already exists
            if (directorService.findByFullName(firstName, lastName).isPresent()) {
                logger.debug("Skipping duplicate director: {} {}", firstName, lastName);
                logger.trace("Director already exists in database: {} {}", firstName, lastName);
                return false;
            } else {
                directorService.save(director);
                return true;
            }
        } catch (Exception e) {
            logger.error("Error creating director '{} {}' at line {}: {}", 
                firstName, lastName, lineNumber, e.getMessage(), e);
            return false;
        }
    }

    private void logLoadingSummary(long startTime, int count, int duplicates, int lineNumber, int errors) {
        long endTime = System.currentTimeMillis();
        long duration = (endTime - startTime) / 1000;
        
        if (errors > 0) {
            logger.warn("Completed loading directors with {} errors. Successfully loaded {} directors ({} duplicates, {} total lines) in {} seconds", 
                errors, count, duplicates, lineNumber, duration);
        } else {
            logger.info("Successfully loaded {} directors ({} duplicates, {} total lines) in {} seconds", 
                count, duplicates, lineNumber, duration);
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
