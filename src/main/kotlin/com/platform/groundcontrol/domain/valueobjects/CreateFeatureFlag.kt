package com.platform.groundcontrol.domain.valueobjects

import java.time.Instant

data class CreateFeatureFlag(
    val name: String,
    val code: String,
    val description: String? = null,
    val isEnabled: Boolean,
    val dueAt: Instant? = null
) {}