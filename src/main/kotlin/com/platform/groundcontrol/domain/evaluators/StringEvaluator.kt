package com.platform.groundcontrol.domain.evaluators

import com.platform.groundcontrol.domain.enums.DataType
import com.platform.groundcontrol.domain.enums.Operator
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeout
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component


@Component
class StringEvaluator: ConditionEvaluator {

    companion object {
        private val logger = LoggerFactory.getLogger(StringEvaluator::class.java)
        const val MAX_REGEX_LENGTH = 1000
        const val REGEX_TIMEOUT_MS = 100L

        private val DANGEROUS_REGEX_PATTERNS = listOf(
            "(.*)*", "(.+)+", "(.{1,}){1,}", "(a|a)*", "(a*)*",
            "(x+x+)+y", "(\\w+)+", "([a-zA-Z]+)*", "(a|ab)*"
        )
    }

    override fun canHandle(
        operator: Operator,
        dataType: DataType
    ): Boolean {
        return dataType == DataType.STRING && operator in listOf(
            Operator.EQUALS,
            Operator.NOT_EQUALS,
            Operator.CONTAINS,
            Operator.ENDS_WITH,
            Operator.STARTS_WITH,
            Operator.REGEX_MATCH
        )
    }

    override fun evaluate(
        attributeValue: Any,
        conditionValue: Any,
        operator: Operator
    ): Boolean {
        val stringAttributeValue = attributeValue.toString()
        val stringConditionValue = conditionValue.toString()

        return when (operator) {
            Operator.EQUALS -> stringAttributeValue == stringConditionValue
            Operator.NOT_EQUALS -> stringAttributeValue != stringConditionValue
            Operator.CONTAINS -> stringAttributeValue.contains(stringConditionValue, ignoreCase = true)
            Operator.STARTS_WITH -> stringAttributeValue.startsWith(stringConditionValue, ignoreCase = true)
            Operator.ENDS_WITH -> stringAttributeValue.endsWith(stringConditionValue, ignoreCase = true)
            Operator.REGEX_MATCH -> matchesRegex(stringAttributeValue, stringConditionValue)
            else -> throw IllegalArgumentException("Unsupported operator for string evaluation: $operator")
        }
    }

    private fun matchesRegex(value: String, pattern: String): Boolean {
        return try {
            // Protection against ReDoS attacks - pattern length check
            if (pattern.length > MAX_REGEX_LENGTH) {
                logger.warn("Regex pattern exceeds maximum length of $MAX_REGEX_LENGTH characters")
                throw IllegalArgumentException("Regex pattern too long (max $MAX_REGEX_LENGTH characters)")
            }

            // Check for common dangerous ReDoS patterns
            if (DANGEROUS_REGEX_PATTERNS.any { pattern.contains(it) }) {
                logger.warn("Potentially dangerous regex pattern detected: $pattern")
                throw IllegalArgumentException("Potentially dangerous regex pattern detected")
            }

            // Execute regex with timeout protection
            runBlocking {
                try {
                    withTimeout(REGEX_TIMEOUT_MS) {
                        val regex = Regex(pattern)
                        value.matches(regex)
                    }
                } catch (e: kotlinx.coroutines.TimeoutCancellationException) {
                    logger.warn("Regex evaluation timeout for pattern: $pattern")
                    false
                }
            }
        } catch (e: IllegalArgumentException) {
            logger.warn("Invalid regex pattern: ${e.message}")
            false
        } catch (e: Exception) {
            logger.error("Unexpected error during regex evaluation", e)
            false
        }
    }
}