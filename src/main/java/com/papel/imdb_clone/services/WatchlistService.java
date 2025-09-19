package com.papel.imdb_clone.services;

import com.papel.imdb_clone.models.Watchlist;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

public class WatchlistService {
    // In-memory storage (replace with database in production)
    private final Map<String, Watchlist> watchlistStore = new ConcurrentHashMap<>();
    private static WatchlistService instance;

    public WatchlistService() {
        // Private constructor for singleton
    }

    public static synchronized WatchlistService getInstance() {
        if (instance == null) {
            instance = new WatchlistService();
        }
        return instance;
    }

    /**
     * Add content to user's watchlist
     */
    public Watchlist addToWatchlist(String userId, String contentId, String contentType) {
        String id = UUID.randomUUID().toString();
        Watchlist item = new Watchlist(userId, contentId, contentType);
        item.setId(id);
        watchlistStore.put(id, item);
        return item;
    }

    /**
     * Remove content from user's watchlist
     */
    public boolean removeFromWatchlist(String userId, String watchlistId) {
        Watchlist item = watchlistStore.get(watchlistId);
        if (item != null && item.getUserId().equals(userId)) {
            watchlistStore.remove(watchlistId);
            return true;
        }
        return false;
    }

    /**
     * Get all items in user's watchlist
     */
    public List<Watchlist> getUserWatchlist(String userId) {
        return watchlistStore.values().stream()
                .filter(item -> item.getUserId().equals(userId))
                .sorted(Comparator.comparing(Watchlist::getAddedDate).reversed())
                .collect(Collectors.toList());
    }

    /**
     * Check if content is in user's watchlist
     */
    public boolean isInWatchlist(String userId, String contentId) {
        return watchlistStore.values().stream()
                .anyMatch(item -> item.getUserId().equals(userId) && item.getContentId().equals(contentId));
    }

    /**
     * Mark content as watched/unwatched
     */
    public boolean toggleWatchedStatus(String userId, String watchlistId, boolean watched) {
        Watchlist item = watchlistStore.get(watchlistId);
        if (item != null && item.getUserId().equals(userId)) {
            item.setWatched(watched);
            return true;
        }
        return false;
    }

    /**
     * Update user rating for a watchlist item
     */
    public boolean updateUserRating(String userId, String watchlistId, int rating) {
        if (rating < 0 || rating > 10) {
            return false;
        }
        Watchlist item = watchlistStore.get(watchlistId);
        if (item != null && item.getUserId().equals(userId)) {
            item.setUserRating(rating);
            return true;
        }
        return false;
    }

    /**
     * Add or update notes for a watchlist item
     */
    public boolean updateNotes(String userId, String watchlistId, String notes) {
        Watchlist item = watchlistStore.get(watchlistId);
        if (item != null && item.getUserId().equals(userId)) {
            item.setNotes(notes);
            return true;
        }
        return false;
    }
}
