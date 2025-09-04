package com.platform.groundcontrol.domain.valueobjects

import com.platform.groundcontrol.domain.enums.EvaluationType
import com.platform.groundcontrol.domain.enums.FlagType
import com.platform.groundcontrol.domain.enums.Reason
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.DisplayName
import kotlin.test.assertEquals
import kotlin.test.assertTrue

@DisplayName("Evaluation Classes Tests")
class EvaluationTest {

    @Nested
    @DisplayName("EvaluationContext Tests")
    inner class EvaluationContextTests {

        @Test
        fun `should create evaluation context with subject and attributes`() {
            val attributes = mapOf(
                "plan" to "premium",
                "score" to 750,
                "active" to true
            )
            val context = EvaluationContext("user_123", attributes)

            assertEquals("user_123", context.subjectId)
            assertEquals(attributes, context.attributes)
        }

        @Test
        fun `should create evaluation context with empty attributes by default`() {
            val context = EvaluationContext("user_456")

            assertEquals("user_456", context.subjectId)
            assertEquals(emptyMap(), context.attributes)
        }

        @Test
        fun `should get distribution key using subjectId when attribute is null`() {
            val context = EvaluationContext("user_123", mapOf("plan" to "premium"))

            val distributionKey = context.getDistributionKey(null)

            assertEquals("user_123", distributionKey)
        }

        @Test
        fun `should get distribution key from attributes when attribute name is provided`() {
            val context = EvaluationContext(
                "user_123",
                mapOf("orgId" to "org_456", "plan" to "premium")
            )

            val distributionKey = context.getDistributionKey("orgId")

            assertEquals("org_456", distributionKey)
        }

        @Test
        fun `should fall back to subjectId when attribute is not found`() {
            val context = EvaluationContext(
                "user_123",
                mapOf("plan" to "premium")
            )

            val distributionKey = context.getDistributionKey("orgId")

            assertEquals("user_123", distributionKey)
        }

        @Test
        fun `should convert attribute value to string for distribution key`() {
            val context = EvaluationContext(
                "user_123",
                mapOf("orgId" to 12345, "active" to true)
            )

            assertEquals("12345", context.getDistributionKey("orgId"))
            assertEquals("true", context.getDistributionKey("active"))
        }

        @Test
        fun `should handle complex attribute values`() {
            val context = EvaluationContext(
                "user_123",
                mapOf(
                    "tags" to listOf("vip", "beta"),
                    "metadata" to mapOf("tier" to "gold"),
                    "score" to 85.5
                )
            )

            assertEquals("user_123", context.subjectId)
            assertTrue(context.attributes["tags"] is List<*>)
            assertTrue(context.attributes["metadata"] is Map<*, *>)
            assertEquals(85.5, context.attributes["score"])
        }

        @Test
        fun `should create evaluation context with null subjectId for global evaluation`() {
            val context = EvaluationContext()

            assertEquals(null, context.subjectId)
            assertEquals(emptyMap(), context.attributes)
        }

        @Test
        fun `should create evaluation context with attributes but no subjectId`() {
            val attributes = mapOf("feature" to "enabled", "version" to "1.0")
            val context = EvaluationContext(attributes = attributes)

            assertEquals(null, context.subjectId)
            assertEquals(attributes, context.attributes)
        }

        @Test
        fun `should return null distribution key when subjectId is null`() {
            val context = EvaluationContext(attributes = mapOf("plan" to "premium"))

            val distributionKey = context.getDistributionKey(null)

            assertEquals(null, distributionKey)
        }

        @Test
        fun `should return attribute value when subjectId is null but attribute exists`() {
            val context = EvaluationContext(attributes = mapOf("userId" to "fallback_123"))

            val distributionKey = context.getDistributionKey("userId")

            assertEquals("fallback_123", distributionKey)
        }

        @Test
        fun `should return null when both subjectId and requested attribute are null`() {
            val context = EvaluationContext(attributes = mapOf("plan" to "premium"))

            val distributionKey = context.getDistributionKey("nonexistent")

            assertEquals(null, distributionKey)
        }
    }

