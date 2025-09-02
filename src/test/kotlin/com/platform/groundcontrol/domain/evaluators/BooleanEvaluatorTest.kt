package com.platform.groundcontrol.domain.evaluators

import com.platform.groundcontrol.domain.enums.DataType
import com.platform.groundcontrol.domain.enums.Operator
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.assertThrows
import kotlin.test.assertFalse
import kotlin.test.assertTrue

@DisplayName("BooleanEvaluator Tests")
class BooleanEvaluatorTest {

    private val evaluator = BooleanEvaluator()

    @Nested
    @DisplayName("canHandle Tests")
    inner class CanHandleTests {

        @Test
        fun `should handle BOOLEAN data type with EQUALS operator`() {
            assertTrue(evaluator.canHandle(Operator.EQUALS, DataType.BOOLEAN))
        }

        @Test
        fun `should handle BOOLEAN data type with NOT_EQUALS operator`() {
            assertTrue(evaluator.canHandle(Operator.NOT_EQUALS, DataType.BOOLEAN))
        }

        @Test
        fun `should not handle BOOLEAN data type with GREATER_THAN operator`() {
            assertFalse(evaluator.canHandle(Operator.GREATER_THAN, DataType.BOOLEAN))
        }

        @Test
        fun `should not handle BOOLEAN data type with CONTAINS operator`() {
            assertFalse(evaluator.canHandle(Operator.CONTAINS, DataType.BOOLEAN))
        }

        @Test
        fun `should not handle BOOLEAN data type with STARTS_WITH operator`() {
            assertFalse(evaluator.canHandle(Operator.STARTS_WITH, DataType.BOOLEAN))
        }

        @Test
        fun `should not handle STRING data type with any operator`() {
            assertFalse(evaluator.canHandle(Operator.EQUALS, DataType.STRING))
            assertFalse(evaluator.canHandle(Operator.NOT_EQUALS, DataType.STRING))
        }

        @Test
        fun `should not handle NUMBER data type with any operator`() {
            assertFalse(evaluator.canHandle(Operator.EQUALS, DataType.NUMBER))
            assertFalse(evaluator.canHandle(Operator.NOT_EQUALS, DataType.NUMBER))
        }

        @Test
        fun `should not handle ARRAY data type with any operator`() {
            assertFalse(evaluator.canHandle(Operator.EQUALS, DataType.ARRAY))
            assertFalse(evaluator.canHandle(Operator.NOT_EQUALS, DataType.ARRAY))
        }

        @Test
        fun `should not handle DATE data type with any operator`() {
            assertFalse(evaluator.canHandle(Operator.EQUALS, DataType.DATE))
            assertFalse(evaluator.canHandle(Operator.NOT_EQUALS, DataType.DATE))
        }
    }

    @Nested
    @DisplayName("EQUALS operator Tests")
    inner class EqualsTests {

        @Test
        fun `should return true when both values are true`() {
            assertTrue(evaluator.evaluate(true, true, Operator.EQUALS))
        }

        @Test
        fun `should return true when both values are false`() {
            assertTrue(evaluator.evaluate(false, false, Operator.EQUALS))
        }

        @Test
        fun `should return false when values differ`() {
            assertFalse(evaluator.evaluate(true, false, Operator.EQUALS))
            assertFalse(evaluator.evaluate(false, true, Operator.EQUALS))
        }

        @Test
        fun `should convert string true values`() {
            assertTrue(evaluator.evaluate("true", true, Operator.EQUALS))
            assertTrue(evaluator.evaluate(true, "true", Operator.EQUALS))
            assertTrue(evaluator.evaluate("1", true, Operator.EQUALS))
            assertTrue(evaluator.evaluate("yes", true, Operator.EQUALS))
            assertTrue(evaluator.evaluate("on", true, Operator.EQUALS))
        }

        @Test
        fun `should convert string false values`() {
            assertTrue(evaluator.evaluate("false", false, Operator.EQUALS))
            assertTrue(evaluator.evaluate(false, "false", Operator.EQUALS))
            assertTrue(evaluator.evaluate("0", false, Operator.EQUALS))
            assertTrue(evaluator.evaluate("no", false, Operator.EQUALS))
            assertTrue(evaluator.evaluate("off", false, Operator.EQUALS))
        }

        @Test
        fun `should be case insensitive for string conversions`() {
            assertTrue(evaluator.evaluate("TRUE", true, Operator.EQUALS))
            assertTrue(evaluator.evaluate("False", false, Operator.EQUALS))
            assertTrue(evaluator.evaluate("YES", true, Operator.EQUALS))
            assertTrue(evaluator.evaluate("NO", false, Operator.EQUALS))
            assertTrue(evaluator.evaluate("On", true, Operator.EQUALS))
            assertTrue(evaluator.evaluate("OFF", false, Operator.EQUALS))
        }

        @Test
        fun `should convert numeric values`() {
            assertTrue(evaluator.evaluate(1, true, Operator.EQUALS))
            assertTrue(evaluator.evaluate(0, false, Operator.EQUALS))
            assertTrue(evaluator.evaluate(42, true, Operator.EQUALS))
            assertTrue(evaluator.evaluate(-1, true, Operator.EQUALS))
            assertTrue(evaluator.evaluate(0.0, false, Operator.EQUALS))
            assertTrue(evaluator.evaluate(1.5, true, Operator.EQUALS))
        }

        @Test
        fun `should return false for invalid string conversions`() {
            assertFalse(evaluator.evaluate("invalid", true, Operator.EQUALS))
            assertFalse(evaluator.evaluate("maybe", false, Operator.EQUALS))
            assertFalse(evaluator.evaluate("", true, Operator.EQUALS))
        }

        @Test
        fun `should return false when conversion fails`() {
            assertFalse(evaluator.evaluate(listOf(1, 2, 3), true, Operator.EQUALS))
            assertFalse(evaluator.evaluate(mapOf("key" to "value"), false, Operator.EQUALS))
        }

        @Test
        fun `should return false when either value cannot be converted`() {
            assertFalse(evaluator.evaluate(true, "invalid", Operator.EQUALS))
            assertFalse(evaluator.evaluate("invalid", true, Operator.EQUALS))
            assertFalse(evaluator.evaluate("invalid", "also-invalid", Operator.EQUALS))
        }
    }

