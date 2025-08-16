package com.platform.groundcontrol.domain.evaluators

import com.platform.groundcontrol.domain.enums.DataType
import com.platform.groundcontrol.domain.enums.Operator
import org.springframework.stereotype.Component

@Component
class ArrayEvaluator: ConditionEvaluator {
    override fun canHandle(operator: Operator, dataType: DataType): Boolean {
        return dataType == DataType.ARRAY && operator in listOf(
            Operator.IN,
            Operator.NOT_IN
        )
    }

    override fun evaluate(attributeValue: Any, conditionValue: Any, operator: Operator): Boolean {
        val valueList = convertToList(conditionValue) ?: return false
        return when (operator) {
            Operator.IN -> valueList.contains(attributeValue)
            Operator.NOT_IN -> !valueList.contains(attributeValue)
            else -> throw IllegalArgumentException("Unsupported operator for array evaluation: $operator")
        }
    }

    private fun convertToList(value: Any): List<Any>? {
        return when (value) {
            is List<*> -> value.filterNotNull()
            is Array<*> -> value.filterNotNull()
            is String -> value.split(",").map { it.trim() } // Support comma-separated strings
            else -> null
        }
    }

}