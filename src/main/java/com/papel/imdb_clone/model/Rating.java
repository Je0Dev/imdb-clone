package com.papel.imdb_clone.model;

import java.time.LocalDateTime;
import java.util.Objects;

public class Rating {
    private int id;
    private int userId;
    private int contentId;
    private double score;
    private String review;
    private LocalDateTime createdAt;

    public Rating(int id, double rating) {
        this.createdAt = LocalDateTime.now();
    }

    public Rating(int userId, int contentId, double score) {
        this.userId = userId;
        this.contentId = contentId;
        this.score = score;
        this.createdAt = LocalDateTime.now();
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public int getContentId() {
        return contentId;
    }

    public void setContentId(int contentId) {
        this.contentId = contentId;
    }

    public double getScore() {
        return score;
    }

    public void setScore(double score) {
        if (score >= 0 && score <= 10) {
            this.score = score;
        }
    }

    public String getReview() {
        return review;
    }

    public void setReview(String review) {
        this.review = review;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    // Legacy method for backward compatibility
    public int getRating() {
        return (int) Math.round(score);
    }

    public void setRating(int rating) {
        setScore(rating);
    }

    public LocalDateTime getTimestamp() {
        return createdAt;
    }

    /**
     * Compares this {@code Rating} object with the specified object for equality.
     * Returns {@code true} if the given object is also a {@code Rating} and
     * all its primitive fields ({@code id}, {@code userId}, {@code contentId}, and {@code score}) are equal.
     * This method performs direct primitive equality checks for all fields.
     *
     * @param obj The object to be compared for equality.
     * @return {@code true} if the specified object is equal to this {@code Rating}; {@code false} otherwise.
     */
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        Rating rating1 = (Rating) obj;
        return id == rating1.id &&
                userId == rating1.userId &&
                contentId == rating1.contentId &&
                Double.compare(rating1.score, score) == 0 &&
                Objects.equals(review, rating1.review) &&
                Objects.equals(createdAt, rating1.createdAt);
    }

    /**
     * Generates a hash code for this {@code Rating} object.
     * The hash code is computed based on the values of the {@code id}, {@code userId},
     * {@code contentId}, {@code score}, {@code review}, and {@code createdAt} fields.
     * This method is consistent with {@link #equals(Object)}, ensuring that
     * equal {@code Rating} objects produce the same hash code.
     *
     * @return A hash code value for this object.
     */
    @Override
    public int hashCode() {
        return Objects.hash(id, userId, contentId, score, review, createdAt);
    }

    @Override
    public String toString() {
        return "Rating{" +
                "id=" + id +
                ", userId=" + userId +
                ", contentId=" + contentId +
                ", score=" + score +
                ", review='" + review + '\'' +
                ", createdAt=" + createdAt +
                '}';
    }
}