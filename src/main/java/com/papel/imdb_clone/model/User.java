package com.papel.imdb_clone.model;

import com.papel.imdb_clone.exceptions.InvalidEntityException;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDate;
import java.util.regex.Pattern;

/**
 * Represents a user of the application.
 */
public class User implements Serializable {
    //serial version uid to ensure compatibility across different versions for object serialization
    //serialization is the process of converting an object into a byte stream to store it in a file or transmit it over a network
    @Serial
    private static final long serialVersionUID = 1L;

    private int id;
    private String password; // This will store the hashed password
    private String firstName;
    private String lastName;
    private String username;
    private String email;


    /**
     * Email pattern to validate email addresses
     * The email pattern is:^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$
     * This pattern matches:
     * - One or more characters that are letters, digits, plus, underscore, dot, or hyphen
     * - An @ symbol
     * - One or more characters that are letters, digits, dot, or hyphen
     * - A dot
     * - Two or more letters
     */
    private static final Pattern EMAIL_PATTERN = Pattern.compile(
            "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$");
    private LocalDate joinDate;

    /**
     * Constructor for User
     * @param firstName
     * @param lastName
     * @param username
     * @param gender
     * @param email
     */
    public User(String firstName, String lastName, String username, char gender, String email) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.username = username;
        setEmail(email);
    }

    //getters and setters
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

    //set password string with validation
    public void setPassword(String password) {
        if (password == null || password.trim().isEmpty()) {
            throw new IllegalArgumentException("Password cannot be empty");
        }
        this.password = password;
    }

    public String getPassword() {
        return password;
    }

    //set email if it follows-matches the EMAIL_PATTERN above
    public void setEmail(String email) {
        if (email == null || !EMAIL_PATTERN.matcher(email).matches()) {
            throw new InvalidEntityException("Invalid email address: " + email);
        }
        this.email = email;
    }


    @Override
    public String toString() {
        String base = "User{" +
                "id=" + id +
                ", firstName='" + firstName + '\'' +
                ", lastName='" + lastName + '\'' +
                ", username='" + username + '\'' +
                ", email='" + email + '\'' ;

        if (password != null) {
            base += ", password='" + password + '\'';
        }
        return base + '}';
    }

    //set join date for when the user joins the application
    public void setJoinDate(LocalDate joinDate) {
        this.joinDate = joinDate;
    }

}