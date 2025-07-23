package com.papel.imdb_clone.model;

import com.papel.imdb_clone.enums.Genre;

import java.util.*;

public class Movie extends Content {
    private final int year;
    private final int duration;
    private final Director director;
    private final List<Actor> actors;

    public Movie(String title, Date year, Genre genre, String director, Map<Integer, Integer> userRatings, int duration, List<Actor> actors) {
        super();
        this.duration = duration;
        this.actors = actors;
        this.title = title;
        this.year = year;
        this.genre = genre;
        this.director = director;
        this.userRatings = new HashMap<>();
    }


    public int getYear() {
        return year;
    }

    public int getDuration() {
        return duration;
    }

    public Director getDirector() {
        return director;
    }

    public double getImdbRating() {
        return imdbRating;
    }

    public List<Actor> getActors() {
        return new ArrayList<>(actors);
    }

    public Actor getMainActor() {
        return actors.isEmpty() ? null : actors.getFirst();
    }

    public void setImdbRating(double rating) {
        if (rating >= 0.0 && rating <= 10.0) {
            this.imdbRating = rating;
        }
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
     * <li>Their {@code imdbRating} fields are equal, handling floating-point comparison
     * correctly using {@link Double#compare(double, double)}.</li>
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
        return year == movie.year &&
                duration == movie.duration &&
                Double.compare(movie.imdbRating, imdbRating) == 0 &&
                Objects.equals(director, movie.director) &&
                Objects.equals(actors, movie.actors);
    }

    /**
     * Generates a hash code for this {@code Movie} object.
     * The hash code is computed based on the hash code of the superclass and the
     * values of the {@code year}, {@code duration}, {@code director}, {@code imdbRating},
     * and {@code actors} fields.
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
        return Objects.hash(super.hashCode(), year, duration, director, imdbRating, actors);
    }

    @Override
    public String toString() {
        return "Movie{" +
                "id=" + getId() +
                ", title='" + getTitle() + '\'' +
                ", year=" + year +
                ", genre=" + getGenre() +
                ", duration=" + duration +
                ", director=" + (director != null ? director.getFullName() : "Unknown") +
                ", imdbRating=" + imdbRating +
                ", actors=" + actors.size() +
                '}';
    }
}
