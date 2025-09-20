package com.papel.imdb_clone.service.data.base;

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
     * Parses a CSV line, handling quoted fields and special characters.
     * Supports:
     * - Quoted fields containing commas
     * - Escaped quotes ("")
     * - Trims whitespace from fields
     * - Skips empty lines and comments
     *
     * @param line the line to parse
     * @return array of parsed fields, or empty array for empty/comment lines
     */
    protected String[] parseCSVLine(String line) {
        // Handle null or empty input
        if (line == null || (line = line.trim()).isEmpty() || line.startsWith("#")) {
            return new String[0];
        }

        List<String> fields = new ArrayList<>();
        StringBuilder currentField = new StringBuilder();
        boolean inQuotes = false;
        
        // Track if we're at the start of a field
        boolean atStart = true;

        for (int i = 0; i < line.length(); i++) {
            char c = line.charAt(i);
            
            // Handle quoted fields
            if (c == '"') {
                if (inQuotes) {
                    // Check for escaped quote ("")
                    if (i + 1 < line.length() && line.charAt(i + 1) == '"') {
                        currentField.append('"');
                        i++; // Skip the next quote
                        continue;
                    }
                    //end of quoted field
                    inQuotes = false;
                    continue;
                } else if (atStart) {
                    //start of quoted field
                    inQuotes = true;
                    atStart = false;
                    continue;
                }
            }
            
            // Handle field separator (comma) when not in quotes
            if (c == ',' && !inQuotes) {
                fields.add(currentField.toString().trim());
                currentField.setLength(0);
                atStart = true;
                continue;
            }
            
            // Add character to current field
            currentField.append(c);
            atStart = false;
        }
        
        // Add the last field
        fields.add(currentField.toString().trim());
        
        // Handle case where line ends with a comma (add empty field)
        if (line.endsWith(",")) {
            fields.add("");
        }

        //return array of fields
        return fields.toArray(new String[0]);
    }
}
