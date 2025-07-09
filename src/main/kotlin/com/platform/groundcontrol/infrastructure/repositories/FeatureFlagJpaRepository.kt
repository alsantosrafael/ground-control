package com.platform.groundcontrol.infrastructure.repositories

import com.platform.groundcontrol.domain.entities.FeatureFlagEntity
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import java.util.UUID

@Repository
interface FeatureFlagJpaRepository : JpaRepository<FeatureFlagEntity, UUID> {
    fun findByCode(code: String): FeatureFlagEntity?
}