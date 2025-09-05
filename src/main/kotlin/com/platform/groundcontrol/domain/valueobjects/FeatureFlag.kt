package com.platform.groundcontrol.domain.valueobjects

import com.platform.groundcontrol.domain.enums.FlagType
import java.io.Serializable
import java.time.Instant

data class FeatureFlag(
    val id: Long?,
    var code: String,
    var name: String,
    var description: String?,
    var value: Any?,
    var valueType: FlagType,
    var enabled: Boolean,
    var dueAt: Instant? = null,
    val rolloutRules: MutableList<RolloutRule> = mutableListOf(),
    var createdAt: Instant = Instant.now(),
    var updatedAt: Instant = Instant.now()
) : Serializable {
    init {
        require(code.matches(Regex("[a-zA-Z0-9_-]+"))) { "Feature flag code must contain only letters, numbers, hyphens, or underscores." }
        require(code.length <= 50) { "Feature flag code cannot exceed 50 characters." }
        require(name.isNotBlank()) { "Feature flag name cannot be blank." }
        require(name.length <= 100) { "Feature flag name cannot exceed 100 characters." }
    }
    
    fun enable() {
        if (!enabled) {
            enabled = true
            updatedAt = Instant.now()
        }
    }

    fun disable() {
        if (enabled) {
            enabled = false
            updatedAt = Instant.now()
        }
    }

    fun updateDetails(newName: String?, newCode: String?, newDescription: String?, newDueAt: Instant?) {
        var hasChanges = false

        newName?.let {
            require(it.isNotBlank()) { "Feature flag name cannot be blank." }
            require(it.length <= 100) { "Feature flag name cannot exceed 100 characters." }
            if (it != name) {
                this.name = it
                hasChanges = true
            }
        }

        newCode?.let {
            require(it.matches(Regex("[a-zA-Z0-9_-]+"))) { "Feature flag code must contain only letters, numbers, hyphens, or underscores." }
            require(it.length <= 50) { "Feature flag code cannot exceed 50 characters." }
            if (it != code) {
                this.code = it
                hasChanges = true
            }
        }

        if (newDescription != this.description) {
            this.description = newDescription
            hasChanges = true
        }

        if (newDueAt != this.dueAt) {
            this.dueAt = newDueAt
            hasChanges = true
        }

        if (hasChanges) {
            this.updatedAt = Instant.now()
        }
    }

    fun isExpired(now: Instant = Instant.now()): Boolean =
        dueAt?.isBefore(now) ?: false

    fun addRolloutRule(rolloutRule: RolloutRule) {
        this.rolloutRules.add(rolloutRule)
        updatedAt = Instant.now()
    }

    fun removeRolloutRule(rolloutRule: RolloutRule) {
        this.rolloutRules.remove(rolloutRule)
        updatedAt = Instant.now()
    }
}