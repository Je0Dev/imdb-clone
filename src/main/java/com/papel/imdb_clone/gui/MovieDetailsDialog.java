package com.papel.imdb_clone.gui;

import com.papel.imdb_clone.enums.Genre;
import com.papel.imdb_clone.model.Content;
import com.papel.imdb_clone.model.Movie;
import com.papel.imdb_clone.model.Series;
import javafx.geometry.Insets;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;

/**
 * Enhanced dialog for displaying detailed movie information
 */
public class MovieDetailsDialog {

    /**
     * Creates a basic info grid for a Movie
     */
    private static GridPane createBasicInfoGrid(Movie movie) {
        GridPane grid = new GridPane();
        grid.setHgap(15);
        grid.setVgap(10);
        grid.setPadding(new Insets(10));
        grid.setStyle("-fx-background-color: #f8f9fa; -fx-background-radius: 5px;");

        int row = 0;

        // Year
        addInfoRow(grid, row++, "Release Year:", String.valueOf(movie.getYear().getYear() + 1900));

        // Genre
        addInfoRow(grid, row++, "Genre:", movie.getGenre().toString());

        // Director
        addInfoRow(grid, row++, "Director:", movie.getDirector() != null ? movie.getDirector() : "Unknown");

        // Duration
        if (movie.getDuration() > 0) {
            addInfoRow(grid, row++, "Duration:", movie.getDuration() + " minutes");
        }

        // IMDB Rating
        addInfoRow(grid, row++, "IMDB Rating:", String.format("%.1f/10", movie.getImdbRating()));

        // User Rating
        String userRating = movie.getUserRating() != null
                ? String.format("%.1f/10", movie.getUserRating())
                : "Not rated";
        addInfoRow(grid, row++, "Your Rating:", userRating);

        // MPAA Rating
        if (movie.getMpaaRating() != null && !movie.getMpaaRating().isEmpty()) {
            addInfoRow(grid, row++, "MPAA Rating:", movie.getMpaaRating());
        }

        return grid;
    }

    /**
     * Creates a basic info grid for a Series
     */
    private static GridPane createBasicInfoGrid(Series series) {
        GridPane grid = new GridPane();
        grid.setHgap(15);
        grid.setVgap(10);
        grid.setPadding(new Insets(10));
        grid.setStyle("-fx-background-color: #f8f9fa; -fx-background-radius: 5px;");

        int row = 0;

        // Year
        addInfoRow(grid, row++, "First Aired:", String.valueOf(series.getYear().getYear() + 1900));

        // Genre
        addInfoRow(grid, row++, "Genre:", series.getGenre().toString());

        // Number of Seasons
        addInfoRow(grid, row++, "Seasons:", String.valueOf(series.getSeasons().size()));

        // Total Episodes
        int totalEpisodes = series.getSeasons().stream()
                .mapToInt(season -> season.getEpisodes().size())
                .sum();
        addInfoRow(grid, row++, "Total Episodes:", String.valueOf(totalEpisodes));

        // User Rating (for Series, we only show user rating, not IMDB rating)
        String userRating = series.getUserRating() != null
                ? String.format("%.1f/10", series.getUserRating())
                : "Not rated";
        addInfoRow(grid, row++, "User Rating:", userRating);

        return grid;
    }

    private static VBox createDescriptionSection(Content content) {
        VBox section = new VBox(8);
        String summary = content.getSummary();

        if (summary != null && !summary.trim().isEmpty()) {
            Label descLabel = new Label("Summary");
            descLabel.setFont(Font.font("System", FontWeight.BOLD, 14));
            descLabel.setStyle("-fx-text-fill: #2c3e50;");

            TextArea descArea = new TextArea(summary);
            descArea.setEditable(false);
            descArea.setWrapText(true);
            descArea.setPrefRowCount(4);
            descArea.setStyle("-fx-background-color: #ffffff; -fx-border-color: #dee2e6; -fx-border-radius: 3px;");

            section.getChildren().addAll(descLabel, descArea);
        }

        return section;
    }

