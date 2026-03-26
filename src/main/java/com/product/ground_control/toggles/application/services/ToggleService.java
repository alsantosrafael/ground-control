package com.product.ground_control.toggles.application.services;

import com.product.ground_control.toggles.domain.FeatureFlagRepository;

import java.util.Map;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/**
 * Thin Application Service for feature flag evaluation.
 * Acts as a simple entry point to the domain logic.
 */
@Service
@RequiredArgsConstructor
public class ToggleService {

    private final FeatureFlagRepository repository;

    /**
     * Result of a feature flag evaluation, including its type and key metadata.
     */
    public record EvaluationResult(String value, String type, String featureKey) {}

    /**
     * Evaluates a feature flag by its key against the provided context.
     * Returns the result, flag type, and unique key to enable safe downstream caching.
     */
    public Optional<EvaluationResult> evaluate(String featureKey, Map<String, String> context) {
        return repository.findByKey(featureKey)
            .map(toggle -> new EvaluationResult(toggle.evaluate(context), toggle.getType().name(), toggle.getKey()));
    }

}
