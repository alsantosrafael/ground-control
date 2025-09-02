package com.platform.groundcontrol.domain.evaluators

import com.platform.groundcontrol.domain.enums.DataType
import com.platform.groundcontrol.domain.enums.Operator
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.assertThrows
import kotlin.test.assertFalse
import kotlin.test.assertTrue

@DisplayName("NumericEvaluator Tests")
class NumericEvaluatorTest {

    private val evaluator = NumericEvaluator()

    @Nested
    @DisplayName("canHandle Tests")
    inner class CanHandleTests {

        @Test
        fun `should handle NUMBER data type with EQUALS operator`() {
            assertTrue(evaluator.canHandle(Operator.EQUALS, DataType.NUMBER))
        }

        @Test
        fun `should handle NUMBER data type with NOT_EQUALS operator`() {
            assertTrue(evaluator.canHandle(Operator.NOT_EQUALS, DataType.NUMBER))
        }

        @Test
        fun `should handle NUMBER data type with GREATER_THAN operator`() {
            assertTrue(evaluator.canHandle(Operator.GREATER_THAN, DataType.NUMBER))
        }

        @Test
        fun `should handle NUMBER data type with GREATER_EQUAL operator`() {
            assertTrue(evaluator.canHandle(Operator.GREATER_EQUAL, DataType.NUMBER))
        }

        @Test
        fun `should handle NUMBER data type with LESS_THAN operator`() {
            assertTrue(evaluator.canHandle(Operator.LESS_THAN, DataType.NUMBER))
        }

        @Test
        fun `should handle NUMBER data type with LESS_EQUAL operator`() {
            assertTrue(evaluator.canHandle(Operator.LESS_EQUAL, DataType.NUMBER))
        }

        @Test
        fun `should not handle NUMBER data type with CONTAINS operator`() {
            assertFalse(evaluator.canHandle(Operator.CONTAINS, DataType.NUMBER))
        }

        @Test
        fun `should not handle NUMBER data type with STARTS_WITH operator`() {
            assertFalse(evaluator.canHandle(Operator.STARTS_WITH, DataType.NUMBER))
        }

        @Test
        fun `should not handle NUMBER data type with IN operator`() {
            assertFalse(evaluator.canHandle(Operator.IN, DataType.NUMBER))
        }

        @Test
        fun `should not handle STRING data type with any operator`() {
            assertFalse(evaluator.canHandle(Operator.EQUALS, DataType.STRING))
            assertFalse(evaluator.canHandle(Operator.GREATER_THAN, DataType.STRING))
        }

        @Test
        fun `should not handle BOOLEAN data type with any operator`() {
            assertFalse(evaluator.canHandle(Operator.EQUALS, DataType.BOOLEAN))
            assertFalse(evaluator.canHandle(Operator.GREATER_THAN, DataType.BOOLEAN))
        }

        @Test
        fun `should not handle ARRAY data type with any operator`() {
            assertFalse(evaluator.canHandle(Operator.EQUALS, DataType.ARRAY))
            assertFalse(evaluator.canHandle(Operator.GREATER_THAN, DataType.ARRAY))
        }

        @Test
        fun `should not handle DATE data type with any operator`() {
            assertFalse(evaluator.canHandle(Operator.EQUALS, DataType.DATE))
            assertFalse(evaluator.canHandle(Operator.GREATER_THAN, DataType.DATE))
        }
    }

    @Nested
    @DisplayName("EQUALS operator Tests")
    inner class EqualsTests {

        @Test
        fun `should return true when numbers are equal`() {
            assertTrue(evaluator.evaluate(42, 42, Operator.EQUALS))
            assertTrue(evaluator.evaluate(42.0, 42.0, Operator.EQUALS))
            assertTrue(evaluator.evaluate(42, 42.0, Operator.EQUALS))
        }

        @Test
        fun `should return false when numbers are not equal`() {
            assertFalse(evaluator.evaluate(42, 43, Operator.EQUALS))
            assertFalse(evaluator.evaluate(42.5, 42.6, Operator.EQUALS))
        }

        @Test
        fun `should handle negative numbers`() {
            assertTrue(evaluator.evaluate(-42, -42, Operator.EQUALS))
            assertFalse(evaluator.evaluate(-42, 42, Operator.EQUALS))
        }

        @Test
        fun `should handle zero`() {
            assertTrue(evaluator.evaluate(0, 0, Operator.EQUALS))
            assertTrue(evaluator.evaluate(0.0, 0, Operator.EQUALS))
            assertFalse(evaluator.evaluate(0, 1, Operator.EQUALS))
        }

        @Test
        fun `should handle decimal numbers`() {
            assertTrue(evaluator.evaluate(3.14159, 3.14159, Operator.EQUALS))
            assertFalse(evaluator.evaluate(3.14159, 3.14160, Operator.EQUALS))
        }

        @Test
        fun `should convert string numbers`() {
            assertTrue(evaluator.evaluate("42", 42, Operator.EQUALS))
            assertTrue(evaluator.evaluate(42, "42", Operator.EQUALS))
            assertTrue(evaluator.evaluate("42.5", 42.5, Operator.EQUALS))
        }

        @Test
        fun `should return false for invalid number strings`() {
            assertFalse(evaluator.evaluate("invalid", 42, Operator.EQUALS))
            assertFalse(evaluator.evaluate(42, "invalid", Operator.EQUALS))
        }

        @Test
        fun `should return false for non-numeric types`() {
            assertFalse(evaluator.evaluate(listOf(1, 2, 3), 42, Operator.EQUALS))
            assertFalse(evaluator.evaluate(42, mapOf("key" to "value"), Operator.EQUALS))
        }
    }

