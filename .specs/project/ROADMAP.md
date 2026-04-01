# Project Roadmap: Ground Control

## Milestones

### 🎯 Milestone 1: The Engine (The "Heart")
**Status**: ✅ Complete
- [x] **Setup**: Project structure with `toggles`, `analytics`, and `management` modules.
- [x] **Core Logic**: Implementation of the `FeatureFlag` aggregate and the `Evaluator` service.
- [x] **Feature**: Boolean, String, and Percentage value support.
- [x] **Feature**: Two-stage evaluation (Targeting + Distribution).
- [x] **Checkpoint**: A unit test suite verifying a complex 3-rule cascade with stable 10% rollout results.

### 🎯 Milestone 2: High-Performance Edge (The "Speed")
**Status**: ✅ Complete (Verified via file existence and `STATE.md`)
- [x] **Caffeine**: Integration of the in-memory cache for active flags.
- [x] **gRPC**: Protobuf definitions and the `EvaluationService` implementation.
- [x] **REST Eval**: Implementation of the `/api/v1/flags/eval` endpoint for universal access.
- [x] **Checkpoint**: A benchmark showing < 5ms latency for flag evaluation via both REST and gRPC.

### 🎯 Milestone 3: The Data Pulse (The "Gold")
**Status**: ✅ Complete
- [x] **Persistence**: Analytics schema and event storage logic.
- [x] **Async Flow**: Spring Modulith outbox pattern — `@ApplicationModuleListener` runs post-commit in its own transaction. Analytics ingestion is fully async and does not block the evaluation response.
- [x] **API**: Implementation of the `/api/v1/events` endpoint with idempotency via `X-Idempotency-Key`.
- [x] **Checkpoint**: Evaluation results can be correlated with ingested events in the database. The evaluation *response* is not delayed by analytics processing (async post-commit via outbox). Note: each evaluation writes one outbox entry synchronously within the transaction; see AD-006 for the performance tradeoff and Milestone 5 benchmarking plan.

### 🎯 Milestone 4: The Control Plane (The "UI-Ready")
**Status**: ⏳ Planned
- [ ] **Management API**: Full CRUD for `FeatureFlag` with validation.
- [ ] **Context Mapping**: Definition of standard "Context" attributes (Region, VIP, Tier).
- [ ] **Audit Log**: Basic persistence of who changed what and why.
- [ ] **Checkpoint**: An Agent can successfully create, update, and then evaluate a flag via the API.

### 🎯 Milestone 5: Production Ready (The "Native")
**Status**: ⏳ Planned
- [ ] **GraalVM**: Configuration and successful build of the Native Image.
- [ ] **Observability**: Integration of Micrometer/Prometheus metrics for latency and cache hits.
- [ ] **Load Test**: Stress testing the "Nerve Center" under 1000+ RPS.
- [ ] **Checkpoint**: Successful execution of the full suite within a GraalVM native container with p99 < 5ms.
