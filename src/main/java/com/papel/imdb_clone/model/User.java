package com.papel.imdb_clone.model;

import com.papel.imdb_clone.exceptions.InvalidEntityException;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDate;
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


    public void setAdmin(boolean admin) {
        this.admin = admin;
    }

    public void setJoinDate(LocalDate joinDate) {
        this.joinDate = joinDate;
    }

}