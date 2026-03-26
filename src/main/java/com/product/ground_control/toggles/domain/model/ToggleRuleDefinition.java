package com.product.ground_control.toggles.domain.model;

import com.product.ground_control.toggles.application.services.ToggleHasher;
import java.util.List;
import java.util.Map;

/**
 * Defines a rule within a feature flag.
 * A rule consists of multiple targeting conditions AND a rollout distribution.
 */
public record ToggleRuleDefinition(
    int priority,
    List<ToggleRuleCondition> conditions,
    String result,
    String subject,            // Distribution Key (e.g., "userId")
    Double rolloutPercentage   // [0.0, 100.0]
) {
    public ToggleRuleDefinition {
        if (priority < 1) throw new IllegalArgumentException("Priority must be greater than 0");
    }

    /**
     * Evaluates if this rule applies to the given context and feature key.
     */
    public boolean matches(String featureKey, Map<String, String> context) {
        // 1. Check Targeting Conditions (AND logic)
        boolean targetingMatched = conditions == null || conditions.stream()
            .allMatch(condition -> {
                String actualValue = context.get(condition.property());
                return condition.matches(actualValue);
            });

        if (!targetingMatched) return false;

        // 2. Check Distribution (Rollout)
        if (subject == null || rolloutPercentage == null || rolloutPercentage >= 100.0) {
            return true;
        }

        if (rolloutPercentage <= 0.0) {
            return false;
        }

        String subjectValue = context.get(subject);
        if (subjectValue == null) return false;

        double hash = ToggleHasher.calculate(featureKey, subjectValue);
        return hash < rolloutPercentage;
    }
}
