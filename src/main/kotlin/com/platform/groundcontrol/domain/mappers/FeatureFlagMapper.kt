package com.platform.groundcontrol.domain.mappers

import com.platform.groundcontrol.domain.valueobjects.FeatureFlag
import com.platform.groundcontrol.domain.valueobjects.FeatureFlagCode
import com.platform.groundcontrol.domain.valueobjects.FeatureFlagId
import com.platform.groundcontrol.domain.valueobjects.FeatureFlagName
import com.platform.groundcontrol.domain.entities.FeatureFlagEntity

object FeatureFlagMapper {

    fun FeatureFlagEntity.toDomain(): FeatureFlag {
        return FeatureFlag(
            id = FeatureFlagId(this.id!!),
            code = FeatureFlagCode(this.code),
            name = FeatureFlagName(this.name),
            description = this.description,
            initialEnabled = this.enabled,
            dueAt = this.dueAt
        ).apply {



            this.updatedAt = this@toDomain.updatedAt
        }
    }

    fun FeatureFlag.toEntity(): FeatureFlagEntity {
        return FeatureFlagEntity(
            id = this.id.value,
            code = this.code.value,
            name = this.name.value,
            description = this.description,
            enabled = this.enabled,
            createdAt = this.createdAt,
            updatedAt = this.updatedAt,
            dueAt = this.dueAt
        )
    }
}