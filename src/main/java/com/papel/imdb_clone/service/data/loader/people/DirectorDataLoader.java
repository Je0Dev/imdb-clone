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
import java.util.Objects;

/**
 * Loads director data from files.
 */
public class DirectorDataLoader extends BaseDataLoader {

    //logger
    private static final Logger logger = LoggerFactory.getLogger(DirectorDataLoader.class);
    private final CelebrityService<Director> directorService;

    //date formatter
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    private Ethnicity ethnicity;

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

        //counters to track loading progress
        int count = 0;
        int errors = 0;
        int duplicates = 0;
        int lineNumber = 0;
        logger.debug("Initializing director data loading process");

        try (InputStream inputStream = getResourceAsStream(filename);
             //create buffered reader to read file line by line
             BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream))) {

            validateInput(inputStream, filename);
            String line;

            /*
             * Read each line of the file
             */
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

                        // Parse birth date with better error handling and fallback
                        LocalDate birthDate = null;
                        String birthDateStr = parts[2].trim();
                        
                        if (birthDateStr.isEmpty() || birthDateStr.equalsIgnoreCase("n/a")) {
                            // Generate a unique default birth date with more variation
                            // Use a combination of count, name hash, and line number for more uniqueness
                            int nameHash = Math.abs((firstName + lastName).hashCode());
                            int yearVariation = (count + nameHash) % 50; // 0-49 years variation
                            int monthVariation = (count + nameHash) % 12 + 1; // 1-12 months
                            int dayVariation = ((count + nameHash) % 28) + 1; // 1-28 days
                            
                            birthDate = LocalDate.now()
                                .minusYears(35 + yearVariation) // 35-84 years old
                                .minusMonths(monthVariation)
                                .minusDays(dayVariation);
                                
                            logger.warn("No birth date specified for director {} {} at line {}. Generated unique default: {}", 
                                firstName, lastName, lineNumber, birthDate);
                        } else {
                            try {
                                birthDate = parseDate(birthDateStr);
                                
                                // Validate birthdate is reasonable (not in the future and not too old)
                                LocalDate now = LocalDate.now();
                                LocalDate minBirthDate = now.minusYears(120); // Max age 120 years
                                LocalDate maxBirthDate = now.plusYears(1); // Allow for timezone issues
                                
                                if (birthDate.isBefore(minBirthDate) || birthDate.isAfter(maxBirthDate)) {
                                    logger.warn("Birth date {} for director {} {} is out of reasonable range. Adjusting.", 
                                        birthDate, firstName, lastName);
                                    // Set to 35 years ago with some variation
                                    birthDate = now.minusYears(35 + (count % 20));
                                }
                            } catch (Exception e) {
                                logger.warn("Invalid birth date format '{}' for director {} {} at line {}. Using default. Error: {}", 
                                    birthDateStr, firstName, lastName, lineNumber, e.getMessage());
                                // Set default birthdate with some variation
                                birthDate = LocalDate.now().minusYears(35 + (count % 20));
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
                            if (!nationality.isEmpty() && !nationality.equalsIgnoreCase("N/A")) {
                                ethnicity = com.papel.imdb_clone.enums.Ethnicity.fromLabel(nationality);
                            }
                        } catch (IllegalArgumentException e) {
                            logger.warn("Unknown ethnicity '{}' for director {} {} at line {}", nationality, firstName, lastName, lineNumber);
                        }

                        // Notable works (optional) - check multiple possible positions
                        String notableWorks = "";
                        // Try different columns that might contain notable works (indices 5,6,7)
                        for (int i = 5; i < parts.length && i < 8; i++) {
                            if (!parts[i].trim().isEmpty() && !parts[i].trim().equalsIgnoreCase("N/A")) {
                                String potentialWorks = parts[i].trim();
                                // Check if this looks like a list of works (contains commas or semicolons)
                                if (potentialWorks.matches(".*[,;].*")) {
                                    notableWorks = potentialWorks;
                                    break;
                                } else if (notableWorks.isEmpty()) {
                                    // If no works found yet, use this as a single work
                                    notableWorks = potentialWorks;
                                }
                            }
                        }
                        
                        // Set default notable works if still empty
                        if (notableWorks.isEmpty()) {
                            // Generate some default works based on director's name
                            String defaultWork1 = String.format("Director of \"The %s Project\"", lastName);
                            String defaultWork2 = String.format("Director of \"%s's Vision\"", lastName);
                            notableWorks = defaultWork1 + ", " + defaultWork2;
                            logger.debug("Using default notable works for director {} {}: {}", firstName, lastName, notableWorks);
                        }

                        // Create and save the director
                        try {
                            // Use factory method to get or create director instance
                            Director director = Director.getInstance(
                                firstName,
                                lastName,
                                birthDate,
                                gender,
                                ethnicity != null ? ethnicity : com.papel.imdb_clone.enums.Ethnicity.UNKNOWN
                            );
                            
                            // Set notable works if provided
                            if (!notableWorks.equalsIgnoreCase("N/A")) {
                                director.setNotableWorks(notableWorks);
                            }
                            
                            // Check if director already exists
                            if (directorService.findByFullName(firstName, lastName).isPresent()) {
                                logger.debug("Skipping duplicate director: {} {}", firstName, lastName);
                                logger.trace("Director already exists in database: {} {}", firstName, lastName);
                                duplicates++;
                            } else {
                                directorService.save(director);
                                count++;
                            }
                        } catch (Exception e) {
                            logger.error("Error creating director '{} {}' at line {}: {}", 
                                firstName, lastName, lineNumber, e.getMessage(), e);
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

            // Log summary of the loading process
            long endTime = System.currentTimeMillis();
            long duration = (endTime - startTime) / 1000;
            
            if (errors > 0) {
                logger.warn("Completed loading directors with {} errors. Successfully loaded {} directors ({} duplicates, {} total lines) in {} seconds", 
                    errors, count, duplicates, lineNumber, duration);
            } else {
                logger.info("Successfully loaded {} directors ({} duplicates, {} total lines) in {} seconds", 
                    count, duplicates, lineNumber, duration);
            }

        } catch (IOException e) {
            logger.error("Error reading directors file: {}", e.getMessage(), e);
            throw new FileParsingException("Error reading directors file: " + e.getMessage());
        } finally {
            logger.debug("Director data loading process completed");
        }
    }

    private static String getString(String[] parts) {
        String notableWorks = "";
        if (parts.length > 6 && !parts[5].trim().isEmpty() && !parts[5].trim().equalsIgnoreCase("N/A")) {
            notableWorks = parts[5].trim();
        } else if (parts.length > 5 && !parts[4].trim().isEmpty() && !parts[4].trim().equalsIgnoreCase("N/A")) {
            // Fallback to previous position if current is empty
            notableWorks = parts[4].trim();
        }

        // Active years (optional)
        String activeYears = parts.length > 6 ? parts[6].trim() : "";

        // Set default notable works if still empty
        if (notableWorks.isEmpty()) {
            notableWorks = "Various films and TV shows";
        }
        return notableWorks;
    }

    /**
     * Parses a date string into a LocalDate object.
     * @param dateStr the date string to parse
     * @return LocalDate object which means the date of birth of the director
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