    @Nested
    @DisplayName("EvaluationResult Tests")
    inner class EvaluationResultTests {

        @Test
        fun `should create evaluation result with all properties`() {
            val result = EvaluationResult(
                enabled = true,
                value = "premium-feature",
                valueType = FlagType.STRING,
                variant = "premium-variant",
                evaluationType = EvaluationType.DEFAULT,
                reason = Reason.RULE_MATCH
            )

            assertTrue(result.enabled)
            assertEquals("premium-feature", result.value)
            assertEquals(FlagType.STRING, result.valueType)
            assertEquals("premium-variant", result.variant)
            assertEquals(EvaluationType.DEFAULT, result.evaluationType)
            assertEquals(Reason.RULE_MATCH, result.reason)
        }

        @Test
        fun `should create evaluation result with minimal properties`() {
            val result = EvaluationResult(
                enabled = false,
                value = null
            )

            assertEquals(false, result.enabled)
            assertEquals(null, result.value)
            assertEquals(null, result.valueType)
            assertEquals(null, result.variant)
            assertEquals(EvaluationType.DEFAULT, result.evaluationType) // default value
            assertEquals(Reason.DEFAULT, result.reason) // default value
        }

        @Test
        fun `should handle different value types`() {
            val booleanResult = EvaluationResult(true, true, FlagType.BOOLEAN)
            val stringResult = EvaluationResult(true, "test", FlagType.STRING)
            val intResult = EvaluationResult(true, 42, FlagType.INT)
            val percentageResult = EvaluationResult(true, 75.5, FlagType.PERCENTAGE)

            assertEquals(true, booleanResult.value)
            assertEquals(FlagType.BOOLEAN, booleanResult.valueType)

            assertEquals("test", stringResult.value)
            assertEquals(FlagType.STRING, stringResult.valueType)

            assertEquals(42, intResult.value)
            assertEquals(FlagType.INT, intResult.valueType)

            assertEquals(75.5, percentageResult.value)
            assertEquals(FlagType.PERCENTAGE, percentageResult.valueType)
        }

        @Test
        fun `should handle different evaluation reasons`() {
            val disabledResult = EvaluationResult(false, null, reason = Reason.FLAG_DISABLED)
            val expiredResult = EvaluationResult(false, null, reason = Reason.FLAG_EXPIRED)
            val defaultResult = EvaluationResult(true, "default", reason = Reason.DEFAULT)
            val ruleMatchResult = EvaluationResult(true, "custom", reason = Reason.RULE_MATCH)

            assertEquals(Reason.FLAG_DISABLED, disabledResult.reason)
            assertEquals(Reason.FLAG_EXPIRED, expiredResult.reason)
            assertEquals(Reason.DEFAULT, defaultResult.reason)
            assertEquals(Reason.RULE_MATCH, ruleMatchResult.reason)
        }

        @Test
        fun `should support variants for A B testing`() {
            val controlResult = EvaluationResult(
                enabled = true,
                value = "feature_v1",
                variant = "control"
            )

            val treatmentResult = EvaluationResult(
                enabled = true,
                value = "feature_v2",
                variant = "treatment"
            )

            assertEquals("control", controlResult.variant)
            assertEquals("treatment", treatmentResult.variant)
        }

        @Test
        fun `should represent disabled flag evaluation`() {
            val result = EvaluationResult(
                enabled = false,
                value = null,
                reason = Reason.FLAG_DISABLED
            )

            assertEquals(false, result.enabled)
            assertEquals(null, result.value)
            assertEquals(Reason.FLAG_DISABLED, result.reason)
        }

        @Test
        fun `should represent expired flag evaluation`() {
            val result = EvaluationResult(
                enabled = false,
                value = null,
                reason = Reason.FLAG_EXPIRED
            )

            assertEquals(false, result.enabled)
            assertEquals(null, result.value)
            assertEquals(Reason.FLAG_EXPIRED, result.reason)
        }

        @Test
        fun `should represent successful rule match evaluation`() {
            val result = EvaluationResult(
                enabled = true,
                value = "special_feature",
                valueType = FlagType.STRING,
                variant = "premium_users",
                reason = Reason.RULE_MATCH
            )

            assertTrue(result.enabled)
            assertEquals("special_feature", result.value)
            assertEquals(FlagType.STRING, result.valueType)
            assertEquals("premium_users", result.variant)
            assertEquals(Reason.RULE_MATCH, result.reason)
        }

        @Test
        fun `should represent default value evaluation`() {
            val result = EvaluationResult(
                enabled = true,
                value = true,
                valueType = FlagType.BOOLEAN,
                reason = Reason.DEFAULT
            )

            assertTrue(result.enabled)
            assertEquals(true, result.value)
            assertEquals(FlagType.BOOLEAN, result.valueType)
            assertEquals(Reason.DEFAULT, result.reason)
        }
    }

    @Nested
    @DisplayName("Real-world Usage Scenarios")
    inner class UsageScenariosTests {

        @Test
        fun `should support enterprise user context`() {
            val context = EvaluationContext(
                "enterprise_user_789",
                mapOf(
                    "organizationId" to "acme_corp",
                    "tier" to "enterprise",
                    "employees" to 5000,
                    "industry" to "finance",
                    "contractValue" to 150000.0,
                    "supportLevel" to "premium"
                )
            )

            assertEquals("enterprise_user_789", context.subjectId)
            assertEquals("acme_corp", context.attributes["organizationId"])
            assertEquals(5000, context.attributes["employees"])
            assertEquals(150000.0, context.attributes["contractValue"])
        }

        @Test
        fun `should support mobile app context`() {
            val context = EvaluationContext(
                "device_abc123",
                mapOf(
                    "os" to "ios",
                    "version" to "15.2",
                    "deviceType" to "iphone",
                    "premium" to true,
                    "lastSeen" to "2024-01-15T14:30:00Z"
                )
            )

            assertEquals("device_abc123", context.subjectId)
            assertEquals("ios", context.attributes["os"])
            assertEquals(true, context.attributes["premium"])
        }

        @Test
        fun `should support gradual rollout result`() {
            val result = EvaluationResult(
                enabled = true,
                value = "new_dashboard_v2",
                valueType = FlagType.STRING,
                variant = "25_percent_rollout",
                evaluationType = EvaluationType.DEFAULT,
                reason = Reason.RULE_MATCH
            )

            assertTrue(result.enabled)
            assertEquals("new_dashboard_v2", result.value)
            assertEquals("25_percent_rollout", result.variant)
            assertEquals(Reason.RULE_MATCH, result.reason)
        }

        @Test
        fun `should support feature kill switch result`() {
            val result = EvaluationResult(
                enabled = false,
                value = null,
                reason = Reason.FLAG_DISABLED
            )

            assertEquals(false, result.enabled)
            assertEquals(null, result.value)
            assertEquals(Reason.FLAG_DISABLED, result.reason)
        }
    }
}