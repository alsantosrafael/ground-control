package com.product.ground_control.toggles.application;

import com.product.ground_control.toggles.domain.FeatureFlag;
import java.util.Map;
import org.springframework.stereotype.Service;

/**
 * Application service for orchestrating feature evaluations.
 */
@Service
public class ToggleEvaluator {

    /**
     * Evaluates a feature rule aggregate against a given context.
     */
    public String evaluate(FeatureFlag feature, Map<String, String> context) {
        if (feature == null) return null;
        return feature.evaluate(context);
    }
}
