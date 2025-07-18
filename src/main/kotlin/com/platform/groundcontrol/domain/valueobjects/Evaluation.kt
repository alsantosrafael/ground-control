package com.platform.groundcontrol.domain.valueobjects

import java.time.Instant

data class EvaluationContext(
    val userId: String,
    val attributes: Map<String, String> = emptyMap(),
    val timestamp: Instant = Instant.now()
){}

data class EvaluationResult(
    val enabled: Boolean,
    val value: Any?,
    val variant: String? = null,
    val reason: String = "default"
) {}
