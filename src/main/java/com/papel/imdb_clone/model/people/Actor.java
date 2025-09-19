package com.papel.imdb_clone.model.people;

import com.papel.imdb_clone.enums.Ethnicity;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Actor class extends Celebrity and adds ethnicity and notable works fields.
 *
 */
public class Actor extends Celebrity {
    private Ethnicity ethnicity;
    private String role; // Actor's role in a specific movie/show

    // Backward-compatible storage of the original race label used in older tests
    private String notableWorks;

    // Actor constructor
    public Actor(String firstName, String lastName, LocalDate birthDate, char gender, Ethnicity ethnicity) {
        super(firstName, lastName, birthDate, gender);
        this.ethnicity = ethnicity;
    }

    // Backward-compatible constructor accepting a race/ethnicity label
    public Actor(String firstName, String lastName, LocalDate birthDate, char gender, String raceLabel) {
        super(firstName, lastName, birthDate, gender);
        if (raceLabel != null && !raceLabel.isBlank()) {
            try {
                this.ethnicity = Ethnicity.fromLabel(raceLabel);
            } catch (IllegalArgumentException ex) {
                this.ethnicity = null; // Unknown label; keep null to remain backward-compatible
            }
        }
    }

    // getter for ethnicity
    public Ethnicity getEthnicity() {
        return ethnicity;
    }

    // setter for ethnicity
    public void setEthnicity(Ethnicity ethnicity) {
        this.ethnicity = ethnicity;
    }
    
    // getter for role
    public String getRole() {
        return role != null ? role : "";
    }
    
    // setter for role
    public void setRole(String role) {
        this.role = role;
    }

    @Override
    public String toString() {
        return getFullName();
    }

    /**
     * Gets a formatted string with the actor's full name, birth year, and notable works.
     * @return Formatted string with actor information
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
        
        // Add ethnicity if available
        if (ethnicity != null) {
            sb.append("\n").append(ethnicity.getLabel());
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

    // setter for id
    public void setId(int id) {
        this.id = id;
    }

    // setter for notable works
    public void setNotableWorks(String notableWorks) {
        this.notableWorks = notableWorks;
    }

    //setters for first and lastname
    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }


    // getter for notable works
    public List<String> getNotableWorks() {
        if (notableWorks == null || notableWorks.trim().isEmpty()) {
            return new ArrayList<>();
        }
        return Arrays.stream(notableWorks.split(",")).map(String::trim).collect(Collectors.toList());
    }
    
    /**
     * Adds a notable work to the actor's list of notable works.
     * @param work The work to add
     */
    public void addNotableWork(String work) {
        if (work != null && !work.trim().isEmpty()) {
            if (notableWorks == null || notableWorks.isEmpty()) {
                notableWorks = work.trim();
            } else {
                notableWorks += ", " + work.trim();
            }
        }
    }

    public Object getName() {
        return getFullName();
    }
}