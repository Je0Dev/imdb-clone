package com.papel.imdb_clone.util;

import com.papel.imdb_clone.service.AuthService;
import com.papel.imdb_clone.model.User;

public class AdminPasswordReset {
    public static void main(String[] args) {
        try {
            // Get the AuthService instance
            AuthService authService = AuthService.getInstance();
            
            // Get the admin user
            User admin = authService.getUsersByUsername().get("admin");
            if (admin == null) {
                System.err.println("Admin user not found!");
                return;
            }
            
            // Set a new password (in a real app, this should be hashed)
            String newPassword = "admin123";
            admin.setPassword(PasswordHasher.hashPassword(newPassword));
            
            // Save the changes
            authService.saveUsers();
            
            System.out.println("Admin password has been reset to: " + newPassword);
            
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
