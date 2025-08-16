package com.platform.groundcontrol.domain.evaluators

import com.platform.groundcontrol.domain.enums.DataType
import com.platform.groundcontrol.domain.enums.Operator

interface ConditionEvaluator {
    fun canHandle(operator: Operator, dataType: DataType): Boolean
    fun evaluate(attributeValue: Any, conditionValue: Any, operator: Operator): Boolean
}