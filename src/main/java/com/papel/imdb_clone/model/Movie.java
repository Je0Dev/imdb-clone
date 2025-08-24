package com.papel.imdb_clone.model;

import com.papel.imdb_clone.enums.Genre;

import java.util.*;

public class Movie extends Content {
    private final int duration;
    private final List<Actor> actors;

    // Rich content fields
    @Deprecated
    private String description;

    private String boxOffice;
    private List<String> awards;
    private List<String> filmingLocations;
    private List<String> productionCompanies;
    private String mpaaRating; // G, PG, PG-13, R, NC-17
    private List<Genre> genres = new ArrayList<>(); // Multiple genres support
    private Date releaseDate;
    private String country;
    private String language;

    // Media URLs
    private String posterUrl;
    private String trailerUrl;
    private List<String> imageUrls;

    public Date getReleaseDate() {
        return releaseDate != null ? new Date(releaseDate.getTime()) : null;
    }

    public void setReleaseDate(Date releaseDate) {
        this.releaseDate = releaseDate != null ? new Date(releaseDate.getTime()) : null;
    }

    public Movie() {
        super("", new Date(), Genre.ACTION, "Unknown", new HashMap<>(), 0.0);
        this.duration = 0;
        this.actors = new ArrayList<>();
        initializeRichContentFields();
    }

    public Movie(String title, int year, String genre, String director, Map<Integer, Integer> userRatings, double imdbRating) {
        super(title, new Date(year - 1900, 0, 1),
                Genre.valueOf(genre.toUpperCase().replace(" ", "_")),
                director, userRatings, imdbRating);
        this.releaseDate = new Date(year - 1900, 0, 1);
        this.duration = 0;
        this.actors = new ArrayList<>();
        initializeRichContentFields();
    }

    // Constructor for FileDataLoaderService
    public Movie(String title, Date year, List<Genre> genres, int duration,
                 double imdbRating, String description, String director, List<Actor> actors) {
        super(title, year,
                (genres != null && !genres.isEmpty()) ? genres.get(0) : Genre.DRAMA,
                director, new HashMap<>(), imdbRating);
        this.releaseDate = year != null ? new Date(year.getTime()) : null;
        this.duration = duration;
        this.actors = new ArrayList<>(actors != null ? actors : new ArrayList<>());
        this.description = description;
        this.genres = genres != null ? new ArrayList<>(genres) : new ArrayList<>();
        initializeRichContentFields();
    }

    private void initializeRichContentFields() {
        this.imageUrls = new ArrayList<>();
        this.awards = new ArrayList<>();
        this.filmingLocations = new ArrayList<>();
        this.productionCompanies = new ArrayList<>();
        this.genres.add(getGenre()); // Add the primary genre from parent class
    }

    public int getDuration() {
        return duration;
    }

    public List<Actor> getActors() {
        return new ArrayList<>(actors);
    }

    public String getDirector() {
        return super.getDirector();
    }

    // Rich content getters and setters

    /**
     * @deprecated Use {@link #getSummary()} instead.
     */
    @Deprecated
    public String getDescription() {
        return getSummary();
    }

    /**
     * @deprecated Use {@link #setSummary(String)} instead.
     */
    @Deprecated
    public void setDescription(String description) {
        setSummary(description);
    }

    // Media URL getters and setters

    public String getPosterUrl() {
        return posterUrl;
    }

    public void setPosterUrl(String posterUrl) {
        this.posterUrl = posterUrl;
    }

    public String getTrailerUrl() {
        return trailerUrl;
    }

    public void setTrailerUrl(String trailerUrl) {
        this.trailerUrl = trailerUrl;
    }

    public List<String> getImageUrls() {
        return imageUrls != null ? new ArrayList<>(imageUrls) : new ArrayList<>();
    }

    public void setImageUrls(List<String> imageUrls) {
        this.imageUrls = imageUrls != null ? new ArrayList<>(imageUrls) : new ArrayList<>();
    }

    public void addImageUrl(String imageUrl) {
        if (this.imageUrls == null) {
            this.imageUrls = new ArrayList<>();
        }
        if (imageUrl != null && !imageUrl.trim().isEmpty()) {
            this.imageUrls.add(imageUrl);
        }
    }

    public String getBoxOffice() {
        return boxOffice;
    }

    public List<String> getAwards() {
        return new ArrayList<>(awards);
    }

    public List<String> getProductionCompanies() {
        return new ArrayList<>(productionCompanies);
    }

    public String getMpaaRating() {
        return mpaaRating;
    }

    public void setGenres(List<Genre> genres) {
        this.genres = genres != null ? new ArrayList<>(genres) : new ArrayList<>();
    }

    public void addGenre(Genre genre) {
        if (genre != null && !this.genres.contains(genre)) {
            this.genres.add(genre);
        }
    }

    public void removeGenre(Genre genre) {
        this.genres.remove(genre);
    }

