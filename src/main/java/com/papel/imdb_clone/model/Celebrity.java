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

    /**
     * Compares this Celebrity object to another object for equality.
     * <p>
     * This method first checks for identity (if both references point to the same object)
     * and then verifies that the other object is a non-null instance of the Celebrity class.
     * Two celebrities are considered equal if their id, gender, first name, last name,
     * and birthdate are all identical.
     *
     * @param obj The object to compare against this one.
     * @return {@code true} if the given object is a Celebrity with the same attribute values,
     * {@code false} otherwise.
     */

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

    /**
     * Generates a hash code for this {@code Person} object.
     * The hash code is computed based on the values of the {@code id}, {@code firstName},
     * {@code lastName}, {@code birthDate}, and {@code gender} fields.
     * This method is consistent with {@link #equals(Object)}, meaning that for any two
     * {@code Person} objects, {@code a} and {@code b}, if {@code a.equals(b)} is true,
     * then {@code a.hashCode()} must be the same as {@code b.hashCode()}.
     *
     * @return A hash code value for this object.
     */
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