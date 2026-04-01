package com.product.ground_control.analytics.domain;

import com.product.ground_control.analytics.domain.entity.AnalyticsEvent;

/**
 * Domain port for AnalyticsEvent persistence.
 * This interface is agnostic of the underlying persistence implementation.
 */
public interface AnalyticsEventRepository {
    /**
     * Persists an analytics event.
     * @param event The entity to save.
     */
    void save(AnalyticsEvent event);
}
