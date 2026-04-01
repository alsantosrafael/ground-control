# Spec - Reliable gRPC Analytics Ingestion

Ensure that gRPC feature flag evaluations trigger analytics ingestion reliably, following best practices to avoid dual-write problems and maintaining strict modularity.

## Requirements

- **API-GRPC-RELIABLE-1**: Every gRPC `Evaluate` call in the `toggles` module must result in a `ToggleEvaluatedEvent` being published.
- **API-GRPC-RELIABLE-2**: The `analytics` module must consume this event and store it in the database.
- **API-GRPC-RELIABLE-3**: Use **Transactional Outbox** (Spring Modulith Event Publication Registry) to ensure that if the evaluation happens in a transaction, the event is also stored reliably.
- **API-GRPC-RELIABLE-4**: Remove the redundant gRPC analytics ingestion service to keep a single point of entry for analytics (Events + REST).

## Success Criteria

- `ToggleService.evaluate` is marked as `@Transactional`.
- `AnalyticsGrpcService` and `analytics.proto` are removed.
- Integration test confirms that a gRPC evaluation triggers a record in the `analytics_events` table.
- `EVENT_PUBLICATION` table is verified to be used during the process.
