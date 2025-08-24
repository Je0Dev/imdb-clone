package com.papel.imdb_clone.model;

import com.papel.imdb_clone.enums.Ethnicity;

import java.time.LocalDate;
import java.util.Objects;

public class Actor extends Celebrity {
    private Ethnicity ethnicity;
    // Backward-compatible storage of the original race label used in older tests
    private String raceLabel;
    private String notableWorks;
    private String biography;

    public Actor(String firstName, String lastName, LocalDate birthDate, char gender, Ethnicity ethnicity) {
        super(firstName, lastName, birthDate, gender);
        this.ethnicity = ethnicity;
        this.raceLabel = ethnicity != null ? ethnicity.getLabel() : null;
    }

    // Backward-compatible constructor accepting a race/ethnicity label
    public Actor(String firstName, String lastName, LocalDate birthDate, char gender, String raceLabel) {
        super(firstName, lastName, birthDate, gender);
        if (raceLabel != null && !raceLabel.isBlank()) {
            this.raceLabel = raceLabel;
            try {
                this.ethnicity = Ethnicity.fromLabel(raceLabel);
            } catch (IllegalArgumentException ex) {
                this.ethnicity = null; // Unknown label; keep null to remain backward-compatible
            }
        }
    }

    public Ethnicity getEthnicity() {
        return ethnicity;
    }


    public String getRace() {
        return raceLabel;
    }


    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        if (!super.equals(obj)) return false;
        Actor actor = (Actor) obj;
        return Objects.equals(this.getRace(), actor.getRace()) && Objects.equals(getFirstName(), actor.getFirstName())
                && Objects.equals(getLastName(), actor.getLastName()) && Objects.equals(getBirthDate(), actor.getBirthDate())
                && getGender() == actor.getGender();
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), getRace());
    }

    @Override
    public String toString() {
        return "Actor{" +
                "id=" + getId() +
                ", firstName='" + getFirstName() + '\'' +
                ", lastName='" + getLastName() + '\'' +
                ", birthDate=" + getBirthDate() +
                ", gender=" + getGender() +
                ", race='" + getRace() + "'" +
                '}';
    }

    public void setId(int id) {
        this.id = id;
    }

    public void setNotableWorks(String notableWorks) {
        this.notableWorks = notableWorks;
    }

    public void setBiography(String biography) {
        this.biography = biography;
    }

    public void addMovie(Movie movie) {

    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }
}