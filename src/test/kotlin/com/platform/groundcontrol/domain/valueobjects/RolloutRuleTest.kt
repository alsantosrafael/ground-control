package com.platform.groundcontrol.domain.valueobjects

import com.platform.groundcontrol.domain.enums.DataType
import com.platform.groundcontrol.domain.enums.Operator
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.DisplayName
import java.time.Instant
import java.util.UUID
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

@DisplayName("RolloutRule Tests")
class RolloutRuleTest {

    @Nested
    @DisplayName("getRuleValue Tests")
    inner class GetRuleValueTests {

        @Test
        fun `should return boolean value when valueBool is set`() {
            val rule = RolloutRule(
                id = UUID.randomUUID(),
                featureFlagId = 1L,
                attributeKey = null,
                attributeValue = null,
                percentage = 50.0,
                distributionKeyAttribute = null,
                valueBool = true,
                valueString = null,
                valueInt = null,
                valuePercentage = null,
                variantName = "test-variant"
            )

            assertEquals(true, rule.getRuleValue())
        }

        @Test
        fun `should return string value when valueString is set`() {
            val rule = RolloutRule(
                id = UUID.randomUUID(),
                featureFlagId = 1L,
                attributeKey = null,
                attributeValue = null,
                percentage = 50.0,
                distributionKeyAttribute = null,
                valueBool = null,
                valueString = "test-string",
                valueInt = null,
                valuePercentage = null,
                variantName = "test-variant"
            )

            assertEquals("test-string", rule.getRuleValue())
        }

        @Test
        fun `should return int value when valueInt is set`() {
            val rule = RolloutRule(
                id = UUID.randomUUID(),
                featureFlagId = 1L,
                attributeKey = null,
                attributeValue = null,
                percentage = 50.0,
                distributionKeyAttribute = null,
                valueBool = null,
                valueString = null,
                valueInt = 42,
                valuePercentage = null,
                variantName = "test-variant"
            )

            assertEquals(42, rule.getRuleValue())
        }

        @Test
        fun `should return percentage value when valuePercentage is set`() {
            val rule = RolloutRule(
                id = UUID.randomUUID(),
                featureFlagId = 1L,
                attributeKey = null,
                attributeValue = null,
                percentage = 50.0,
                distributionKeyAttribute = null,
                valueBool = null,
                valueString = null,
                valueInt = null,
                valuePercentage = 75.5,
                variantName = "test-variant"
            )

            assertEquals(75.5, rule.getRuleValue())
        }

        @Test
        fun `should return true as default when no value is set`() {
            val rule = RolloutRule(
                id = UUID.randomUUID(),
                featureFlagId = 1L,
                attributeKey = null,
                attributeValue = null,
                percentage = 50.0,
                distributionKeyAttribute = null,
                valueBool = null,
                valueString = null,
                valueInt = null,
                valuePercentage = null,
                variantName = "test-variant"
            )

            assertEquals(true, rule.getRuleValue())
        }

        @Test
        fun `should prioritize boolean over other values`() {
            val rule = RolloutRule(
                id = UUID.randomUUID(),
                featureFlagId = 1L,
                attributeKey = null,
                attributeValue = null,
                percentage = 50.0,
                distributionKeyAttribute = null,
                valueBool = false,
                valueString = "should-not-return-this",
                valueInt = 123,
                valuePercentage = 89.0,
                variantName = "test-variant"
            )

            assertEquals(false, rule.getRuleValue())
        }

        @Test
        fun `should prioritize string over int and percentage when boolean is null`() {
            val rule = RolloutRule(
                id = UUID.randomUUID(),
                featureFlagId = 1L,
                attributeKey = null,
                attributeValue = null,
                percentage = 50.0,
                distributionKeyAttribute = null,
                valueBool = null,
                valueString = "correct-value",
                valueInt = 123,
                valuePercentage = 89.0,
                variantName = "test-variant"
            )

            assertEquals("correct-value", rule.getRuleValue())
        }

        @Test
        fun `should prioritize int over percentage when boolean and string are null`() {
            val rule = RolloutRule(
                id = UUID.randomUUID(),
                featureFlagId = 1L,
                attributeKey = null,
                attributeValue = null,
                percentage = 50.0,
                distributionKeyAttribute = null,
                valueBool = null,
                valueString = null,
                valueInt = 456,
                valuePercentage = 89.0,
                variantName = "test-variant"
            )

            assertEquals(456, rule.getRuleValue())
        }
    }

