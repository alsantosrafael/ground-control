package com.platform.groundcontrol.application.services

import com.platform.groundcontrol.domain.enums.DataType
import com.platform.groundcontrol.domain.enums.FlagType
import com.platform.groundcontrol.domain.enums.Operator
import com.platform.groundcontrol.domain.enums.Reason
import com.platform.groundcontrol.domain.evaluators.ConditionEvaluator
import com.platform.groundcontrol.domain.valueobjects.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.assertThrows
import org.mockito.Mock
import org.mockito.Mockito.*
import org.mockito.MockitoAnnotations
import java.time.Instant
import java.util.UUID
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

@DisplayName("EvaluationEngineService Tests")
class EvaluationEngineServiceTest {

    @Mock
    private lateinit var mockConditionEvaluator: ConditionEvaluator

    private lateinit var service: EvaluationEngineService

    private val testSubjectId = "user_123"
    private val testFlagCode = "test-feature"

    @BeforeEach
    fun setUp() {
        MockitoAnnotations.openMocks(this)
        service = EvaluationEngineService(listOf(mockConditionEvaluator))
    }

    @Nested
    @DisplayName("Flag Disabled Tests")
    inner class FlagDisabledTests {

        @Test
        fun `should return disabled result when flag is disabled`() {
            val flag = createTestFlag(enabled = false)
            val context = EvaluationContext(testSubjectId)

            val result = service.evaluate(flag, context)

            assertFalse(result.enabled)
            assertNull(result.value)
            assertEquals(Reason.FLAG_DISABLED, result.reason)
            verifyNoInteractions(mockConditionEvaluator)
        }

        @Test
        fun `should return disabled result even with rollout rules when flag is disabled`() {
            val flag = createTestFlag(enabled = false).apply {
                addRolloutRule(createTestRolloutRule())
            }
            val context = EvaluationContext(testSubjectId)

            val result = service.evaluate(flag, context)

            assertFalse(result.enabled)
            assertNull(result.value)
            assertEquals(Reason.FLAG_DISABLED, result.reason)
        }
    }

    @Nested
    @DisplayName("Flag Expiration Tests")
    inner class FlagExpirationTests {

        @Test
        fun `should return expired result when flag is expired`() {
            val expiredTime = Instant.now().minusSeconds(3600)
            val flag = createTestFlag(dueAt = expiredTime)
            val context = EvaluationContext(testSubjectId)

            val result = service.evaluate(flag, context)

            assertFalse(result.enabled)
            assertNull(result.value)
            assertEquals(Reason.FLAG_EXPIRED, result.reason)
            verifyNoInteractions(mockConditionEvaluator)
        }

        @Test
        fun `should evaluate normally when flag is not expired`() {
            val futureTime = Instant.now().plusSeconds(3600)
            val flag = createTestFlag(dueAt = futureTime)
            val context = EvaluationContext(testSubjectId)

            val result = service.evaluate(flag, context)

            assertTrue(result.enabled)
            assertEquals(flag.value, result.value)
            assertEquals(Reason.DEFAULT, result.reason)
        }

        @Test
        fun `should evaluate normally when flag has no due date`() {
            val flag = createTestFlag(dueAt = null)
            val context = EvaluationContext(testSubjectId)

            val result = service.evaluate(flag, context)

            assertTrue(result.enabled)
            assertEquals(flag.value, result.value)
            assertEquals(Reason.DEFAULT, result.reason)
        }
    }

    @Nested
    @DisplayName("Default Value Tests")
    inner class DefaultValueTests {

        @Test
        fun `should return default value when no rollout rules exist`() {
            val flag = createTestFlag()
            val context = EvaluationContext(testSubjectId)

            val result = service.evaluate(flag, context)

            assertTrue(result.enabled)
            assertEquals(flag.value, result.value)
            assertEquals(flag.valueType, result.valueType)
            assertEquals(Reason.DEFAULT, result.reason)
        }

        @Test
        fun `should return default value when no rollout rules are active`() {
            val inactiveRule = createTestRolloutRule(active = false)
            val flag = createTestFlag().apply {
                addRolloutRule(inactiveRule)
            }
            val context = EvaluationContext(testSubjectId)

            val result = service.evaluate(flag, context)

            assertTrue(result.enabled)
            assertEquals(flag.value, result.value)
            assertEquals(Reason.DEFAULT, result.reason)
        }

        @Test
        fun `should return default value when no rollout rules match`() {
            val rule = createTestRolloutRule(percentage = 0.0) // Never matches
            val flag = createTestFlag().apply {
                addRolloutRule(rule)
            }
            val context = EvaluationContext(testSubjectId)

            val result = service.evaluate(flag, context)

            assertTrue(result.enabled)
            assertEquals(flag.value, result.value)
            assertEquals(Reason.DEFAULT, result.reason)
        }
    }

