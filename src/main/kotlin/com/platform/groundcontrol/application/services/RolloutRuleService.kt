package com.platform.groundcontrol.application.services

import com.platform.groundcontrol.domain.mappers.RolloutRuleMapper.toDomain
import com.platform.groundcontrol.domain.mappers.RolloutRuleMapper.toEntity
import com.platform.groundcontrol.domain.valueobjects.*
import com.platform.groundcontrol.infrastructure.repositories.FeatureFlagRepository
import com.platform.groundcontrol.infrastructure.repositories.RolloutRuleRepository
import org.slf4j.LoggerFactory
import org.springframework.cache.annotation.CacheEvict
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.UUID

@Service
class RolloutRuleService(
    val rolloutRuleRepository: RolloutRuleRepository,
    val featureFlagRepository: FeatureFlagRepository
) {
    companion object {
        private val logger = LoggerFactory.getLogger(RolloutRuleService::class.java)
    }

    @Transactional
    @CacheEvict(value = ["featureFlagByCode"], key = "#flagCode")
    fun create(flagCode: String, request: CreateRolloutRule): RolloutRule {
        val flag = featureFlagRepository.findByCode(flagCode)
            ?: throw NoSuchElementException("Feature flag with code '$flagCode' not found")

        val rule = RolloutRule(
            id = null,
            featureFlagId = flag.id,
            attributeKey = request.attributeKey,
            attributeValue = request.attributeValue,
            percentage = request.percentage,
            distributionKeyAttribute = request.distributionKeyAttribute,
            valueBool = request.valueBool,
            valueString = request.valueString,
            valueInt = request.valueInt,
            valuePercentage = request.valuePercentage,
            variantName = request.variantName,
            startAt = request.startAt,
            endAt = request.endAt,
            priority = request.priority,
            active = request.active,
            conditions = request.conditions.toMutableList()
        )

        val entity = rule.toEntity()
        entity.featureFlag = flag

        val savedEntity = rolloutRuleRepository.save(entity)
        return savedEntity.toDomain()
    }

    @Transactional
    @CacheEvict(value = ["featureFlagByCode"], allEntries = true)
    fun update(ruleId: UUID, request: UpdateRolloutRule): RolloutRule {
        val existingEntity = rolloutRuleRepository.findById(ruleId)
            .orElseThrow { NoSuchElementException("Rollout rule with id '$ruleId' not found") }

        val rule = existingEntity.toDomain()

        request.percentage?.let { rule.percentage = it }
        request.priority?.let { rule.priority = it }
        request.active?.let { rule.active = it }
        request.attributeKey?.let { rule.attributeKey = it }
        request.attributeValue?.let { rule.attributeValue = it }
        request.distributionKeyAttribute?.let { rule.distributionKeyAttribute = it }
        request.valueBool?.let { rule.valueBool = it }
        request.valueString?.let { rule.valueString = it }
        request.valueInt?.let { rule.valueInt = it }
        request.valuePercentage?.let { rule.valuePercentage = it }
        request.variantName?.let { rule.variantName = it }
        request.startAt?.let { rule.startAt = it }
        request.endAt?.let { rule.endAt = it }
        request.conditions?.let {
            rule.conditions.clear()
            rule.conditions.addAll(it)
        }

        val updatedEntity = rule.toEntity()
        updatedEntity.featureFlag = existingEntity.featureFlag
        updatedEntity.id = ruleId

        val savedEntity = rolloutRuleRepository.save(updatedEntity)
        return savedEntity.toDomain()
    }

    @Transactional
    @CacheEvict(value = ["featureFlagByCode"], allEntries = true)
    fun delete(ruleId: UUID) {
        if (!rolloutRuleRepository.existsById(ruleId)) {
            throw NoSuchElementException("Rollout rule with id '$ruleId' not found")
        }

        rolloutRuleRepository.deleteById(ruleId)
    }

    fun getByFlag(flagCode: String): List<RolloutRule> {
        val flag = featureFlagRepository.findByCode(flagCode)
            ?: throw NoSuchElementException("Feature flag with code '$flagCode' not found")

        return rolloutRuleRepository.findByFeatureFlagIdOrderByPriorityAsc(flag.id!!)
            .map { it.toDomain() }
    }

    fun getById(ruleId: UUID): RolloutRule {
        return rolloutRuleRepository.findById(ruleId)
            .orElseThrow { NoSuchElementException("Rollout rule with id '$ruleId' not found") }
            .toDomain()
    }

    @Transactional
    @CacheEvict(value = ["featureFlagByCode"], allEntries = true)
    fun reorder(flagCode: String, request: ReorderRolloutRules) {
        val flag = featureFlagRepository.findByCode(flagCode)
            ?: throw NoSuchElementException("Feature flag with code '$flagCode' not found")

        val rules = rolloutRuleRepository.findByFeatureFlagIdOrderByPriorityAsc(flag.id!!)

        if (request.ruleIds.size != rules.size) {
            throw IllegalArgumentException(
                "Must provide all rule IDs for reordering. Expected ${rules.size} rule IDs, but got ${request.ruleIds.size}"
            )
        }

        val ruleIdSet = rules.map { it.id }.toSet()
        val invalidIds = request.ruleIds.filterNot { it in ruleIdSet }
        if (invalidIds.isNotEmpty()) {
            throw IllegalArgumentException("Invalid rule IDs: $invalidIds")
        }

        request.ruleIds.forEachIndexed { index, ruleId ->
            val rule = rules.first { it.id == ruleId }
            rule.priority = index
        }

        rolloutRuleRepository.saveAll(rules)
    }
}
