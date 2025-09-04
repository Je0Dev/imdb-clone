package com.papel.imdb_clone.model;

import java.time.LocalDateTime;

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