    @Nested
    @DisplayName("hasConditions Tests")
    inner class HasConditionsTests {

        @Test
        fun `should return false when conditions list is empty`() {
            val rule = RolloutRule(
                id = UUID.randomUUID(),
                featureFlagId = 1L,
                attributeKey = null,
                attributeValue = null,
                percentage = 50.0,
                distributionKeyAttribute = null,
                valueBool = true,
                valueString = null,
                valueInt = null,
                valuePercentage = null,
                variantName = "test-variant",
                conditions = mutableListOf()
            )

            assertFalse(rule.hasConditions())
        }

        @Test
        fun `should return true when conditions list has elements`() {
            val condition = Condition(
                attribute = "plan",
                operator = Operator.EQUALS,
                value = "premium",
                dataType = DataType.STRING
            )
            val rule = RolloutRule(
                id = UUID.randomUUID(),
                featureFlagId = 1L,
                attributeKey = null,
                attributeValue = null,
                percentage = 50.0,
                distributionKeyAttribute = null,
                valueBool = true,
                valueString = null,
                valueInt = null,
                valuePercentage = null,
                variantName = "test-variant",
                conditions = mutableListOf(condition)
            )

            assertTrue(rule.hasConditions())
        }

        @Test
        fun `should return true when conditions list has multiple elements`() {
            val condition1 = Condition(
                attribute = "plan",
                operator = Operator.EQUALS,
                value = "premium",
                dataType = DataType.STRING
            )
            val condition2 = Condition(
                attribute = "score",
                operator = Operator.GREATER_THAN,
                value = 700,
                dataType = DataType.NUMBER
            )
            val rule = RolloutRule(
                id = UUID.randomUUID(),
                featureFlagId = 1L,
                attributeKey = null,
                attributeValue = null,
                percentage = 50.0,
                distributionKeyAttribute = null,
                valueBool = true,
                valueString = null,
                valueInt = null,
                valuePercentage = null,
                variantName = "test-variant",
                conditions = mutableListOf(condition1, condition2)
            )

            assertTrue(rule.hasConditions())
        }
    }

