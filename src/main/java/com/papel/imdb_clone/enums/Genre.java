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

    public Object getDisplayName() {
        return displayName;
    }
}