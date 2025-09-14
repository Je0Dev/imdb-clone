package com.papel.imdb_clone.controllers;

import com.papel.imdb_clone.model.Actor;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.image.ImageView;
import javafx.scene.layout.VBox;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public class ContentDetailsController {
    @FXML
    private Label titleLabel;
    @FXML
    private Label yearLabel;
    @FXML
    private Label ratingLabel;
    @FXML
    private Label genreLabel;
    @FXML
    private Label boxOfficeLabel;
    @FXML
    private Label awardsLabel;
    @FXML
    private Label castLabel;
    @FXML
    private VBox root;

    private String title;
    private String year;
    private String rating;
    private String genre;
    private String boxOffice;
    private List<String> awards;
    private List<Actor> cast;

    @FXML
    public void initialize() {
        // Set up the UI with the content details
        if (title != null) {
            updateUI();
        }
    }

    public void setContentDetails(String title, String year, String rating, String genre,
                                  String boxOffice, List<String> awards, List<Actor> cast) {
        // Set the content details
        this.title = title;
        this.year = year;
        this.rating = rating;
        this.genre = genre;
        this.boxOffice = boxOffice;
        this.awards = awards;
        this.cast = cast;

        if (titleLabel != null) {
            updateUI();
        }
    }

    private void updateUI() {
        // Update the UI with the content detailsq
        if (titleLabel != null) titleLabel.setText(title != null ? title : "No title available");
        if (yearLabel != null) yearLabel.setText(year != null ? "(" + year + ")" : "(Year not available)");
        if (ratingLabel != null) ratingLabel.setText(rating != null ? rating : "N/A");
        if (genreLabel != null) genreLabel.setText(genre != null ? genre : "Genre not specified");

        // Handle box office
        String boxOfficeText = "Not available";
        if (boxOffice != null && !boxOffice.trim().isEmpty() && !boxOffice.equalsIgnoreCase("N/A")) {
            boxOfficeText = boxOffice;
        }
        if (boxOfficeLabel != null) boxOfficeLabel.setText(boxOfficeText);

        // Handle awards
        String awardsText = "No major awards";
        if (awards != null && !awards.isEmpty()) {
            awardsText = String.join(", ", awards);
        }
        if (awardsLabel != null) awardsLabel.setText(awardsText);

        // Handle cast
        String castText = "Cast information not available";
        if (cast != null && !cast.isEmpty()) {
            // Create a comma separated list of actor names
            castText = cast.stream()
                    .filter(Objects::nonNull)
                    .map(actor -> actor.getFirstName() + " " + actor.getLastName())
                    .collect(Collectors.joining(", "));
        }
        // Set the cast label
        if (castLabel != null) castLabel.setText(castText);
    }

    @FXML
    private void goBack() {
        com.papel.imdb_clone.service.NavigationService.getInstance().goBack();
    }
}
