package com.platform.groundcontrol.domain.valueobjects

import com.platform.groundcontrol.domain.enums.FlagType
import java.time.Instant

data class CreateFeatureFlag(
    val name: String,
    val code: String,
    val description: String? = null,
    val value: Any? = null,
    val valueType: FlagType,
    val enabled: Boolean,
    val dueAt: Instant? = null
) {}