    @Nested
    @DisplayName("Rule Matching Tests")
    inner class RuleMatchingTests {

        @Test
        fun `should return rule value when rule matches`() {
            val rule = createTestRolloutRule(
                percentage = 100.0,
                valueBool = false,
                variantName = "test-variant"
            )
            val flag = createTestFlag().apply {
                addRolloutRule(rule)
            }
            val context = EvaluationContext(testSubjectId)

            val result = service.evaluate(flag, context)

            assertTrue(result.enabled)
            assertEquals(false, result.value)
            assertEquals(flag.valueType, result.valueType)
            assertEquals("test-variant", result.variant)
            assertEquals(Reason.RULE_MATCH, result.reason)
        }

        @Test
        fun `should evaluate rules in priority order`() {
            val lowPriorityRule = createTestRolloutRule(
                priority = 10,
                percentage = 100.0,
                valueBool = false,
                variantName = "low-priority"
            )
            val highPriorityRule = createTestRolloutRule(
                priority = 1,
                percentage = 100.0,
                valueBool = true,
                variantName = "high-priority"
            )
            val flag = createTestFlag().apply {
                addRolloutRule(lowPriorityRule)
                addRolloutRule(highPriorityRule)
            }
            val context = EvaluationContext(testSubjectId)

            val result = service.evaluate(flag, context)

            assertTrue(result.enabled)
            assertEquals(true, result.value) // High priority rule should win
            assertEquals("high-priority", result.variant)
            assertEquals(Reason.RULE_MATCH, result.reason)
        }

        @Test
        fun `should skip rules that don't match percentage`() {
            val rule1 = createTestRolloutRule(
                priority = 1,
                percentage = 0.0, // Never matches
                valueBool = false,
                variantName = "rule-1"
            )
            val rule2 = createTestRolloutRule(
                priority = 2,
                percentage = 100.0, // Always matches
                valueBool = true,
                variantName = "rule-2"
            )
            val flag = createTestFlag().apply {
                addRolloutRule(rule1)
                addRolloutRule(rule2)
            }
            val context = EvaluationContext(testSubjectId)

            val result = service.evaluate(flag, context)

            assertTrue(result.enabled)
            assertEquals(true, result.value) // Rule 2 should match
            assertEquals("rule-2", result.variant)
            assertEquals(Reason.RULE_MATCH, result.reason)
        }
    }

    @Nested
    @DisplayName("Time-based Rule Tests")
    inner class TimeBasedRuleTests {

        @Test
        fun `should skip rule when current time is before startAt`() {
            val futureStart = Instant.now().plusSeconds(3600)
            val rule = createTestRolloutRule(
                percentage = 100.0,
                startAt = futureStart,
                valueBool = false,
                variantName = "future-rule"
            )
            val flag = createTestFlag().apply {
                addRolloutRule(rule)
            }
            val context = EvaluationContext(testSubjectId)

            val result = service.evaluate(flag, context)

            assertTrue(result.enabled)
            assertEquals(flag.value, result.value) // Default value
            assertEquals(Reason.DEFAULT, result.reason)
        }

        @Test
        fun `should skip rule when current time is after endAt`() {
            val pastEnd = Instant.now().minusSeconds(3600)
            val rule = createTestRolloutRule(
                percentage = 100.0,
                endAt = pastEnd,
                valueBool = false,
                variantName = "expired-rule"
            )
            val flag = createTestFlag().apply {
                addRolloutRule(rule)
            }
            val context = EvaluationContext(testSubjectId)

            val result = service.evaluate(flag, context)

            assertTrue(result.enabled)
            assertEquals(flag.value, result.value) // Default value
            assertEquals(Reason.DEFAULT, result.reason)
        }

        @Test
        fun `should evaluate rule when current time is within start and end bounds`() {
            val now = Instant.now()
            val rule = createTestRolloutRule(
                percentage = 100.0,
                startAt = now.minusSeconds(3600),
                endAt = now.plusSeconds(3600),
                valueBool = false,
                variantName = "active-rule"
            )
            val flag = createTestFlag().apply {
                addRolloutRule(rule)
            }
            val context = EvaluationContext(testSubjectId)

            val result = service.evaluate(flag, context)

            assertTrue(result.enabled)
            assertEquals(false, result.value)
            assertEquals("active-rule", result.variant)
            assertEquals(Reason.RULE_MATCH, result.reason)
        }
    }

