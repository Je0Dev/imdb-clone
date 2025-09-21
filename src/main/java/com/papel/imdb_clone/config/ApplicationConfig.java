package com.papel.imdb_clone.config;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Application configuration management class.
 *
 */
public class ApplicationConfig {
    private static final Logger LOGGER = Logger.getLogger(ApplicationConfig.class.getName());
    private static volatile ApplicationConfig instance;
    private final Properties properties;

    // Default configuration values
    private static final String DEFAULT_APP_TITLE = "IMDb Clone JavaFX";
    private static final String APP_PROPERTIES_FILE = "/application.properties";

    /**
     * Private constructor to prevent direct instantiation.
     * Loads the configuration during object creation.
     */
    private ApplicationConfig() {
        this.properties = new Properties();
        loadConfiguration();
    }

    /**
     * Returns the singleton instance of ApplicationConfig.
     * Uses double-checked locking for thread safety.
     *
     * @return The singleton instance of ApplicationConfig
     */
    public static ApplicationConfig getInstance() {
        ApplicationConfig result = instance;
        if (result == null) {
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
     *
     * @return The configured application title or the default title if not specified
     */
    public String getAppTitle() {
        return properties.getProperty("app.title", DEFAULT_APP_TITLE);
    }

    /**
     * Gets a configuration property as a String.
     *
     * @param key The property key
     * @param defaultValue The default value to return if the key is not found
     * @return The property value or the default value if the key is not found
     */
    public String getProperty(String key, String defaultValue) {
        return properties.getProperty(key, defaultValue);
    }

    /**
     * Gets a configuration property as an integer.
     *
     * @param key The property key
     * @param defaultValue The default value to return if the key is not found or not a valid integer
     * @return The property value as an integer or the default value if the key is not found or invalid
     */
    public int getIntProperty(String key, int defaultValue) {
        try {
            return Integer.parseInt(properties.getProperty(key, String.valueOf(defaultValue)));
        } catch (NumberFormatException e) {
            LOGGER.log(Level.WARNING, "Invalid integer value for property: {0}", key);
            return defaultValue;
        }
    }

    /**
     * Gets a configuration property as a boolean.
     *
     * @param key The property key
     * @param defaultValue The default value to return if the key is not found
     * @return The property value as a boolean or the default value if the key is not found
     */
    public boolean getBooleanProperty(String key, boolean defaultValue) {
        String value = properties.getProperty(key);
        if (value == null) {
            return defaultValue;
        }
        return Boolean.parseBoolean(value);
    }
}