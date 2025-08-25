package com.papel.imdb_clone.service;

import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.KeySpec;
import java.util.Base64;

/**
 * Service class for password encryption using PBKDF2 algorithm.
 */
public class EncryptionService {
    private static EncryptionService instance;
    private final SecureRandom secureRandom;

    EncryptionService() {
        this.secureRandom = new SecureRandom();
    }

    public static synchronized EncryptionService getInstance() {
        if (instance == null) {
            instance = new EncryptionService();
        }
        return instance;
    }

    /**
     * Hashes a password using PBKDF2 algorithm.
     *
     * @param password Plain text password to hash
     * @return String containing the algorithm, salt and hash in format "pbkdf2:salt:hash"
     */
    public String hashPasswordPBKDF2(String password) {
        try {
            byte[] salt = new byte[16];
            secureRandom.nextBytes(salt);

            KeySpec spec = new PBEKeySpec(password.toCharArray(), salt, 65536, 128);
            SecretKeyFactory factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA1");

            byte[] hash = factory.generateSecret(spec).getEncoded();
            String saltBase64 = Base64.getEncoder().encodeToString(salt);
            String hashBase64 = Base64.getEncoder().encodeToString(hash);

            return "pbkdf2:" + saltBase64 + ":" + hashBase64;
        } catch (NoSuchAlgorithmException | InvalidKeySpecException e) {
            throw new RuntimeException("Error hashing password", e);
        }
    }

    /**
     * Checks if a plain text password matches the hashed password.
     *
     * @param plainPassword  Plain text password to verify
     * @param hashedPassword Hashed password with algorithm identifier
     * @return true if passwords match, false otherwise
     */
    public boolean checkPassword(String plainPassword, String hashedPassword) {
        if (hashedPassword == null || !hashedPassword.startsWith("pbkdf2:")) {
            return false;
        }

        // Parse PBKDF2 hash: pbkdf2:salt:hash
        String[] parts = hashedPassword.split(":");
        if (parts.length != 3) {
            return false;
        }

        try {
            byte[] salt = Base64.getDecoder().decode(parts[1]);
            byte[] expectedHash = Base64.getDecoder().decode(parts[2]);

            KeySpec spec = new PBEKeySpec(plainPassword.toCharArray(), salt, 65536, 128);
            SecretKeyFactory factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA1");
            byte[] actualHash = factory.generateSecret(spec).getEncoded();

            // Compare hashes using constant-time comparison to prevent timing attacks
            if (expectedHash.length != actualHash.length) {
                return false;
            }

            int result = 0;
            for (int i = 0; i < expectedHash.length; i++) {
                result |= expectedHash[i] ^ actualHash[i];
            }

            return result == 0;
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Hashes a password using the default algorithm (PBKDF2).
     *
     * @param password Plain text password to hash
     * @return String containing the hashed password with salt
     */
    public String hashPassword(String password) {
        return hashPasswordPBKDF2(password);
    }

    /**
     * Verifies a password against a stored hash.
     *
     * @param plainPassword  The plain text password to verify
     * @param hashedPassword The stored hashed password
     * @return true if the password matches the hash, false otherwise
     */
    public boolean verifyPassword(String plainPassword, String hashedPassword) {
        return checkPassword(plainPassword, hashedPassword);
    }
}
