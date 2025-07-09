package com.platform.groundcontrol.domain.valueobjects

import java.time.Instant

data class UpdateFeatureFlag(
    val name: String? = null,
    val code: String? = null,
    val description: String? = null,
    val dueAt: Instant? = null
)
