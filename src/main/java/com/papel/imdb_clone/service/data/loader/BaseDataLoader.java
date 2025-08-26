package com.papel.imdb_clone.service.data.loader;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Base class for all data loaders providing common functionality.
 */
public abstract class BaseDataLoader {
    protected final Logger logger = LoggerFactory.getLogger(getClass());
    protected static final String DATA_PATH = "data/";
    protected static final String RESOURCES_DIR = "src/main/resources";

    /**
     * Gets an input stream for the specified resource file.
     * Tries multiple class loaders and path variations to find the resource.
     *
     * @param filename the name of the file to load (relative to DATA_PATH)
     * @return InputStream for the resource
     * @throws IOException if the resource cannot be found or loaded
     */
    protected InputStream getResourceAsStream(String filename) throws IOException {
        if (filename == null || filename.trim().isEmpty()) {
            throw new IllegalArgumentException("Filename cannot be null or empty");
        }

        // Normalize the filename
        String normalizedFilename = normalizeFilename(filename);
        logger.debug("Attempting to load file: {}", normalizedFilename);

        // Try different approaches to load the resource
        String[] resourcePaths = {
            "/data/" + normalizedFilename,  // Standard Maven resources directory (with leading slash)
            "data/" + normalizedFilename,   // Standard Maven resources directory (without leading slash)
            "/" + normalizedFilename,       // Root of resources
            normalizedFilename,              // Direct path
            "/src/main/resources/data/" + normalizedFilename, // Common Maven source path
            "src/main/resources/data/" + normalizedFilename   // Common Maven source path (relative)
        };

        // Try classpath resources first
        for (String path : resourcePaths) {
            // Try with context class loader first
            InputStream stream = Thread.currentThread().getContextClassLoader().getResourceAsStream(path);
            if (stream != null) {
                logger.info("Successfully loaded resource from classpath: {}", path);
                return stream;
            }

            // Try with this class's class loader
            stream = getClass().getResourceAsStream(path);
            if (stream != null) {
                logger.info("Successfully loaded resource using class loader: {}", path);
                return stream;
            }
        }

        // Try direct file paths (for development and testing)
        String[] filePaths = {
            "src/main/resources/data/" + normalizedFilename,
            "resources/data/" + normalizedFilename,
            "data/" + normalizedFilename,
            "./" + normalizedFilename,
            "./data/" + normalizedFilename,
            System.getProperty("user.dir") + "/src/main/resources/data/" + normalizedFilename,
            System.getProperty("user.dir") + "/data/" + normalizedFilename,
            System.getProperty("user.dir") + "/target/classes/data/" + normalizedFilename
        };

        for (String path : filePaths) {
            try {
                File file = new File(path).getCanonicalFile();
                if (file.exists() && file.isFile()) {
                    logger.info("Successfully loaded resource from file system: {}", file.getAbsolutePath());
                    return new FileInputStream(file);
                }
                logger.debug("File not found: {}", file.getAbsolutePath());
            } catch (IOException e) {
                logger.debug("Error checking file path {}: {}", path, e.getMessage());
            }
        }

        // If we get here, we couldn't find the file anywhere
        String errorMsg = String.format("""
            Failed to load resource '%s'. Checked the following locations:\n"
            - Classpath: /data/%s, data/%s, /%s, %s\n"
            - File system: %s/src/main/resources/data/%s, %s/data/%s, etc.
            """,
            normalizedFilename, 
            normalizedFilename, normalizedFilename, normalizedFilename, normalizedFilename,
            System.getProperty("user.dir"), normalizedFilename,
            System.getProperty("user.dir"), normalizedFilename
        );
        
        logger.error(errorMsg);
        throw new FileNotFoundException(errorMsg);
    }

    /**
     * Normalizes the filename by removing leading/trailing whitespace and slashes.
     */
    private String normalizeFilename(String filename) {
        if (filename == null) {
            return "";
        }
        
        String normalized = filename.trim()
                .replace("\\", "/")
                .replaceAll("^[\\/]+", "")  // Remove leading slashes
                .replaceAll("[\\/]+$", "");   // Remove trailing slashes
        
        // Remove any leading data/ from the filename to prevent double slashes
        if (normalized.startsWith("data/")) {
            normalized = normalized.substring(5);
        }
        
        logger.debug("Normalized filename '{}' to '{}'", filename, normalized);
        return normalized;
    }

    /**
     * Validates the input stream and filename.
     *
     * @param inputStream the input stream to validate
     * @param filename    the filename for error messages
     * @throws IllegalArgumentException if input stream is null
     */
    protected void validateInput(InputStream inputStream, String filename) {
        if (inputStream == null) {
            throw new IllegalArgumentException("Input stream cannot be null for file: " + filename);
        }
    }

    /**
     * Parses a CSV line, handling quoted fields and semicolon-separated values.
     *
     * @param line the line to parse
     * @return array of parsed fields
     */
    protected String[] parseCSVLine(String line) {
        if (line == null || line.trim().isEmpty()) {
            return new String[0];
        }

        // Skip comment lines
        if (line.trim().startsWith("#")) {
            return new String[0];
        }

        List<String> fields = new ArrayList<>();
        StringBuilder currentField = new StringBuilder();
        boolean inQuotes = false;
        boolean escapeNext = false;

        for (int i = 0; i < line.length(); i++) {
            char c = line.charAt(i);

            if (escapeNext) {
                currentField.append(c);
                escapeNext = false;
                continue;
            }

            if (c == '\\') {
                escapeNext = true;
            } else if (c == '"') {
                // Handle quoted fields
                if (inQuotes && i + 1 < line.length() && line.charAt(i + 1) == '"') {
                    // Escaped quote inside quoted field
                    currentField.append('"');
                    i++; // Skip the next quote
                } else {
                    inQuotes = !inQuotes;
                }
            } else if (c == ',' && !inQuotes) {
                // End of field
                fields.add(currentField.toString().trim());
                currentField = new StringBuilder();
            } else {
                currentField.append(c);
            }
        }

        // Add the last field
        fields.add(currentField.toString().trim());

        // Handle case where line ends with a comma
        if (line.endsWith(",")) {
            fields.add("");
        }

        return fields.toArray(new String[0]);
    }

}
