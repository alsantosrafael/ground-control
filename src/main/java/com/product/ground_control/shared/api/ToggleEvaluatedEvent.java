package com.product.ground_control.shared.api;

import java.time.Instant;
import java.util.UUID;

/**
 * Cross-module domain event published when a feature toggle is evaluated.
 * Part of the shared API contract per Spring Modulith architecture.
 *
 * This event enables asynchronous, event-driven communication between the
 * toggles module (producer) and analytics module (consumer), ensuring loose
 * coupling and reliable delivery via the Spring Modulith outbox pattern.
 *
 * The eventId ensures idempotency: the same event instance (same evaluation)
 * will always have the same ID, even across retries or replays.
 */
public record ToggleEvaluatedEvent(
    UUID eventId,
    FeatureKey featureKey,
    EvaluationVariation variation,
    Subject subject,
    Metadata metadata,
    Instant timestamp
) {
    /**
     * Compact constructor with validation.
     * Ensures all event fields are non-null for data integrity.
     */
    public ToggleEvaluatedEvent {
        if (eventId == null || featureKey == null || variation == null || subject == null ||
            metadata == null || timestamp == null) {
            throw new IllegalArgumentException("All event fields must be non-null");
        }
    }

    /**
     * Factory method to create a new event with generated ID.
     * Use this when publishing a new evaluation event.
     */
    public static ToggleEvaluatedEvent of(
            FeatureKey featureKey,
            EvaluationVariation variation,
            Subject subject,
            Metadata metadata) {
        return new ToggleEvaluatedEvent(
            UUID.randomUUID(),
            featureKey,
            variation,
            subject,
            metadata,
            Instant.now()
        );
    }
}
