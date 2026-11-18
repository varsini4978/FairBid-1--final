package com.fairbid.util;

import org.mindrot.jbcrypt.BCrypt;

/**
 * Utility class for secure password hashing and verification using BCrypt (cost factor 12).
 * Plaintext passwords are NEVER stored or logged.
 */
public final class PasswordUtil {
    private static final int BCRYPT_WORK_FACTOR = 12;

    private PasswordUtil() {
    }

    /**
     * Hashes a plaintext password using BCrypt with a fresh salt.
     *
     * @param plainPassword Raw password entered by the user
     * @return Generated BCrypt hash string
     */
    public static String hash(String plainPassword) {
        if (plainPassword == null || plainPassword.isEmpty()) {
            throw new IllegalArgumentException("Password cannot be null or empty");
        }
        return BCrypt.hashpw(plainPassword, BCrypt.gensalt(BCRYPT_WORK_FACTOR));
    }

    /**
     * Verifies a candidate plaintext password against a stored BCrypt hash.
     *
     * @param candidate Candidate plaintext password
     * @param hashed    Stored BCrypt hash
     * @return true if credentials match, false otherwise
     */
    public static boolean verify(String candidate, String hashed) {
        if (candidate == null || hashed == null || hashed.trim().isEmpty()) {
            return false;
        }
        try {
            return BCrypt.checkpw(candidate, hashed);
        } catch (Exception e) {
            return false;
        }
    }
}