    public boolean hasGenre(Genre genre) {
        return this.genres.contains(genre);
    }

    /**
     * Compares this {@code Movie} object with the specified object for equality.
     * Returns {@code true} if the given object is also a {@code Movie} and
     * all fields (inherited and specific to {@code Movie}) are equal.
     *
     * <p>Specifically, this method considers two {@code Movie} objects equal if:
     * <ul>
     * <li>They are the exact same object (reference equality).</li>
     * <li>The other object is not {@code null} and is an instance of the same class.</li>
     * <li>The superclass's {@code equals} method returns {@code true} (meaning
     * all inherited fields, likely from a {@code Content} superclass, are equal).</li>
     * <li>Their {@code year} fields are equal.</li>
     * <li>Their {@code duration} fields are equal.</li>
     * <li>Their {@code director} fields are equal (using {@link java.util.Objects#equals(Object, Object)}
     * for null-safe comparison). This assumes the {@code Director} class correctly
     * overrides its own {@code equals} method.</li>
     * <li>Their {@code actors} fields are equal (using {@link java.util.Objects#equals(Object, Object)}
     * for null-safe comparison). This typically means {@code actors} is a collection (e.g., {@code List<Actor>})
     * and its {@code equals} method correctly handles element-wise comparison. It's crucial that the
     * elements ({@code Actor} objects) also correctly override their {@code equals} method.</li>
     * </ul>
     *
     * @param obj The object to be compared for equality with this {@code Movie}.
     * @return {@code true} if the specified object is equal to this {@code Movie}; {@code false} otherwise.
     */
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        if (!super.equals(obj)) return false;
        Movie movie = (Movie) obj;
        return duration == movie.duration &&
                Objects.equals(actors, movie.actors) &&
                Objects.equals(description, movie.description) &&
                Objects.equals(posterUrl, movie.posterUrl) &&
                Objects.equals(trailerUrl, movie.trailerUrl) &&
                Objects.equals(imageUrls, movie.imageUrls) &&
                Objects.equals(boxOffice, movie.boxOffice) &&
                Objects.equals(awards, movie.awards) &&
                Objects.equals(filmingLocations, movie.filmingLocations) &&
                Objects.equals(productionCompanies, movie.productionCompanies) &&
                Objects.equals(mpaaRating, movie.mpaaRating) &&
                Objects.equals(genres, movie.genres);
    }

    /**
     * Generates a hash code for this {@code Movie} object.
     * The hash code is computed based on the hash code of the superclass and the
     * values of the {@code year}, {@code duration}, {@code director}, and {@code actors} fields.
     * This method is consistent with {@link #equals(Object)}, meaning that for any two
     * {@code Movie} objects, {@code a} and {@code b}, if {@code a.equals(b)} is true,
     * then {@code a.hashCode()} must be the same as {@code b.hashCode()}.
     *
     * <p>It's important that:
     * <ul>
     * <li>The superclass's {@code hashCode()} method is correctly implemented.</li>
     * <li>If {@code director} is a custom object, its {@code hashCode()} method is correctly implemented.</li>
     * <li>If {@code actors} is a collection, its {@code hashCode()} method correctly computes a hash
     * based on its elements (and the elements themselves, {@code Actor} objects, correctly implement
     * {@code hashCode()}).</li>
     * </ul>
     *
     * @return A hash code value for this object.
     */
    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), duration, actors,
                description, posterUrl, trailerUrl, imageUrls, boxOffice, awards,
                filmingLocations, productionCompanies, mpaaRating, genres);
    }

    @Override
    public String toString() {
        return "Movie{" +
                "id=" + getId() +
                ", title='" + getTitle() + '\'' +
                ", genre=" + getGenre() +
                ", duration=" + duration +
                ", actors=" + actors.size() +
                ", genres=" + genres.size() +
                ", hasDescription=" + (description != null) +
                ", hasPoster=" + (posterUrl != null) +
                ", hasTrailer=" + (trailerUrl != null) +
                ", images=" + (imageUrls != null ? imageUrls.size() : 0) +
                ", mpaaRating='" + mpaaRating + '\'' +
                '}';
    }

    public void setReleaseYear(int year) {
    }

    public void setDuration(int i) {
    }

    public void setYear(Date year) {
        this.year = year;
    }

    public void setActors(List<Actor> objects) {
    }

    public void setStartYear(Date year) {
    }

    public void setEndYear(Date year) {
    }

    public void setCountry(String country) {
        this.country = country;

    }

    public void setLanguage(String language) {
        this.language = language;
    }

    public void setBoxOffice(String boxOffice) {
        this.boxOffice = boxOffice;
    }

    public void setAwards(String awards) {
        this.awards = Collections.singletonList(awards);
    }

    public void setRating(double rating) {
        this.setImdbRating(rating);
    }

    public static double getRating(Object o) {
        return ((Movie) o).getImdbRating();
    }

    public void addActor(Actor actor) {
        this.actors.add(actor);
    }
}
