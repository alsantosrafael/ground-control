package com.platform.groundcontrol.domain.evaluators

import com.platform.groundcontrol.domain.enums.DataType
import com.platform.groundcontrol.domain.enums.Operator
import org.springframework.stereotype.Component

@Component
class NumericEvaluator: ConditionEvaluator {

    override fun canHandle(operator: Operator, dataType: DataType): Boolean {
        return dataType == DataType.NUMBER && operator in listOf(
            Operator.EQUALS,
            Operator.NOT_EQUALS,
            Operator.GREATER_THAN,
            Operator.GREATER_EQUAL,
            Operator.LESS_THAN,
            Operator.LESS_EQUAL
        )
    }

    override fun evaluate(attributeValue: Any, conditionValue: Any, operator: Operator): Boolean {
        val numericAttributeValue = convertToNumber(attributeValue) ?: return false
        val numericConditionValue = convertToNumber(conditionValue) ?: return false

        return when (operator) {
            Operator.EQUALS -> numericAttributeValue == numericConditionValue
            Operator.NOT_EQUALS -> numericAttributeValue != numericConditionValue
            Operator.GREATER_THAN -> numericAttributeValue > numericConditionValue
            Operator.GREATER_EQUAL -> numericAttributeValue >= numericConditionValue
            Operator.LESS_THAN -> numericAttributeValue < numericConditionValue
            Operator.LESS_EQUAL -> numericAttributeValue <= numericConditionValue

            else -> throw IllegalArgumentException("Unsupported operator for numeric evaluation: $operator")
        }
    }

    private fun convertToNumber(value: Any): Double? {
        return when (value) {
            is Number -> value.toDouble()
            is String -> value.toDoubleOrNull()
            else -> null
        }
    }

}