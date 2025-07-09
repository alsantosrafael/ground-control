package com.platform.groundcontrol.domain.valueobjects

import java.time.Instant

data class FeatureFlagUpdate(
    val name: String?,
    val code: String?,
    val description: String?,
    val dueAt: Instant?
)

class FeatureFlagUpdateBuilder {
    var name: String? = null
    var code: String? = null
    var description: String? = null
    var dueAt: Instant? = null

    fun withName(name: String?) = apply { this.name = name }
    fun withCode(code: String?) = apply { this.code = code }
    fun withDescription(description: String?) = apply { this.description = description }
    fun withDueAt(dueAt: Instant?) = apply { this.dueAt = dueAt }

    fun build(): FeatureFlagUpdate = FeatureFlagUpdate(name, code, description, dueAt)
}

fun FeatureFlag.updateWith(builder: FeatureFlagUpdateBuilder.() -> Unit) {
    val update = FeatureFlagUpdateBuilder().apply(builder).build()
    this.updateDetails(update.name, update.code, update.description, update.dueAt)
}