# Design: The Data Pulse (Milestone 3)

## Architecture

We will use **Spring Modulith** for architectural boundaries and **Spring ApplicationEvents** for async communication between the `toggles` and `analytics` modules. To ensure zero impact on evaluation latency, we will leverage **Java 21 Virtual Threads** via a custom `Executor`.

### Components

1.  **Analytics Module** (`analytics`):
    - **Structure**: `api/` (Contracts), `application/` (Services), `domain/` (Entities), `infrastructure/` (Adapters).
    - **Entity**: `AnalyticsEvent` (JPA Entity).
    - **Repository**: `AnalyticsEventRepository` (Module-Private).
    - **Service**: `EventIngestionService` (Transactional).
    - **API**: `AnalyticsController` (`POST /api/v1/events`).
2.  **Integration Layer**:
    - `ToggleService` (in `toggles`) publishes `ToggleEvaluatedEvent` (defined in `toggles.api`).
    - **Reliability**: Uses `Spring Modulith Event Publication Registry`.

### Database Schema

```sql
CREATE TABLE analytics_events (
    id UUID PRIMARY KEY,
    feature_key VARCHAR(255) NOT NULL,
    variation VARCHAR(255) NOT NULL,
    subject VARCHAR(255) NOT NULL,
    timestamp TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP,
    metadata JSONB
);

-- For Spring Modulith Event Publication Registry
-- (Will be managed by Spring Modulith default schema or manual DDL)
```

### Async Strategy

We will use `@ApplicationModuleListener` in the `analytics` module. This ensures the event is handled asynchronously and reliably.
