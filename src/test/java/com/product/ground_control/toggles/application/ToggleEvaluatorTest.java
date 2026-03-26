package com.product.ground_control.toggles.application;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.product.ground_control.toggles.domain.FeatureFlag;
import com.product.ground_control.toggles.domain.FeatureType;
import com.product.ground_control.toggles.domain.model.Operator;
import com.product.ground_control.toggles.domain.model.ToggleRuleCondition;
import com.product.ground_control.toggles.domain.model.ToggleRuleDefinition;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class ToggleEvaluatorTest {

    private final ToggleEvaluator evaluator = new ToggleEvaluator();

    @Test
    void shouldReturnDefaultValueWhenNoRulesMatch() {
        var feature = new FeatureFlag(
            UUID.randomUUID(),
            "test_feature",
            FeatureType.BOOLEAN,
            List.of(),
            "false",
            LocalDateTime.now(),
            LocalDateTime.now()
        );

        String result = evaluator.evaluate(feature, Map.of());

        assertEquals("false", result);
    }

    @Test
    void shouldReturnFirstMatchingRuleValueWithNoDistribution() {
        var rule1 = new ToggleRuleDefinition(
            1,
            List.of(new ToggleRuleCondition("region", Operator.EQUALS, "BR")),
            "true_from_br",
            null,
            null
        );
        var rule2 = new ToggleRuleDefinition(
            2,
            List.of(new ToggleRuleCondition("tier", Operator.EQUALS, "VIP")),
            "true_from_vip",
            null,
            null
        );

        var feature = new FeatureFlag(
            UUID.randomUUID(),
            "test_feature",
            FeatureType.BOOLEAN,
            List.of(rule1, rule2),
            "false",
            LocalDateTime.now(),
            LocalDateTime.now()
        );

        assertEquals("true_from_br", evaluator.evaluate(feature, Map.of("region", "BR", "tier", "VIP")));
        assertEquals("true_from_vip", evaluator.evaluate(feature, Map.of("region", "US", "tier", "VIP")));
    }

    @Test
    void shouldHandleRuleLevelDistribution() {
        // 10% rollout for EVERYONE based on userId
        var rule = new ToggleRuleDefinition(
            1,
            List.of(), // No targeting, matches everyone
            "enabled",
            "userId",
            10.0
        );

        var feature = new FeatureFlag(
            null,
            "rollout_feature",
            FeatureType.BOOLEAN,
            List.of(rule),
            "disabled",
            LocalDateTime.now(),
            LocalDateTime.now()
        );

        // Find users in and out of the 10%
        String userIn = null;
        String userOut = null;
        for (int i = 0; i < 1000; i++) {
            String u = "user_" + i;
            double hash = ToggleHasher.calculate("rollout_feature", u);
            if (hash < 10.0 && userIn == null) userIn = u;
            if (hash >= 10.0 && userOut == null) userOut = u;
            if (userIn != null && userOut != null) break;
        }

        assertEquals("enabled", evaluator.evaluate(feature, Map.of("userId", userIn)));
        assertEquals("disabled", evaluator.evaluate(feature, Map.of("userId", userOut)));
    }

    @Test
    void shouldHandleTargetedRollout() {
        // 50% rollout ONLY for VIP users based on userId
        var rule = new ToggleRuleDefinition(
            1,
            List.of(new ToggleRuleCondition("tier", Operator.EQUALS, "VIP")),
            "enabled",
            "userId",
            50.0
        );

        var feature = new FeatureFlag(
            null,
            "targeted_rollout",
            FeatureType.BOOLEAN,
            List.of(rule),
            "disabled",
            LocalDateTime.now(),
            LocalDateTime.now()
        );

        // Find a VIP user in the 50%
        String vipIn = null;
        String vipOut = null;
        for (int i = 0; i < 1000; i++) {
            String u = "user_" + i;
            double hash = ToggleHasher.calculate("targeted_rollout", u);
            if (hash < 50.0 && vipIn == null) vipIn = u;
            if (hash >= 50.0 && vipOut == null) vipOut = u;
            if (vipIn != null && vipOut != null) break;
        }

        // VIP users match targeting, but follow distribution
        assertEquals("enabled", evaluator.evaluate(feature, Map.of("tier", "VIP", "userId", vipIn)));
        assertEquals("disabled", evaluator.evaluate(feature, Map.of("tier", "VIP", "userId", vipOut)));

        // Non-VIP users don't even get to distribution, they fail targeting
        assertEquals("disabled", evaluator.evaluate(feature, Map.of("tier", "BASIC", "userId", vipIn)));
    }

    @ParameterizedTest
    @CsvSource({
        "GREATER_THAN, 20, 18, true",
        "GREATER_THAN, 18, 20, false",
        "GREATER_THAN, 18, 18, false",
        "GREATER_OR_EQUAL, 18, 18, true",
        "LESS_THAN, 15, 18, true",
        "LESS_OR_EQUAL, 18, 18, true",
        "NOT_EQUALS, v2, v1, true",
        "NOT_EQUALS, v1, v1, false",
        "EQUALS, v1, v1, true",
        "CONTAINS, beta-v1, beta, true"
    })
    void shouldVerifyOperator(Operator operator, String actual, String expected, boolean shouldMatch) {
        var condition = new ToggleRuleCondition("prop", operator, expected);
        assertEquals(shouldMatch, condition.matches(actual), 
            String.format("Failed for %s: %s vs %s", operator, actual, expected));
    }
}
