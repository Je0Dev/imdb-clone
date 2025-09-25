package com.papel.imdb_clone.service.content;

import com.papel.imdb_clone.enums.Ethnicity;
import com.papel.imdb_clone.enums.Genre;
import com.papel.imdb_clone.model.content.Series;
import com.papel.imdb_clone.model.people.Actor;
import com.papel.imdb_clone.util.FileUtils;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

/**
 * Service class for managing Series content.
 */
public class SeriesService extends BaseContentService<Series> {
    private static final Logger logger = Logger.getLogger(SeriesService.class.getName());
    private static SeriesService instance;
    
    private SeriesService() {
        super(Series.class);
    }

    //return singleton instance of SeriesService which means that only one instance of SeriesService can exist at a time
    public static synchronized SeriesService getInstance() {
        if (instance == null) {
            instance = new SeriesService();
        }
        return instance;
    }

    /**
     * Loads series from the series_updated.txt file.
     * The expected format is: Title,Genre,Seasons,Year,Rating,Director,Actor1;Actor2;...
     */
    @Override
    protected void loadFromFile() {
        String filePath = "src/main/resources/data/content/series_updated.txt";
        List<String> lines = FileUtils.readLines(filePath);
        
        if (lines.isEmpty()) {
            logger.warning("No series found in file: " + filePath);
            return;
        }
        
        // Skip header line if it exists
        if (!lines.isEmpty() && lines.get(0).startsWith("#")) {
            lines.remove(0);
        }
        
        for (String line : lines) {
            try {
                // Skip empty lines and comments
                if (line.trim().isEmpty() || line.trim().startsWith("#")) {
                    continue;
                }
                
                String[] parts = line.split(",", -1); // -1 to keep empty values
                if (parts.length < 8) {
                    logger.warning("Skipping invalid series line (not enough fields): " + line);
                    continue;
                }

                // Parse the line parts with proper indices
                String title = parts[0].trim();
                Genre genre = Genre.valueOf(parts[1].trim().toUpperCase().replace(" ", "_"));
                int seasons = Integer.parseInt(parts[2].trim());
                int startYear = Integer.parseInt(parts[3].trim());
                
                // Parse end year (5th field, index 4)
                Integer endYear = null;
                String endYearStr = parts[4].trim();
                if (!endYearStr.isEmpty() && !endYearStr.equals("-")) {
                    try {
                        endYear = Integer.parseInt(endYearStr);
                        logger.info("Set end year for " + title + " to: " + endYear);
                    } catch (NumberFormatException e) {
                        logger.warning("Invalid end year format for series: " + title + ", value: " + endYearStr);
                    }
                } else {
                    logger.info("No end year specified for series: " + title);
                }

                // Parse rating (6th field, index 5)
                double rating = 0.0;
                try {
                    // In the file, the rating is at index 5 (6th field)
                    if (!parts[5].trim().isEmpty()) {
                        rating = Double.parseDouble(parts[5].trim());
                        logger.info("Set rating for " + title + " to: " + rating);
                    } else {
                        logger.warning("No rating found for series: " + title);
                        rating = 0.0;
                    }
                } catch (NumberFormatException e) {
                    logger.warning("Invalid rating format for series: " + title + 
                                ", value: " + parts[5] +
                                ", using default 0.0");
                    rating = 0.0;
                }
                
                // Director is at index 7 (8th field)
                String director = "";
                director = parts[7].trim();

                // Create series with basic info
                Series series = new Series(title);
                series.setGenre(genre);
                series.setStartYear(startYear);
                if (endYear != null) {
                    series.setEndYear(endYear);
                    logger.info("Successfully set end year for " + title + ": " + endYear);
                } else {
                    logger.info("No end year set for " + title);
                }
                series.setImdbRating(rating);
                series.setDirector(director);
                series.setSeasons(seasons);
                
                // Log the series details for debugging
                logger.info(String.format("Loaded series: %s (%d-%s), Rating: %.1f, Director: %s", 
                    title, startYear, (endYear != null ? endYear : "-"), rating, director));

                // Add actors if available
                String[] actorNames = parts[6].split(";");
                for (String actorName : actorNames) {
                    String[] nameParts = actorName.trim().split("\\s+", 2);
                    if (nameParts.length == 2) {
                        Actor actor = Actor.getInstance(nameParts[0], nameParts[1],
                                LocalDate.now(), ' ', Ethnicity.UNKNOWN);
                        series.addActor(actor);
                    }
                }

                save(series);

            } catch (Exception e) {
                logger.log(Level.WARNING, "Error processing series line: " + line, e);
            }
        }
        
        logger.info("Loaded " + contentList.size() + " series from file");
    }

    
    /**
     * Escapes commas in strings to prevent CSV parsing issues.
     * @param input The input string to escape
     * @return The escaped string
     */
    private String escapeCommas(String input) {
        if (input == null) {
            return "";
        }
        // If the input contains a comma, wrap it in quotes
        if (input.contains(",")) {
            return "\"" + input.replace("\"", "\"\"") + "\"";
        }
        return input;
    }

