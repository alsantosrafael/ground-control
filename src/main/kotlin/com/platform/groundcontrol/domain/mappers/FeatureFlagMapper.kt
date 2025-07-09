package com.platform.groundcontrol.domain.mappers

import com.platform.groundcontrol.domain.valueobjects.FeatureFlag
import com.platform.groundcontrol.domain.valueobjects.FeatureFlagCode
import com.platform.groundcontrol.domain.valueobjects.FeatureFlagId
import com.platform.groundcontrol.domain.valueobjects.FeatureFlagName
import com.platform.groundcontrol.infrastructure.entities.FeatureFlagEntity

object FeatureFlagMapper {

    fun FeatureFlagEntity.toDomain(): FeatureFlag {
        return FeatureFlag(
            id = FeatureFlagId(this.id!!),
            code = FeatureFlagCode(this.code),
            name = FeatureFlagName(this.name),
            description = this.description,
            initialEnabled = this.isEnabled,
            dueAt = this.dueAt
        ).apply {

            if (this@toDomain.previousEnabledState != null &&
                this@toDomain.previousEnabledState != this@toDomain.isEnabled) {

                this.previousEnabledState = this@toDomain.previousEnabledState!!

                if (this@toDomain.isEnabled) {
                    this.enable()
                } else {
                    this.disable()
                }
            }

            this.updatedAt = this@toDomain.updatedAt
        }
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
}