    @Nested
    @DisplayName("NOT_EQUALS operator Tests")
    inner class NotEqualsTests {

        @Test
        fun `should return false when numbers are equal`() {
            assertFalse(evaluator.evaluate(42, 42, Operator.NOT_EQUALS))
            assertFalse(evaluator.evaluate(42.0, 42.0, Operator.NOT_EQUALS))
        }

        @Test
        fun `should return true when numbers are not equal`() {
            assertTrue(evaluator.evaluate(42, 43, Operator.NOT_EQUALS))
            assertTrue(evaluator.evaluate(42.5, 42.6, Operator.NOT_EQUALS))
        }

        @Test
        fun `should handle negative numbers`() {
            assertFalse(evaluator.evaluate(-42, -42, Operator.NOT_EQUALS))
            assertTrue(evaluator.evaluate(-42, 42, Operator.NOT_EQUALS))
        }

        @Test
        fun `should convert string numbers`() {
            assertFalse(evaluator.evaluate("42", 42, Operator.NOT_EQUALS))
            assertTrue(evaluator.evaluate("42", 43, Operator.NOT_EQUALS))
        }
    }

    @Nested
    @DisplayName("GREATER_THAN operator Tests")
    inner class GreaterThanTests {

        @Test
        fun `should return true when first number is greater`() {
            assertTrue(evaluator.evaluate(43, 42, Operator.GREATER_THAN))
            assertTrue(evaluator.evaluate(42.1, 42.0, Operator.GREATER_THAN))
        }

        @Test
        fun `should return false when first number is less`() {
            assertFalse(evaluator.evaluate(41, 42, Operator.GREATER_THAN))
            assertFalse(evaluator.evaluate(41.9, 42.0, Operator.GREATER_THAN))
        }

        @Test
        fun `should return false when numbers are equal`() {
            assertFalse(evaluator.evaluate(42, 42, Operator.GREATER_THAN))
            assertFalse(evaluator.evaluate(42.0, 42.0, Operator.GREATER_THAN))
        }

        @Test
        fun `should handle negative numbers`() {
            assertTrue(evaluator.evaluate(-10, -20, Operator.GREATER_THAN))
            assertFalse(evaluator.evaluate(-20, -10, Operator.GREATER_THAN))
            assertTrue(evaluator.evaluate(10, -5, Operator.GREATER_THAN))
        }

        @Test
        fun `should handle zero comparisons`() {
            assertTrue(evaluator.evaluate(1, 0, Operator.GREATER_THAN))
            assertTrue(evaluator.evaluate(0, -1, Operator.GREATER_THAN))
            assertFalse(evaluator.evaluate(0, 1, Operator.GREATER_THAN))
        }

        @Test
        fun `should convert string numbers`() {
            assertTrue(evaluator.evaluate("43", 42, Operator.GREATER_THAN))
            assertTrue(evaluator.evaluate(43, "42", Operator.GREATER_THAN))
            assertFalse(evaluator.evaluate("41", 42, Operator.GREATER_THAN))
        }
    }

