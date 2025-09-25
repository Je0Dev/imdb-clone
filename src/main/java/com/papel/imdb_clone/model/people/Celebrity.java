package com.papel.imdb_clone.model.people;

import com.papel.imdb_clone.enums.Ethnicity;
import java.time.LocalDate;
import java.util.Objects;
import java.util.List;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.stream.Collectors;

/**
 * Celebrity class is an abstract class that represents a celebrity.
 * Implements proper equality checks and unique ID generation.
 */
public abstract class Celebrity {
    private static int nextId = 1;
    protected int id;
    protected String firstName;
    protected String lastName;
    protected LocalDate birthDate;
    protected char gender;
    private Ethnicity ethnicity;
    protected List<String> notableWorks = new ArrayList<>();

    /**
     * Celebrity constructor with required fields.
     * @param firstName First name of the celebrity
     * @param lastName Last name of the celebrity
     * @param birthDate Birth date (can be null)
     * @param gender Gender (M/F/other)
     */
    protected Celebrity(String firstName, String lastName, LocalDate birthDate, char gender) {
        this.id = nextId++;
        this.firstName = firstName != null ? firstName.trim() : "";
        this.lastName = lastName != null ? lastName.trim() : "";
        this.birthDate = birthDate;
        this.gender = gender;
    }


    public Celebrity(String actorName) {
        this(actorName, "", null, 'U');
        this.id = nextId++;
        this.firstName = actorName;
    }

    // Getters
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
        return String.format("%s %s", firstName, lastName).trim();
    }

    public Ethnicity getEthnicity() {
        return ethnicity;
    }

    // Setters with validation
    public void setEthnicity(Ethnicity ethnicity) {
        this.ethnicity = ethnicity;
    }
    
    public void setFirstName(String firstName) {
        this.firstName = firstName != null ? firstName.trim() : "";
    }

    public void setLastName(String lastName) {
        this.lastName = lastName != null ? lastName.trim() : "";
    }

    public void setBirthDate(LocalDate birthDate) {
        this.birthDate = birthDate;
    }

    public void setGender(char gender) {
        this.gender = gender;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Celebrity celebrity = (Celebrity) o;
        return gender == celebrity.gender &&
               Objects.equals(firstName.toLowerCase(), celebrity.firstName.toLowerCase()) &&
               Objects.equals(lastName.toLowerCase(), celebrity.lastName.toLowerCase()) &&
               Objects.equals(birthDate, celebrity.birthDate);
    }

    @Override
    public int hashCode() {
        return Objects.hash(firstName.toLowerCase(), lastName.toLowerCase(), birthDate, gender);
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + "{" +
                "id=" + id +
                ", firstName='" + firstName + '\'' +
                ", lastName='" + lastName + '\'' +
                ", birthDate=" + birthDate +
                ", gender=" + gender +
                ", ethnicity=" + ethnicity +
                '}';
    }

    public Ethnicity getNationality() {
        return ethnicity;
    }
    
    /**
     * Gets the list of notable works for this celebrity.
     * @return List of notable works
     */
    public List<String> getNotableWorks() {
        return new ArrayList<>(notableWorks);
    }
    
    /**
     * Sets the notable works for this celebrity.
     * @param works List of notable works
     */
    public void setNotableWorks(List<String> works) {
        if (works != null) {
            this.notableWorks = new ArrayList<>(works);
        } else {
            this.notableWorks = new ArrayList<>();
        }
    }
    
    /**
     * Sets the notable works from a comma-separated string.
     * @param worksString Comma-separated string of notable works
     */
    public void setNotableWorks(String worksString) {
        if (worksString != null && !worksString.trim().isEmpty()) {
            this.notableWorks = Arrays.stream(worksString.split(","))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .collect(Collectors.toList());
        } else {
            this.notableWorks = new ArrayList<>();
        }
    }
    
    /**
     * Adds a notable work to the list if it doesn't already exist.
     * @param work The notable work to add
     */
    public void addNotableWork(String work) {
        if (work != null && !work.trim().isEmpty() && !notableWorks.contains(work.trim())) {
            notableWorks.add(work.trim());
        }
    }
    
    /**
     * Generates a unique key for this celebrity used for duplicate detection.
     * @return A string key combining name, birth date, and gender
     */
    public String generateKey() {
        return String.format("%s|%s|%s", 
            firstName != null ? firstName.toLowerCase() : "",
            lastName != null ? lastName.toLowerCase() : "",
            birthDate != null ? birthDate.toString() : ""
        );
    }

    //setter for id
    public void setId(int andIncrement) {
        this.id = andIncrement;
    }
}