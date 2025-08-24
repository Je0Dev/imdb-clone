package com.papel.imdb_clone.model;

import java.time.LocalDateTime;
import java.util.Objects;

/**
 * Represents a detailed user rating and review for a movie or series.
 */
public class UserRating {
    private int id;
    private final int userId;
    private final int contentId;
    private double rating; // 1.0 to 10.0
    private String review;
    private String title;
    private final LocalDateTime createdAt;
    private final boolean isSpoiler;
    private final int helpfulVotes;
    private final int totalVotes;

    public UserRating(int userId, int contentId, double rating) {
        this.userId = userId;
        this.contentId = contentId;
        this.rating = rating;
        this.createdAt = LocalDateTime.now();
        this.isSpoiler = false;
        this.helpfulVotes = 0;
        this.totalVotes = 0;
    }

    public UserRating(int userId, int contentId, double rating, String review) {
        this(userId, contentId, rating);
        this.review = review;
    }

    public UserRating(int userId, int contentId, double rating, String title, String review) {
        this(userId, contentId, rating, review);
        this.title = title;
    }

    // Getters and Setters
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getUserId() {
        return userId;
    }


    public int getContentId() {
        return contentId;
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

    public String getReview() {
        return review;
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


    public boolean hasReview() {
        return review != null && !review.trim().isEmpty();
    }

    public boolean hasTitle() {
        return title != null && !title.trim().isEmpty();
    }


    public String getShortReview(int maxLength) {
        if (review == null || review.length() <= maxLength) {
            return review;
        }
        return review.substring(0, maxLength) + "...";
    }


    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        UserRating that = (UserRating) obj;
        return id == that.id &&
                userId == that.userId &&
                contentId == that.contentId;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, userId, contentId);
    }

    @Override
    public String toString() {
        return "UserRating{" +
                "id=" + id +
                ", userId=" + userId +
                ", contentId=" + contentId +
                ", rating=" + rating +
                ", title='" + title + '\'' +
                ", hasReview=" + hasReview() +
                ", isSpoiler=" + isSpoiler +
                ", helpfulVotes=" + helpfulVotes +
                ", totalVotes=" + totalVotes +
                ", createdAt=" + createdAt +
                '}';
    }
}