    @Nested
    @DisplayName("NOT_EQUALS operator Tests")
    inner class NotEqualsTests {

        @Test
        fun `should return false when both values are true`() {
            assertFalse(evaluator.evaluate(true, true, Operator.NOT_EQUALS))
        }

        @Test
        fun `should return false when both values are false`() {
            assertFalse(evaluator.evaluate(false, false, Operator.NOT_EQUALS))
        }

        @Test
        fun `should return true when values differ`() {
            assertTrue(evaluator.evaluate(true, false, Operator.NOT_EQUALS))
            assertTrue(evaluator.evaluate(false, true, Operator.NOT_EQUALS))
        }

        @Test
        fun `should convert string values for comparison`() {
            assertFalse(evaluator.evaluate("true", true, Operator.NOT_EQUALS))
            assertTrue(evaluator.evaluate("true", false, Operator.NOT_EQUALS))
            assertFalse(evaluator.evaluate("false", false, Operator.NOT_EQUALS))
            assertTrue(evaluator.evaluate("false", true, Operator.NOT_EQUALS))
        }

        @Test
        fun `should convert numeric values for comparison`() {
            assertFalse(evaluator.evaluate(1, true, Operator.NOT_EQUALS))
            assertTrue(evaluator.evaluate(1, false, Operator.NOT_EQUALS))
            assertFalse(evaluator.evaluate(0, false, Operator.NOT_EQUALS))
            assertTrue(evaluator.evaluate(0, true, Operator.NOT_EQUALS))
        }

        @Test
        fun `should return false when conversion fails`() {
            assertFalse(evaluator.evaluate("invalid", true, Operator.NOT_EQUALS))
            assertFalse(evaluator.evaluate(listOf(1, 2, 3), false, Operator.NOT_EQUALS))
        }
    }

    @Nested
    @DisplayName("Boolean conversion Tests")
    inner class BooleanConversionTests {

        @Test
        fun `should convert various true string representations`() {
            assertTrue(evaluator.evaluate("true", true, Operator.EQUALS))
            assertTrue(evaluator.evaluate("TRUE", true, Operator.EQUALS))
            assertTrue(evaluator.evaluate("True", true, Operator.EQUALS))
            assertTrue(evaluator.evaluate("1", true, Operator.EQUALS))
            assertTrue(evaluator.evaluate("yes", true, Operator.EQUALS))
            assertTrue(evaluator.evaluate("YES", true, Operator.EQUALS))
            assertTrue(evaluator.evaluate("on", true, Operator.EQUALS))
            assertTrue(evaluator.evaluate("ON", true, Operator.EQUALS))
        }

        @Test
        fun `should convert various false string representations`() {
            assertTrue(evaluator.evaluate("false", false, Operator.EQUALS))
            assertTrue(evaluator.evaluate("FALSE", false, Operator.EQUALS))
            assertTrue(evaluator.evaluate("False", false, Operator.EQUALS))
            assertTrue(evaluator.evaluate("0", false, Operator.EQUALS))
            assertTrue(evaluator.evaluate("no", false, Operator.EQUALS))
            assertTrue(evaluator.evaluate("NO", false, Operator.EQUALS))
            assertTrue(evaluator.evaluate("off", false, Operator.EQUALS))
            assertTrue(evaluator.evaluate("OFF", false, Operator.EQUALS))
        }

        @Test
        fun `should convert numeric zero to false`() {
            assertTrue(evaluator.evaluate(0, false, Operator.EQUALS))
            assertTrue(evaluator.evaluate(0.0, false, Operator.EQUALS))
            assertTrue(evaluator.evaluate(-0.0, false, Operator.EQUALS))
        }

        @Test
        fun `should convert non-zero numbers to true`() {
            assertTrue(evaluator.evaluate(1, true, Operator.EQUALS))
            assertTrue(evaluator.evaluate(-1, true, Operator.EQUALS))
            assertTrue(evaluator.evaluate(42, true, Operator.EQUALS))
            assertTrue(evaluator.evaluate(3.14159, true, Operator.EQUALS))
            assertTrue(evaluator.evaluate(-100.5, true, Operator.EQUALS))
        }

        @Test
        fun `should handle different numeric types`() {
            assertTrue(evaluator.evaluate(1.toByte(), true, Operator.EQUALS))
            assertTrue(evaluator.evaluate(0.toShort(), false, Operator.EQUALS))
            assertTrue(evaluator.evaluate(1.toInt(), true, Operator.EQUALS))
            assertTrue(evaluator.evaluate(0.toLong(), false, Operator.EQUALS))
            assertTrue(evaluator.evaluate(1.toFloat(), true, Operator.EQUALS))
            assertTrue(evaluator.evaluate(0.toDouble(), false, Operator.EQUALS))
        }

        @Test
        fun `should return null for invalid conversions`() {
            assertFalse(evaluator.evaluate("maybe", true, Operator.EQUALS))
            assertFalse(evaluator.evaluate("invalid", false, Operator.EQUALS))
            assertFalse(evaluator.evaluate("", true, Operator.EQUALS))
            assertFalse(evaluator.evaluate("null", false, Operator.EQUALS))
        }

        @Test
        fun `should return null for non-convertible types`() {
            assertFalse(evaluator.evaluate(listOf(1, 2, 3), true, Operator.EQUALS))
            assertFalse(evaluator.evaluate(mapOf("key" to "value"), false, Operator.EQUALS))
            assertFalse(evaluator.evaluate(setOf(1, 2, 3), true, Operator.EQUALS))
        }

    }

