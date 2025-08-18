package com.platform.groundcontrol.domain.mappers

import com.platform.groundcontrol.domain.entities.RolloutRuleEntity
import com.platform.groundcontrol.domain.valueobjects.RolloutRule

object RolloutRuleMapper {
    fun RolloutRuleEntity.toDomain(): RolloutRule =
        RolloutRule(
            id = this.id,
            featureFlagId = this.featureFlag?.id,
            attributeKey = this.attributeKey,
            attributeValue = this.attributeValue,
            percentage = this.percentage,
            distributionKeyAttribute = this.distributionKeyAttribute,
            valueBool = this.valueBool,
            valueString = this.valueString,
            valueInt = this.valueInt,
            valuePercentage = this.valuePercentage,
            variantName = this.variantName,
            startAt = this.startAt,
            endAt = this.endAt,
            priority = this.priority,
            active = this.active,
            conditions = this.conditions
        )

    fun RolloutRule.toEntity(): RolloutRuleEntity =
        RolloutRuleEntity(
            id = this.id,
            featureFlag = null, // Será setado no FeatureFlagMapper
            attributeKey = this.attributeKey,
            attributeValue = this.attributeValue,
            percentage = this.percentage,
            distributionKeyAttribute = this.distributionKeyAttribute,
            valueBool = this.valueBool,
            valueString = this.valueString,
            valueInt = this.valueInt,
            valuePercentage = this.valuePercentage,
            variantName = this.variantName,
            startAt = this.startAt,
            endAt = this.endAt,
            priority = this.priority ?: 0,
            active = this.active,
            conditions = this.conditions
        )
}