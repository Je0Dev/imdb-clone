package com.papel.imdb_clone.config;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * Application configuration management.
 * Loads configuration from properties files and provides typed access to configuration values.
 */
public class ApplicationConfig {
    private static ApplicationConfig instance;
    private final Properties properties;

    // Default configuration values
    private static final String DEFAULT_APP_TITLE = "IMDB Clone - Movie & Series Manager";
    private static final double DEFAULT_MIN_WIDTH = 5000.0;
    private static final double DEFAULT_MIN_HEIGHT = 3000.0;

    // Default data file paths
    private ApplicationConfig() {
        properties = new Properties();
        loadConfiguration();
    }
    // Singleton pattern
    public static synchronized ApplicationConfig getInstance() {
        if (instance == null) {
            instance = new ApplicationConfig();
        }
        // Return the instance, the instance being created only once and reused each time
        return instance;
    }

    // Load configuration from properties files
    private void loadConfiguration() {
        // Load from application.properties if it exists
        try (InputStream is = getClass().getResourceAsStream("/application.properties")) {
            if (is != null) {
                properties.load(is);
            }
        } catch (IOException e) {
            System.err.println("Could not load application.properties: " + e.getMessage());
        }

        // Load system properties (these override file properties)
        properties.putAll(System.getProperties());
    }

    // Application settings
    public String getAppTitle() {
        return properties.getProperty(
                "app.title", DEFAULT_APP_TITLE);
    }


}