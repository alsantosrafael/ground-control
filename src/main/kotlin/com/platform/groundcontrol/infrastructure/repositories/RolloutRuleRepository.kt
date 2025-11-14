package com.platform.groundcontrol.infrastructure.repositories

import com.platform.groundcontrol.domain.entities.RolloutRuleEntity
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository
import java.util.UUID

@Repository
interface RolloutRuleRepository : JpaRepository<RolloutRuleEntity, UUID> {
    fun findByFeatureFlagId(flagId: Long): List<RolloutRuleEntity>
    fun findByFeatureFlagIdOrderByPriorityAsc(flagId: Long): List<RolloutRuleEntity>
    fun findByFeatureFlagIdAndActive(flagId: Long, active: Boolean): List<RolloutRuleEntity>
    fun existsByIdAndFeatureFlagId(id: UUID, flagId: Long): Boolean
    fun countByFeatureFlagId(flagId: Long): Long
}
