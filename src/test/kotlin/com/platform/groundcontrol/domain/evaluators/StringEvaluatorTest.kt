package com.platform.groundcontrol.domain.evaluators

import com.platform.groundcontrol.domain.enums.DataType
import com.platform.groundcontrol.domain.enums.Operator
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.assertThrows
import kotlin.test.assertFalse
import kotlin.test.assertTrue

@DisplayName("StringEvaluator Tests")
class StringEvaluatorTest {

    private val evaluator = StringEvaluator()

    @Nested
    @DisplayName("canHandle Tests")
    inner class CanHandleTests {

        @Test
        fun `should handle STRING data type with EQUALS operator`() {
            assertTrue(evaluator.canHandle(Operator.EQUALS, DataType.STRING))
        }

        @Test
        fun `should handle STRING data type with NOT_EQUALS operator`() {
            assertTrue(evaluator.canHandle(Operator.NOT_EQUALS, DataType.STRING))
        }

        @Test
        fun `should handle STRING data type with CONTAINS operator`() {
            assertTrue(evaluator.canHandle(Operator.CONTAINS, DataType.STRING))
        }

        @Test
        fun `should handle STRING data type with STARTS_WITH operator`() {
            assertTrue(evaluator.canHandle(Operator.STARTS_WITH, DataType.STRING))
        }

        @Test
        fun `should handle STRING data type with ENDS_WITH operator`() {
            assertTrue(evaluator.canHandle(Operator.ENDS_WITH, DataType.STRING))
        }

        @Test
        fun `should handle STRING data type with REGEX_MATCH operator`() {
            assertTrue(evaluator.canHandle(Operator.REGEX_MATCH, DataType.STRING))
        }

        @Test
        fun `should not handle STRING data type with GREATER_THAN operator`() {
            assertFalse(evaluator.canHandle(Operator.GREATER_THAN, DataType.STRING))
        }

        @Test
        fun `should not handle STRING data type with LESS_THAN operator`() {
            assertFalse(evaluator.canHandle(Operator.LESS_THAN, DataType.STRING))
        }

        @Test
        fun `should not handle STRING data type with IN operator`() {
            assertFalse(evaluator.canHandle(Operator.IN, DataType.STRING))
        }

        @Test
        fun `should not handle NUMBER data type with any operator`() {
            assertFalse(evaluator.canHandle(Operator.EQUALS, DataType.NUMBER))
            assertFalse(evaluator.canHandle(Operator.CONTAINS, DataType.NUMBER))
            assertFalse(evaluator.canHandle(Operator.STARTS_WITH, DataType.NUMBER))
        }

        @Test
        fun `should not handle BOOLEAN data type with any operator`() {
            assertFalse(evaluator.canHandle(Operator.EQUALS, DataType.BOOLEAN))
            assertFalse(evaluator.canHandle(Operator.CONTAINS, DataType.BOOLEAN))
        }

        @Test
        fun `should not handle ARRAY data type with any operator`() {
            assertFalse(evaluator.canHandle(Operator.EQUALS, DataType.ARRAY))
            assertFalse(evaluator.canHandle(Operator.CONTAINS, DataType.ARRAY))
        }

        @Test
        fun `should not handle DATE data type with any operator`() {
            assertFalse(evaluator.canHandle(Operator.EQUALS, DataType.DATE))
            assertFalse(evaluator.canHandle(Operator.CONTAINS, DataType.DATE))
        }
    }

    @Nested
    @DisplayName("EQUALS operator Tests")
    inner class EqualsTests {

        @Test
        fun `should return true when strings are equal`() {
            assertTrue(evaluator.evaluate("test", "test", Operator.EQUALS))
        }

        @Test
        fun `should return false when strings are not equal`() {
            assertFalse(evaluator.evaluate("test", "other", Operator.EQUALS))
        }

        @Test
        fun `should handle case-sensitive comparison`() {
            assertFalse(evaluator.evaluate("Test", "test", Operator.EQUALS))
            assertFalse(evaluator.evaluate("TEST", "test", Operator.EQUALS))
        }

        @Test
        fun `should handle empty strings`() {
            assertTrue(evaluator.evaluate("", "", Operator.EQUALS))
            assertFalse(evaluator.evaluate("test", "", Operator.EQUALS))
            assertFalse(evaluator.evaluate("", "test", Operator.EQUALS))
        }

        @Test
        fun `should convert non-string values to string`() {
            assertTrue(evaluator.evaluate(123, "123", Operator.EQUALS))
            assertTrue(evaluator.evaluate(true, "true", Operator.EQUALS))
        }
    }

