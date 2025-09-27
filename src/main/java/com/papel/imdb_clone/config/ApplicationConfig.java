package com.papel.imdb_clone.config;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Application configuration management class.
 */
public class ApplicationConfig {

    private static final Logger LOGGER = Logger.getLogger(ApplicationConfig.class.getName()); // Logger for logging
    private static volatile ApplicationConfig instance; // Singleton instance
    private final Properties properties; // Properties for configuration

    // Default configuration values
    private static final String DEFAULT_APP_TITLE = "IMDb Clone JavaFX";
    private static final String APP_PROPERTIES_FILE = "/application.properties";
    private static final String DEFAULT_APP_VERSION = "1.0";
    /**
     * Private constructor to prevent direct instantiation.
     * Loads the configuration during object creation
     */
    private ApplicationConfig() {
        this.properties = new Properties();
        this.properties.put("app.title", DEFAULT_APP_TITLE);
        this.properties.put("app.version", DEFAULT_APP_VERSION);
        loadConfiguration();
    }

    /**
     * Returns the singleton instance of ApplicationConfig.
     * Uses double-checked locking for thread safety.
     * @return The singleton instance of ApplicationConfig
     */
    public static ApplicationConfig getInstance() {
        ApplicationConfig result = instance;
        if (result == null) {
            // double-checked locking
            synchronized (ApplicationConfig.class) {
                result = instance;
                if (result == null) {
                    instance = result = new ApplicationConfig();

                }
            }
        }
        return result;
    }

    /**
     * Loads configuration from application.properties and system properties.
     * System properties take precedence over file properties.
     */
    private void loadConfiguration() {
        loadPropertiesFromFile();
        loadSystemProperties();
    }

    /**
     * Loads properties from the application.properties file.
     * Logs a warning if the file cannot be loaded.
     */
    private void loadPropertiesFromFile() {
        try (InputStream is = getClass().getResourceAsStream(APP_PROPERTIES_FILE)) {
            if (is != null) {
                properties.load(is);
                LOGGER.log(Level.INFO, "Successfully loaded properties from {0}", APP_PROPERTIES_FILE);
            } else {
                LOGGER.log(Level.WARNING, "Could not find {0} in the classpath", APP_PROPERTIES_FILE);
            }
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, "Error loading properties from " + APP_PROPERTIES_FILE, e);
        }
    }

    /**
     * Loads system properties, which will override any existing properties
     * with the same keys.
     */
    private void loadSystemProperties() {
        properties.putAll(System.getProperties());
        if (LOGGER.isLoggable(Level.FINER)) {
            LOGGER.finer("System properties loaded and merged with existing properties");
        }
    }

    /**
     * Gets the application title from configuration.
     * @return The configured application title or the default title if not specified
     */
    public String getAppTitle() {
        return properties.getProperty("app.title", DEFAULT_APP_TITLE);
    }

}