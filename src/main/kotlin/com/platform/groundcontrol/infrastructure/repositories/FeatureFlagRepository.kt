package com.platform.groundcontrol.infrastructure.repositories

import com.platform.groundcontrol.domain.entities.FeatureFlagEntity
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository
@Repository
interface FeatureFlagRepository : JpaRepository<FeatureFlagEntity, Long> {
    
    @Query("SELECT f FROM FeatureFlagEntity f LEFT JOIN FETCH f.rolloutRules WHERE f.code = :code")
    fun findByCodeWithRules(@Param("code") code: String): FeatureFlagEntity?
    
    fun findByCode(code: String): FeatureFlagEntity?
    
    @Query("SELECT f FROM FeatureFlagEntity f LEFT JOIN FETCH f.rolloutRules WHERE f.code IN :codes")
    fun findByCodeInWithRules(@Param("codes") codes: List<String>): List<FeatureFlagEntity>
    
    fun findByCodeIn(codes: List<String>): List<FeatureFlagEntity>
    
    
    @Query("SELECT f FROM FeatureFlagEntity f WHERE f.enabled = :enabled ORDER BY f.updatedAt DESC")
    fun findByEnabledOrderByUpdatedAtDesc(@Param("enabled") enabled: Boolean): List<FeatureFlagEntity>

    @Query("SELECT COUNT(f) FROM FeatureFlagEntity f WHERE f.enabled = true")
    fun countEnabledFlags(): Long

    fun existsByCode(code: String): Boolean

    fun existsByName(name: String): Boolean
}