package com.product.ground_control.shared.api;

import jakarta.persistence.Embeddable;

/**
 * Value Object representing the subject (user/entity) being evaluated.
 * Immutable and validated to ensure all instances are valid.
 */
@Embeddable
public record Subject(String identifier) {

    /**
     * Compact constructor with validation.
     */
    public Subject {
        if (identifier == null || identifier.isBlank()) {
            throw new IllegalArgumentException("Subject identifier cannot be null or blank");
        }
    }

    /**
     * Factory method to create a Subject from a string identifier.
     *
     * @param identifier the subject identifier
     * @return a new Subject instance
     */
    public static Subject of(String identifier) {
        return new Subject(identifier);
    }

    /**
     * Explicit getter for the identifier field.
     * Provided in addition to auto-generated identifier() accessor for clarity.
     *
     * @return the subject identifier
     */
    public String getIdentifier() {
        return identifier;
    }
}
