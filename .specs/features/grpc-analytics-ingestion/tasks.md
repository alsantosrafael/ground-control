# Tasks - gRPC Analytics Ingestion

Granular tasks for implementing high-performance gRPC ingestion for analytics.

## Phase 1: Communication Contract
- [ ] T1: Create `src/main/proto/analytics.proto` with `AnalyticsService` definition <!-- id: 1 -->
- [ ] T2: Trigger protobuf generation via `./gradlew generateProto` <!-- id: 2 -->
- [ ] T3: Verify generated Java classes in `build/generated/source/proto` <!-- id: 3 -->

## Phase 2: Implementation
- [ ] T4: Implement `AnalyticsGrpcService` in `analytics.infrastructure.api.grpc` <!-- id: 4 -->
    - [ ] Extend `AnalyticsServiceGrpc.AnalyticsServiceImplBase`
    - [ ] Inject `EventIngestionService`
    - [ ] Map `IngestRequest` to domain `ingest` call
    - [ ] Add `@GrpcService` annotation (if using net.devh starter as seen in build.gradle.kts)
- [ ] T5: Ensure proper error handling and transactional consistency <!-- id: 5 -->

## Phase 3: Verification
- [ ] T6: Create `AnalyticsGrpcIntegrationTest` to verify end-to-end ingestion <!-- id: 6 -->
- [ ] T7: Run `ModularityTests` to ensure no boundary violations <!-- id: 7 -->
- [ ] T8: Full build check via `./gradlew build` <!-- id: 8 -->
