package com.platform.groundcontrol.domain.mappers

import com.platform.groundcontrol.domain.valueobjects.FeatureFlag
import com.platform.groundcontrol.domain.entities.FeatureFlagEntity
import com.platform.groundcontrol.domain.enums.FlagType
import com.platform.groundcontrol.domain.mappers.RolloutRuleMapper.toDomain
import com.platform.groundcontrol.domain.mappers.RolloutRuleMapper.toEntity

object FeatureFlagMapper {

    fun FeatureFlagEntity.toDomain(): FeatureFlag {
        return FeatureFlag(
            id = this.id,
            code = this.code,
            name = this.name,
            description = this.description,
            value = when(this.flagType) {
                FlagType.INT -> this.defaultIntValue
                FlagType.STRING -> this.defaultStringValue
                FlagType.PERCENTAGE -> this.defaultPercentageValue
                FlagType.BOOLEAN -> this.defaultBoolValue
                else -> throw RuntimeException("Something went wrong")
            },
            valueType = this.flagType ?: throw RuntimeException("FlagType is null"),
            enabled = this.enabled,
            dueAt = this.dueAt,
            rolloutRules = this.rolloutRules.map { it.toDomain() }.toMutableList()
        )
    }

    fun FeatureFlag.toEntity(): FeatureFlagEntity {
        val entity = FeatureFlagEntity(
            id = this.id,
            code = this.code,
            name = this.name,
            description = this.description,
            enabled = this.enabled,
            flagType = this.valueType,
            defaultBoolValue = if (this.valueType == FlagType.BOOLEAN) this.value as? Boolean else null,
            defaultIntValue = if (this.valueType == FlagType.INT) this.value as? Int else null,
            defaultStringValue = if (this.valueType == FlagType.STRING) this.value as? String else null,
            defaultPercentageValue = if (this.valueType == FlagType.PERCENTAGE) this.value as? Double else null,
            createdAt = this.createdAt,
            updatedAt = this.updatedAt,
            dueAt = this.dueAt
        )
        val rolloutEntities = this.rolloutRules.map { rule ->
            val ruleEntity = rule.toEntity()
            ruleEntity.featureFlag = entity
            ruleEntity
        }
        entity.rolloutRules.addAll(rolloutEntities)
        return entity
    }
}