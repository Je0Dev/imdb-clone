package com.papel.imdb_clone.exceptions;

/**
 * Exception thrown when a rate limit has been exceeded.
 */
public class RateLimitExceededException extends Exception {
    private static final long serialVersionUID = 1L;
    private final long remainingTime;

    public RateLimitExceededException(String message, long remainingTime) {
        super(message);
        this.remainingTime = remainingTime;
    }

    public RateLimitExceededException(String message, long remainingTime, Throwable cause) {
        super(message, cause);
        this.remainingTime = remainingTime;
    }

    /**
     * Gets the remaining time in minutes until the rate limit resets.
     *
     * @return The remaining time in minutes
     */
    public long getRemainingTime() {
        return remainingTime;
    }
}
