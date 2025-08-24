package com.papel.imdb_clone.model;

import com.papel.imdb_clone.exceptions.InvalidEntityException;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDate;
import java.util.Objects;
import java.util.regex.Pattern;

public class User implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    private int id;
    private String password; // This will store the hashed password
    private String firstName;
    private String lastName;
    private String username;
    private String email;
    private final char gender;


    private static final Pattern EMAIL_PATTERN = Pattern.compile(
            "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$");
    private boolean active;
    private boolean admin;
    private LocalDate joinDate;
    private LocalDate lastLogin;
    private String accountStatus;
    private LocalDate birthDate;
    private String country;

    public User(String firstName, String lastName, String username, char gender, String email) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.username = username;
        this.gender = gender;
        setEmail(email);
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getUsername() {
        return username;
    }

    public String getEmail() {
        return email;
    }


    public void setUsername(String username) {
        this.username = username;
    }

    public void setPassword(String password) {
        if (password == null || password.trim().isEmpty()) {
            throw new IllegalArgumentException("Password cannot be empty");
        }
        this.password = password;
    }

    public String getPassword() {
        return password;
    }

    public void setEmail(String email) {
        if (email == null || !EMAIL_PATTERN.matcher(email).matches()) {
            throw new InvalidEntityException("Invalid email address: " + email);
        }
        this.email = email;
    }


    /**
     * Overrides the default `equals` method to provide a meaningful comparison between `User` objects.
     * Two `User` objects are considered equal if all their significant fields match.
     *
     * @param obj The object to compare with this `User` object.
     * @return `true` if the given object is a `User` and all its significant fields
     * (id, gender, username, firstName, lastName, email) are equal to this `User`'s fields;
     * `false` otherwise.
     */

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        User user = (User) obj;
        return id == user.id &&
                gender == user.gender &&
                Objects.equals(username, user.username) &&
                Objects.equals(firstName, user.firstName) &&
                Objects.equals(lastName, user.lastName) &&
                Objects.equals(email, user.email);
    }

    /**
     * Overrides the default `hashCode` method, providing a hash code consistent with the `equals` method.
     * This ensures that if two `User` objects are equal according to `equals()`, they will have the same hash code.
     * It's crucial for correct functionality in hash-based collections like `HashMap` and `HashSet`.
     *
     * @return A hash code value for this `User` object, calculated from all significant fields
     * (id, firstName, lastName, username, email, gender).
     */
    @Override
    public int hashCode() {
        return Objects.hash(id, firstName, lastName, username, email, gender);
    }

    @Override
    public String toString() {
        String base = "User{" +
                "id=" + id +
                ", firstName='" + firstName + '\'' +
                ", lastName='" + lastName + '\'' +
                ", username='" + username + '\'' +
                ", email='" + email + '\'' +
                ", gender=" + gender;

        if (password != null) {
            base += ", password='" + password + '\'';
        }
        return base + '}';
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public boolean isActive() {
        return active;
    }

    public void setAdmin(boolean admin) {
        this.admin = admin;
    }

    public boolean isAdmin() {
        return admin;
    }

    public void setJoinDate(LocalDate joinDate) {
        this.joinDate = joinDate;
    }

    public void setLastLogin(LocalDate lastLogin) {
        this.lastLogin = lastLogin;
    }

    public void setAccountStatus(String accountStatus) {
        if (accountStatus == null || accountStatus.trim().isEmpty()) {
            throw new IllegalArgumentException("Account status cannot be empty");
        }
        this.accountStatus = accountStatus;
    }

    public void setBirthDate(LocalDate birthDate) {
        this.birthDate = birthDate;
    }

    public void setCountry(String country) {
        this.country = country;
    }
}