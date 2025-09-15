package com.papel.imdb_clone.tools;

import com.papel.imdb_clone.exceptions.UserAlreadyExistsException;
import com.papel.imdb_clone.model.User;
import com.papel.imdb_clone.service.AuthService;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.Objects;

public class UserDataRegenerator {

    /**
     * Regenerates user data by reading from a file and registering users.
     * This method reads user information from a text file, creates User objects,
     * and registers them using the AuthService. It handles exceptions for existing users
     * and other registration errors.
     *
     * @param args Command line arguments (not used in this implementation)
     */
    public static void main(String[] args) {
        // Get the base path from classpath resources
        String usersFile = Objects.requireNonNull(UserDataRegenerator.class.getClassLoader()
                .getResource("data/users_updated.txt")).getFile();
        // Get the default password from the AuthService
        String defaultPassword = "Password123";
        AuthService authService = AuthService.getInstance();
        int count = 0;

        System.out.println("Loading users from: " + usersFile);

        try (BufferedReader br = new BufferedReader(new FileReader(usersFile))) {
            String line;
            /**
             * Read each line from the file
             */
            while ((line = br.readLine()) != null) {
                /**
                 * Split the line into parts
                 */
                String[] parts = line.split(",");
                if (parts.length < 5) continue;
                String username = parts[0].trim();
                String firstName = parts[1].trim();
                String lastName = parts[2].trim();
                char gender = parts[3].trim().isEmpty() ? 'U' : parts[3].trim().charAt(0);
                String email = parts[4].trim();
                User user = new User(firstName, lastName, username, gender, email);
                try {
                    /**
                     * Register the user through AuthService
                     */
                    authService.register(user, defaultPassword);
                    count++;
                } catch (UserAlreadyExistsException e) {
                    System.out.println("User already exists: " + username);
                } catch (Exception e) {
                    System.out.println("Failed to register user: " + username + ", error: " + e.getMessage());
                }
            }
        } catch (IOException e) {
            System.out.println("Failed to read users_updated.txt: " + e.getMessage());
        }
        /**
         * Print the number of users registered and the default password
         */
        System.out.println("Done. Registered " + count + " users. Default password: " + defaultPassword);
    }
}
