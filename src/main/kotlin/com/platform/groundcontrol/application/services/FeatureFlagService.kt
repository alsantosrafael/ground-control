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
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class FeatureFlagService(
    val featureFlagRepository: FeatureFlagRepository
) {
    
    companion object {
        private val logger = LoggerFactory.getLogger(FeatureFlagService::class.java)
    }
    @Transactional
    @CacheEvict(value = ["featureFlagByCode"], key = "#request.code")
    fun create(request: CreateFeatureFlag): FeatureFlag {
        val startTime = System.currentTimeMillis()
        val flagCode = request.code

        MDC.put("flagCode", flagCode)
        MDC.put("operation", "create")

        try {
            logger.info("Creating feature flag: code={}, name={}, valueType={}, enabled={}",
                flagCode, request.name, request.valueType, request.enabled)

            // Proactive check: verify code doesn't already exist
            if (featureFlagRepository.existsByCode(flagCode)) {
                throw IllegalStateException("A feature flag with code '$flagCode' already exists. Please use a different code.")
            }

            // Proactive check: verify name doesn't already exist
            if (featureFlagRepository.existsByName(request.name)) {
                throw IllegalStateException("A feature flag with name '${request.name}' already exists. Please use a different name.")
            }

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

    fun getAll(pageable: Pageable): Page<FeatureFlag> {
        val startTime = System.currentTimeMillis()
        
        MDC.put("operation", "getAllPaginated")
        MDC.put("page", pageable.pageNumber.toString())
        MDC.put("size", pageable.pageSize.toString())
        
        try {
            logger.debug("Retrieving paginated feature flags: page={}, size={}", 
                pageable.pageNumber, pageable.pageSize)
            
            val page = featureFlagRepository.findAll(pageable)
            val result = page.map { it.toDomain() }
            val duration = System.currentTimeMillis() - startTime
            
            logger.info("Retrieved paginated feature flags: page={}, size={}, totalElements={}, totalPages={}, durationMs={}", 
                pageable.pageNumber, pageable.pageSize, result.totalElements, result.totalPages, duration)
            
            return result
            
        } catch (e: Exception) {
            val duration = System.currentTimeMillis() - startTime
            logger.error("Failed to retrieve paginated feature flags: page={}, size={}, error={}, durationMs={}", 
                pageable.pageNumber, pageable.pageSize, e.message, duration, e)
            throw e
        } finally {
            MDC.clear()
        }
    }

    fun getAllByCodes(codes: List<String>): FindByCodes {
        val featureFlags = featureFlagRepository.findByCodeInWithRules(codes)
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

    @Transactional
    @CacheEvict(value = ["featureFlagByCode"], key = "#code")
    fun update(code: String, request: UpdateFeatureFlag) {
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

    @Transactional
    @CacheEvict(value = ["featureFlagByCode"], key = "#code")
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