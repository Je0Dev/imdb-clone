package com.papel.imdb_clone.enums;

public enum Genre {
    ACTION("Action"),
    COMEDY("Comedy"),
    DRAMA("Drama"),
    HORROR("Horror"),
    THRILLER("Thriller"),
    ROMANCE("Romance"),
    SCI_FI("Science Fiction"),
    FANTASY("Fantasy"),
    DOCUMENTARY("Documentary"),
    ANIMATION("Animation"),
    CRIME("Crime"),
    MYSTERY("Mystery"),
    ADVENTURE("Adventure"),
    BIOGRAPHY("Biography"),
    MUSICAL("Musical"),
    WESTERN("Western"),
    WAR("War"),
    FAMILY("Family"),
    SPORT("Sport"),
    HISTORY("History"),
    UNKNOWN("Unknown");

    private final String displayName;

    Genre(String displayName) {
        this.displayName = displayName;
    }


    @Override
    public String toString() {
        return displayName;
    }

    /**
     * Converts a string to a Genre enum value (case-insensitive).
     *
     * @param value The string value to convert
     * @return The matching Genre or null if no match found
     */
    public static Genre fromString(String value) {
        if (value == null || value.trim().isEmpty()) {
            return null;
        }
        // Normalize the input: trim, uppercase, replace spaces and hyphens with underscores
        String normalized = value.trim().toUpperCase().replace(" ", "_").replace("-", "_");
        try {
            return Genre.valueOf(normalized);
        } catch (IllegalArgumentException e) {
            // Try to find a case-insensitive match
            for (Genre genre : values()) {
                if (genre.displayName.equalsIgnoreCase(value.trim()) || 
                    genre.name().equalsIgnoreCase(normalized)) {
                    return genre;
                }
            }
            return null;
        }
    }

}