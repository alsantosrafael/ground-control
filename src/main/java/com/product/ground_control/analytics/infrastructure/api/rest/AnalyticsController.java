package com.product.ground_control.analytics.infrastructure.api.rest;

import com.product.ground_control.analytics.application.dto.AnalyticsEventRequest;
import com.product.ground_control.analytics.application.services.EventIngestionService;
import com.product.ground_control.analytics.application.services.IngestionSource;
import com.product.ground_control.shared.api.EvaluationVariation;
import com.product.ground_control.shared.api.FeatureKey;
import com.product.ground_control.shared.api.Metadata;
import com.product.ground_control.shared.api.Subject;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

/**
 * REST Controller for analytics event ingestion.
 */
@RestController
@RequestMapping("/api/v1/events")
@RequiredArgsConstructor
@Tag(name = "Analytics", description = "Ingestion endpoint for the Data Pulse")
public class AnalyticsController {

    private final EventIngestionService ingestionService;

    @PostMapping
    @ResponseStatus(HttpStatus.ACCEPTED)
    @Operation(
        summary = "Ingest an analytics event",
        description = "Asynchronously persists an event for the Data Pulse. " +
                     "Supports idempotent retries via X-Idempotency-Key header. " +
                     "If no key is provided, a random key is generated (best-effort, non-idempotent)."
    )
    public void ingest(
            @RequestHeader(value = "X-Idempotency-Key", required = false) UUID idempotencyKey,
            @Valid @RequestBody AnalyticsEventRequest request) {

        // If no idempotency key is provided, generate one (best-effort, non-idempotent by default).
        // Callers that need idempotent retries must supply X-Idempotency-Key.
        UUID effectiveKey = idempotencyKey != null ? idempotencyKey : UUID.randomUUID();

        ingestionService.ingest(
            IngestionSource.REST_API,
            effectiveKey,
            FeatureKey.of(request.getFeatureKey()),
            EvaluationVariation.of(request.getVariation()),
            Subject.of(request.getSubject()),
            Metadata.of(request.getMetadata())
        );
    }
}
