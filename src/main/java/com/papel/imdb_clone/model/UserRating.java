package com.papel.imdb_clone.model;

import java.time.LocalDateTime;

/**
 * Represents a detailed user rating and review for a movie or series.
 */
public class UserRating {
    private int id;
    private final int userId;
    private final int contentId;
    private double rating; // 1.0 to 10.0
    private String title;
    private final LocalDateTime createdAt;

    public UserRating(int userId, int contentId, double rating) {
        this.userId = userId;
        this.contentId = contentId;
        this.rating = rating;
        this.createdAt = LocalDateTime.now();
    }


    public UserRating(int userId, int contentId, double rating, String title) {
        this(userId, contentId, rating);
        this.title = title;
    }

    // Getters and Setters
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }


    public double getRating() {
        return rating;
    }

    public void setRating(double rating) {
        if (rating >= 1.0 && rating <= 10.0) {
            this.rating = rating;
        } else {
            throw new IllegalArgumentException("Rating must be between 1.0 and 10.0");
        }
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }


    @Override
    public String toString() {
        return "UserRating{" +
                "id=" + id +
                ", userId=" + userId +
                ", contentId=" + contentId +
                ", rating=" + rating +
                ", title='" + title + '\'' +
                ", createdAt=" + createdAt +
                '}';
    }
}