package com.example.mobileapp;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;

public class SecurityUtils {

    // Generate SHA-256 hash
    public static String sha256(String input) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(input.getBytes(StandardCharsets.UTF_8));

            StringBuilder hexString = new StringBuilder();

            for (byte b : hash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) {
                    hexString.append('0');
                }
                hexString.append(hex);
            }

            return hexString.toString().toUpperCase();

        } catch (Exception e) {
            throw new RuntimeException("Hashing error", e); // better handling
        }
    }

    // Generate short token (16 chars)
    public static String shortToken(String input) {
        String fullHash = sha256(input);
        return fullHash.substring(0, Math.min(16, fullHash.length()));
    }

    // 🔐 NEW: Optional method (for future upgrade)
    public static String generateSecureToken(String atmData, long timestamp) {
        return shortToken(atmData + "|" + timestamp + "_MOBILE_SECRET");
    }
}