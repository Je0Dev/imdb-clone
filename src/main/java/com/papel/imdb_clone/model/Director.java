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


    @Override
    public String toString() {
        return "Director{" +
                "id=" + getId() +
                ", firstName='" + getFirstName() + '\'' +
                ", lastName='" + getLastName() + '\'' +
                ", birthDate=" + getBirthDate() +
                ", gender=" + getGender() +
                ", bestWorks=" + bestWorks +
                ", ethnicity=" + (ethnicity != null ? ('\'' + ethnicity.getLabel() + '\'') : null) +
                '}';
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