package com.product.ground_control.toggles.infrastructure.api.rest;

import com.product.ground_control.toggles.application.services.ToggleService;
import com.product.ground_control.toggles.domain.dto.EvalRequest;
import com.product.ground_control.toggles.domain.dto.EvalResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * REST Controller for high-performance feature flag evaluation.
 */
@RestController
@RequestMapping("/api/v1/rules")
@RequiredArgsConstructor
@Tag(name = "Evaluation", description = "Evaluation endpoint for the Toggles")
public class EvaluationController {

    private final ToggleService toggleService;

    @PostMapping("/eval")
    @Operation(summary = "Evaluate a feature flag", description = "Returns the evaluated result for a given feature key and context.")
    public ResponseEntity<EvalResponse> evaluate(@RequestBody EvalRequest request) {
        return toggleService.evaluate(request.featureKey(), request.context())
            .map(result -> ResponseEntity.ok(new EvalResponse(result.value(), result.type(), result.featureKey())))
            .orElseGet(() -> ResponseEntity.notFound().build());
    }
}
