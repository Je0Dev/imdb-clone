package com.papel.imdb_clone.util;

import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.spec.InvalidKeySpecException;
import java.util.Base64;


/**
 * Utility class for hashing and verifying passwords using PBKDF2.
 * This class cannot be instantiated.
 */
public class PasswordHasher {
    /**
     * Private constructor to prevent instantiation of this utility class.
     */
    private PasswordHasher() {
        throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");
    }
    
    // Constants for the hash function
    private static final int ITERATIONS = 10000; // means the number of times to hash the password to make it more secure
    private static final int KEY_LENGTH = 256; // means the length of the key in bits

    /*
    * PBKDF2WithHmacSHA256 is a key derivation function that uses the PBKDF2 algorithm with HMAC and SHA-256.
    * It is a secure way to generate a key from a password.
    * It works by using a random salt and a number of iterations to generate a key from a password.
    * The salt is a random value that is used to make the hash unique for each user.
    * The iterations are the number of times the hash function is applied to the password.
    * The key length is the length of the key in bits.
     */
    private static final String ALGORITHM = "PBKDF2WithHmacSHA256"; // means the algorithm to use.
    private static final SecureRandom RANDOM = new SecureRandom();

    /**
     * Hashes a password with a random salt.
     *
     * @param password the password to hash
     * @return a string containing the algorithm, iterations, salt, and hash
     */
    public static String hashPassword(String password) {
        byte[] salt = new byte[16];
        RANDOM.nextBytes(salt); // means the salt to use.
        // Generate the hash
        byte[] hash = pbkdf2(password.toCharArray(), salt, KEY_LENGTH);
        return String.format("pbkdf2:%s:%s",
                Base64.getEncoder().encodeToString(salt),
                Base64.getEncoder().encodeToString(hash));
    }

    /**
     * Verifies a password against a stored hash.
     *
     * @param password the password to verify
     * @param storedHash the stored hash to verify against
     * @return true if the password matches the hash, false otherwise
     */
    public static boolean verifyPassword(String password, String storedHash) {
        if (storedHash == null || !storedHash.startsWith("pbkdf2:")) {
            return false;
        }

        // Split the stored hash into algorithm, iterations, salt, and hash
        String[] parts = storedHash.split(":");
        if (parts.length != 3) {
            return false;
        }

        // Extract the salt and hash from the stored hash which helps to verify the password
        byte[] salt = Base64.getDecoder().decode(parts[1]);
        byte[] hash = Base64.getDecoder().decode(parts[2]);
        byte[] testHash = pbkdf2(password.toCharArray(), salt, hash.length * 8);
        
        // Constant time comparison to prevent timing attacks
        int diff = hash.length ^ testHash.length;
        for (int i = 0; i < hash.length && i < testHash.length; i++) {
            diff |= hash[i] ^ testHash[i];
        }
        // Return true if the password matches the hash
        return diff == 0;
    }

    /**
     * Helper method to perform PBKDF2 hashing.
     *
     * @param password  the password to hash
     * @param salt      the salt to use which means random bytes
     * @param keyLength the key length to use which means the length of the key in bits
     * @return the hash of the password
     */
    private static byte[] pbkdf2(char[] password, byte[] salt, int keyLength) {
        try {
            // PBEKeySpec is a key specification for a password-based key
            PBEKeySpec spec = new PBEKeySpec(password, salt, PasswordHasher.ITERATIONS, keyLength);
            SecretKeyFactory factory = SecretKeyFactory.getInstance(ALGORITHM);
            return factory.generateSecret(spec).getEncoded();
        } catch (NoSuchAlgorithmException | InvalidKeySpecException e) {
            throw new RuntimeException("Error hashing password", e);
        }
    }
}
