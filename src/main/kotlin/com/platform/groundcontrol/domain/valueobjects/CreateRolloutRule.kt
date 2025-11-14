package com.platform.groundcontrol.domain.valueobjects

import jakarta.validation.constraints.Max
import jakarta.validation.constraints.Min
import jakarta.validation.constraints.Size
import java.time.Instant

data class CreateRolloutRule(
    @field:Min(value = 0, message = "Percentage must be between 0 and 100")
    @field:Max(value = 100, message = "Percentage must be between 0 and 100")
    val percentage: Double? = null,

    @field:Min(value = 0, message = "Priority must be non-negative")
    val priority: Int = 0,

    val active: Boolean = true,

    @field:Size(max = 255, message = "Attribute key cannot exceed 255 characters")
    val attributeKey: String? = null,

    @field:Size(max = 255, message = "Attribute value cannot exceed 255 characters")
    val attributeValue: String? = null,

    @field:Size(max = 255, message = "Distribution key attribute cannot exceed 255 characters")
    val distributionKeyAttribute: String? = null,

    val valueBool: Boolean? = null,

    @field:Size(max = 1000, message = "Value string cannot exceed 1000 characters")
    val valueString: String? = null,

    val valueInt: Int? = null,

    @field:Min(value = 0, message = "Value percentage must be between 0 and 100")
    @field:Max(value = 100, message = "Value percentage must be between 0 and 100")
    val valuePercentage: Double? = null,

    @field:Size(max = 255, message = "Variant name cannot exceed 255 characters")
    val variantName: String? = null,

    val startAt: Instant? = null,
    val endAt: Instant? = null,

    @field:Size(max = 50, message = "Cannot have more than 50 conditions per rule")
    val conditions: List<Condition> = emptyList()
) {
    init {
        if (startAt != null && endAt != null) {
            require(startAt.isBefore(endAt)) { "startAt must be before endAt" }
        }

        val valueCount = listOfNotNull(valueBool, valueString, valueInt, valuePercentage).size
        require(valueCount <= 1) { "Only one value type can be specified" }
    }
}
