package com.product.ground_control.shared.api;

import jakarta.persistence.Embeddable;

/**
 * Value Object representing the result of a feature flag evaluation.
 * Immutable and validated to ensure all instances are valid.
 */
@Embeddable
public record EvaluationVariation(String variant) {

    /**
     * Compact constructor with validation.
     */
    public EvaluationVariation {
        if (variant == null || variant.isBlank()) {
            throw new IllegalArgumentException("Evaluation variant cannot be null or blank");
        }
    }

    /**
     * Factory method to create an EvaluationVariation from a string value.
     *
     * @param variant the variation string (e.g., "true", "false", "control", "treatment")
     * @return a new EvaluationVariation instance
     */
    public static EvaluationVariation of(String variant) {
        return new EvaluationVariation(variant);
    }

    /**
     * Explicit getter for the variant field.
     * Provided in addition to auto-generated variant() accessor for clarity.
     *
     * @return the variant value
     */
    public String getVariant() {
        return variant;
    }
}
