# Spec - gRPC Analytics Ingestion

Implement gRPC ingestion for the `analytics` module to allow external clients to send analytics events using high-performance gRPC calls.

## Requirements

- **API-GRPC-1**: The system must expose a gRPC service `AnalyticsService` with an `Ingest` RPC.
- **API-GRPC-2**: The `Ingest` RPC must accept the following fields:
    - `featureKey`: String (Value Object `FeatureKey`)
    - `variation`: String (Value Object `EvaluationVariation`)
    - `subjectId`: String (Value Object `Subject`)
    - `metadata`: map<string, string> (Value Object `Metadata`)
- **API-GRPC-3**: The system must use the existing `EventIngestionService` to process the events.
- **API-GRPC-4**: The implementation must avoid "dual-write" problems by ensuring the ingestion is transactional and potentially leveraging the Transactional Outbox pattern if events are propagated further. (Note: Currently uses Spring Modulith's built-in outbox).
- **API-GRPC-5**: The gRPC response should indicate success or failure based on the outcome of the transactional ingestion.

## Success Criteria

- Proto file `analytics.proto` successfully generates Java classes.
- `AnalyticsGrpcService` implemented and registered as a Spring bean.
- Integration test verifies that a gRPC call results in a correctly stored `AnalyticsEvent` in the database.
- No boundary violations (verified by `ModularityTests`).
