package com.product.ground_control.analytics.infrastructure.repository;

import java.util.UUID;

import com.product.ground_control.analytics.domain.entity.AnalyticsEvent;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Spring Data JPA repository for AnalyticsEvent.
 * Kept private to the infrastructure layer.
 */
@Repository
public interface EventRepository extends JpaRepository<AnalyticsEvent, UUID> {
}