    @Nested
    @DisplayName("GREATER_EQUAL operator Tests")
    inner class GreaterEqualTests {

        @Test
        fun `should return true when first number is greater`() {
            assertTrue(evaluator.evaluate(43, 42, Operator.GREATER_EQUAL))
            assertTrue(evaluator.evaluate(42.1, 42.0, Operator.GREATER_EQUAL))
        }

        @Test
        fun `should return true when numbers are equal`() {
            assertTrue(evaluator.evaluate(42, 42, Operator.GREATER_EQUAL))
            assertTrue(evaluator.evaluate(42.0, 42.0, Operator.GREATER_EQUAL))
        }

        @Test
        fun `should return false when first number is less`() {
            assertFalse(evaluator.evaluate(41, 42, Operator.GREATER_EQUAL))
            assertFalse(evaluator.evaluate(41.9, 42.0, Operator.GREATER_EQUAL))
        }

        @Test
        fun `should handle negative numbers`() {
            assertTrue(evaluator.evaluate(-10, -20, Operator.GREATER_EQUAL))
            assertTrue(evaluator.evaluate(-10, -10, Operator.GREATER_EQUAL))
            assertFalse(evaluator.evaluate(-20, -10, Operator.GREATER_EQUAL))
        }

        @Test
        fun `should convert string numbers`() {
            assertTrue(evaluator.evaluate("42", 42, Operator.GREATER_EQUAL))
            assertTrue(evaluator.evaluate("43", 42, Operator.GREATER_EQUAL))
            assertFalse(evaluator.evaluate("41", 42, Operator.GREATER_EQUAL))
        }
    }

    @Nested
    @DisplayName("LESS_THAN operator Tests")
    inner class LessThanTests {

        @Test
        fun `should return true when first number is less`() {
            assertTrue(evaluator.evaluate(41, 42, Operator.LESS_THAN))
            assertTrue(evaluator.evaluate(41.9, 42.0, Operator.LESS_THAN))
        }

        @Test
        fun `should return false when first number is greater`() {
            assertFalse(evaluator.evaluate(43, 42, Operator.LESS_THAN))
            assertFalse(evaluator.evaluate(42.1, 42.0, Operator.LESS_THAN))
        }

        @Test
        fun `should return false when numbers are equal`() {
            assertFalse(evaluator.evaluate(42, 42, Operator.LESS_THAN))
            assertFalse(evaluator.evaluate(42.0, 42.0, Operator.LESS_THAN))
        }

        @Test
        fun `should handle negative numbers`() {
            assertTrue(evaluator.evaluate(-20, -10, Operator.LESS_THAN))
            assertFalse(evaluator.evaluate(-10, -20, Operator.LESS_THAN))
            assertTrue(evaluator.evaluate(-5, 10, Operator.LESS_THAN))
        }

        @Test
        fun `should handle zero comparisons`() {
            assertTrue(evaluator.evaluate(-1, 0, Operator.LESS_THAN))
            assertTrue(evaluator.evaluate(0, 1, Operator.LESS_THAN))
            assertFalse(evaluator.evaluate(1, 0, Operator.LESS_THAN))
        }

        @Test
        fun `should convert string numbers`() {
            assertTrue(evaluator.evaluate("41", 42, Operator.LESS_THAN))
            assertTrue(evaluator.evaluate(41, "42", Operator.LESS_THAN))
            assertFalse(evaluator.evaluate("43", 42, Operator.LESS_THAN))
        }
    }

    @Nested
    @DisplayName("LESS_EQUAL operator Tests")
    inner class LessEqualTests {

        @Test
        fun `should return true when first number is less`() {
            assertTrue(evaluator.evaluate(41, 42, Operator.LESS_EQUAL))
            assertTrue(evaluator.evaluate(41.9, 42.0, Operator.LESS_EQUAL))
        }

        @Test
        fun `should return true when numbers are equal`() {
            assertTrue(evaluator.evaluate(42, 42, Operator.LESS_EQUAL))
            assertTrue(evaluator.evaluate(42.0, 42.0, Operator.LESS_EQUAL))
        }

        @Test
        fun `should return false when first number is greater`() {
            assertFalse(evaluator.evaluate(43, 42, Operator.LESS_EQUAL))
            assertFalse(evaluator.evaluate(42.1, 42.0, Operator.LESS_EQUAL))
        }

        @Test
        fun `should handle negative numbers`() {
            assertTrue(evaluator.evaluate(-20, -10, Operator.LESS_EQUAL))
            assertTrue(evaluator.evaluate(-10, -10, Operator.LESS_EQUAL))
            assertFalse(evaluator.evaluate(-10, -20, Operator.LESS_EQUAL))
        }

        @Test
        fun `should convert string numbers`() {
            assertTrue(evaluator.evaluate("42", 42, Operator.LESS_EQUAL))
            assertTrue(evaluator.evaluate("41", 42, Operator.LESS_EQUAL))
            assertFalse(evaluator.evaluate("43", 42, Operator.LESS_EQUAL))
        }
    }