    @Nested
    @DisplayName("RolloutRule Creation Tests")
    inner class CreationTests {

        @Test
        fun `should create rollout rule with all fields`() {
            val uuid = UUID.randomUUID()
            val startAt = Instant.now()
            val endAt = Instant.now().plusSeconds(3600)
            val conditions = mutableListOf<Condition>()
            
            val rule = RolloutRule(
                id = uuid,
                featureFlagId = 1L,
                attributeKey = "user_type",
                attributeValue = "premium",
                percentage = 75.0,
                distributionKeyAttribute = "user_id",
                valueBool = true,
                valueString = "enabled",
                valueInt = 100,
                valuePercentage = 85.5,
                variantName = "premium-variant",
                startAt = startAt,
                endAt = endAt,
                priority = 1,
                active = true,
                conditions = conditions
            )

            assertEquals(uuid, rule.id)
            assertEquals(1L, rule.featureFlagId)
            assertEquals("user_type", rule.attributeKey)
            assertEquals("premium", rule.attributeValue)
            assertEquals(75.0, rule.percentage)
            assertEquals("user_id", rule.distributionKeyAttribute)
            assertEquals(true, rule.valueBool)
            assertEquals("enabled", rule.valueString)
            assertEquals(100, rule.valueInt)
            assertEquals(85.5, rule.valuePercentage)
            assertEquals("premium-variant", rule.variantName)
            assertEquals(startAt, rule.startAt)
            assertEquals(endAt, rule.endAt)
            assertEquals(1, rule.priority)
            assertEquals(true, rule.active)
            assertEquals(conditions, rule.conditions)
        }

        @Test
        fun `should create rollout rule with default values`() {
            val rule = RolloutRule(
                id = null,
                featureFlagId = 1L,
                attributeKey = null,
                attributeValue = null,
                percentage = null,
                distributionKeyAttribute = null,
                valueBool = null,
                valueString = null,
                valueInt = null,
                valuePercentage = null,
                variantName = null
            )

            assertNull(rule.id)
            assertEquals(1L, rule.featureFlagId)
            assertNull(rule.attributeKey)
            assertNull(rule.attributeValue)
            assertNull(rule.percentage)
            assertNull(rule.distributionKeyAttribute)
            assertNull(rule.valueBool)
            assertNull(rule.valueString)
            assertNull(rule.valueInt)
            assertNull(rule.valuePercentage)
            assertNull(rule.variantName)
            assertNull(rule.startAt)
            assertNull(rule.endAt)
            assertEquals(0, rule.priority) // default value
            assertTrue(rule.active) // default value
            assertTrue(rule.conditions.isEmpty()) // default empty list
        }

        @Test
        fun `should create rollout rule with minimal required fields`() {
            val rule = RolloutRule(
                id = null,
                featureFlagId = 2L,
                attributeKey = null,
                attributeValue = null,
                percentage = 50.0,
                distributionKeyAttribute = null,
                valueBool = false,
                valueString = null,
                valueInt = null,
                valuePercentage = null,
                variantName = "test-variant"
            )

            assertNull(rule.id)
            assertEquals(2L, rule.featureFlagId)
            assertEquals(50.0, rule.percentage)
            assertEquals(false, rule.valueBool)
            assertEquals("test-variant", rule.variantName)
            assertTrue(rule.active)
            assertEquals(0, rule.priority)
        }
    }

    @Nested
    @DisplayName("Conditions Management Tests")
    inner class ConditionsManagementTests {

        @Test
        fun `should allow adding conditions to mutable list`() {
            val rule = RolloutRule(
                id = UUID.randomUUID(),
                featureFlagId = 1L,
                attributeKey = null,
                attributeValue = null,
                percentage = 50.0,
                distributionKeyAttribute = null,
                valueBool = true,
                valueString = null,
                valueInt = null,
                valuePercentage = null,
                variantName = "test-variant"
            )

            assertFalse(rule.hasConditions())
            
            val condition = Condition(
                attribute = "plan",
                operator = Operator.EQUALS,
                value = "premium",
                dataType = DataType.STRING
            )
            
            rule.conditions.add(condition)
            
            assertTrue(rule.hasConditions())
            assertEquals(1, rule.conditions.size)
            assertEquals(condition, rule.conditions[0])
        }

        @Test
        fun `should allow removing conditions from mutable list`() {
            val condition = Condition(
                attribute = "plan",
                operator = Operator.EQUALS,
                value = "premium",
                dataType = DataType.STRING
            )
            
            val rule = RolloutRule(
                id = UUID.randomUUID(),
                featureFlagId = 1L,
                attributeKey = null,
                attributeValue = null,
                percentage = 50.0,
                distributionKeyAttribute = null,
                valueBool = true,
                valueString = null,
                valueInt = null,
                valuePercentage = null,
                variantName = "test-variant",
                conditions = mutableListOf(condition)
            )

            assertTrue(rule.hasConditions())
            assertEquals(1, rule.conditions.size)
            
            rule.conditions.remove(condition)
            
            assertFalse(rule.hasConditions())
            assertEquals(0, rule.conditions.size)
        }

        @Test
        fun `should handle complex conditions scenarios`() {
            val planCondition = Condition(
                attribute = "plan",
                operator = Operator.EQUALS,
                value = "premium",
                dataType = DataType.STRING
            )
            
            val scoreCondition = Condition(
                attribute = "creditScore",
                operator = Operator.GREATER_THAN,
                value = 700,
                dataType = DataType.NUMBER
            )
            
            val countryCondition = Condition(
                attribute = "country",
                operator = Operator.IN,
                value = listOf("US", "CA", "UK"),
                dataType = DataType.ARRAY
            )
            
            val rule = RolloutRule(
                id = UUID.randomUUID(),
                featureFlagId = 1L,
                attributeKey = null,
                attributeValue = null,
                percentage = 50.0,
                distributionKeyAttribute = null,
                valueBool = true,
                valueString = null,
                valueInt = null,
                valuePercentage = null,
                variantName = "premium-variant",
                conditions = mutableListOf(planCondition, scoreCondition, countryCondition)
            )

            assertTrue(rule.hasConditions())
            assertEquals(3, rule.conditions.size)
            assertTrue(rule.conditions.contains(planCondition))
            assertTrue(rule.conditions.contains(scoreCondition))
            assertTrue(rule.conditions.contains(countryCondition))
        }
    }

