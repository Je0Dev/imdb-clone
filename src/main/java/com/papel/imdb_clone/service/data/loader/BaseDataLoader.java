package com.papel.imdb_clone.service.data.loader;

import com.papel.imdb_clone.util.DataFileLoader;
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
        
        // Use our utility class to handle the file loading
        try {
            // First try with the filename as is
            return DataFileLoader.getResourceAsStream(filename);
        } catch (FileNotFoundException e) {
            logger.warn("Could not load file '{}' directly, trying with 'data/' prefix", filename);
            
            // If that fails, try with 'data/' prefix
            if (!filename.startsWith("data/")) {
                return DataFileLoader.getResourceAsStream("data/" + filename);
            }
            throw e;
        }
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
