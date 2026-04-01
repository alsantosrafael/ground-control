package com.product.ground_control.analytics.infrastructure.messaging;

import com.product.ground_control.analytics.application.services.EventIngestionService;
import com.product.ground_control.analytics.application.services.IngestionSource;
import com.product.ground_control.shared.api.EvaluationVariation;
import com.product.ground_control.shared.api.FeatureKey;
import com.product.ground_control.shared.api.Metadata;
import com.product.ground_control.shared.api.Subject;
import com.product.ground_control.shared.api.ToggleEvaluatedEvent;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Map;

import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class ToggleEvaluatedEventListenerTest {

    @Mock
    private EventIngestionService ingestionService;

    @InjectMocks
    private ToggleEvaluatedEventListener listener;

    @Test
    void onToggleEvaluated_delegatesWithEventDrivenSourceAndEventId() {
        ToggleEvaluatedEvent event = ToggleEvaluatedEvent.of(
            FeatureKey.of("my-feature"),
            EvaluationVariation.of("true"),
            Subject.of("user-123"),
            Metadata.of(Map.of("region", "US"))
        );

        listener.onToggleEvaluated(event);

        verify(ingestionService).ingest(
            IngestionSource.EVENT_DRIVEN,
            event.eventId(),
            event.featureKey(),
            event.variation(),
            event.subject(),
            event.metadata()
        );
    }

    @Test
    void onToggleEvaluated_usesEventIdAsIdempotencyKey() {
        ToggleEvaluatedEvent event = ToggleEvaluatedEvent.of(
            FeatureKey.of("flag-a"),
            EvaluationVariation.of("false"),
            Subject.of("device-456"),
            Metadata.empty()
        );

        listener.onToggleEvaluated(event);

        // The event ID must be passed as the idempotency key — this is the replay-safety contract
        verify(ingestionService).ingest(
            IngestionSource.EVENT_DRIVEN,
            event.eventId(),
            event.featureKey(),
            event.variation(),
            event.subject(),
            event.metadata()
        );
    }
}
