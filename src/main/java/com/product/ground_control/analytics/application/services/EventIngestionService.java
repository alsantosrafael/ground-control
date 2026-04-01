package com.product.ground_control.analytics.application.services;

import com.product.ground_control.analytics.domain.AnalyticsEventRepository;
import com.product.ground_control.analytics.domain.entity.AnalyticsEvent;
import com.product.ground_control.shared.api.EvaluationVariation;
import com.product.ground_control.shared.api.FeatureKey;
import com.product.ground_control.shared.api.Metadata;
import com.product.ground_control.shared.api.Subject;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.sql.SQLException;
import java.util.UUID;

/**
 * Service for ingesting analytics events into the Data Pulse.
 * <p>
 * Implements idempotent event processing using unique event IDs as idempotency keys.
 * This service is called by {@link com.product.ground_control.analytics.infrastructure.messaging.ToggleEvaluatedEventListener}
 * which runs via Spring Modulith's @ApplicationModuleListener.
 * <p>
 * <b>Transaction Guarantees (Spring Modulith):</b>
 * <ul>
 *   <li>Listener runs in its own transaction wrapping this service call</li>
 *   <li>If this transaction commits successfully, Spring Modulith marks the event as complete in EVENT_PUBLICATION</li>
 *   <li>If this transaction fails, the event remains incomplete and will be retried</li>
 *   <li>Idempotency key prevents duplicate data during retries</li>
 * </ul>
 * <p>
 * This design eliminates the dual-write problem - there's no scenario where we save analytics
 * data but lose track of event processing state (or vice versa).
 *
 * @see <a href="https://docs.spring.io/spring-modulith/reference/events.html">Spring Modulith Events</a>
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class EventIngestionService {

    /**
     * SQL State code for UNIQUE_VIOLATION constraint violation.
     * This is a standard code across PostgreSQL, H2, MySQL, and other SQL databases.
     */
    private static final String SQLSTATE_UNIQUE_VIOLATION = "23505";

    private final AnalyticsEventRepository eventRepository;

    /**
     * Ingests an analytics event with idempotent behavior.
     * <p>
     * <b>Async Execution:</b> This method runs asynchronously on the virtual thread executor
     * with semaphore-based concurrency control (max 100 concurrent ingestions). When called from
     * the REST API, the response is returned immediately (202 ACCEPTED) while ingestion proceeds
     * asynchronously. For event-driven ingestion, the async task ensures the listener doesn't block.
     * <p>
     * Delegates to source-specific private methods based on the ingestion source:
     * <ul>
     *   <li>{@link IngestionSource#EVENT_DRIVEN} - Event ID from ToggleEvaluatedEvent as idempotency key</li>
     *   <li>{@link IngestionSource#REST_API} - Client-provided idempotency key for retry protection</li>
     * </ul>
     * <p>
     * <b>Transaction Safety:</b> For event-driven ingestion, this async method runs within Spring Modulith's
     * listener transaction. Only if this transaction commits successfully will the event be marked complete.
     *
     * @param source         the ingestion source (event-driven or REST API)
     * @param idempotencyKey the idempotency key (event ID for event-driven, client key for REST API)
     * @param featureKey     the feature key
     * @param variation      the evaluation variation
     * @param subject        the subject
     * @param metadata       the metadata
     * @throws RuntimeException if a non-duplicate database error occurs
     */
    @Async("asyncExecutor")
    @Transactional
    public void ingest(IngestionSource source, UUID idempotencyKey, FeatureKey featureKey,
                       EvaluationVariation variation, Subject subject, Metadata metadata) {
        log.debug("Async ingestion task started [source={}, idempotencyKey={}]", source, idempotencyKey);
        switch (source) {
            case EVENT_DRIVEN -> ingestFromEvent(idempotencyKey, featureKey, variation, subject, metadata);
            case REST_API -> ingestFromRestApi(idempotencyKey, featureKey, variation, subject, metadata);
        }
    }

    /**
     * Handles event-driven ingestion via Spring Modulith @ApplicationModuleListener.
     * <p>
     * <b>Idempotency:</b> Uses the event ID from ToggleEvaluatedEvent as the idempotency key.
     * If the same event is reprocessed (due to retry, replay, or duplicate publish),
     * the unique constraint on idempotency_key will be violated and we skip processing.
     *
     * @param eventId    the unique event ID from ToggleEvaluatedEvent (idempotency key)
     * @param featureKey the feature key
     * @param variation  the evaluation variation
     * @param subject    the subject
     * @param metadata   the metadata
     */
    private void ingestFromEvent(UUID eventId, FeatureKey featureKey, EvaluationVariation variation,
                                  Subject subject, Metadata metadata) {
        try {
            AnalyticsEvent event = AnalyticsEvent.create(eventId, featureKey, variation, subject, metadata);
            eventRepository.save(event);

            log.info("Successfully ingested analytics event [eventId={}, feature={}]",
                eventId, featureKey.getValue());
            log.debug("Analytics event subject [eventId={}, subject={}]", eventId, subject.getIdentifier());

        } catch (DataIntegrityViolationException e) {
            if (isDuplicateKeyViolation(e)) {
                log.debug("Duplicate event detected [eventId={}] - already processed, skipping (idempotent)", eventId);
            } else {
                log.error("Data integrity violation ingesting event [eventId={}, feature={}]: {}",
                    eventId, featureKey.getValue(), e.getMessage(), e);
                throw e;
            }
        }
    }

    /**
     * Handles synchronous REST API ingestion with client-provided idempotency key.
     * <p>
     * <b>Idempotency:</b> Clients should provide their own idempotency key
     * (e.g., request ID, correlation ID via X-Idempotency-Key header)
     * to ensure duplicate requests don't create duplicate data.
     *
     * @param idempotencyKey client-provided idempotency key
     * @param featureKey     the feature key
     * @param variation      the evaluation variation
     * @param subject        the subject
     * @param metadata       the metadata
     */
    private void ingestFromRestApi(UUID idempotencyKey, FeatureKey featureKey, EvaluationVariation variation,
                                    Subject subject, Metadata metadata) {
        try {
            AnalyticsEvent event = AnalyticsEvent.create(idempotencyKey, featureKey, variation, subject, metadata);
            eventRepository.save(event);

            log.info("Successfully ingested analytics event from REST API [idempotencyKey={}, feature={}]",
                idempotencyKey, featureKey.getValue());
            log.debug("Analytics event subject [idempotencyKey={}, subject={}]", idempotencyKey, subject.getIdentifier());

        } catch (DataIntegrityViolationException e) {
            if (isDuplicateKeyViolation(e)) {
                log.debug("Duplicate REST API request [idempotencyKey={}] - already processed, returning success",
                    idempotencyKey);
            } else {
                log.error("Data integrity violation ingesting from REST API [idempotencyKey={}, feature={}]: {}",
                    idempotencyKey, featureKey.getValue(), e.getMessage(), e);
                throw e;
            }
        }
    }

    /**
     * Checks if a DataIntegrityViolationException is due to a unique constraint violation.
     * <p>
     * <b>Database-agnostic detection:</b> Uses SQL SQLSTATE code 23505 (UNIQUE_VIOLATION),
     * which is standard across PostgreSQL, H2, MySQL, and other databases. This approach
     * avoids brittle error message parsing that varies by database vendor.
     *
     * @param e the exception
     * @return true if this is a UNIQUE_VIOLATION (SQLSTATE 23505)
     */
    private boolean isDuplicateKeyViolation(DataIntegrityViolationException e) {
        Throwable cause = e.getCause();
        while (cause != null) {
            if (cause instanceof SQLException sqlEx) {
                String sqlState = sqlEx.getSQLState();
                if (SQLSTATE_UNIQUE_VIOLATION.equals(sqlState)) {
                    return true;
                }
            }
            cause = cause.getCause();
        }
        return false;
    }
}
