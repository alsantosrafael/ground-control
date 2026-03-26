package com.product.ground_control.toggles.application.services;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * Utility to calculate deterministic hashes for percentage-based rollouts.
 * Uses SHA-256 for uniform distribution.
 */
public class ToggleHasher {

    private static final ThreadLocal<MessageDigest> DIGEST_HOLDER = ThreadLocal.withInitial(() -> {
        try {
            return MessageDigest.getInstance("SHA-256");
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("SHA-256 algorithm not found", e);
        }
    });

    /**
     * Calculates a deterministic percentage (0.0 to 100.0) for a given feature key and subject value.
     * The result is in the range [0.0, 100.0).
     */
    public static double calculate(String featureKey, String subjectValue) {
        if (featureKey == null || subjectValue == null) return 0.0;

        MessageDigest digest = DIGEST_HOLDER.get();
        digest.reset();
        
        String input = featureKey + ":" + subjectValue;
        byte[] hashBytes = digest.digest(input.getBytes(StandardCharsets.UTF_8));
        
        // Take the first 8 bytes to form a long for high precision
        long value = 0;
        for (int i = 0; i < 8; i++) {
            value = (value << 8) | (hashBytes[i] & 0xFF);
        }

        // Normalize to [0.0, 100.0)
        // Using unsigned long bits and dividing by the max possible value + 1
        return (double) (value & Long.MAX_VALUE) / (double) Long.MAX_VALUE * 100.0;
    }
}
