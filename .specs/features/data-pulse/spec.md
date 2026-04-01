# Specification: The Data Pulse (Milestone 3)

## 1. Goal
Connect the engine to real-world outcomes through non-blocking event ingestion.

## 2. Requirements

- **DP-01**: Ingest analytics events via REST `POST /api/v1/events`.
- **DP-02**: Store events in the `analytics_events` table in PostgreSQL.
- **DP-03**: Ingestion must be asynchronous and should not block the evaluation path or the API caller for more than 10ms.
- **DP-04**: Use Java 21 Virtual Threads for non-blocking persistence.
- **DP-05**: Support an `ImpactScore` calculated from event metadata (Targeting for V2, but schema should support basic metadata).

## 3. Data Model

### AnalyticsEvent
- `id` (UUID)
- `featureKey` (String) - Reference to the flag.
- `variation` (String) - Result value received.
- `subject` (String) - User/Entity identifier.
- `timestamp` (Instant)
- `metadata` (JSONB) - Arbitrary context.

## 4. Verification Criteria
- [ ] `POST /api/v1/events` returns `202 Accepted` within 10ms.
- [ ] Event record appears in `analytics_events` table asynchronously.
- [ ] No circular dependencies between `toggles` and `analytics` modules.
- [ ] Performance bench showing zero impact on `ToggleService.evaluate()` latency when internal events are published.
