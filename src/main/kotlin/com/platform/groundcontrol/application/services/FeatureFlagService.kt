package com.platform.groundcontrol.application.services

import com.platform.groundcontrol.domain.mappers.FeatureFlagMapper.toDomain
import com.platform.groundcontrol.domain.mappers.FeatureFlagMapper.toEntity
import com.platform.groundcontrol.domain.valueobjects.CreateFeatureFlag
import com.platform.groundcontrol.domain.valueobjects.FeatureFlag
import com.platform.groundcontrol.domain.valueobjects.FeatureFlagCode
import com.platform.groundcontrol.domain.valueobjects.FeatureFlagId
import com.platform.groundcontrol.domain.valueobjects.FeatureFlagName
import com.platform.groundcontrol.domain.valueobjects.FindByCodes
import com.platform.groundcontrol.domain.valueobjects.UpdateFeatureFlag
import com.platform.groundcontrol.domain.valueobjects.UpdateFeatureFlagState
import com.platform.groundcontrol.domain.valueobjects.updateWith
import com.platform.groundcontrol.infrastructure.repositories.FeatureFlagJpaRepository
import org.springframework.stereotype.Service

@Service
class FeatureFlagService(
    val featureFlagRepository: FeatureFlagJpaRepository
) {
    fun create(request: CreateFeatureFlag): FeatureFlag {
        val ff = FeatureFlag(
            FeatureFlagId(null as Long?),
            FeatureFlagCode(request.code),
            FeatureFlagName(request.name),
            request.description,
            request.value,
            request.valueType,
            request.enabled,
            request.dueAt
        )
        val createdFF = featureFlagRepository.save(ff.toEntity())
        return createdFF.toDomain()
    }

    fun getAll(): List<FeatureFlag> {
        val list = featureFlagRepository.findAll()
        return list.map { it.toDomain() }
    }

    fun getAllByCodes(codes: List<String>): FindByCodes {
        val featureFlags =  featureFlagRepository.findByCodeIn(codes);
        val foundCodes = featureFlags.map { it.code }
        val notFoundCodes = codes.filterNot { it in foundCodes }
        return FindByCodes(featureFlags.map { it.toDomain() }, notFoundCodes)
    }

    fun getByCode(code: String): FeatureFlag {
        val flag = featureFlagRepository.findByCode(code)
            ?: throw NoSuchElementException("Feature flag with code '$code' not found")
        return flag.toDomain()
    }

    fun update(code: String, request: UpdateFeatureFlag): Unit {
        val flag = featureFlagRepository.findByCode(code)?.toDomain()
            ?: throw NoSuchElementException("Feature flag with code '$code' not found")
        flag.updateWith{
            withName(request.name)
            withCode(request.code)
            withDescription(request.description)
            withDueAt(request.dueAt)
        }
        featureFlagRepository.save(flag.toEntity())
    }

    fun updateFeatureFlagStatus(code: String, request: UpdateFeatureFlagState) {
        val flag = featureFlagRepository.findByCode(code)?.toDomain()
            ?: throw NoSuchElementException("Feature flag with code '$code' not found")

        if (request.isEnabled) {
            flag.enable()
        } else {
            flag.disable()
        }

        featureFlagRepository.save(flag.toEntity())
    }

}