    @Nested
    @DisplayName("NOT_EQUALS operator Tests")
    inner class NotEqualsTests {

        @Test
        fun `should return false when strings are equal`() {
            assertFalse(evaluator.evaluate("test", "test", Operator.NOT_EQUALS))
        }

        @Test
        fun `should return true when strings are not equal`() {
            assertTrue(evaluator.evaluate("test", "other", Operator.NOT_EQUALS))
        }

        @Test
        fun `should handle case-sensitive comparison`() {
            assertTrue(evaluator.evaluate("Test", "test", Operator.NOT_EQUALS))
            assertTrue(evaluator.evaluate("TEST", "test", Operator.NOT_EQUALS))
        }

        @Test
        fun `should handle empty strings`() {
            assertFalse(evaluator.evaluate("", "", Operator.NOT_EQUALS))
            assertTrue(evaluator.evaluate("test", "", Operator.NOT_EQUALS))
            assertTrue(evaluator.evaluate("", "test", Operator.NOT_EQUALS))
        }
    }

    @Nested
    @DisplayName("CONTAINS operator Tests")
    inner class ContainsTests {

        @Test
        fun `should return true when string contains substring`() {
            assertTrue(evaluator.evaluate("hello world", "world", Operator.CONTAINS))
            assertTrue(evaluator.evaluate("hello world", "hello", Operator.CONTAINS))
            assertTrue(evaluator.evaluate("hello world", "o w", Operator.CONTAINS))
        }

        @Test
        fun `should return false when string does not contain substring`() {
            assertFalse(evaluator.evaluate("hello world", "foo", Operator.CONTAINS))
            assertTrue(evaluator.evaluate("hello world", "WORLD", Operator.CONTAINS)) // case insensitive should pass
        }

        @Test
        fun `should be case insensitive`() {
            assertTrue(evaluator.evaluate("Hello World", "world", Operator.CONTAINS))
            assertTrue(evaluator.evaluate("hello world", "WORLD", Operator.CONTAINS))
            assertTrue(evaluator.evaluate("HELLO WORLD", "hello", Operator.CONTAINS))
        }

        @Test
        fun `should handle empty substring`() {
            assertTrue(evaluator.evaluate("hello world", "", Operator.CONTAINS))
        }

        @Test
        fun `should handle empty main string`() {
            assertFalse(evaluator.evaluate("", "test", Operator.CONTAINS))
            assertTrue(evaluator.evaluate("", "", Operator.CONTAINS))
        }

        @Test
        fun `should handle full string match`() {
            assertTrue(evaluator.evaluate("test", "test", Operator.CONTAINS))
        }
    }

    @Nested
    @DisplayName("STARTS_WITH operator Tests")
    inner class StartsWithTests {

        @Test
        fun `should return true when string starts with prefix`() {
            assertTrue(evaluator.evaluate("hello world", "hello", Operator.STARTS_WITH))
            assertTrue(evaluator.evaluate("hello world", "h", Operator.STARTS_WITH))
        }

        @Test
        fun `should return false when string does not start with prefix`() {
            assertFalse(evaluator.evaluate("hello world", "world", Operator.STARTS_WITH))
            assertFalse(evaluator.evaluate("hello world", "foo", Operator.STARTS_WITH))
        }

        @Test
        fun `should be case insensitive`() {
            assertTrue(evaluator.evaluate("Hello World", "hello", Operator.STARTS_WITH))
            assertTrue(evaluator.evaluate("hello world", "HELLO", Operator.STARTS_WITH))
        }

        @Test
        fun `should handle empty prefix`() {
            assertTrue(evaluator.evaluate("hello world", "", Operator.STARTS_WITH))
        }

        @Test
        fun `should handle empty main string`() {
            assertFalse(evaluator.evaluate("", "test", Operator.STARTS_WITH))
            assertTrue(evaluator.evaluate("", "", Operator.STARTS_WITH))
        }

        @Test
        fun `should handle full string match`() {
            assertTrue(evaluator.evaluate("test", "test", Operator.STARTS_WITH))
        }
    }

    @Nested
    @DisplayName("ENDS_WITH operator Tests")
    inner class EndsWithTests {

        @Test
        fun `should return true when string ends with suffix`() {
            assertTrue(evaluator.evaluate("hello world", "world", Operator.ENDS_WITH))
            assertTrue(evaluator.evaluate("hello world", "d", Operator.ENDS_WITH))
        }

        @Test
        fun `should return false when string does not end with suffix`() {
            assertFalse(evaluator.evaluate("hello world", "hello", Operator.ENDS_WITH))
            assertFalse(evaluator.evaluate("hello world", "foo", Operator.ENDS_WITH))
        }

        @Test
        fun `should be case insensitive`() {
            assertTrue(evaluator.evaluate("Hello World", "world", Operator.ENDS_WITH))
            assertTrue(evaluator.evaluate("hello world", "WORLD", Operator.ENDS_WITH))
        }

        @Test
        fun `should handle empty suffix`() {
            assertTrue(evaluator.evaluate("hello world", "", Operator.ENDS_WITH))
        }

        @Test
        fun `should handle empty main string`() {
            assertFalse(evaluator.evaluate("", "test", Operator.ENDS_WITH))
            assertTrue(evaluator.evaluate("", "", Operator.ENDS_WITH))
        }

        @Test
        fun `should handle full string match`() {
            assertTrue(evaluator.evaluate("test", "test", Operator.ENDS_WITH))
        }
    }

