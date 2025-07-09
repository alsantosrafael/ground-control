package com.platform.groundcontrol.application.services

import com.platform.groundcontrol.domain.mappers.FeatureFlagMapper.toDomain
import com.platform.groundcontrol.domain.mappers.FeatureFlagMapper.toEntity
import com.platform.groundcontrol.domain.valueobjects.CreateFeatureFlag
import com.platform.groundcontrol.domain.valueobjects.FeatureFlag
import com.platform.groundcontrol.domain.valueobjects.FeatureFlagCode
import com.platform.groundcontrol.domain.valueobjects.FeatureFlagId
import com.platform.groundcontrol.domain.valueobjects.FeatureFlagName
import com.platform.groundcontrol.infrastructure.repositories.FeatureFlagJpaRepository
import org.springframework.stereotype.Service
import java.util.UUID

@Service
class FeatureFlagService(
    val featureFlagRepository: FeatureFlagJpaRepository
) {
    fun create(request: CreateFeatureFlag): FeatureFlag {
        val ff = FeatureFlag(
            FeatureFlagId(null as UUID?),
            FeatureFlagCode(request.code),
            FeatureFlagName(request.name),
            request.description,
            request.isEnabled,
            request.dueAt
        )
        val createdFF = featureFlagRepository.save(ff.toEntity())
        return createdFF.toDomain()
    }

    fun fetchAll(): List<FeatureFlag> {
        val list = featureFlagRepository.findAll()
        return list.map { it.toDomain() }
    }
}