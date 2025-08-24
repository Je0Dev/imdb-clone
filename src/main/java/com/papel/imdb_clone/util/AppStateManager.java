package com.papel.imdb_clone.util;

import com.papel.imdb_clone.model.User;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Properties;

/**
 * Centralized manager for application state including session and settings.
 * Combines functionality from the previous SessionManager and SettingsManager.
 */
public class AppStateManager {
    // Session related constants and fields
    public static final String EVT_USER_LOGGED_IN = "USER_LOGGED_IN";
    public static final String EVT_USER_LOGGED_OUT = "USER_LOGGED_OUT";

    private static final AppStateManager INSTANCE = new AppStateManager();

    // Session state
    private volatile User currentUser;
    private volatile String sessionToken;

    // Settings related constants and fields
    private static final String SETTINGS_DIR = ".imdb_clone";
    private static final String SETTINGS_FILE = "settings.properties";

    // Setting keys
    private static final String KEY_THEME = "theme";
    private static final String KEY_FONT_SIZE = "fontSize";
    private static final String KEY_REDUCED_MOTION = "reducedMotion";
    private static final String KEY_WINDOW_WIDTH = "windowWidth";
    private static final String KEY_WINDOW_HEIGHT = "windowHeight";
    private static final String KEY_LAST_TAB_INDEX = "lastTabIndex";

    private final Path settingsFile;
    private final Properties properties = new Properties();

    private AppStateManager() {

        // Initialize settings file path
        String userHome = System.getProperty("user.home");
        Path dir = Paths.get(userHome, SETTINGS_DIR);
        this.settingsFile = dir.resolve(SETTINGS_FILE);

        // Load settings
        try {
            if (!Files.exists(dir)) {
                Files.createDirectories(dir);
            }
            if (Files.exists(settingsFile)) {
                try (var in = Files.newInputStream(settingsFile)) {
                    properties.load(in);
                }
            } else {
                // Set default values
                setDefaultSettings();
                saveSettings();
            }
        } catch (IOException e) {
            // Fallback to default settings if loading fails
            setDefaultSettings();
        }
    }

    private void setDefaultSettings() {
        properties.setProperty(KEY_THEME, "classic");
        properties.setProperty(KEY_FONT_SIZE, "13");
        properties.setProperty(KEY_REDUCED_MOTION, "false");
        properties.setProperty(KEY_WINDOW_WIDTH, "1000");
        properties.setProperty(KEY_WINDOW_HEIGHT, "700");
        properties.setProperty(KEY_LAST_TAB_INDEX, "0");
    }

    // ========== Session Management ==========

    public static AppStateManager getInstance() {
        return INSTANCE;
    }

    public synchronized void setSession(User user, String token) {
        this.currentUser = user;
        this.sessionToken = token;
        AppEventBus.getInstance().publish(EVT_USER_LOGGED_IN, user);
    }

    public synchronized void clearSession() {
        this.currentUser = null;
        this.sessionToken = null;
        AppEventBus.getInstance().publish(EVT_USER_LOGGED_OUT, null);
    }


    /**
     * Updates the current user in the application state.
     * This is a convenience method that updates the current user
     * without changing the session token.
     *
     * @param user The updated user object
     */
    public void setCurrentUser(User user) {
        this.currentUser = user;
        // Notify listeners that user data was updated
        if (user != null) {
            AppEventBus.getInstance().publish(EVT_USER_LOGGED_IN, user);
        } else {
            AppEventBus.getInstance().publish(EVT_USER_LOGGED_OUT, null);
        }
    }


    // ========== Settings Management ==========

    private void saveSettings() {
        try (var out = Files.newOutputStream(settingsFile)) {
            properties.store(out, "IMDb Clone Settings");
        } catch (IOException ignored) {
            // Non-critical failure
        }
    }


}
