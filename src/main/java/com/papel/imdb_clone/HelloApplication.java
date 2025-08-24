package com.papel.imdb_clone;
//change when necessary to use another gui

import com.papel.imdb_clone.gui.ImprovedMovieApp;
import javafx.application.Application;

/**
 * Main application entry point for the IMDB Clone application.
 */
public class HelloApplication {

    /**
     * Main method that launches the JavaFX application.
     *
     * @param args Command line arguments (not used)
     */
    public static void main(String[] args) {
        Application.launch(ImprovedMovieApp.class, args);
    }
}