    @Nested
    @DisplayName("Number conversion Tests")
    inner class NumberConversionTests {

        @Test
        fun `should handle different numeric types`() {
            assertTrue(evaluator.evaluate(42.toByte(), 42.toShort(), Operator.EQUALS))
            assertTrue(evaluator.evaluate(42.toInt(), 42.toLong(), Operator.EQUALS))
            assertTrue(evaluator.evaluate(42.toFloat(), 42.toDouble(), Operator.EQUALS))
        }

        @Test
        fun `should convert valid string numbers`() {
            assertTrue(evaluator.evaluate("42", 42, Operator.EQUALS))
            assertTrue(evaluator.evaluate("42.5", 42.5, Operator.EQUALS))
            assertTrue(evaluator.evaluate("-42", -42, Operator.EQUALS))
            assertTrue(evaluator.evaluate("0", 0, Operator.EQUALS))
        }

        @Test
        fun `should handle scientific notation in strings`() {
            assertTrue(evaluator.evaluate("1e2", 100, Operator.EQUALS))
            assertTrue(evaluator.evaluate("1.5e1", 15.0, Operator.EQUALS))
        }

        @Test
        fun `should return false for invalid string conversions`() {
            assertFalse(evaluator.evaluate("not-a-number", 42, Operator.EQUALS))
            assertFalse(evaluator.evaluate("42abc", 42, Operator.EQUALS))
            assertFalse(evaluator.evaluate("", 42, Operator.EQUALS))
            assertFalse(evaluator.evaluate("null", 42, Operator.EQUALS))
        }

        @Test
        fun `should return false for non-convertible types`() {
            assertFalse(evaluator.evaluate(listOf(1, 2, 3), 42, Operator.EQUALS))
            assertFalse(evaluator.evaluate(mapOf("key" to "value"), 42, Operator.EQUALS))
            assertFalse(evaluator.evaluate(true, 42, Operator.EQUALS))
        }

    }

    @Nested
    @DisplayName("Edge cases Tests")
    inner class EdgeCasesTests {

        @Test
        fun `should handle very large numbers`() {
            assertTrue(evaluator.evaluate(Double.MAX_VALUE, Double.MAX_VALUE, Operator.EQUALS))
            assertTrue(evaluator.evaluate(Long.MAX_VALUE, Long.MAX_VALUE, Operator.EQUALS))
        }

        @Test
        fun `should handle very small numbers`() {
            assertTrue(evaluator.evaluate(Double.MIN_VALUE, Double.MIN_VALUE, Operator.EQUALS))
            assertTrue(evaluator.evaluate(Long.MIN_VALUE, Long.MIN_VALUE, Operator.EQUALS))
        }

        @Test
        fun `should handle infinity values`() {
            assertTrue(evaluator.evaluate(Double.POSITIVE_INFINITY, Double.POSITIVE_INFINITY, Operator.EQUALS))
            assertTrue(evaluator.evaluate(Double.NEGATIVE_INFINITY, Double.NEGATIVE_INFINITY, Operator.EQUALS))
            assertFalse(evaluator.evaluate(Double.POSITIVE_INFINITY, Double.NEGATIVE_INFINITY, Operator.EQUALS))
        }

        @Test
        fun `should handle NaN values`() {
            assertFalse(evaluator.evaluate(Double.NaN, Double.NaN, Operator.EQUALS))
            assertFalse(evaluator.evaluate(Double.NaN, 42, Operator.EQUALS))
        }
    }

    @Nested
    @DisplayName("Unsupported operator Tests")
    inner class UnsupportedOperatorTests {

        @Test
        fun `should throw exception for unsupported operators`() {
            assertThrows<IllegalArgumentException> {
                evaluator.evaluate(42, 42, Operator.CONTAINS)
            }
        }

        @Test
        fun `should throw exception for STARTS_WITH operator`() {
            assertThrows<IllegalArgumentException> {
                evaluator.evaluate(42, 42, Operator.STARTS_WITH)
            }
        }

        @Test
        fun `should throw exception for ENDS_WITH operator`() {
            assertThrows<IllegalArgumentException> {
                evaluator.evaluate(42, 42, Operator.ENDS_WITH)
            }
        }

        @Test
        fun `should throw exception for IN operator`() {
            assertThrows<IllegalArgumentException> {
                evaluator.evaluate(42, 42, Operator.IN)
            }
        }

        @Test
        fun `should throw exception for REGEX_MATCH operator`() {
            assertThrows<IllegalArgumentException> {
                evaluator.evaluate(42, 42, Operator.REGEX_MATCH)
            }
        }
    }
}