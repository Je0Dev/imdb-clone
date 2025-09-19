package com.papel.imdb_clone.model.people;

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
    private String notableWorks = ""; // Initialize to empty string to avoid NPE


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
        // First, check if we have any works in bestWorks
        if ((bestWorks != null && !bestWorks.isEmpty())) {
            return new ArrayList<>(bestWorks);
        }
        
        // Then check notableWorks string
        if (notableWorks != null && !notableWorks.trim().isEmpty()) {
            // Split by comma and clean up the strings
            return Arrays.stream(notableWorks.split(","))
                       .map(String::trim)
                       .filter(s -> !s.isEmpty())
                       .collect(Collectors.toList());
        }
        
        // Return empty list if no works found
        return new ArrayList<>();
    }

    /**
     * Sets the notable works from a list of strings
     * @param works List of works
     */
    public void setNotableWorks(List<String> works) {
        this.notableWorks = works != null ? String.join(", ", works) : "";
    }

    /**
     * Gets the nationality/ethnicity as a string.
     *
     * @return The ethnicity label or null if not set
     */
    public Ethnicity getNationality() {
        return ethnicity != null ? Ethnicity.valueOf(ethnicity.getLabel()) : null;
    }

    /**
     * Gets a formatted string with the director's full name, birth year, and notable works.
     * @return Formatted string with director information
     */
    public String getFormattedInfo() {
        StringBuilder sb = new StringBuilder();
        sb.append("<html><b>").append(getFullName()).append("</b>");
        
        // Add birth year if available
        if (getBirthDate() != null) {
            sb.append("<br>Born: ").append(getBirthDate());
        }
        
        // Add gender if available
        if (getGender() != '\u0000') {
            String genderStr = "";
            if (getGender() == 'M' || getGender() == 'm') {
                genderStr = "Male";
            } else if (getGender() == 'F' || getGender() == 'f') {
                genderStr = "Female";
            } else if (getGender() == 'U') {
                genderStr = "Unknown";
            } else {
                genderStr = String.valueOf(getGender());
            }
            sb.append("<br>Gender: ").append(genderStr);
        }
        
        // Add nationality/ethnicity if available
        if (getEthnicity() != null) {
            sb.append("<br>Nationality: ").append(getEthnicity().getLabel());
        }
        
        // Add notable works if available
        List<String> works = getNotableWorks();
        if (!works.isEmpty()) {
            sb.append("<br><br><b>Notable Works:</b><br>");
            for (int i = 0; i < Math.min(5, works.size()); i++) {
                sb.append("• ").append(works.get(i));
                if (i < Math.min(5, works.size()) - 1) {
                    sb.append("<br>");
                }
            }
            if (works.size() > 5) {
                sb.append("<br>... and ").append(works.size() - 5).append(" more");
            }
        }
        
        sb.append("</html>");
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