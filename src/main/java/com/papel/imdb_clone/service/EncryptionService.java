package com.papel.imdb_clone.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
    private static final Logger logger = LoggerFactory.getLogger(EncryptionService.class);
    private static EncryptionService instance;
    private final SecureRandom secureRandom;

    EncryptionService() {
        this.secureRandom = new SecureRandom();
    }

    public static synchronized EncryptionService getInstance() {
        if (instance == null) {
            logger.debug("Creating new instance of EncryptionService");
            instance = new EncryptionService();
        } else {
            logger.trace("Returning existing EncryptionService instance");
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
        logger.debug("Hashing password using PBKDF2");
        try {
            byte[] salt = new byte[16];
            secureRandom.nextBytes(salt);

            KeySpec spec = new PBEKeySpec(password.toCharArray(), salt, 65536, 128);
            SecretKeyFactory factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA1");

            byte[] hash = factory.generateSecret(spec).getEncoded();
            String saltBase64 = Base64.getEncoder().encodeToString(salt);
            String hashBase64 = Base64.getEncoder().encodeToString(hash);
            
            logger.trace("Password hashed successfully");
            return "pbkdf2:" + saltBase64 + ":" + hashBase64;
        } catch (NoSuchAlgorithmException | InvalidKeySpecException e) {
            logger.error("Error hashing password: {}", e.getMessage(), e);
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
        logger.debug("Checking password against hash");
        if (hashedPassword == null) {
            logger.warn("Hashed password is null");
            return false;
        }
        if (!hashedPassword.startsWith("pbkdf2:")) {
            logger.warn("Unsupported hash format: {}", hashedPassword.split(":")[0]);
            return false;
        }

        // Parse PBKDF2 hash: pbkdf2:salt:hash
        String[] parts = hashedPassword.split(":");
        if (parts.length != 3) {
            logger.warn("Invalid hash format: expected 3 parts, got {}", parts.length);
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
                logger.debug("Hash length mismatch: expected {} bytes, got {}", expectedHash.length, actualHash.length);
                return false;
            }

            int result = 0;
            for (int i = 0; i < expectedHash.length; i++) {
                result |= expectedHash[i] ^ actualHash[i];
            }

            boolean match = result == 0;
            logger.trace("Password check result: {}", match ? "MATCH" : "NO MATCH");
            return match;
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
        logger.debug("Hashing password using default algorithm (PBKDF2)");
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
        logger.debug("Verifying password against hash");
        boolean result = checkPassword(plainPassword, hashedPassword);
        logger.debug("Password verification {}", result ? "succeeded" : "failed");
        return result;
    }
}
