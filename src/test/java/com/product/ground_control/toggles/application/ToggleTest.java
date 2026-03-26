package com.product.ground_control.toggles.application;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.product.ground_control.toggles.application.services.ToggleHasher;
import com.product.ground_control.toggles.domain.FeatureType;
import com.product.ground_control.toggles.domain.model.Operator;
import com.product.ground_control.toggles.domain.model.ToggleRuleCondition;
import com.product.ground_control.toggles.domain.model.ToggleRuleDefinition;
import com.product.ground_control.toggles.domain.entity.Toggle;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class ToggleTest {

    @Test
    void shouldReturnDefaultValueWhenNoRulesMatch() {
        var toggle = new Toggle(
            UUID.randomUUID(),
            "test_feature",
            FeatureType.BOOLEAN,
            List.of(),
            "false",
            LocalDateTime.now(),
            LocalDateTime.now()
        );

        String result = toggle.evaluate(Map.of());

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

        var toggle = new Toggle(
            UUID.randomUUID(),
            "test_feature",
            FeatureType.BOOLEAN,
            List.of(rule1, rule2),
            "false",
            LocalDateTime.now(),
            LocalDateTime.now()
        );

        assertEquals("true_from_br", toggle.evaluate(Map.of("region", "BR", "tier", "VIP")));
        assertEquals("true_from_vip", toggle.evaluate(Map.of("region", "US", "tier", "VIP")));
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

        var toggle = new Toggle(
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

        assertEquals("enabled", toggle.evaluate(Map.of("userId", userIn)));
        assertEquals("disabled", toggle.evaluate(Map.of("userId", userOut)));
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

        var toggle = new Toggle(
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
        assertEquals("enabled", toggle.evaluate(Map.of("tier", "VIP", "userId", vipIn)));
        assertEquals("disabled", toggle.evaluate(Map.of("tier", "VIP", "userId", vipOut)));

        // Non-VIP users don't even get to distribution, they fail targeting
        assertEquals("disabled", toggle.evaluate(Map.of("tier", "BASIC", "userId", vipIn)));
    }

    @Test
    void shouldHandlePercentageFeatureType() {
        var rule = new ToggleRuleDefinition(
            1,
            List.of(new ToggleRuleCondition("region", Operator.EQUALS, "US")),
            "75.5",
            null,
            null
        );

        var toggle = new Toggle(
            UUID.randomUUID(),
            "percentage_toggle",
            FeatureType.PERCENTAGE,
            List.of(rule),
            "10.0",
            LocalDateTime.now(),
            LocalDateTime.now()
        );

        assertEquals("75.5", toggle.evaluate(Map.of("region", "US")));
        assertEquals("10.0", toggle.evaluate(Map.of("region", "BR")));
    }

    @Test
    void shouldReturnDefaultValueWhenSubjectIsMissingInContext() {
        // Rule requires 'userId' for 50% rollout
        var rule = new ToggleRuleDefinition(
            1,
            List.of(),
            "enabled",
            "userId",
            50.0
        );

        var toggle = new Toggle(
            null,
            "missing_subject_feature",
            FeatureType.BOOLEAN,
            List.of(rule),
            "fallback_default",
            LocalDateTime.now(),
            LocalDateTime.now()
        );

        // Context is missing 'userId' -> Rule cannot be evaluated -> Falls back to defaultValue
        assertEquals("fallback_default", toggle.evaluate(Map.of("other_prop", "value")));
    }

    @Test
    void shouldReturnRuleResultWhenNoDistributionIsDefined() {
        // Rule with NO subject/rollout matches 100% of targeted users
        var rule = new ToggleRuleDefinition(
            1,
            List.of(new ToggleRuleCondition("region", Operator.EQUALS, "US")),
            "rule_result",
            null,
            null
        );

        var toggle = new Toggle(
            null,
            "no_dist_feature",
            FeatureType.BOOLEAN,
            List.of(rule),
            "fallback_default",
            LocalDateTime.now(),
            LocalDateTime.now()
        );

        assertEquals("rule_result", toggle.evaluate(Map.of("region", "US")));
        assertEquals("fallback_default", toggle.evaluate(Map.of("region", "BR")));
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
