package com.product.ground_control.shared.api;

import jakarta.persistence.Embeddable;

/**
 * Value Object representing a unique feature flag identifier.
 * Immutable and validated to ensure all instances are valid.
 */
@Embeddable
public record FeatureKey(String value) {

    /**
     * Compact constructor with validation.
     */
    public FeatureKey {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("Feature key cannot be null or blank");
        }
    }

    /**
     * Factory method to create a FeatureKey from a string value.
     *
     * @param value the feature key string
     * @return a new FeatureKey instance
     */
    public static FeatureKey of(String value) {
        return new FeatureKey(value);
    }

    /**
     * Explicit getter for the value field.
     * Provided in addition to auto-generated value() accessor for clarity.
     *
     * @return the feature key value
     */
    public String getValue() {
        return value;
    }
}
