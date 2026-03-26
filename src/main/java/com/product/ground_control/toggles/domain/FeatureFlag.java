package com.product.ground_control.toggles.domain;

import com.product.ground_control.toggles.domain.model.ToggleRuleDefinition;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Aggregate Root for Feature Rules.
 * Encapsulates the business logic for feature evaluation (The Cascade).
 */
public record FeatureFlag(
    UUID id,
    String key,
    FeatureType type,
    List<ToggleRuleDefinition> rules,
    String defaultValue,
    LocalDateTime createdAt,
    LocalDateTime updatedAt
) {
    public FeatureFlag {
        if (rules != null) {
            rules = rules.stream()
                .sorted(Comparator.comparingInt(ToggleRuleDefinition::priority))
                .toList();
        } else {
            rules = Collections.emptyList();
        }
    }

    public String evaluate(Map<String, String> context) {
        for (ToggleRuleDefinition rule : rules) {
            if (rule.matches(this.key, context)) {
                return rule.result();
            }
        }
        return defaultValue;
    }

    public static FeatureFlag create(String key, FeatureType type, String defaultValue) {
        var now = LocalDateTime.now();
        return new FeatureFlag(null, key, type, Collections.emptyList(), defaultValue, now, now);
    }
}
