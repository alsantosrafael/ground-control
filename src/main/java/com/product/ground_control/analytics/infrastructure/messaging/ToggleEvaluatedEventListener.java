package com.product.ground_control.analytics.infrastructure.messaging;

import com.product.ground_control.analytics.application.services.EventIngestionService;
import com.product.ground_control.analytics.application.services.IngestionSource;
import com.product.ground_control.shared.api.ToggleEvaluatedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.modulith.events.ApplicationModuleListener;
import org.springframework.stereotype.Component;

/**
 * Listener for ToggleEvaluatedEvents.
 * Located in infrastructure.messaging layer per Ground Control standards.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class ToggleEvaluatedEventListener {

    private final EventIngestionService ingestionService;

    @ApplicationModuleListener
    public void onToggleEvaluated(ToggleEvaluatedEvent event) {
        log.debug("Received evaluation event [{}] for feature: {}", event.eventId(), event.featureKey());

        ingestionService.ingest(
            IngestionSource.EVENT_DRIVEN,
            event.eventId(),
            event.featureKey(),
            event.variation(),
            event.subject(),
            event.metadata()
        );
    }
}
