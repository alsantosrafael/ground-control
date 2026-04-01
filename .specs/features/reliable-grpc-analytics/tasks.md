# Tasks - Reliable gRPC Analytics Ingestion

Granular tasks for ensuring reliable analytics ingestion from gRPC and avoiding dual-write problems.

## Phase 1: Cleanup Redundant Components
- [ ] T1: Delete `src/main/proto/analytics.proto` <!-- id: 1 -->
- [ ] T2: Delete `src/main/java/com/product/ground_control/analytics/infrastructure/api/grpc/AnalyticsGrpcService.java` <!-- id: 2 -->
- [ ] T3: Clean up generated gRPC classes via `./gradlew clean` (or just ignore them) <!-- id: 3 -->

## Phase 2: Transactional Integrity
- [ ] T4: Apply `@Transactional` to `ToggleService.evaluate` in `toggles` module <!-- id: 4 -->
- [ ] T5: Verify `ToggleEvaluatedEventListener` is using `@ApplicationModuleListener` in `analytics` module <!-- id: 5 -->

## Phase 3: Verification
- [ ] T6: Run `EvaluationIntegrationTest` (gRPC evaluation test) <!-- id: 6 -->
- [ ] T7: Add verification step to check `analytics_events` table after gRPC call in `EvaluationIntegrationTest` <!-- id: 7 -->
- [ ] T8: Full build and modularity check via `./gradlew check` <!-- id: 8 -->
