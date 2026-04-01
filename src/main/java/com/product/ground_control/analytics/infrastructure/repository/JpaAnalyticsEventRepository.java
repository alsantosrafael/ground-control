package com.product.ground_control.analytics.infrastructure.repository;

import com.product.ground_control.analytics.domain.AnalyticsEventRepository;
import com.product.ground_control.analytics.domain.entity.AnalyticsEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

/**
 * Infrastructure implementation of AnalyticsEventRepository.
 * Handles database access via JpaRepository.
 */
@Repository
@RequiredArgsConstructor
public class JpaAnalyticsEventRepository implements AnalyticsEventRepository {

    private final EventRepository repository;

    @Override
    public void save(AnalyticsEvent event) {
        repository.save(event);
    }
}
