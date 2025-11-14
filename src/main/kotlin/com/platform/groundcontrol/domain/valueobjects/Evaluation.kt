package com.platform.groundcontrol.domain.valueobjects

import com.platform.groundcontrol.domain.enums.EvaluationType
import com.platform.groundcontrol.domain.enums.FlagType
import com.platform.groundcontrol.domain.enums.Reason
import jakarta.validation.constraints.Size

data class EvaluationContext(
    @field:Size(max = 100, message = "Subject ID must not exceed 100 characters")
    val subjectId: String? = null,

    @field:Size(max = 50, message = "Attributes map must not contain more than 50 entries")
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
