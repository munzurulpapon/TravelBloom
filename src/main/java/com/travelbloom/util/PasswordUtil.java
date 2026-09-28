package com.travelbloom.util;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * Simple SHA-256 based password hashing.
 * NOTE: For a real production app you'd want bcrypt/argon2 with a per-user
 * salt. This keeps things dependency-free for a JavaFX desktop project,
 * which is a reasonable trade-off for a local SQLite-backed app like this.
 */
public class PasswordUtil {

    public static String hash(String rawPassword) {

        try {

            MessageDigest digest = MessageDigest.getInstance("SHA-256");

            byte[] hashBytes = digest.digest(rawPassword.getBytes());

            StringBuilder hex = new StringBuilder();

            for (byte b : hashBytes) {
                hex.append(String.format("%02x", b));
            }

            return hex.toString();

        } catch (NoSuchAlgorithmException e) {

            throw new RuntimeException("SHA-256 not available", e);
        }
    }

    public static boolean matches(String rawPassword, String storedHash) {
        return hash(rawPassword).equals(storedHash);
    }
}