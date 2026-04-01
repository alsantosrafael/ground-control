package com.product.ground_control.analytics.infrastructure.api.rest;

import com.product.ground_control.analytics.application.dto.AnalyticsEventRequest;
import com.product.ground_control.analytics.application.services.EventIngestionService;
import com.product.ground_control.shared.api.EvaluationVariation;
import com.product.ground_control.shared.api.FeatureKey;
import com.product.ground_control.shared.api.Metadata;
import com.product.ground_control.shared.api.Subject;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

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
    @Operation(summary = "Ingest an analytics event", description = "Asynchronously persists an event for the Data Pulse.")
    public void ingest(@RequestBody AnalyticsEventRequest request) {
        ingestionService.ingest(
            FeatureKey.of(request.getFeatureKey()),
            EvaluationVariation.of(request.getVariation()),
            Subject.of(request.getSubject()),
            Metadata.of(request.getMetadata())
        );
    }
}
