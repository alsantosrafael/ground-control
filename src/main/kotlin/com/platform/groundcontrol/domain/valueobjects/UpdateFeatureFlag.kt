package com.platform.groundcontrol.domain.valueobjects

import jakarta.validation.constraints.Pattern
import jakarta.validation.constraints.Size
import java.time.Instant

data class UpdateFeatureFlag(
    @field:Size(min = 1, max = 100, message = "Name must be between 1 and 100 characters")
    val name: String? = null,

    @field:Size(min = 1, max = 50, message = "Code must be between 1 and 50 characters")
    @field:Pattern(
        regexp = "^[a-zA-Z0-9_-]+$",
        message = "Code must contain only letters, numbers, hyphens, or underscores"
    )
    val code: String? = null,

    @field:Size(max = 500, message = "Description cannot exceed 500 characters")
    val description: String? = null,

    val dueAt: Instant? = null
)
