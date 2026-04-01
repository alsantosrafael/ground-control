package com.product.ground_control.analytics.domain.entity;

import com.product.ground_control.shared.api.EvaluationVariation;
import com.product.ground_control.shared.api.FeatureKey;
import com.product.ground_control.shared.api.Metadata;
import com.product.ground_control.shared.api.Subject;
import jakarta.persistence.AttributeOverride;
import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.UUID;

/**
 * Domain entity for an analytics event.
 * Follows Ground Control standards: Rich Domain, Instant (UTC), and JPA UUID.
 */
@Entity
@Table(name = "analytics_events")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder(access = AccessLevel.PRIVATE)
public class AnalyticsEvent {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Embedded
    @AttributeOverride(name = "value", column = @Column(name = "feature_key", nullable = false))
    private FeatureKey featureKey;

    @Embedded
    @AttributeOverride(name = "variant", column = @Column(name = "variation", nullable = false))
    private EvaluationVariation variation;

    @Embedded
    @AttributeOverride(name = "identifier", column = @Column(name = "subject", nullable = false))
    private Subject subject;

    @Column(name = "timestamp", nullable = false)
    private Instant timestamp;

    @Column(name = "metadata", nullable = false)
    private Metadata metadata;

    /**
     * Idempotency key to prevent duplicate event processing.
     * Uses the event ID from ToggleEvaluatedEvent - ensures the same event instance
     * (same evaluation) is only processed once, even across retries or replays.
     */
    @Column(name = "idempotency_key", nullable = false, unique = true, length = 36)
    private String idempotencyKey;

    /**
     * Creation method for a new analytics event from Value Objects.
     * Used by event listeners when consuming domain events.
     *
     * @param eventId    the unique event ID from ToggleEvaluatedEvent (for idempotency)
     * @param featureKey the feature key
     * @param variation  the evaluation variation
     * @param subject    the subject
     * @param metadata   the metadata
     * @return a new AnalyticsEvent instance
     */
    public static AnalyticsEvent create(
            UUID eventId,
            FeatureKey featureKey,
            EvaluationVariation variation,
            Subject subject,
            Metadata metadata) {

        return AnalyticsEvent.builder()
            .featureKey(featureKey)
            .variation(variation)
            .subject(subject)
            .timestamp(Instant.now())
            .metadata(metadata)
            .idempotencyKey(eventId.toString())
            .build();
    }
}
