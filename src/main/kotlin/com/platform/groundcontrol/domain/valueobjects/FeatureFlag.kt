package com.platform.groundcontrol.domain.valueobjects

import com.platform.groundcontrol.infrastructure.entities.FeatureFlagEntity
import java.time.Instant
import java.util.UUID

@JvmInline
value class FeatureFlagId(val value: UUID?) {}

@JvmInline
value class FeatureFlagCode(val value: String) {
    init {
        require(value.matches(Regex("[a-zA-Z0-9_-]+"))) { "Feature flag code must contain only letters, numbers, hyphens, or underscores." }
        require(value.length <= 50) { "Feature flag code cannot exceed 50 characters." }
    }
}

@JvmInline
value class FeatureFlagName(val value: String) {
    init {
        require(value.isNotBlank()) { "Feature flag name cannot be blank." }
        require(value.length <= 100) { "Feature flag name cannot exceed 100 characters." }
    }
}

class FeatureFlag(
    val id: FeatureFlagId,
    code: FeatureFlagCode,
    name: FeatureFlagName,
    description: String?,
    initialEnabled: Boolean,
    dueAt: Instant? = null
) {
    var name: FeatureFlagName = name
        private set

    var code: FeatureFlagCode = code
        private set

    var description: String? = description
        private set

    var isEnabled: Boolean = initialEnabled
        private set

    var previousEnabledState: Boolean = initialEnabled

    var createdAt: Instant = Instant.now()

    var updatedAt: Instant = Instant.now()

    var dueAt: Instant? = dueAt
        private set

    fun enable() {
        if (!isEnabled) {
            previousEnabledState = isEnabled
            isEnabled = true
            updatedAt = Instant.now()
        }
    }

    fun disable() {
        if (isEnabled) {
            previousEnabledState = isEnabled
            isEnabled = false
            updatedAt = Instant.now()
        }
    }

    fun updateDetails(newName: FeatureFlagName, newDescription: String?) {
        if (newName != name || newDescription != description) {
            this.name = newName
            this.description = newDescription
            this.updatedAt = Instant.now()
        }
    }

    fun isExpired(now: Instant = Instant.now()): Boolean =
        dueAt?.isBefore(now) ?: false
}

fun FeatureFlag.toEntity(): FeatureFlagEntity {
    return FeatureFlagEntity(
        id = this.id.value,
        code = this.code.value,
        name = this.name.value,
        description = this.description,
        isEnabled = this.isEnabled,
        previousEnabledState = this.previousEnabledState,
        createdAt = this.createdAt,
        updatedAt = this.updatedAt,
        dueAt = this.dueAt
    )
}