package com.platform.groundcontrol.domain.valueobjects

import com.platform.groundcontrol.domain.enums.FlagType
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotNull
import jakarta.validation.constraints.Pattern
import jakarta.validation.constraints.Size
import java.time.Instant

data class CreateFeatureFlag(
    @field:NotBlank(message = "Name is required")
    @field:Size(min = 3, max = 100, message = "Name must be between 3 and 100 characters")
    val name: String,

    @field:NotBlank(message = "Code is required")
    @field:Size(min = 2, max = 50, message = "Code must be between 2 and 50 characters")
    @field:Pattern(
        regexp = "^[a-zA-Z0-9_-]+$",
        message = "Code must contain only alphanumeric characters, hyphens, and underscores"
    )
    val code: String,

    @field:Size(max = 500, message = "Description must not exceed 500 characters")
    val description: String? = null,

    val value: Any? = null,

    @field:NotNull(message = "Value type is required")
    val valueType: FlagType,

    @field:NotNull(message = "Enabled status is required")
    val enabled: Boolean,

    val dueAt: Instant? = null
)