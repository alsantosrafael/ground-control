package com.product.ground_control.toggles.infrastructure.repository;

import com.product.ground_control.toggles.domain.FeatureFlagRepository;
import java.util.Optional;

import com.product.ground_control.toggles.application.configuration.CacheConfig;
import com.product.ground_control.toggles.domain.entity.Toggle;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Repository;

/**
 * Infrastructure implementation of the FeatureFlagRepository.
 * Handles database access via JpaRepository and in-memory caching via Caffeine.
 */
@Repository
@RequiredArgsConstructor
public class JpaFeatureFlagRepository implements FeatureFlagRepository {

    private final ToggleRepository toggleRepository;

    @Override
    @Cacheable(value = CacheConfig.FEATURE_RULES_CACHE, key = "#key", unless = "#result == null")
    public Optional<Toggle> findByKey(String key) {
        return toggleRepository.findByKey(key);
    }
}
