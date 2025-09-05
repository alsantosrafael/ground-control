package com.platform.groundcontrol.domain.valueobjects

import java.io.Serializable
import java.time.Instant
import java.util.UUID

data class RolloutRule(
    val id: UUID?,
    val featureFlagId: Long?,
    var attributeKey: String?,
    var attributeValue: String?,
    var percentage: Double?,
    var distributionKeyAttribute: String?,
    var valueBool: Boolean?,
    var valueString: String?,
    var valueInt: Int?,
    var valuePercentage: Double?,
    var variantName: String?,
    var startAt: Instant? = null,
    var endAt: Instant? = null,
    var priority: Int? = 0,
    var active: Boolean = true,
    val conditions: MutableList<Condition> = mutableListOf()
) : Serializable {
    init {
        // Validate time constraints
        if (startAt != null && endAt != null) {
            require(startAt!!.isBefore(endAt)) { "startAt must be before endAt" }
        }
        
        // Validate percentage range
        percentage?.let {
            require(it >= 0.0 && it <= 100.0) { "Percentage must be between 0.0 and 100.0, got: $it" }
        }
    }
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