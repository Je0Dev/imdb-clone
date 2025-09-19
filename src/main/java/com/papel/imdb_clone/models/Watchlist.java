package com.papel.imdb_clone.models;

import java.time.LocalDateTime;
import java.util.Objects;

public class Watchlist {
    private String id;
    private String userId;
    private String contentId;
    private String contentType; // "movie" or "tv"
    private LocalDateTime addedDate;
    private boolean watched;
    private int userRating;
    private String notes;

    public Watchlist() {
        this.addedDate = LocalDateTime.now();
    }

    public Watchlist(String userId, String contentId, String contentType) {
        this();
        this.userId = userId;
        this.contentId = contentId;
        this.contentType = contentType;
        this.watched = false;
        this.userRating = 0;
    }

    // Getters and Setters
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getContentId() {
        return contentId;
    }

    public void setContentId(String contentId) {
        this.contentId = contentId;
    }

    public String getContentType() {
        return contentType;
    }

    public void setContentType(String contentType) {
        this.contentType = contentType;
    }

    public LocalDateTime getAddedDate() {
        return addedDate;
    }

    public void setAddedDate(LocalDateTime addedDate) {
        this.addedDate = addedDate;
    }

    public boolean isWatched() {
        return watched;
    }

    public void setWatched(boolean watched) {
        this.watched = watched;
    }

    public int getUserRating() {
        return userRating;
    }

    public void setUserRating(int userRating) {
        if (userRating >= 0 && userRating <= 10) {
            this.userRating = userRating;
        }
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Watchlist watchlist = (Watchlist) o;
        return Objects.equals(id, watchlist.id) &&
               Objects.equals(userId, watchlist.userId) &&
               Objects.equals(contentId, watchlist.contentId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, userId, contentId);
    }

    @Override
    public String toString() {
        return "Watchlist{" +
                "id='" + id + '\'' +
                ", userId='" + userId + '\'' +
                ", contentId='" + contentId + '\'' +
                ", contentType='" + contentType + '\'' +
                ", addedDate=" + addedDate +
                ", watched=" + watched +
                ", userRating=" + userRating +
                '}';
    }
}
