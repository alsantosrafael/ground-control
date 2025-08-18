package com.platform.groundcontrol.application.services

import com.platform.groundcontrol.domain.mappers.FeatureFlagMapper.toDomain
import com.platform.groundcontrol.domain.mappers.FeatureFlagMapper.toEntity
import com.platform.groundcontrol.domain.valueobjects.CreateFeatureFlag
import com.platform.groundcontrol.domain.valueobjects.FeatureFlag
import com.platform.groundcontrol.domain.valueobjects.FindByCodes
import com.platform.groundcontrol.domain.valueobjects.UpdateFeatureFlag
import com.platform.groundcontrol.domain.valueobjects.UpdateFeatureFlagState
import com.platform.groundcontrol.domain.valueobjects.updateWith
import com.platform.groundcontrol.infrastructure.repositories.FeatureFlagRepository
import org.slf4j.LoggerFactory
import org.slf4j.MDC
import org.springframework.cache.annotation.CacheEvict
import org.springframework.cache.annotation.Cacheable
import org.springframework.stereotype.Service

@Service
class FeatureFlagService(
    val featureFlagRepository: FeatureFlagRepository
) {
    
    companion object {
        private val logger = LoggerFactory.getLogger(FeatureFlagService::class.java)
    }
    @CacheEvict(value = ["featureFlags"], allEntries = true)
    fun create(request: CreateFeatureFlag): FeatureFlag {
        val startTime = System.currentTimeMillis()
        val flagCode = request.code
        
        MDC.put("flagCode", flagCode)
        MDC.put("operation", "create")
        
        try {
            logger.info("Creating feature flag: code={}, name={}, valueType={}, enabled={}", 
                flagCode, request.name, request.valueType, request.enabled)
            
            val ff = FeatureFlag(
                id = null,
                code = request.code,
                name = request.name,
                description = request.description,
                value = request.value,
                valueType = request.valueType,
                enabled = request.enabled,
                dueAt = request.dueAt
            )
            
            val createdFF = featureFlagRepository.save(ff.toEntity())
            val result = createdFF.toDomain()
            val duration = System.currentTimeMillis() - startTime
            
            logger.info("Feature flag created: code={}, id={}, durationMs={}", 
                flagCode, result.id, duration)
            
            return result
            
        } catch (e: Exception) {
            val duration = System.currentTimeMillis() - startTime
            logger.error("Feature flag creation failed: code={}, error={}, durationMs={}", 
                flagCode, e.message, duration, e)
            throw e
        } finally {
            MDC.clear()
        }
    }

    @Cacheable("featureFlags")
    fun getAll(): List<FeatureFlag> {
        val startTime = System.currentTimeMillis()
        
        MDC.put("operation", "getAll")
        
        try {
            logger.debug("Retrieving all feature flags")
            
            val list = featureFlagRepository.findAllWithRules()
            val result = list.map { it.toDomain() }
            val duration = System.currentTimeMillis() - startTime
            
            logger.info("Retrieved all feature flags: count={}, durationMs={}", result.size, duration)
            return result
            
        } catch (e: Exception) {
            val duration = System.currentTimeMillis() - startTime
            logger.error("Failed to retrieve all feature flags: error={}, durationMs={}", 
                e.message, duration, e)
            throw e
        } finally {
            MDC.clear()
        }
    }

    fun getAllByCodes(codes: List<String>): FindByCodes {
        val featureFlags =  featureFlagRepository.findByCodeInWithRules(codes);
        val foundCodes = featureFlags.map { it.code }
        val notFoundCodes = codes.filterNot { it in foundCodes }
        return FindByCodes(featureFlags.map { it.toDomain() }, notFoundCodes)
    }

    @Cacheable("featureFlagByCode", key = "#code")
    fun getByCode(code: String): FeatureFlag {
        val startTime = System.currentTimeMillis()
        
        MDC.put("flagCode", code)
        MDC.put("operation", "getByCode")
        
        try {
            logger.debug("Retrieving feature flag by code: code={}", code)
            
            val flag = featureFlagRepository.findByCodeWithRules(code)
                ?: throw NoSuchElementException("Feature flag with code '$code' not found").also {
                    logger.warn("Feature flag not found: code={}", code)
                }
            
            val result = flag.toDomain()
            val duration = System.currentTimeMillis() - startTime
            
            logger.debug("Retrieved feature flag: code={}, id={}, enabled={}, rulesCount={}, durationMs={}", 
                code, result.id, result.enabled, result.rolloutRules.size, duration)
            
            return result
            
        } catch (e: Exception) {
            val duration = System.currentTimeMillis() - startTime
            logger.error("Failed to retrieve feature flag: code={}, error={}, durationMs={}", 
                code, e.message, duration, e)
            throw e
        } finally {
            MDC.clear()
        }
    }

    @CacheEvict(value = ["featureFlags", "featureFlagByCode"], allEntries = true)
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

    @CacheEvict(value = ["featureFlags", "featureFlagByCode"], allEntries = true)
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