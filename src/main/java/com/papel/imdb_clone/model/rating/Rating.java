package com.papel.imdb_clone.model.rating;

import java.time.LocalDateTime;

/**
 * Represents a rating.
 */
public class Rating {
    private int id;
    private int userId;
    private int contentId;
    private double score;
    private String review;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;


    public Rating(int id, double rating) {
        this.id = id;
        this.score = rating;
        this.review = "";
        this.createdAt = LocalDateTime.now();
    }

    //constructor for rating creation
    public Rating(int userId, int contentId, double score) {
        this.userId = userId;
        this.contentId = contentId;
        this.score = score;
        this.createdAt = LocalDateTime.now();
        this.review = "";
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
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

    //set score with validation
    public void setScore(double score) {
        if (score >= 0 && score <= 10) {
            this.score = score;
        }
    }

    public double getRating() {
        return score;
    }

    public void setRating(int rating) {
        setScore(rating);
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

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    public void updateRating(double score) {
        this.score = score;
        this.updatedAt = LocalDateTime.now();
    }

    public void updateReview(String review) {
        this.review = review;
        this.updatedAt = LocalDateTime.now();
    }

    public void updateRatingAndReview(double score, String review) {
        this.score = score;
        this.review = review;
        this.updatedAt = LocalDateTime.now();
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
                ", updatedAt=" + updatedAt +
                '}';
    }

}