    @Nested
    @DisplayName("Condition Evaluation Tests")
    inner class ConditionEvaluationTests {

        @Test
        fun `should evaluate rule with conditions when conditions match`() {
            val condition = Condition("plan", Operator.EQUALS, "premium", DataType.STRING)
            val rule = createTestRolloutRule(
                percentage = 100.0,
                valueBool = false,
                variantName = "premium-rule"
            ).apply {
                conditions.add(condition)
            }
            val flag = createTestFlag().apply {
                addRolloutRule(rule)
            }
            val context = EvaluationContext(
                testSubjectId,
                mapOf("plan" to "premium")
            )

            `when`(mockConditionEvaluator.canHandle(Operator.EQUALS, DataType.STRING)).thenReturn(true)
            `when`(mockConditionEvaluator.evaluate("premium", "premium", Operator.EQUALS)).thenReturn(true)

            val result = service.evaluate(flag, context)

            assertTrue(result.enabled)
            assertEquals(false, result.value)
            assertEquals("premium-rule", result.variant)
            assertEquals(Reason.RULE_MATCH, result.reason)

            verify(mockConditionEvaluator).canHandle(Operator.EQUALS, DataType.STRING)
            verify(mockConditionEvaluator).evaluate("premium", "premium", Operator.EQUALS)
        }

        @Test
        fun `should skip rule when conditions don't match`() {
            val condition = Condition("plan", Operator.EQUALS, "premium", DataType.STRING)
            val rule = createTestRolloutRule(
                percentage = 100.0,
                valueBool = false,
                variantName = "premium-rule"
            ).apply {
                conditions.add(condition)
            }
            val flag = createTestFlag().apply {
                addRolloutRule(rule)
            }
            val context = EvaluationContext(
                testSubjectId,
                mapOf("plan" to "basic")
            )

            `when`(mockConditionEvaluator.canHandle(Operator.EQUALS, DataType.STRING)).thenReturn(true)
            `when`(mockConditionEvaluator.evaluate("basic", "premium", Operator.EQUALS)).thenReturn(false)

            val result = service.evaluate(flag, context)

            assertTrue(result.enabled)
            assertEquals(flag.value, result.value) // Default value
            assertEquals(Reason.DEFAULT, result.reason)

            verify(mockConditionEvaluator).canHandle(Operator.EQUALS, DataType.STRING)
            verify(mockConditionEvaluator).evaluate("basic", "premium", Operator.EQUALS)
        }

        @Test
        fun `should skip rule when required attribute is missing`() {
            val condition = Condition("plan", Operator.EQUALS, "premium", DataType.STRING)
            val rule = createTestRolloutRule(
                percentage = 100.0,
                valueBool = false,
                variantName = "premium-rule"
            ).apply {
                conditions.add(condition)
            }
            val flag = createTestFlag().apply {
                addRolloutRule(rule)
            }
            val context = EvaluationContext(testSubjectId) // No plan attribute

            val result = service.evaluate(flag, context)

            assertTrue(result.enabled)
            assertEquals(flag.value, result.value) // Default value
            assertEquals(Reason.DEFAULT, result.reason)

            verifyNoInteractions(mockConditionEvaluator)
        }

        @Test
        fun `should require all conditions to match (AND logic)`() {
            val condition1 = Condition("plan", Operator.EQUALS, "premium", DataType.STRING)
            val condition2 = Condition("score", Operator.GREATER_THAN, 700, DataType.NUMBER)
            val rule = createTestRolloutRule(
                percentage = 100.0,
                valueBool = false,
                variantName = "premium-rule"
            ).apply {
                conditions.add(condition1)
                conditions.add(condition2)
            }
            val flag = createTestFlag().apply {
                addRolloutRule(rule)
            }
            val context = EvaluationContext(
                testSubjectId,
                mapOf("plan" to "premium", "score" to 750)
            )

            `when`(mockConditionEvaluator.canHandle(Operator.EQUALS, DataType.STRING)).thenReturn(true)
            `when`(mockConditionEvaluator.canHandle(Operator.GREATER_THAN, DataType.NUMBER)).thenReturn(true)
            `when`(mockConditionEvaluator.evaluate("premium", "premium", Operator.EQUALS)).thenReturn(true)
            `when`(mockConditionEvaluator.evaluate(750, 700, Operator.GREATER_THAN)).thenReturn(true)

            val result = service.evaluate(flag, context)

            assertTrue(result.enabled)
            assertEquals(false, result.value)
            assertEquals("premium-rule", result.variant)
            assertEquals(Reason.RULE_MATCH, result.reason)
        }

        @Test
        fun `should fail when any condition fails (AND logic)`() {
            val condition1 = Condition("plan", Operator.EQUALS, "premium", DataType.STRING)
            val condition2 = Condition("score", Operator.GREATER_THAN, 700, DataType.NUMBER)
            val rule = createTestRolloutRule(
                percentage = 100.0,
                valueBool = false,
                variantName = "premium-rule"
            ).apply {
                conditions.add(condition1)
                conditions.add(condition2)
            }
            val flag = createTestFlag().apply {
                addRolloutRule(rule)
            }
            val context = EvaluationContext(
                testSubjectId,
                mapOf("plan" to "premium", "score" to 650) // Score too low
            )

            `when`(mockConditionEvaluator.canHandle(Operator.EQUALS, DataType.STRING)).thenReturn(true)
            `when`(mockConditionEvaluator.canHandle(Operator.GREATER_THAN, DataType.NUMBER)).thenReturn(true)
            `when`(mockConditionEvaluator.evaluate("premium", "premium", Operator.EQUALS)).thenReturn(true)
            `when`(mockConditionEvaluator.evaluate(650, 700, Operator.GREATER_THAN)).thenReturn(false)

            val result = service.evaluate(flag, context)

            assertTrue(result.enabled)
            assertEquals(flag.value, result.value) // Default value
            assertEquals(Reason.DEFAULT, result.reason)
        }

        @Test
        fun `should throw exception when no evaluator is found`() {
            val condition = Condition("plan", Operator.EQUALS, "premium", DataType.STRING)
            val rule = createTestRolloutRule(
                percentage = 100.0,
                valueBool = false,
                variantName = "premium-rule"
            ).apply {
                conditions.add(condition)
            }
            val flag = createTestFlag().apply {
                addRolloutRule(rule)
            }
            val context = EvaluationContext(
                testSubjectId,
                mapOf("plan" to "premium")
            )

            `when`(mockConditionEvaluator.canHandle(Operator.EQUALS, DataType.STRING)).thenReturn(false)

            assertThrows<IllegalArgumentException> {
                service.evaluate(flag, context)
            }
        }

        @Test
        fun `should handle evaluator exceptions gracefully`() {
            val condition = Condition("plan", Operator.EQUALS, "premium", DataType.STRING)
            val rule = createTestRolloutRule(
                percentage = 100.0,
                valueBool = false,
                variantName = "premium-rule"
            ).apply {
                conditions.add(condition)
            }
            val flag = createTestFlag().apply {
                addRolloutRule(rule)
            }
            val context = EvaluationContext(
                testSubjectId,
                mapOf("plan" to "premium")
            )

            `when`(mockConditionEvaluator.canHandle(Operator.EQUALS, DataType.STRING)).thenReturn(true)
            `when`(mockConditionEvaluator.evaluate("premium", "premium", Operator.EQUALS))
                .thenThrow(RuntimeException("Evaluator error"))

            val result = service.evaluate(flag, context)

            assertTrue(result.enabled)
            assertEquals(flag.value, result.value) // Default value due to evaluator error
            assertEquals(Reason.DEFAULT, result.reason)
        }
    }

