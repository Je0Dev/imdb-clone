package com.papel.imdb_clone.util;

import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.spec.InvalidKeySpecException;
import java.util.Base64;

/**
 * Utility class for hashing and verifying passwords using PBKDF2.
 */
public class PasswordHasher {
    private static final int ITERATIONS = 10000;
    private static final int KEY_LENGTH = 256;
    private static final String ALGORITHM = "PBKDF2WithHmacSHA256";
    private static final SecureRandom RANDOM = new SecureRandom();

    /**
     * Hashes a password with a random salt.
     *
     * @param password the password to hash
     * @return a string containing the algorithm, iterations, salt, and hash
     */
    public static String hashPassword(String password) {
        byte[] salt = new byte[16];
        RANDOM.nextBytes(salt);
        byte[] hash = pbkdf2(password.toCharArray(), salt, ITERATIONS, KEY_LENGTH);
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

        String[] parts = storedHash.split(":");
        if (parts.length != 3) {
            return false;
        }

        byte[] salt = Base64.getDecoder().decode(parts[1]);
        byte[] hash = Base64.getDecoder().decode(parts[2]);
        byte[] testHash = pbkdf2(password.toCharArray(), salt, ITERATIONS, hash.length * 8);
        
        // Constant time comparison to prevent timing attacks
        int diff = hash.length ^ testHash.length;
        for (int i = 0; i < hash.length && i < testHash.length; i++) {
            diff |= hash[i] ^ testHash[i];
        }
        return diff == 0;
    }

    private static byte[] pbkdf2(char[] password, byte[] salt, int iterations, int keyLength) {
        try {
            PBEKeySpec spec = new PBEKeySpec(password, salt, iterations, keyLength);
            SecretKeyFactory factory = SecretKeyFactory.getInstance(ALGORITHM);
            return factory.generateSecret(spec).getEncoded();
        } catch (NoSuchAlgorithmException | InvalidKeySpecException e) {
            throw new RuntimeException("Error hashing password", e);
        }
    }
}
