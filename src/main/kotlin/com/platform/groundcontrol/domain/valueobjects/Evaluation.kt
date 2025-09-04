package com.platform.groundcontrol.domain.valueobjects

import com.platform.groundcontrol.domain.enums.EvaluationType
import com.platform.groundcontrol.domain.enums.FlagType
import com.platform.groundcontrol.domain.enums.Reason

data class EvaluationContext(
    val subjectId: String? = null,
    val attributes: Map<String, Any> = emptyMap(),
){
    fun getDistributionKey(attributeName: String?): String? {
        return when (attributeName) {
            null -> subjectId
            else -> attributes[attributeName]?.toString() ?: subjectId
        }
    }
}

data class EvaluationResult(
    val enabled: Boolean,
    val value: Any?,
    val valueType: FlagType? = null,
    val variant: String? = null,
    val evaluationType: EvaluationType? = EvaluationType.DEFAULT,
    val reason: Reason? = Reason.DEFAULT
) {}
