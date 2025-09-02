package com.platform.groundcontrol.domain.valueobjects

import com.platform.groundcontrol.domain.enums.DataType
import com.platform.groundcontrol.domain.enums.Operator
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.DisplayName
import kotlin.test.assertEquals
import kotlin.test.assertTrue

@DisplayName("Condition Tests")
class ConditionTest {

    @Nested
    @DisplayName("Data Class Tests")
    inner class DataClassTests {

        @Test
        fun `should create condition with all properties`() {
            val condition = Condition(
                attribute = "plan",
                operator = Operator.EQUALS,
                value = "premium",
                dataType = DataType.STRING
            )

            assertEquals("plan", condition.attribute)
            assertEquals(Operator.EQUALS, condition.operator)
            assertEquals("premium", condition.value)
            assertEquals(DataType.STRING, condition.dataType)
        }

        @Test
        fun `should support different data types`() {
            val stringCondition = Condition("name", Operator.CONTAINS, "test", DataType.STRING)
            val numberCondition = Condition("score", Operator.GREATER_THAN, 100, DataType.NUMBER)
            val booleanCondition = Condition("active", Operator.EQUALS, true, DataType.BOOLEAN)
            val arrayCondition = Condition("tags", Operator.IN, listOf("tag1", "tag2"), DataType.ARRAY)

            assertEquals(DataType.STRING, stringCondition.dataType)
            assertEquals(DataType.NUMBER, numberCondition.dataType)
            assertEquals(DataType.BOOLEAN, booleanCondition.dataType)
            assertEquals(DataType.ARRAY, arrayCondition.dataType)
        }

        @Test
        fun `should support different operators`() {
            val equalsCondition = Condition("attr", Operator.EQUALS, "value", DataType.STRING)
            val containsCondition = Condition("attr", Operator.CONTAINS, "value", DataType.STRING)
            val greaterCondition = Condition("attr", Operator.GREATER_THAN, 50, DataType.NUMBER)

            assertEquals(Operator.EQUALS, equalsCondition.operator)
            assertEquals(Operator.CONTAINS, containsCondition.operator)
            assertEquals(Operator.GREATER_THAN, greaterCondition.operator)
        }

        @Test
        fun `should handle various value types`() {
            val stringValue = Condition("attr", Operator.EQUALS, "string", DataType.STRING)
            val intValue = Condition("attr", Operator.EQUALS, 42, DataType.NUMBER)
            val doubleValue = Condition("attr", Operator.EQUALS, 3.14, DataType.NUMBER)
            val booleanValue = Condition("attr", Operator.EQUALS, true, DataType.BOOLEAN)
            val listValue = Condition("attr", Operator.IN, listOf("a", "b"), DataType.ARRAY)

            assertEquals("string", stringValue.value)
            assertEquals(42, intValue.value)
            assertEquals(3.14, doubleValue.value)
            assertEquals(true, booleanValue.value)
            assertEquals(listOf("a", "b"), listValue.value)
        }
    }

    @Nested
    @DisplayName("Equality and HashCode Tests")
    inner class EqualityTests {

        @Test
        fun `should be equal when all properties match`() {
            val condition1 = Condition("plan", Operator.EQUALS, "premium", DataType.STRING)
            val condition2 = Condition("plan", Operator.EQUALS, "premium", DataType.STRING)

            assertEquals(condition1, condition2)
            assertEquals(condition1.hashCode(), condition2.hashCode())
        }

        @Test
        fun `should not be equal when attribute differs`() {
            val condition1 = Condition("plan", Operator.EQUALS, "premium", DataType.STRING)
            val condition2 = Condition("tier", Operator.EQUALS, "premium", DataType.STRING)

            assertTrue(condition1 != condition2)
        }

        @Test
        fun `should not be equal when operator differs`() {
            val condition1 = Condition("plan", Operator.EQUALS, "premium", DataType.STRING)
            val condition2 = Condition("plan", Operator.CONTAINS, "premium", DataType.STRING)

            assertTrue(condition1 != condition2)
        }

        @Test
        fun `should not be equal when value differs`() {
            val condition1 = Condition("plan", Operator.EQUALS, "premium", DataType.STRING)
            val condition2 = Condition("plan", Operator.EQUALS, "basic", DataType.STRING)

            assertTrue(condition1 != condition2)
        }

        @Test
        fun `should not be equal when dataType differs`() {
            val condition1 = Condition("active", Operator.EQUALS, "true", DataType.STRING)
            val condition2 = Condition("active", Operator.EQUALS, true, DataType.BOOLEAN)

            assertTrue(condition1 != condition2)
        }
    }

