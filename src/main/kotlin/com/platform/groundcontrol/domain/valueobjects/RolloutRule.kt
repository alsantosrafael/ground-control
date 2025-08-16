package com.platform.groundcontrol.domain.valueobjects

import java.time.Instant
import java.util.UUID

@JvmInline
value class RolloutRuleId(val value: UUID?)

class RolloutRule(
    val id: RolloutRuleId,
    val featureFlagId: FeatureFlagId,
    attributeKey: String?,
    attributeValue: String?,
    percentage: Double?,
    distributionKeyAttribute: String?,
    valueBool: Boolean?,
    valueString: String?,
    valueInt: Int?,
    valuePercentage: Double?,
    variantName: String?,
    startAt: Instant? = null,
    endAt: Instant? = null,
    priority: Int? = 0,
    active: Boolean,
    conditions: MutableList<Condition> = mutableListOf()
) {
    var attributeKey: String? = attributeKey
        private set

    var attributeValue: String? = attributeValue
        private set

    var percentage: Double? = percentage
        private set

    var distributionKeyAttribute: String? = distributionKeyAttribute
        private set

    var valueBool: Boolean? = valueBool
        private set

    var valueString: String? = valueString
        private set

    var valueInt: Int? = valueInt
        private set

    var valuePercentage: Double? = valuePercentage
        private set

    var variantName: String? = variantName
        private set

    var startAt: Instant? = startAt
        private set

    var endAt: Instant? = endAt
        private set

    var priority: Int? = priority
        private set

    var active: Boolean = active
        private set

    var conditions: MutableList<Condition> = mutableListOf()
        private set

    fun getRuleValue(): Any? {
        return when {
            valueBool != null -> valueBool
            valueString != null -> valueString
            valueInt != null -> valueInt
            valuePercentage != null -> valuePercentage
            else -> true
        }
    }

    fun hasConditions(): Boolean = conditions.isNotEmpty()


}