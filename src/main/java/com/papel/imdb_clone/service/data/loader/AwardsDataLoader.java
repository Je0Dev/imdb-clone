package com.papel.imdb_clone.service.data.loader;

import com.papel.imdb_clone.exceptions.FileParsingException;
import com.papel.imdb_clone.model.content.Movie;
import com.papel.imdb_clone.model.content.Series;
import com.papel.imdb_clone.repository.impl.InMemoryMovieRepository;
import com.papel.imdb_clone.service.content.SeriesService;
import com.papel.imdb_clone.service.data.base.BaseDataLoader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Calendar;
import java.util.Date;
import java.util.Optional;

/**
 * Loads awards and box office data from files.
 */
public class AwardsDataLoader extends BaseDataLoader {
    private static final Logger logger = LoggerFactory.getLogger(AwardsDataLoader.class);
    private final InMemoryMovieRepository movieRepository;
    private final SeriesService seriesService;

    /**
     * Constructor for AwardsDataLoader
     * @param movieRepository the movie repository
     * @param seriesService the series service
     */
    public AwardsDataLoader(InMemoryMovieRepository movieRepository, SeriesService seriesService) {
        this.movieRepository = movieRepository;
        this.seriesService = seriesService;
    }

    /**
     * Loads awards and box office data from the specified file.
     *
     * @param filename the name of the file to load
     * @throws IOException if there is an error reading the file
     */
    public void load(String filename) throws IOException {
        logger.info("Loading awards and box office data from {}", filename);
        int count = 0;
        int errors = 0;
        int notFound = 0;
        int lineNumber = 0;

        try (InputStream inputStream = getResourceAsStream(filename);
             BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream))) {

            validateInput(inputStream, filename);
            String line;

            /*
              Read each line of the file
             */
            while ((line = reader.readLine()) != null) {
                lineNumber++;
                if (line.trim().isEmpty() || line.trim().startsWith("#")) {
                    continue;
                }

                try {
                    /*
                     * Parse CSV line into parts
                     */
                    String[] parts = parseCSVLine(line);
                    if (parts.length >= 4) {
                        String contentType = parts[0].trim();
                        String title = parts[1].trim();
                        String yearStr = parts[2].trim();
                        String awards = parts[3].trim();
                        String boxOffice = parts.length > 4 ? parts[4].trim() : null;
                        String nominations = parts.length > 5 ? parts[5].trim() : null;

                        // Handle year ranges by taking the start year
                        int year;
                        if (yearStr.contains("-")) {
                            year = Integer.parseInt(yearStr.split("-")[0].trim());
                        } else {
                            year = Integer.parseInt(yearStr);
                        }

                        boolean found = false;

                        if ("movie".equalsIgnoreCase(contentType)) {
                            // Find movie by title and year
                            // Get the calendar instance to extract year from Date
                            Calendar cal = Calendar.getInstance();
                            Optional<Movie> movieOpt = movieRepository.findByTitle(title)
                                    .stream()
                                    .filter(m -> {
                                        Date releaseDate = m.getReleaseDate();
                                        if (releaseDate == null) return false;
                                        cal.setTime(releaseDate);
                                        return cal.get(Calendar.YEAR) == year;
                                    })
                                    .findFirst();

                            if (movieOpt.isPresent()) {
                                Movie movie = movieOpt.get();

                                if (!awards.isEmpty()) {
                                    movie.setAwards(awards);
                                }
                                if (boxOffice != null && !boxOffice.isEmpty()) {
                                    movie.setBoxOffice(boxOffice);
                                }
                                movie = movieRepository.save(movie);
                                found = true;
                                count++;
                            }
                        } else if ("series".equalsIgnoreCase(contentType)) {
                            // Find series by title and year
                            Optional<Series> seriesOpt = seriesService.getAll().stream()
                                    .filter(s -> s.getTitle().equalsIgnoreCase(title) &&
                                            s.getStartYear() == year)
                                    .findFirst();

                            if (seriesOpt.isPresent()) {
                                Series series = seriesOpt.get();
                                // Series class has setAwards and setNominations methods
                                if (!awards.isEmpty()) {
                                    series.setAwards(awards);
                                }
                                if (nominations != null && !nominations.isEmpty()) {
                                    series.setNominations(nominations);
                                }
                                series = seriesService.save(series);
                                found = true;
                                count++;
                            }
                        }

                        if (!found) {
                            logger.debug("{} '{}' ({}) not found",
                                    contentType, title, year);
                            notFound++;
                        }
                    } else {
                        logger.warn("Invalid awards data format at line {}: {}", lineNumber, line);
                        errors++;
                    }
                } catch (Exception e) {
                    logger.error("Error processing line {}: {}", lineNumber, e.getMessage());
                    errors++;
                }
            }

            // Log the result
            logger.info("Successfully updated awards for {} items ({} not found, {} errors, {} total lines)",
                    count, notFound, errors, lineNumber);

        } catch (IOException e) {
            // Log error and rethrow
            logger.error("Error reading awards file: {}", e.getMessage(), e);
            throw new FileParsingException("Error reading awards file: " + e.getMessage());
        }
    }
}
