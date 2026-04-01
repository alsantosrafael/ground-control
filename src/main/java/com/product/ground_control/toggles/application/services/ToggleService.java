package com.product.ground_control.toggles.application.services;

import com.product.ground_control.shared.api.EvaluationVariation;
import com.product.ground_control.shared.api.FeatureKey;
import com.product.ground_control.shared.api.Metadata;
import com.product.ground_control.shared.api.Subject;
import com.product.ground_control.shared.api.ToggleEvaluatedEvent;
import com.product.ground_control.toggles.domain.FeatureFlagRepository;
import java.time.Instant;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Thin Application Service for feature flag evaluation.
 * Acts as a simple entry point to the domain logic.
 */
@Service
@RequiredArgsConstructor
public class ToggleService {

    private final FeatureFlagRepository repository;
    private final ApplicationEventPublisher eventPublisher;

    /**
     * Result of a feature flag evaluation, including its type and key metadata.
     */
    public record EvaluationResult(String value, String type, String featureKey) {}

    /**
     * Evaluates a feature flag by its key against the provided context.
     * Returns the result, flag type, and unique key to enable safe downstream caching.
     * Transactional to ensure reliable event publication via the Modulith outbox.
     */
    @Transactional
    public Optional<EvaluationResult> evaluate(String featureKey, Map<String, String> context) {
        return repository.findByKey(featureKey)
            .map(toggle -> {
                String resultValue = toggle.evaluate(context);
                
                // Publish evaluation event for analytics and other observers
                eventPublisher.publishEvent(ToggleEvaluatedEvent.of(
                    FeatureKey.of(toggle.getKey()),
                    EvaluationVariation.of(resultValue),
                    Subject.of(context.getOrDefault("subject", "anonymous")),
                    Metadata.of(context.entrySet().stream()
                        .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue)))
                ));
                
                return new EvaluationResult(resultValue, toggle.getType().name(), toggle.getKey());
            });
    }

}
