package com.papel.imdb_clone.model;

import com.papel.imdb_clone.enums.Ethnicity;

import java.time.LocalDate;
import java.util.Objects;

public abstract class Celebrity {
    int id;
    String firstName;
    public String lastName;
    private LocalDate birthDate;
    private final char gender;
    private Ethnicity ethnicity;

    public Celebrity(String firstName, String lastName, LocalDate birthDate, char gender) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.birthDate = birthDate;
        this.gender = gender;
    }

    public int getId() {
        return id;
    }

    public String getFirstName() {
        return firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public LocalDate getBirthDate() {
        return birthDate;
    }

    public char getGender() {
        return gender;
    }

    public String getFullName() {
        return firstName + " " + lastName;
    }

    public void setBirthDate(LocalDate birthDate) {
        this.birthDate = birthDate;
    }


    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        Celebrity celebrity = (Celebrity) obj;
        return id == celebrity.id &&
                gender == celebrity.gender &&
                Objects.equals(firstName, celebrity.firstName) &&
                Objects.equals(lastName, celebrity.lastName) &&
                Objects.equals(birthDate, celebrity.birthDate);
    }


    @Override
    public int hashCode() {
        return Objects.hash(id, firstName, lastName, birthDate, gender);
    }

    @Override
    public String toString() {
        return "Celebrity{" +
                "id=" + id +
                ", firstName='" + firstName + '\'' +
                ", lastName='" + lastName + '\'' +
                ", birthDate=" + birthDate +
                ", gender=" + gender +
                '}';
    }

    public void setId(int id) {
        this.id = id;
    }

    public Ethnicity getEthnicity() {
        return ethnicity;
    }

}