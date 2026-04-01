-- Milestone 3: The Data Pulse (Persistence)

-- Analytics Events table
-- Stores toggle evaluation events for the Data Pulse
CREATE TABLE IF NOT EXISTS analytics_events (
    id UUID PRIMARY KEY,
    feature_key VARCHAR(255) NOT NULL,
    variation VARCHAR(255) NOT NULL,
    subject VARCHAR(255) NOT NULL,
    timestamp TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    metadata JSONB NOT NULL DEFAULT '{}',

    -- Idempotency: Event ID from ToggleEvaluatedEvent
    -- Ensures the same event instance (same evaluation) is only processed once
    -- Critical for replay scenarios: event ID is preserved across retries and replays
    idempotency_key VARCHAR(36) NOT NULL,

    CONSTRAINT uq_analytics_idempotency UNIQUE (idempotency_key)
);

CREATE INDEX idx_analytics_feature_key ON analytics_events(feature_key);
CREATE INDEX idx_analytics_timestamp ON analytics_events(timestamp);

COMMENT ON COLUMN analytics_events.idempotency_key IS 'Event ID from ToggleEvaluatedEvent - ensures idempotent processing across retries and replays';

-- Spring Modulith Event Publication Registry table (PostgreSQL)
-- Outbox pattern for reliable event delivery with retry support
CREATE TABLE IF NOT EXISTS EVENT_PUBLICATION (
  ID                      UUID PRIMARY KEY,
  PUBLICATION_DATE        TIMESTAMPTZ NOT NULL,
  EVENT_TYPE              VARCHAR(512) NOT NULL,
  LISTENER_ID             VARCHAR(512) NOT NULL,
  SERIALIZED_EVENT        TEXT NOT NULL,
  COMPLETION_DATE         TIMESTAMPTZ,
  STATUS                  VARCHAR(50) DEFAULT 'INCOMPLETE',
  COMPLETION_ATTEMPTS     INTEGER DEFAULT 0,
  LAST_RESUBMISSION_DATE  TIMESTAMPTZ
);

-- Index for finding incomplete events (retry queries)
-- Critical for performance: Spring Modulith queries by status to find events needing retry
CREATE INDEX idx_event_publication_status ON EVENT_PUBLICATION(STATUS) WHERE COMPLETION_DATE IS NULL;

-- Composite index for idempotency: prevents duplicate processing of same event by same listener
-- Ensures: "This listener has already processed this type of event with this status"
-- Used for retry logic to check if an event is already being processed or completed
CREATE INDEX idx_event_publication_idempotency ON EVENT_PUBLICATION(LISTENER_ID, EVENT_TYPE, STATUS);

-- Index for cleanup queries (purging old completed events)
CREATE INDEX idx_event_publication_completion ON EVENT_PUBLICATION(COMPLETION_DATE) WHERE COMPLETION_DATE IS NOT NULL;
