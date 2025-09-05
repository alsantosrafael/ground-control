package com.platform.groundcontrol.domain.mappers

import com.platform.groundcontrol.domain.valueobjects.FeatureFlag
import com.platform.groundcontrol.domain.entities.FeatureFlagEntity
import com.platform.groundcontrol.domain.enums.FlagType
import com.platform.groundcontrol.domain.mappers.RolloutRuleMapper.toDomain
import com.platform.groundcontrol.domain.mappers.RolloutRuleMapper.toEntity

object FeatureFlagMapper {

    fun FeatureFlagEntity.toDomain(): FeatureFlag {
        val featureFlag = FeatureFlag(
            id = this.id,
            code = this.code,
            name = this.name,
            description = this.description,
            value = when(this.flagType) {
                FlagType.INT -> this.defaultIntValue
                FlagType.STRING -> this.defaultStringValue
                FlagType.PERCENTAGE -> this.defaultPercentageValue
                FlagType.BOOLEAN -> this.defaultBoolValue
                null -> throw IllegalStateException("FlagType cannot be null")
            },
            valueType = this.flagType ?: throw IllegalStateException("FlagType cannot be null"),
            enabled = this.enabled,
            dueAt = this.dueAt
        )
        
        // Add rollout rules after creation
        featureFlag.rolloutRules.addAll(this.rolloutRules.map { it.toDomain() })
        
        return featureFlag
    }

    fun FeatureFlag.toEntity(): FeatureFlagEntity {
        val entity = FeatureFlagEntity(
            id = this.id,
            code = this.code,
            name = this.name,
            description = this.description,
            enabled = this.enabled,
            flagType = this.valueType,
            defaultBoolValue = if (this.valueType == FlagType.BOOLEAN) {
                when (val v = this.value) {
                    is Boolean -> v
                    else -> throw IllegalArgumentException("Value type mismatch: expected Boolean but got ${v?.javaClass?.simpleName}")
                }
            } else null,
            defaultIntValue = if (this.valueType == FlagType.INT) {
                when (val v = this.value) {
                    is Int -> v
                    else -> throw IllegalArgumentException("Value type mismatch: expected Int but got ${v?.javaClass?.simpleName}")
                }
            } else null,
            defaultStringValue = if (this.valueType == FlagType.STRING) {
                when (val v = this.value) {
                    is String -> v
                    else -> throw IllegalArgumentException("Value type mismatch: expected String but got ${v?.javaClass?.simpleName}")
                }
            } else null,
            defaultPercentageValue = if (this.valueType == FlagType.PERCENTAGE) {
                when (val v = this.value) {
                    is Double -> v
                    else -> throw IllegalArgumentException("Value type mismatch: expected Double but got ${v?.javaClass?.simpleName}")
                }
            } else null,
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