package com.papel.imdb_clone.model;

import com.papel.imdb_clone.enums.Ethnicity;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class Director extends Celebrity {
    private final List<String> bestWorks;
    private Ethnicity ethnicity;
    private String notableWorks;


    public Director(String firstName, String lastName, LocalDate birthDate, char gender) {
        super(firstName, lastName, birthDate, gender);
        this.bestWorks = new ArrayList<>();
    }

    public Director(String firstName, String lastName, LocalDate birthDate, char gender, Ethnicity ethnicity) {
        super(firstName, lastName, birthDate, gender);
        this.bestWorks = new ArrayList<>();
        this.ethnicity = ethnicity;
    }

    // Backward-compatible constructor accepting label
    public Director(String firstName, String lastName, LocalDate birthDate, char gender, String ethnicityLabel) {
        super(firstName, lastName, birthDate, gender);
        this.bestWorks = new ArrayList<>();
        if (ethnicityLabel != null && !ethnicityLabel.isBlank()) {
            try {
                this.ethnicity = Ethnicity.fromLabel(ethnicityLabel);
            } catch (IllegalArgumentException ignored) {
                this.ethnicity = null;
            }
        }
    }

    public List<String> getBestWorks() {
        return new ArrayList<>(bestWorks);
    }

    public void addBestWork(String work) {
        if (work != null && !work.trim().isEmpty()) {
            bestWorks.add(work.trim());
        }
    }

    public Ethnicity getEthnicity() {
        return ethnicity;
    }

    public void setEthnicity(Ethnicity ethnicity) {
        this.ethnicity = ethnicity;
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

    public void setId(int id) {
        this.id = id;
    }

    public void setNationality(String nationality) {
        this.ethnicity = null;
        if (nationality != null && !nationality.trim().isEmpty()) {
            try {
                this.ethnicity = Ethnicity.fromLabel(nationality);
            } catch (IllegalArgumentException ignored) {
                this.ethnicity = null;
            }
        }
    }

    public void setNotableWorks(String notableWorks) {
        this.bestWorks.clear();
        if (notableWorks != null && !notableWorks.trim().isEmpty()) {
            String[] works = notableWorks.split(",");
            for (String work : works) {
                addBestWork(work.trim());
            }
        }
    }


    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public List<String> getNotableWorks() {
        if (notableWorks == null || notableWorks.trim().isEmpty()) {
            return new ArrayList<>();
        }
        return Arrays.stream(notableWorks.split(",")).map(String::trim).collect(Collectors.toList());
    }
}