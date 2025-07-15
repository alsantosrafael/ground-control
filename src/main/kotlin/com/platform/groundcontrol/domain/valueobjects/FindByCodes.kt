package com.platform.groundcontrol.domain.valueobjects

data class FindByCodes(
    val featureFlags: List<FeatureFlag>,
    val notFoundCodes: List<String>
)