    @Nested
    @DisplayName("REGEX_MATCH operator Tests")
    inner class RegexMatchTests {

        @Test
        fun `should return true for valid regex matches`() {
            assertTrue(evaluator.evaluate("hello123", "hello\\d+", Operator.REGEX_MATCH))
            assertTrue(evaluator.evaluate("test@example.com", ".*@.*\\.com", Operator.REGEX_MATCH))
            assertTrue(evaluator.evaluate("abc", "[a-z]+", Operator.REGEX_MATCH))
        }

        @Test
        fun `should return false for non-matching regex`() {
            assertFalse(evaluator.evaluate("hello", "\\d+", Operator.REGEX_MATCH))
            assertFalse(evaluator.evaluate("test@example.org", ".*@.*\\.com", Operator.REGEX_MATCH))
        }

        @Test
        fun `should handle simple string patterns`() {
            assertTrue(evaluator.evaluate("test", "test", Operator.REGEX_MATCH))
            assertFalse(evaluator.evaluate("test", "other", Operator.REGEX_MATCH))
        }

        @Test
        fun `should handle complex regex patterns`() {
            assertTrue(evaluator.evaluate("user123", "user\\d{3}", Operator.REGEX_MATCH))
            assertFalse(evaluator.evaluate("user12", "user\\d{3}", Operator.REGEX_MATCH))
        }

        @Test
        fun `should return false for invalid regex patterns`() {
            assertFalse(evaluator.evaluate("test", "[invalid", Operator.REGEX_MATCH))
            assertFalse(evaluator.evaluate("test", "*invalid*", Operator.REGEX_MATCH))
        }

        @Test
        fun `should handle empty string patterns`() {
            assertTrue(evaluator.evaluate("", "", Operator.REGEX_MATCH))
            assertFalse(evaluator.evaluate("test", "", Operator.REGEX_MATCH))
        }

        @Test
        fun `should handle word boundaries and anchors`() {
            assertTrue(evaluator.evaluate("hello world", "^hello.*", Operator.REGEX_MATCH))
            assertTrue(evaluator.evaluate("hello world", ".*world$", Operator.REGEX_MATCH))
            assertFalse(evaluator.evaluate("hello world", "^world.*", Operator.REGEX_MATCH))
        }
    }

    @Nested
    @DisplayName("Unsupported operator Tests")
    inner class UnsupportedOperatorTests {

        @Test
        fun `should throw exception for unsupported operators`() {
            assertThrows<IllegalArgumentException> {
                evaluator.evaluate("test", "test", Operator.GREATER_THAN)
            }
        }

        @Test
        fun `should throw exception for IN operator`() {
            assertThrows<IllegalArgumentException> {
                evaluator.evaluate("test", listOf("test"), Operator.IN)
            }
        }

        @Test
        fun `should throw exception for LESS_THAN operator`() {
            assertThrows<IllegalArgumentException> {
                evaluator.evaluate("test", "test", Operator.LESS_THAN)
            }
        }

        @Test
        fun `should throw exception for GREATER_EQUAL operator`() {
            assertThrows<IllegalArgumentException> {
                evaluator.evaluate("test", "test", Operator.GREATER_EQUAL)
            }
        }
    }

    @Nested
    @DisplayName("Type conversion Tests")
    inner class TypeConversionTests {

        @Test
        fun `should convert numbers to strings`() {
            assertTrue(evaluator.evaluate(123, "123", Operator.EQUALS))
            assertTrue(evaluator.evaluate(123.45, "123.45", Operator.EQUALS))
            assertTrue(evaluator.evaluate(-42, "-42", Operator.EQUALS))
        }

        @Test
        fun `should convert boolean to string`() {
            assertTrue(evaluator.evaluate(true, "true", Operator.EQUALS))
            assertTrue(evaluator.evaluate(false, "false", Operator.EQUALS))
        }


        @Test
        fun `should work with mixed types for contains`() {
            assertTrue(evaluator.evaluate("user123", 123, Operator.CONTAINS))
            assertTrue(evaluator.evaluate(12345, "234", Operator.CONTAINS))
        }

        @Test
        fun `should work with mixed types for starts_with`() {
            assertTrue(evaluator.evaluate("user123", "user", Operator.STARTS_WITH))
            assertTrue(evaluator.evaluate(12345, 123, Operator.STARTS_WITH))
        }

        @Test
        fun `should work with mixed types for ends_with`() {
            assertTrue(evaluator.evaluate("user123", 123, Operator.ENDS_WITH))
            assertTrue(evaluator.evaluate(12345, 345, Operator.ENDS_WITH))
        }
    }
}