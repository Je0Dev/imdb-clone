package com.papel.imdb_clone.model;

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

    @Override
    public String toString() {
        return "Actor{" +
                "id=" + getId() +
                ", firstName='" + getFirstName() + '\'' +
                ", lastName='" + getLastName() + '\'' +
                ", birthDate=" + getBirthDate() +
                ", gender=" + getGender() +
                '}';
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

}