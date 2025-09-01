package com.papel.imdb_clone.util;

import java.io.*;
import java.net.URL;
import java.nio.file.*;
import java.util.Objects;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DataFileLoader {
    private static final Logger logger = LoggerFactory.getLogger(DataFileLoader.class);
    private static final String[] SEARCH_PATHS = {
        "src/main/resources/data/",
        "target/classes/data/",
        "data/",
        "../data/"
    };

    public static InputStream getResourceAsStream(String filename) throws IOException {
        Objects.requireNonNull(filename, "Filename cannot be null");
        
        // Try to load from classpath first
        InputStream is = DataFileLoader.class.getClassLoader().getResourceAsStream("data/" + filename);
        if (is != null) {
            logger.debug("Loaded {} from classpath", filename);
            return is;
        }

        // Try different file system locations
        for (String path : SEARCH_PATHS) {
            File file = new File(path + filename);
            if (file.exists() && !file.isDirectory()) {
                logger.debug("Loaded {} from filesystem: {}", filename, file.getAbsolutePath());
                return new FileInputStream(file);
            }
        }

        // Try absolute path as last resort
        File file = new File(filename);
        if (file.exists() && !file.isDirectory()) {
            logger.debug("Loaded {} using absolute path: {}", filename, file.getAbsolutePath());
            return new FileInputStream(file);
        }

        throw new FileNotFoundException("Could not find data file: " + filename + " in any of the searched locations");
    }

    public static String getResourcePath(String filename) throws IOException {
        // Try to get the resource URL first
        URL url = DataFileLoader.class.getClassLoader().getResource("data/" + filename);
        if (url != null) {
            return url.getPath();
        }

        // Try different file system locations
        for (String path : SEARCH_PATHS) {
            File file = new File(path + filename);
            if (file.exists() && !file.isDirectory()) {
                return file.getAbsolutePath();
            }
        }

        throw new FileNotFoundException("Could not find data file: " + filename);
    }
}
