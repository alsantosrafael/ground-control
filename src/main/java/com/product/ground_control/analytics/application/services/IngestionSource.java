package com.product.ground_control.analytics.application.services;

/**
 * Defines the source of an analytics event ingestion.
 */
public enum IngestionSource {
    /**
     * Event-driven ingestion via Spring Modulith @ApplicationModuleListener.
     * Uses event ID as idempotency key for replay protection.
     */
    EVENT_DRIVEN,

    /**
     * Synchronous REST API ingestion.
     * Uses client-provided idempotency key for retry protection.
     */
    REST_API
}
