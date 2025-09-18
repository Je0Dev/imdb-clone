package com.papel.imdb_clone.model;

import com.papel.imdb_clone.enums.Ethnicity;

import java.time.LocalDate;

/**
 * Celebrity class is an abstract class that represents a celebrity.
 */
public abstract class Celebrity {
    int id;
    String firstName;
    public String lastName;
    private LocalDate birthDate;
    private char gender;
    private Ethnicity ethnicity;

    //Celebrity constructor
    public Celebrity(String firstName, String lastName, LocalDate birthDate, char gender) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.birthDate = birthDate;
        this.gender = gender;
    }

    //getters
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

    public Ethnicity getEthnicity() {
        return ethnicity;
    }

    //setters
    public void setId(int id) {
        this.id = id;
    }

    public void setEthnicity(Ethnicity ethnicity) {
        this.ethnicity = ethnicity;
    }
    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public void setBirthDate(LocalDate birthDate) {
        this.birthDate = birthDate;
    }

    public void setGender(char gender) {
        this.gender = gender;
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

    public Ethnicity getNationality() {
        return ethnicity;
    }
}