    @Nested
    @DisplayName("Serialization Tests")
    inner class SerializationTests {

        @Test
        fun `should implement Serializable`() {
            val condition = Condition("test", Operator.EQUALS, "value", DataType.STRING)

            assertTrue(condition is java.io.Serializable)
        }
    }

    @Nested
    @DisplayName("toString Tests")
    inner class ToStringTests {

        @Test
        fun `should have meaningful string representation`() {
            val condition = Condition("plan", Operator.EQUALS, "premium", DataType.STRING)

            val result = condition.toString()

            // Data class toString should include all properties
            assertTrue(result.contains("plan"))
            assertTrue(result.contains("EQUALS"))
            assertTrue(result.contains("premium"))
            assertTrue(result.contains("STRING"))
        }
    }

    @Nested
    @DisplayName("Real-world Usage Examples")
    inner class UsageExamplesTests {

        @Test
        fun `should support user tier conditions`() {
            val premiumCondition = Condition("userTier", Operator.EQUALS, "premium", DataType.STRING)
            val basicCondition = Condition("userTier", Operator.EQUALS, "basic", DataType.STRING)

            assertEquals("userTier", premiumCondition.attribute)
            assertEquals("premium", premiumCondition.value)
            assertEquals("basic", basicCondition.value)
        }

        @Test
        fun `should support numeric threshold conditions`() {
            val scoreCondition = Condition("creditScore", Operator.GREATER_THAN, 700, DataType.NUMBER)
            val ageCondition = Condition("age", Operator.GREATER_EQUAL, 18, DataType.NUMBER)

            assertEquals("creditScore", scoreCondition.attribute)
            assertEquals(Operator.GREATER_THAN, scoreCondition.operator)
            assertEquals(700, scoreCondition.value)
            
            assertEquals("age", ageCondition.attribute)
            assertEquals(Operator.GREATER_EQUAL, ageCondition.operator)
            assertEquals(18, ageCondition.value)
        }

        @Test
        fun `should support boolean flag conditions`() {
            val betaCondition = Condition("isBetaTester", Operator.EQUALS, true, DataType.BOOLEAN)
            val activeCondition = Condition("isActive", Operator.NOT_EQUALS, false, DataType.BOOLEAN)

            assertEquals(true, betaCondition.value)
            assertEquals(false, activeCondition.value)
        }

        @Test
        fun `should support array membership conditions`() {
            val countriesCondition = Condition("country", Operator.IN, listOf("US", "CA", "UK"), DataType.ARRAY)
            val tagsCondition = Condition("tags", Operator.CONTAINS, "vip", DataType.ARRAY)

            assertEquals(listOf("US", "CA", "UK"), countriesCondition.value)
            assertEquals("vip", tagsCondition.value)
        }

        @Test
        fun `should support string pattern conditions`() {
            val emailCondition = Condition("email", Operator.ENDS_WITH, "@company.com", DataType.STRING)
            val nameCondition = Condition("name", Operator.STARTS_WITH, "John", DataType.STRING)
            val regexCondition = Condition("phone", Operator.REGEX_MATCH, "^\\+1\\d{10}$", DataType.STRING)

            assertEquals("@company.com", emailCondition.value)
            assertEquals("John", nameCondition.value)
            assertEquals("^\\+1\\d{10}$", regexCondition.value)
        }
    }
}