    @Nested
    @DisplayName("Edge cases Tests")
    inner class EdgeCasesTests {

        @Test
        fun `should handle mixed type comparisons`() {
            assertTrue(evaluator.evaluate(true, "true", Operator.EQUALS))
            assertTrue(evaluator.evaluate("false", false, Operator.EQUALS))
            assertTrue(evaluator.evaluate(1, "yes", Operator.EQUALS))
            assertTrue(evaluator.evaluate("no", 0, Operator.EQUALS))
        }

        @Test
        fun `should handle case variations`() {
            assertTrue(evaluator.evaluate("tRuE", true, Operator.EQUALS))
            assertTrue(evaluator.evaluate("fAlSe", false, Operator.EQUALS))
            assertTrue(evaluator.evaluate("yEs", true, Operator.EQUALS))
            assertTrue(evaluator.evaluate("nO", false, Operator.EQUALS))
        }

        @Test
        fun `should handle whitespace in strings`() {
            assertFalse(evaluator.evaluate(" true ", true, Operator.EQUALS))
            assertFalse(evaluator.evaluate("true ", true, Operator.EQUALS))
            assertFalse(evaluator.evaluate(" false", false, Operator.EQUALS))
        }

        @Test
        fun `should handle special numeric values`() {
            assertTrue(evaluator.evaluate(Double.POSITIVE_INFINITY, true, Operator.EQUALS))
            assertTrue(evaluator.evaluate(Double.NEGATIVE_INFINITY, true, Operator.EQUALS))
            // Note: NaN is converted to non-zero number so it becomes true
            assertTrue(evaluator.evaluate(Double.NaN, true, Operator.EQUALS))
        }
    }

    @Nested
    @DisplayName("Unsupported operator Tests")
    inner class UnsupportedOperatorTests {

        @Test
        fun `should throw exception for unsupported operators`() {
            assertThrows<IllegalArgumentException> {
                evaluator.evaluate(true, true, Operator.GREATER_THAN)
            }
        }

        @Test
        fun `should throw exception for CONTAINS operator`() {
            assertThrows<IllegalArgumentException> {
                evaluator.evaluate(true, true, Operator.CONTAINS)
            }
        }

        @Test
        fun `should throw exception for STARTS_WITH operator`() {
            assertThrows<IllegalArgumentException> {
                evaluator.evaluate(true, true, Operator.STARTS_WITH)
            }
        }

        @Test
        fun `should throw exception for ENDS_WITH operator`() {
            assertThrows<IllegalArgumentException> {
                evaluator.evaluate(true, true, Operator.ENDS_WITH)
            }
        }

        @Test
        fun `should throw exception for LESS_THAN operator`() {
            assertThrows<IllegalArgumentException> {
                evaluator.evaluate(true, true, Operator.LESS_THAN)
            }
        }

        @Test
        fun `should throw exception for IN operator`() {
            assertThrows<IllegalArgumentException> {
                evaluator.evaluate(true, true, Operator.IN)
            }
        }

        @Test
        fun `should throw exception for REGEX_MATCH operator`() {
            assertThrows<IllegalArgumentException> {
                evaluator.evaluate(true, "true", Operator.REGEX_MATCH)
            }
        }
    }
}