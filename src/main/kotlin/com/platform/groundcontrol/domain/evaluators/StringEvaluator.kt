package com.platform.groundcontrol.domain.evaluators

import com.platform.groundcontrol.domain.enums.DataType
import com.platform.groundcontrol.domain.enums.Operator
import org.springframework.stereotype.Component


@Component
class StringEvaluator: ConditionEvaluator {
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
            value.matches(Regex(pattern))
        } catch (e: Exception) {
            false
        }
    }
}