    @Nested
    @DisplayName("Time-based Rule Tests")
    inner class TimeBasedTests {

        @Test
        fun `should handle start and end times`() {
            val startAt = Instant.parse("2023-01-01T00:00:00Z")
            val endAt = Instant.parse("2023-12-31T23:59:59Z")
            
            val rule = RolloutRule(
                id = UUID.randomUUID(),
                featureFlagId = 1L,
                attributeKey = null,
                attributeValue = null,
                percentage = 50.0,
                distributionKeyAttribute = null,
                valueBool = true,
                valueString = null,
                valueInt = null,
                valuePercentage = null,
                variantName = "time-based-variant",
                startAt = startAt,
                endAt = endAt
            )

            assertEquals(startAt, rule.startAt)
            assertEquals(endAt, rule.endAt)
        }

        @Test
        fun `should handle null start and end times`() {
            val rule = RolloutRule(
                id = UUID.randomUUID(),
                featureFlagId = 1L,
                attributeKey = null,
                attributeValue = null,
                percentage = 50.0,
                distributionKeyAttribute = null,
                valueBool = true,
                valueString = null,
                valueInt = null,
                valuePercentage = null,
                variantName = "no-time-variant"
            )

            assertNull(rule.startAt)
            assertNull(rule.endAt)
        }
    }

    @Nested
    @DisplayName("Priority and Active Status Tests")
    inner class PriorityAndActiveTests {

        @Test
        fun `should handle different priority values`() {
            val highPriorityRule = RolloutRule(
                id = UUID.randomUUID(),
                featureFlagId = 1L,
                attributeKey = null,
                attributeValue = null,
                percentage = 50.0,
                distributionKeyAttribute = null,
                valueBool = true,
                valueString = null,
                valueInt = null,
                valuePercentage = null,
                variantName = "high-priority",
                priority = 10
            )

            val lowPriorityRule = RolloutRule(
                id = UUID.randomUUID(),
                featureFlagId = 1L,
                attributeKey = null,
                attributeValue = null,
                percentage = 50.0,
                distributionKeyAttribute = null,
                valueBool = true,
                valueString = null,
                valueInt = null,
                valuePercentage = null,
                variantName = "low-priority",
                priority = 1
            )

            assertEquals(10, highPriorityRule.priority)
            assertEquals(1, lowPriorityRule.priority)
        }

        @Test
        fun `should handle active status correctly`() {
            val activeRule = RolloutRule(
                id = UUID.randomUUID(),
                featureFlagId = 1L,
                attributeKey = null,
                attributeValue = null,
                percentage = 50.0,
                distributionKeyAttribute = null,
                valueBool = true,
                valueString = null,
                valueInt = null,
                valuePercentage = null,
                variantName = "active-rule",
                active = true
            )

            val inactiveRule = RolloutRule(
                id = UUID.randomUUID(),
                featureFlagId = 1L,
                attributeKey = null,
                attributeValue = null,
                percentage = 50.0,
                distributionKeyAttribute = null,
                valueBool = true,
                valueString = null,
                valueInt = null,
                valuePercentage = null,
                variantName = "inactive-rule",
                active = false
            )

            assertTrue(activeRule.active)
            assertFalse(inactiveRule.active)
        }
    }
}