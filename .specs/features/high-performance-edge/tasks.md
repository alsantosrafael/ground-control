# Tasks: High-Performance Edge (Milestone 2)

**Design**: `.specs/features/high-performance-edge/design.md`
**Status**: Draft

---

## Execution Plan

### Phase 1: In-Memory Caching (Sequential)
- **T1**: Caffeine cache configuration and `CacheManager` setup.
- **T2**: `CachedToggleService` implementation (Cache-aside strategy).
- **T3**: Cache invalidation unit tests.

### Phase 2: Dual-Protocol Adapters (Parallel OK)
- **T4 [P]**: Protobuf definition (`evaluation.proto`) and code generation.
- **T5 [P]**: gRPC `EvaluationService` implementation.
- **T6 [P]**: REST `EvaluationController` implementation.

### Phase 3: Validation (Sequential)
- **T7**: Integration test for gRPC/REST evaluation with caching.
- **T8**: Baseline latency benchmark (REST vs gRPC).

---

## Task Breakdown

### T1: Caffeine Cache Configuration
- **What**: Configure `CaffeineCacheManager` in the `toggles` module.
- **Where**: `com.product.ground_control.toggles.infrastructure.CacheConfig`.
- **Requirements**: `EDGE-01`.
- **Done when**: Cache bean is available in the application context.

### T2: CachedToggleService Implementation
- **What**: Create a service that combines `ToggleEvaluator` with the Caffeine cache.
- **Where**: `com.product.ground_control.toggles.application.CachedToggleService`.
- **Requirements**: `EDGE-01`.
- **Done when**: Evaluations return from cache on subsequent calls.

### T3: Cache Invalidation Tests
- **What**: Verify that cache entries expire or can be invalidated.
- **Requirements**: `EDGE-02`.
- **Done when**: Unit test confirms cache miss after expiration.

### T4: Protobuf Definition
- **What**: Create `evaluation.proto` and trigger `./gradlew generateProto`.
- **Where**: `src/main/proto/evaluation.proto`.
- **Requirements**: `EDGE-03`.
- **Done when**: Protobuf Java classes are generated.

### T5: gRPC Evaluation Service
- **What**: Implement the gRPC server using the generated stubs.
- **Where**: `com.product.ground_control.toggles.api.grpc.ToggleEvaluationGrpcService`.
- **Requirements**: `EDGE-04`.
- **Done when**: gRPC client can successfully call the `Evaluate` method.

### T6: REST Evaluation Controller
- **What**: Implement the REST endpoint for rule evaluation.
- **Where**: `com.product.ground_control.toggles.api.rest.EvaluationController`.
- **Requirements**: `EDGE-05`.
- **Done when**: `POST /api/v1/rules/eval` returns the correct evaluation result.

### T7: Integration Verification
- **What**: Full integration test verifying the cache + gRPC + REST path.
- **Requirements**: `EDGE-01, EDGE-03, EDGE-04, EDGE-05`.
- **Done when**: End-to-end evaluation works across both protocols.
