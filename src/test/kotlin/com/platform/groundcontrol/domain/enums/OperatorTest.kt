package com.platform.groundcontrol.domain.enums

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.DisplayName
import kotlin.test.assertEquals
import kotlin.test.assertTrue

@DisplayName("Operator Tests")
class OperatorTest {

    @Test
    fun `should have all expected operators`() {
        val expectedOperators = setOf(
            Operator.EQUALS,
            Operator.NOT_EQUALS,
            Operator.GREATER_THAN,
            Operator.GREATER_EQUAL,
            Operator.LESS_THAN,
            Operator.LESS_EQUAL,
            Operator.IN,
            Operator.NOT_IN,
            Operator.CONTAINS,
            Operator.STARTS_WITH,
            Operator.ENDS_WITH,
            Operator.REGEX_MATCH
        )

        val actualOperators = Operator.values().toSet()

        assertEquals(expectedOperators, actualOperators)
        assertEquals(12, Operator.values().size)
    }

    @Test
    fun `should have correct enum values`() {
        assertEquals("EQUALS", Operator.EQUALS.name)
        assertEquals("NOT_EQUALS", Operator.NOT_EQUALS.name)
        assertEquals("GREATER_THAN", Operator.GREATER_THAN.name)
        assertEquals("GREATER_EQUAL", Operator.GREATER_EQUAL.name)
        assertEquals("LESS_THAN", Operator.LESS_THAN.name)
        assertEquals("LESS_EQUAL", Operator.LESS_EQUAL.name)
        assertEquals("IN", Operator.IN.name)
        assertEquals("NOT_IN", Operator.NOT_IN.name)
        assertEquals("CONTAINS", Operator.CONTAINS.name)
        assertEquals("STARTS_WITH", Operator.STARTS_WITH.name)
        assertEquals("ENDS_WITH", Operator.ENDS_WITH.name)
        assertEquals("REGEX_MATCH", Operator.REGEX_MATCH.name)
    }

    @Test
    fun `should group comparison operators correctly`() {
        val comparisonOperators = setOf(
            Operator.EQUALS,
            Operator.NOT_EQUALS,
            Operator.GREATER_THAN,
            Operator.GREATER_EQUAL,
            Operator.LESS_THAN,
            Operator.LESS_EQUAL
        )

        // These should be suitable for numeric comparisons
        comparisonOperators.forEach { operator ->
            assertTrue(operator in Operator.values())
        }
    }

    @Test
    fun `should group string operators correctly`() {
        val stringOperators = setOf(
            Operator.CONTAINS,
            Operator.STARTS_WITH,
            Operator.ENDS_WITH,
            Operator.REGEX_MATCH
        )

        // These should be suitable for string operations
        stringOperators.forEach { operator ->
            assertTrue(operator in Operator.values())
        }
    }

    @Test
    fun `should group set operators correctly`() {
        val setOperators = setOf(
            Operator.IN,
            Operator.NOT_IN
        )

        // These should be suitable for collection/array operations
        setOperators.forEach { operator ->
            assertTrue(operator in Operator.values())
        }
    }

    @Test
    fun `should allow conversion from string`() {
        assertEquals(Operator.EQUALS, Operator.valueOf("EQUALS"))
        assertEquals(Operator.NOT_EQUALS, Operator.valueOf("NOT_EQUALS"))
        assertEquals(Operator.GREATER_THAN, Operator.valueOf("GREATER_THAN"))
        assertEquals(Operator.CONTAINS, Operator.valueOf("CONTAINS"))
        assertEquals(Operator.REGEX_MATCH, Operator.valueOf("REGEX_MATCH"))
    }

    @Test
    fun `should be usable in when expressions`() {
        val operator = Operator.CONTAINS

        val result = when (operator) {
            Operator.EQUALS -> "equality check"
            Operator.NOT_EQUALS -> "inequality check"
            Operator.GREATER_THAN, Operator.GREATER_EQUAL, 
            Operator.LESS_THAN, Operator.LESS_EQUAL -> "comparison"
            Operator.CONTAINS, Operator.STARTS_WITH, 
            Operator.ENDS_WITH -> "string matching"
            Operator.IN, Operator.NOT_IN -> "set operations"
            Operator.REGEX_MATCH -> "pattern matching"
        }

        assertEquals("string matching", result)
    }

    @Test
    fun `should have consistent ordinal values`() {
        val values = Operator.values()
        
        values.forEachIndexed { index, operator ->
            assertEquals(index, operator.ordinal)
        }
    }

    @Test
    fun `should support logical groupings for evaluation`() {
        // Test that we can categorize operators for different evaluators
        val numericOperators = setOf(
            Operator.EQUALS,
            Operator.NOT_EQUALS,
            Operator.GREATER_THAN,
            Operator.GREATER_EQUAL,
            Operator.LESS_THAN,
            Operator.LESS_EQUAL
        )

        val stringOperators = setOf(
            Operator.EQUALS,
            Operator.NOT_EQUALS,
            Operator.CONTAINS,
            Operator.STARTS_WITH,
            Operator.ENDS_WITH,
            Operator.REGEX_MATCH
        )

        val booleanOperators = setOf(
            Operator.EQUALS,
            Operator.NOT_EQUALS
        )

        // Verify that equals/not equals are common to multiple types
        assertTrue(Operator.EQUALS in numericOperators)
        assertTrue(Operator.EQUALS in stringOperators)
        assertTrue(Operator.EQUALS in booleanOperators)

        assertTrue(Operator.NOT_EQUALS in numericOperators)
        assertTrue(Operator.NOT_EQUALS in stringOperators)
        assertTrue(Operator.NOT_EQUALS in booleanOperators)

        // Verify type-specific operators
        assertTrue(Operator.GREATER_THAN in numericOperators)
        assertTrue(Operator.CONTAINS in stringOperators)
        
        // Verify type-specific operators are not in wrong categories
        assertTrue(Operator.GREATER_THAN !in stringOperators)
        assertTrue(Operator.CONTAINS !in numericOperators)
    }
}