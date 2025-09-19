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
    private final LocalDateTime createdAt;

    public Rating(int id, double rating) {
        this.createdAt = LocalDateTime.now();
    }

    //constructor for rating creation
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


    public int getContentId() {
        return contentId;
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

    public void setRating(int rating) {
        setScore(rating);
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