    @Override
    protected void initializeSampleData() {
        lock.writeLock().lock();
        try {
            // Clear existing data
            contentList.clear();
            
            // Load series from file
            loadFromFile();
            
            // If no series were loaded from file, fall back to sample data
            if (contentList.isEmpty()) {
                logger.warning("No series loaded from file, using sample data");
                
                // Sample data as fallback
                // Breaking Bad
                List<Genre> breakingBadGenres = new ArrayList<>();
                breakingBadGenres.add(Genre.CRIME);
                breakingBadGenres.add(Genre.DRAMA);
                breakingBadGenres.add(Genre.THRILLER);
                
                Series series1 = new Series(
                    "Breaking Bad",
                    Genre.CRIME,
                    9.5,
                    9.5,
                    2008
                );
                series1.setDirector("Vince Gilligan");
                series1.setGenres(breakingBadGenres);
                
                List<Actor> breakingBadActors = new ArrayList<>();
                breakingBadActors.add(Actor.getInstance("Bryan", "Cranston", LocalDate.of(1956, 3, 7), 'M', Ethnicity.CAUCASIAN));
                breakingBadActors.add(Actor.getInstance("Aaron", "Paul", LocalDate.of(1979, 8, 27), 'M', Ethnicity.CAUCASIAN));
                series1.setActors(breakingBadActors);
                series1.setEndYear(2013);
                save(series1);
                
                // Series 2 - Game of Thrones
                List<Genre> gotGenres = new ArrayList<>();
                gotGenres.add(Genre.FANTASY);
                gotGenres.add(Genre.DRAMA);
                gotGenres.add(Genre.ADVENTURE);
                
                Series series2 = new Series(
                    "Game of Thrones",
                    Genre.FANTASY,
                    9.3,
                    9.0,
                    2011
                );
                //set director
                series2.setDirector("David Benioff, D.B. Weiss");
                series2.setGenres(gotGenres);
                series2.getActors().add(Actor.getInstance("Emilia", "Clarke", LocalDate.of(1986, 10, 23), 'F', Ethnicity.CAUCASIAN));
                series2.getActors().add(Actor.getInstance("Kit", "Harington", LocalDate.of(1986, 12, 26), 'M', Ethnicity.CAUCASIAN));
                series2.getActors().add(Actor.getInstance("Peter", "Dinklage", LocalDate.of(1969, 6, 11), 'M', Ethnicity.CAUCASIAN));
                series2.setEndYear(2019);
                save(series2);
                
                // Series 3 - Stranger Things
                Series series3 = new Series("Stranger Things", Genre.SCI_FI, 8.7, 8.7, 2016);
                series3.setDirector("The Duffer Brothers");
                series3.getGenres().add(Genre.SCI_FI);
                series3.getGenres().add(Genre.HORROR);
                series3.getGenres().add(Genre.MYSTERY);
                series3.getActors().add(Actor.getInstance("Millie Bobby", "Brown", LocalDate.of(2004, 2, 19), 'F', Ethnicity.CAUCASIAN));
                series3.getActors().add(Actor.getInstance("Finn", "Wolfhard", LocalDate.of(2002, 12, 23), 'M', Ethnicity.CAUCASIAN));
                series3.setEndYear(2025);
                save(series3);
                
                logger.info("Initialized with " + contentList.size() + " sample series");
            }
        } finally {
            //unlock the write lock when done, which means that other threads can modify the list
            lock.writeLock().unlock();
        }
    }
}