    /**
     * Creates additional info section for a Movie
     */
    private static VBox createAdditionalInfoSection(Movie movie) {
        VBox section = new VBox(10);

        // Actors section
        if (!movie.getActors().isEmpty()) {
            Label actorsLabel = new Label("Cast");
            actorsLabel.setFont(Font.font("System", FontWeight.BOLD, 14));
            actorsLabel.setStyle("-fx-text-fill: #2c3e50;");

            VBox actorsList = new VBox(3);
            actorsList.setStyle("-fx-background-color: #f8f9fa; -fx-padding: 10px; -fx-background-radius: 3px;");

            movie.getActors().stream()
                    .limit(10) // Limit to first 10 actors
                    .forEach(actor -> {
                        Label actorLabel = new Label("• " + actor.getFullName());
                        actorLabel.setStyle("-fx-text-fill: #495057;");
                        actorsList.getChildren().add(actorLabel);
                    });

            section.getChildren().addAll(actorsLabel, actorsList);
        }

        // Genres section (if multiple genres)
        if (!movie.getGenres().isEmpty() && movie.getGenres().size() > 1) {
            Label genresLabel = new Label("All Genres");
            genresLabel.setFont(Font.font("System", FontWeight.BOLD, 14));
            genresLabel.setStyle("-fx-text-fill: #2c3e50;");

            HBox genresBox = new HBox(5);
            for (Genre genre : movie.getGenres()) {
                Label genreTag = new Label(genre.name());
                genreTag.setStyle("-fx-background-color: #007bff; -fx-text-fill: white; " +
                        "-fx-padding: 3px 8px; -fx-background-radius: 12px; -fx-font-size: 11px;");
                genresBox.getChildren().add(genreTag);
            }

            section.getChildren().addAll(genresLabel, genresBox);
        }

        // Awards section
        if (!movie.getAwards().isEmpty()) {
            Label awardsLabel = new Label("Awards");
            awardsLabel.setFont(Font.font("System", FontWeight.BOLD, 14));
            awardsLabel.setStyle("-fx-text-fill: #2c3e50;");

            VBox awardsList = new VBox(3);
            awardsList.setStyle("-fx-background-color: #fff3cd; -fx-padding: 10px; -fx-background-radius: 3px;");

            movie.getAwards().forEach(award -> {
                Label awardLabel = new Label("🏆 " + award);
                awardLabel.setStyle("-fx-text-fill: #856404;");
                awardsList.getChildren().add(awardLabel);
            });

            section.getChildren().addAll(awardsLabel, awardsList);
        }

        // Box Office section
        if (movie.getBoxOffice() != null && !movie.getBoxOffice().isEmpty()) {
            Label boxOfficeLabel = new Label("Box Office");
            boxOfficeLabel.setFont(Font.font("System", FontWeight.BOLD, 14));
            boxOfficeLabel.setStyle("-fx-text-fill: #2c3e50;");

            Label boxOfficeValue = new Label("💰 " + movie.getBoxOffice());
            boxOfficeValue.setStyle("-fx-text-fill: #28a745; -fx-font-weight: bold;");

            section.getChildren().addAll(boxOfficeLabel, boxOfficeValue);
        }

        // Production Companies
        if (!movie.getProductionCompanies().isEmpty()) {
            Label companiesLabel = new Label("Production Companies");
            companiesLabel.setFont(Font.font("System", FontWeight.BOLD, 14));
            companiesLabel.setStyle("-fx-text-fill: #2c3e50;");

            VBox companiesList = new VBox(3);
            companiesList.setStyle("-fx-background-color: #e9ecef; -fx-padding: 10px; -fx-background-radius: 3px;");

            movie.getProductionCompanies().forEach(company -> {
                Label companyLabel = new Label("🏢 " + company);
                companyLabel.setStyle("-fx-text-fill: #495057;");
                companiesList.getChildren().add(companyLabel);
            });

            section.getChildren().addAll(companiesLabel, companiesList);
        }

        return section;
    }

    /**
     * Creates additional info section for a Series
     */
    private static VBox createAdditionalInfoSection(Series series) {
        VBox section = new VBox(10);

        // Awards section
        if (!series.getAwards().isEmpty()) {
            Label awardsLabel = new Label("Awards");
            awardsLabel.setFont(Font.font("System", FontWeight.BOLD, 14));
            awardsLabel.setStyle("-fx-text-fill: #2c3e50;");

            VBox awardsList = new VBox(3);
            awardsList.setStyle("-fx-background-color: #fff3cd; -fx-padding: 10px; -fx-background-radius: 3px;");

            series.getAwards().forEach(award -> {
                Label awardLabel = new Label("🏆 " + award);
                awardLabel.setStyle("-fx-text-fill: #856404;");
                awardsList.getChildren().add(awardLabel);
            });

            section.getChildren().addAll(awardsLabel, awardsList);
        }

        // Box Office section
        if (series.getBoxOffice() != null && !series.getBoxOffice().isEmpty()) {
            Label boxOfficeLabel = new Label("Box Office");
            boxOfficeLabel.setFont(Font.font("System", FontWeight.BOLD, 14));
            boxOfficeLabel.setStyle("-fx-text-fill: #2c3e50;");

            Label boxOfficeValue = new Label("💰 " + series.getBoxOffice());
            boxOfficeValue.setStyle("-fx-text-fill: #28a745; -fx-font-weight: bold;");

            section.getChildren().addAll(boxOfficeLabel, boxOfficeValue);
        }

        return section;
    }

    /**
     * Helper method to add a row to the info grid
     */
    private static void addInfoRow(GridPane grid, int row, String label, String value) {
        Label labelNode = new Label(label);
        labelNode.setFont(Font.font("System", FontWeight.BOLD, 12));
        labelNode.setStyle("-fx-text-fill: #495057;");

        Label valueNode = new Label(value);
        valueNode.setStyle("-fx-text-fill: #212529;");
        valueNode.setWrapText(true);

        grid.add(labelNode, 0, row);
        grid.add(valueNode, 1, row);

        // Make value column grow
        GridPane.setHgrow(valueNode, Priority.ALWAYS);
    }
}