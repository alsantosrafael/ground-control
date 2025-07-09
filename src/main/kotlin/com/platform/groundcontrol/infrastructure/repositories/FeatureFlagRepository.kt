package com.platform.groundcontrol.infrastructure.repositories

import com.platform.groundcontrol.domain.valueobjects.FeatureFlag
import com.platform.groundcontrol.domain.valueobjects.FeatureFlagCode
import com.platform.groundcontrol.domain.valueobjects.FeatureFlagId

interface FeatureFlagRepository {
    fun save(featureFlag: FeatureFlag): FeatureFlag
    fun findById(id: FeatureFlagId): FeatureFlag?
    fun findByCode(code: FeatureFlagCode): FeatureFlag?
    fun findAll(): List<FeatureFlag>
    fun delete(featureFlag: FeatureFlag)
}