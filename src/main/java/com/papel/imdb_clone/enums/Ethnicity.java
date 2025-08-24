package com.papel.imdb_clone.enums;

public enum Ethnicity {
    CAUCASOID("Caucasoid"),
    NEGROID("Negroid"),
    MONGOLOID("Mongoloid"),
    ASIAN("Asian"),
    PACIFIC_ISLAND_AND_AUSTRALIAN("Pacific Island and Australian"),
    AMERINDIANS_AND_ESKIMOS("Amerindians and Eskimos");

    private final String label;

    Ethnicity(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }

    public static Ethnicity fromLabel(String input) {
        if (input == null) {
            throw new IllegalArgumentException("Ethnicity cannot be null");
        }
        String normalized = input.trim();
        for (Ethnicity e : values()) {
            if (e.label.equalsIgnoreCase(normalized)) {
                return e;
            }
        }
        // Try matching enum name style
        String nameLike = normalized.toUpperCase().replace('-', ' ').replace(' ', '_');
        for (Ethnicity e : values()) {
            if (e.name().equals(nameLike)) {
                return e;
            }
        }
        throw new IllegalArgumentException("Unknown ethnicity: " + input);
    }
}
