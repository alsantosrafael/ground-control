package com.platform.groundcontrol.domain.valueobjects

import jakarta.validation.constraints.Size
import java.util.UUID

data class ReorderRolloutRules(
    @field:Size(min = 1, message = "Must provide at least one rule to reorder")
    val ruleIds: List<UUID>
)
