package com.platform.groundcontrol.domain.evaluators

import com.platform.groundcontrol.domain.enums.DataType
import com.platform.groundcontrol.domain.enums.Operator
import org.springframework.stereotype.Component

@Component
class BooleanEvaluator: ConditionEvaluator {
    override fun canHandle(operator: Operator, dataType: DataType): Boolean {
        return dataType == DataType.BOOLEAN && operator in listOf(
            Operator.EQUALS,
            Operator.NOT_EQUALS
        )
    }

    override fun evaluate(attributeValue: Any, conditionValue: Any, operator: Operator): Boolean {
        val booleanAttributeValue = convertToBoolean(attributeValue)
        val booleanConditionValue = convertToBoolean(conditionValue)

        if (booleanAttributeValue == null || booleanConditionValue == null) {
            return false
        }

        return when (operator) {
            Operator.EQUALS -> booleanAttributeValue == booleanConditionValue
            Operator.NOT_EQUALS -> booleanAttributeValue != booleanConditionValue
            else -> throw IllegalArgumentException("Unsupported operator for boolean evaluation: $operator")
        }
    }

    private fun convertToBoolean(value: Any): Boolean? {
        return when (value) {
            is Boolean -> value
            is String -> when (value.lowercase()) {
                "true", "1", "yes", "on" -> true
                "false", "0", "no", "off" -> false
                else -> null
            }
            is Number -> value.toDouble() != 0.0
            else -> null
        }
    }
}