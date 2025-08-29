package com.papel.imdb_clone.utils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

/**
 * Utility class for loading content data from the data file.
 */
public class ContentDataLoader {
    private static final String DATA_FILE = "/data/awards_boxoffice_updated.txt";
    private static Map<String, ContentData> contentDataMap;

    /**
     * Represents the data for a single content item (movie/series).
     */
    public static class ContentData {
        private final String title;
        private final String year;
        private final String rating;
        private final String genre;
        private final String boxOffice;
        private final String awards;

        public ContentData(String title, String year, String rating, String genre,
                           String boxOffice, String awards) {
            this.title = title;
            this.year = year;
            this.rating = rating;
            this.genre = genre;
            this.boxOffice = boxOffice;
            this.awards = awards;
        }

        public String getTitle() {
            return title;
        }

        public String getYear() {
            return year;
        }

        public String getRating() {
            return rating;
        }

        public String getGenre() {
            return genre;
        }

        public String getBoxOffice() {
            return boxOffice;
        }

        public String getAwards() {
            return awards;
        }
    }

    /**
     * Loads content data from the data file.
     *
     * @throws IOException If there's an error reading the data file
     */
    private static void loadData() throws IOException {
        contentDataMap = new HashMap<>();

        try (InputStream is = ContentDataLoader.class.getResourceAsStream(DATA_FILE);
             BufferedReader reader = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8))) {

            String line;
            // Skip header line
            reader.readLine();

            while ((line = reader.readLine()) != null) {
                if (line.trim().isEmpty() || line.startsWith("#")) {
                    continue; // Skip empty lines and comments
                }

                String[] parts = line.split(",", 7); // Split into max 7 parts
                if (parts.length < 7) continue; // Skip malformed lines

                String title = parts[1].trim();
                String year = parts[2].trim();
                String boxOffice = parts[3].replaceAll("^\"|\"$", "").trim();
                String awards = parts[4].replaceAll("^\"|\"$", "").trim();
                String rating = parts[5].trim();
                String genre = parts[6].trim();

                contentDataMap.put(title.toLowerCase(),
                        new ContentData(title, year, rating, genre, boxOffice, awards));
            }
        }
    }

    /**
     * Gets content data for a specific title.
     *
     * @param title The title of the content to look up
     * @return The ContentData object, or null if not found
     */
    public static ContentData getContentData(String title) {
        if (contentDataMap == null) {
            try {
                loadData();
            } catch (IOException e) {
                e.printStackTrace();
                return null;
            }
        }
        return contentDataMap.get(title.toLowerCase());
    }
}
