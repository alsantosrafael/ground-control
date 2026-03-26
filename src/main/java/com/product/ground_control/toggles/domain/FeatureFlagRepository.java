package com.product.ground_control.toggles.domain;

import com.product.ground_control.toggles.domain.entity.Toggle;
import java.util.Optional;

/**
 * Domain Repository interface for Toggle domain entities.
 * Decouples the domain from infrastructure persistence and caching details.
 */
public interface FeatureFlagRepository {
    Optional<Toggle> findByKey(String key);
}
