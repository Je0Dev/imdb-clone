package com.papel.imdb_clone.service.data.loader;

import com.papel.imdb_clone.exceptions.FileParsingException;
import com.papel.imdb_clone.model.User;
import com.papel.imdb_clone.repository.impl.InMemoryUserRepository;
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
 * Loads user data from files.
 */
public class UserDataLoader extends BaseDataLoader {
    private static final Logger logger = LoggerFactory.getLogger(UserDataLoader.class);
    private final InMemoryUserRepository userRepository;
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    private char gender;

    public UserDataLoader(InMemoryUserRepository userRepository) {
        this.userRepository = userRepository;
    }

    /**
     * Loads users from the specified file.
     *
     * @param filename the name of the file to load
     * @throws IOException if there is an error reading the file
     */
    public void load(String filename) throws IOException {
        logger.info("Loading users from {}", filename);
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
                        // Expected format: id,username,email,password,fullName,gender,dateOfBirth,country
                        int id = Integer.parseInt(parts[0].trim());
                        String username = parts[1].trim();
                        String email = parts[2].trim();
                        String password = parts[3].trim();
                        String fullName = parts[4].trim();
                        char gender = parts.length > 5 && !parts[5].trim().isEmpty() ? parts[5].trim().charAt(0) : 'U';
                        LocalDate birthDate = parts.length > 6 ? parseDate(parts[6].trim()) : null;
                        String country = parts.length > 7 ? parts[7].trim() : null;

                        // Check if user already exists
                        if (userRepository.findByUsername(username).isPresent()) {
                            logger.debug("User already exists: {}", username);
                            duplicates++;
                            continue;
                        }

                        // Create and save the user
                        try {
                            // Split fullName into first and last name
                            String[] nameParts = fullName.split(" ", 2);
                            String firstName = nameParts.length > 0 ? nameParts[0] : "";
                            String lastName = nameParts.length > 1 ? nameParts[1] : "";
                            
                            // Create user with required fields
                            User user = new User(firstName, lastName, username, gender, email);
                            user.setId(id);
                            user.setPassword(password); // Set the password (will be hashed by the service)
                            
                            // Set optional fields if available
                            if (birthDate != null) {
                                user.setBirthDate(birthDate);
                            }
                            if (country != null && !country.trim().isEmpty()) {
                                user.setCountry(country);
                            }
                            
                            // Set default values
                            user.setActive(true);
                            user.setAdmin(false);
                            user.setJoinDate(LocalDate.now());

                            // Check if user with this ID already exists
                            if (userRepository.findById(id).isPresent()) {
                                logger.debug("User with ID {} already exists, skipping", id);
                                duplicates++;
                            } else {
                                userRepository.addUser(user);  // Use addUser() instead of save() to preserve the ID
                                logger.info("Created user: {} (ID: {})", username, id);
                                count++;
                            }
                        } catch (Exception e) {
                            logger.error("Error creating user at line {}: {}", lineNumber, e.getMessage());
                            errors++;
                        }
                    } else {
                        logger.warn("Invalid user data format at line {}: {}", lineNumber, line);
                        errors++;
                    }
                } catch (Exception e) {
                    logger.error("Error processing line {}: {}", lineNumber, e.getMessage());
                    errors++;
                }
            }

            logger.info("Successfully loaded {} users ({} duplicates, {} errors, {} total lines)",
                    count, duplicates, errors, lineNumber);

        } catch (IOException e) {
            logger.error("Error reading users file: {}", e.getMessage(), e);
            throw new FileParsingException("Error reading users file: " + e.getMessage());
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
