package com.papel.imdb_clone.model.rating;

import java.time.LocalDateTime;

/**
 * Represents a detailed user rating and review for a movie or series.
 */
public class UserRating {
    private int id;
    private final int userId;
    private final int contentId;
    private Integer rating; // 1-10
    private String title;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    /**
     * Constructor for UserRating
     * @param userId The user's ID
     * @param contentId The content's ID
     * @param rating The user's rating
     */
    public UserRating(int userId, int contentId, int rating) {
        this.userId = userId;
        this.contentId = contentId;
        this.rating = rating;
        this.createdAt = LocalDateTime.now();
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

    //if rating is between 1 and 10, set it to that value-int, else throw exception
    public void setRating(int rating) {
        if (rating >= 1 && rating <= 10) {
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

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
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
                ", updatedAt=" + updatedAt +
                '}';
    }
}