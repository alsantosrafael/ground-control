package com.platform.groundcontrol.domain.valueobjects

import com.platform.groundcontrol.domain.enums.EvaluationType
import com.platform.groundcontrol.domain.enums.FlagType
import java.time.Instant

data class EvaluationContext(
    val userId: String,
    val attributes: Map<String, String> = emptyMap(),
    val timestamp: Instant = Instant.now()
){
    fun getDistributionKey(attributeName: String?): String {
        return when (attributeName) {
            null -> userId
            else -> attributes[attributeName] ?: userId
        }
    }
}

data class EvaluationResult(
    val enabled: Boolean,
    val value: Any,
    val valueType: FlagType,
    val variant: String? = null,
    val evaluationType: EvaluationType? = EvaluationType.DEFAULT,
    val reason: String? = "default"
) {}