    @Nested
    @DisplayName("Percentage Rollout Tests")
    inner class PercentageRolloutTests {

        @Test
        fun `should be deterministic for same subject and flag`() {
            val rule = createTestRolloutRule(
                percentage = 50.0,
                valueBool = false,
                variantName = "fifty-percent"
            )
            val flag = createTestFlag().apply {
                addRolloutRule(rule)
            }
            val context = EvaluationContext("consistent-user")

            val result1 = service.evaluate(flag, context)
            val result2 = service.evaluate(flag, context)

            // Results should be consistent
            assertEquals(result1.enabled, result2.enabled)
            assertEquals(result1.value, result2.value)
            assertEquals(result1.reason, result2.reason)
        }

        @Test
        fun `should distribute users across percentage buckets`() {
            val rule = createTestRolloutRule(
                percentage = 30.0, // 30% rollout
                valueBool = false,
                variantName = "thirty-percent"
            )
            val flag = createTestFlag().apply {
                addRolloutRule(rule)
            }

            var matchedCount = 0
            val totalUsers = 1000

            for (i in 1..totalUsers) {
                val context = EvaluationContext("user_$i")
                val result = service.evaluate(flag, context)
                
                if (result.reason == Reason.RULE_MATCH) {
                    matchedCount++
                }
            }

            // Should be approximately 30% (allowing for some variance due to hashing)
            val percentage = (matchedCount.toDouble() / totalUsers) * 100
            assertTrue(percentage >= 25.0 && percentage <= 35.0, "Expected ~30%, got $percentage%")
        }
    }

