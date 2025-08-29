package com.papel.imdb_clone.controllers;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.VBox;

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
    private ImageView posterImage;
    @FXML
    private VBox root;

    private String title;
    private String year;
    private String rating;
    private String genre;
    private String boxOffice;
    private String awards;
    private String cast;

    private ContentDetailsController NavigationService;

    @FXML
    public void initialize() {
        // Set up the UI with the content details
        if (title != null) {
            updateUI();
        }
    }

    public void setContentDetails(String title, String year, String rating, String genre,
                                  String boxOffice, String awards, String cast) {
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
        titleLabel.setText(title);
        yearLabel.setText("(" + year + ")");
        ratingLabel.setText(rating);
        genreLabel.setText(genre);
        boxOfficeLabel.setText(boxOffice.equals("N/A") ? "Not available" : boxOffice);
        awardsLabel.setText(awards.equals("N/A") ? "No major awards" : awards);
        castLabel.setText(cast != null ? cast : "Cast information not available");

    }

    @FXML
    private void goBack() {
        NavigationService.getInstance().goBack();
    }

    private ContentDetailsController getInstance() {
        return this;
    }
}
