package com.papel.imdb_clone.model;

import com.papel.imdb_clone.enums.Ethnicity;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Represents a director of a movie or TV show.
 */
public class Director extends Celebrity {
    private final List<String> bestWorks;
    private Ethnicity ethnicity;
    private String notableWorks;


    /**
     * Constructor for Director
     * @param firstName
     * @param lastName
     * @param birthDate
     * @param gender
     * @param ethnicity
     */
    public Director(String firstName, String lastName, LocalDate birthDate, char gender, Ethnicity ethnicity) {
        super(firstName, lastName, birthDate, gender);
        this.bestWorks = new ArrayList<>();
        this.ethnicity = ethnicity;
    }

    //getters
    public List<String> getBestWorks() {
        return new ArrayList<>(bestWorks);
    }

    public Ethnicity getEthnicity() {
        return ethnicity;
    }

    /**
     * Gets the notable works of the director.
     *
     * @return A list of notable works, or an empty list if not set
     */
    public List<String> getNotableWorks() {
        if (notableWorks == null || notableWorks.trim().isEmpty()) {
            return new ArrayList<>();
        }
        return Arrays.stream(notableWorks.split(",")).map(String::trim).collect(Collectors.toList());
    }

    /**
     * Gets the nationality/ethnicity as a string.
     *
     * @return The ethnicity label or null if not set
     */
    public String getNationality() {
        return ethnicity != null ? ethnicity.getLabel() : null;
    }

    /**
     * Gets a formatted string with the director's full name, birth year, and notable works.
     * @return Formatted string with director information
     */
    public String getFormattedInfo() {
        StringBuilder sb = new StringBuilder();
        sb.append(getFullName());
        
        // Add birth year if available
        if (getBirthDate() != null) {
            sb.append(" (");
            sb.append(getBirthDate().getYear());
            sb.append(")");
        }
        
        // Add notable works if available
        List<String> works = getNotableWorks();
        if (!works.isEmpty()) {
            sb.append("\n\nNotable Works:\n");
            for (int i = 0; i < Math.min(5, works.size()); i++) {
                sb.append("• ").append(works.get(i));
                if (i < Math.min(5, works.size()) - 1) {
                    sb.append("\n");
                }
            }
            if (works.size() > 5) {
                sb.append("\n... and ").append(works.size() - 5).append(" more");
            }
        }
        
        return sb.toString();
    }

    @Override
    public String toString() {
        return getFullName();
    }

    //setters
    public void setId(int id) {
        this.id = id;
    }

    /**
     * Sets the notable works of the director.
     *
     * @param notableWorks A comma-separated string of notable works
     */
    public void setNotableWorks(String notableWorks) {
        this.bestWorks.clear();
        if (notableWorks != null && !notableWorks.trim().isEmpty()) {
            String[] works = notableWorks.split(",");
            for (String work : works) {
                addBestWork(work.trim());
            }
        }
    }

    //set first and last name
    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }



    public void addBestWork(String work) {
        if (work != null && !work.trim().isEmpty()) {
            bestWorks.add(work.trim());
        }
    }
}