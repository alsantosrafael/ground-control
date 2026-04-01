package com.product.ground_control.analytics.application;

import com.product.ground_control.analytics.application.services.EventIngestionService;
import com.product.ground_control.analytics.application.services.IngestionSource;
import com.product.ground_control.analytics.domain.AnalyticsEventRepository;
import com.product.ground_control.analytics.domain.entity.AnalyticsEvent;
import com.product.ground_control.shared.api.EvaluationVariation;
import com.product.ground_control.shared.api.FeatureKey;
import com.product.ground_control.shared.api.Metadata;
import com.product.ground_control.shared.api.Subject;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;

import java.sql.SQLException;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class EventIngestionServiceTest {

    @Mock
    private AnalyticsEventRepository eventRepository;

    @InjectMocks
    private EventIngestionService service;

    private final UUID eventId = UUID.randomUUID();
    private final FeatureKey featureKey = FeatureKey.of("my-feature");
    private final EvaluationVariation variation = EvaluationVariation.of("true");
    private final Subject subject = Subject.of("user-123");
    private final Metadata metadata = Metadata.of(Map.of("region", "US"));

    // --- EVENT_DRIVEN ingestion ---

    @Test
    void eventDriven_savesEvent() {
        service.ingest(IngestionSource.EVENT_DRIVEN, eventId, featureKey, variation, subject, metadata);

        ArgumentCaptor<AnalyticsEvent> captor = ArgumentCaptor.forClass(AnalyticsEvent.class);
        verify(eventRepository).save(captor.capture());
        assertThat(captor.getValue().getIdempotencyKey()).isEqualTo(eventId.toString());
        assertThat(captor.getValue().getFeatureKey()).isEqualTo(featureKey);
        assertThat(captor.getValue().getVariation()).isEqualTo(variation);
    }

    @Test
    void eventDriven_duplicateKey_silentlySucceeds() {
        doThrow(duplicateKeyException("uq_analytics_idempotency"))
            .when(eventRepository).save(any());

        // Must not throw — idempotent duplicate is expected behavior
        service.ingest(IngestionSource.EVENT_DRIVEN, eventId, featureKey, variation, subject, metadata);
    }

    @Test
    void eventDriven_otherDataIntegrityViolation_rethrows() {
        doThrow(new DataIntegrityViolationException("some other constraint violation"))
            .when(eventRepository).save(any());

        assertThatThrownBy(() ->
            service.ingest(IngestionSource.EVENT_DRIVEN, eventId, featureKey, variation, subject, metadata)
        ).isInstanceOf(DataIntegrityViolationException.class);
    }

    // --- REST_API ingestion ---

    @Test
    void restApi_savesEvent() {
        UUID clientKey = UUID.randomUUID();
        service.ingest(IngestionSource.REST_API, clientKey, featureKey, variation, subject, metadata);

        ArgumentCaptor<AnalyticsEvent> captor = ArgumentCaptor.forClass(AnalyticsEvent.class);
        verify(eventRepository).save(captor.capture());
        assertThat(captor.getValue().getIdempotencyKey()).isEqualTo(clientKey.toString());
    }

    @Test
    void restApi_duplicateKey_silentlySucceeds() {
        UUID clientKey = UUID.randomUUID();
        doThrow(duplicateKeyException("uq_analytics_idempotency"))
            .when(eventRepository).save(any());

        // Idempotent retry — must not throw
        service.ingest(IngestionSource.REST_API, clientKey, featureKey, variation, subject, metadata);
    }

    @Test
    void restApi_otherDataIntegrityViolation_rethrows() {
        UUID clientKey = UUID.randomUUID();
        doThrow(new DataIntegrityViolationException("foreign key violation"))
            .when(eventRepository).save(any());

        assertThatThrownBy(() ->
            service.ingest(IngestionSource.REST_API, clientKey, featureKey, variation, subject, metadata)
        ).isInstanceOf(DataIntegrityViolationException.class);
    }

    // --- Duplicate key detection ---

    @Test
    void duplicateKeyDetection_nullMessage_returnsFalse() {
        // Exception with null message should not be treated as a known duplicate
        doThrow(new DataIntegrityViolationException(null) {})
            .when(eventRepository).save(any());

        assertThatThrownBy(() ->
            service.ingest(IngestionSource.EVENT_DRIVEN, eventId, featureKey, variation, subject, metadata)
        ).isInstanceOf(DataIntegrityViolationException.class);
    }

    // --- Helpers ---

    /**
     * Creates a DataIntegrityViolationException with a proper SQLException cause
     * that has SQLSTATE 23505 (UNIQUE_VIOLATION). This mimics how Spring wraps
     * database exceptions when a unique constraint is violated.
     */
    private DataIntegrityViolationException duplicateKeyException(String constraintName) {
        SQLException sqlEx = new SQLException(
            "duplicate key value violates unique constraint \"" + constraintName + "\"",
            "23505"  // SQLSTATE for UNIQUE_VIOLATION
        );
        return new DataIntegrityViolationException("Could not execute statement", sqlEx);
    }
}
