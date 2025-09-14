package com.papel.imdb_clone.enums;

public enum Ethnicity {
    // Regional Ethnicities
    AFRICAN("African"),
    ARAB("Arab"),
    ASIAN("Asian"),
    CAUCASIAN("Caucasian"),
    HISPANIC("Hispanic"),
    INDIAN("Indian"),
    INDIGENOUS_AMERICAN("Indigenous American"),
    INDIGENOUS_AUSTRALIAN("Indigenous Australian"),
    PACIFIC_ISLANDER("Pacific Islander"),
    
    // Nationalities (for more specific representation)
    AMERICAN("American"),
    BRITISH("British"),
    CANADIAN("Canadian"),
    CHINESE("Chinese"),
    FRENCH("French"),
    GERMAN("German"),
    INDIAN_NATIONAL("Indian"),
    ITALIAN("Italian"),
    JAPANESE("Japanese"),
    KOREAN("Korean"),
    MEXICAN("Mexican"),
    RUSSIAN("Russian"),
    SPANISH("Spanish"),
    
    // Additional categories
    MIXED("Mixed"),
    OTHER("Other"),
    UNKNOWN("Unknown"),
    CAUCASOID("Caucasoid");

    private final String label;

    //orizo
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
