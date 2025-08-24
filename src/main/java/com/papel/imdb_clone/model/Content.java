package com.papel.imdb_clone.model;

import com.papel.imdb_clone.enums.Genre;

import java.util.*;

public abstract class Content {
    Date year;
    private String director;
    private int id;
    public String title;
    protected Genre genre;
    private final Map<Integer, Integer> userRatings; // userId -> rating
    private Double imdbRating; // IMDb rating
    private Double userRating; // Average user rating
    private String summary; // Summary/description of the content
    private Date releaseDate;
    private int startYear;
    private List<Genre> genres = new ArrayList<>();


    public Content(String title, Date year, Genre genre, String director, Map<Integer, Integer> userRatings, Double imdbRating) {
        this.title = title;
        this.year = year;
        this.genre = genre;
        this.director = director;
        this.userRatings = new HashMap<>();
        this.imdbRating = imdbRating;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public Genre getGenre() {
        return genre;
    }

    public List<Genre> getGenres() {
        return new ArrayList<>(genres);
    }

    public void setGenre(Genre genre) {
        this.genre = genre;
        if (genre != null && !genres.contains(genre)) {
            genres.add(genre);
        }
    }

    public String getDirector() {
        return director;
    }

    public void setDirector(String director) {
        this.director = director;
    }

    public Double getImdbRating() {
        return imdbRating;
    }

    /**
     * Sets the IMDb rating for this content
     *
     * @param imdbRating the IMDb rating to set (0.0 to 10.0)
     * @throws IllegalArgumentException if the rating is not between 0.0 and 10.0
     */
    public void setImdbRating(Double imdbRating) {
        if (imdbRating != null && (imdbRating < 0.0 || imdbRating > 10.0)) {
            throw new IllegalArgumentException("IMDb rating must be between 0.0 and 10.0");
        }
        this.imdbRating = imdbRating;
    }

    public Date getYear() {
        return year;
    }

    public int getReleaseYear() {
        return startYear; // Using startYear as release year
    }

    public Map<Integer, Integer> getUserRatings() {
        return new HashMap<>(userRatings);
    }


    public double calculateAverageUserRating() {
        if (userRatings.isEmpty()) {
            return 0.0;
        }
        double sum = userRatings.values().stream().mapToInt(Integer::intValue).sum();
        return sum / userRatings.size();
    }

    /**
     * Sets the title of the content
     *
     * @param title the title to set
     */
    public void setTitle(String title) {
        this.title = title;
    }

    /**
     * Gets the summary/description of the content
     *
     * @return the summary of the content
     */
    public String getSummary() {
        return summary;
    }

    /**
     * Sets the summary/description of the content
     *
     * @param summary the summary to set
     */
    public void setSummary(String summary) {
        this.summary = summary;
    }


    /**
     * Adds a genre to the content's genres list
     *
     * @param genre the genre to add
     */
    public void addGenre(Genre genre) {
        if (genre != null && !genres.contains(genre)) {
            genres.add(genre);
            // If no primary genre is set, set it
            if (this.genre == null) {
                this.genre = genre;
            }
        }
    }


    /**
     * Sets all genres for this content
     *
     * @param genres the list of genres to set
     */
    public void setGenres(List<Genre> genres) {
        this.genres = new ArrayList<>(genres);
        if (!genres.isEmpty()) {
            this.genre = genres.get(0);
        } else {
            this.genre = null;
        }
    }

    /**
     * Gets the release date of the content
     *
     * @return the release date
     */
    public Date getReleaseDate() {
        return releaseDate != null ? new Date(releaseDate.getTime()) : null;
    }

    /**
     * Sets the release date of the content
     *
     * @param releaseDate the release date to set
     */
    public void setReleaseDate(Date releaseDate) {
        this.releaseDate = releaseDate != null ? new Date(releaseDate.getTime()) : null;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        Content content = (Content) obj;
        return id == content.id &&
                Objects.equals(title, content.title) &&
                genre == content.genre &&
                Objects.equals(imdbRating, content.imdbRating);
    }


    @Override
    public int hashCode() {
        return Objects.hash(id, title, genre, imdbRating);
    }

    @Override
    public String toString() {
        return "Content{" +
                "id=" + id +
                ", title='" + title + '\'' +
                ", genre=" + genre +
                ", averageRating=" + calculateAverageUserRating() +
                ", imdbRating=" + imdbRating +
                '}';
    }

    public Double getUserRating() {
        return userRating;
    }

    public void setUserRating(Double userRating) {
        this.userRating = userRating;
    }


    public int getStartYear() {
        return startYear;
    }
}