    @Nested
    @DisplayName("Different Value Types Tests")
    inner class ValueTypesTests {

        @Test
        fun `should handle string rule values`() {
            val rule = createTestRolloutRule(
                percentage = 100.0,
                valueString = "custom-value",
                variantName = "string-variant"
            )
            val flag = createTestFlag(valueType = FlagType.STRING).apply {
                addRolloutRule(rule)
            }
            val context = EvaluationContext(testSubjectId)

            val result = service.evaluate(flag, context)

            assertTrue(result.enabled)
            assertEquals("custom-value", result.value)
            assertEquals(FlagType.STRING, result.valueType)
            assertEquals("string-variant", result.variant)
            assertEquals(Reason.RULE_MATCH, result.reason)
        }

        @Test
        fun `should handle integer rule values`() {
            val rule = createTestRolloutRule(
                percentage = 100.0,
                valueInt = 42,
                variantName = "int-variant"
            )
            val flag = createTestFlag(valueType = FlagType.INT).apply {
                addRolloutRule(rule)
            }
            val context = EvaluationContext(testSubjectId)

            val result = service.evaluate(flag, context)

            assertTrue(result.enabled)
            assertEquals(42, result.value)
            assertEquals(FlagType.INT, result.valueType)
            assertEquals("int-variant", result.variant)
            assertEquals(Reason.RULE_MATCH, result.reason)
        }

        @Test
        fun `should handle percentage rule values`() {
            val rule = createTestRolloutRule(
                percentage = 100.0,
                valuePercentage = 75.5,
                variantName = "percentage-variant"
            )
            val flag = createTestFlag(valueType = FlagType.PERCENTAGE).apply {
                addRolloutRule(rule)
            }
            val context = EvaluationContext(testSubjectId)

            val result = service.evaluate(flag, context)

            assertTrue(result.enabled)
            assertEquals(75.5, result.value)
            assertEquals(FlagType.PERCENTAGE, result.valueType)
            assertEquals("percentage-variant", result.variant)
            assertEquals(Reason.RULE_MATCH, result.reason)
        }
    }

    private fun createTestFlag(
        enabled: Boolean = true,
        dueAt: Instant? = null,
        valueType: FlagType = FlagType.BOOLEAN,
        value: Any? = true
    ): FeatureFlag {
        return FeatureFlag(
            id = 1L,
            code = testFlagCode,
            name = "Test Feature",
            description = "Test feature description",
            value = value,
            valueType = valueType,
            enabled = enabled,
            dueAt = dueAt
        )
    }

    private fun createTestRolloutRule(
        percentage: Double? = 100.0,
        priority: Int? = 1,
        active: Boolean = true,
        valueBool: Boolean? = null,
        valueString: String? = null,
        valueInt: Int? = null,
        valuePercentage: Double? = null,
        variantName: String? = null,
        startAt: Instant? = null,
        endAt: Instant? = null
    ): RolloutRule {
        return RolloutRule(
            id = UUID.randomUUID(),
            featureFlagId = 1L,
            attributeKey = null,
            attributeValue = null,
            percentage = percentage,
            distributionKeyAttribute = null,
            valueBool = valueBool,
            valueString = valueString,
            valueInt = valueInt,
            valuePercentage = valuePercentage,
            variantName = variantName,
            startAt = startAt,
            endAt = endAt,
            priority = priority,
            active = active
        )
    }
}