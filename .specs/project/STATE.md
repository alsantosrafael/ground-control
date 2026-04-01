# State

**Last Updated:** 2026-04-01
**Current Work:** Milestone 3 Complete - The Data Pulse (with async + observability improvements)

---

## Recent Decisions (Last 60 days)

### AD-001: Rule Engine Design (Updated 2026-03-26)
**Decision:** Use a Two-Stage Prioritized Cascade implemented in `FeatureFlag`.
1. **Targeting Stage:** Evaluate boolean conditions via `ToggleRuleCondition` against context properties (e.g., `region == 'US'`).
2. **Distribution Stage:** Apply deterministic rollout percentage (`rolloutPercentage`) based on a `subject` (e.g., `userId`) via `ToggleRuleDefinition`.
**Core Principle:** Distribution != FlagValueEvaluation.
**Impact:** Clearer domain model, support for targeted canary rollouts.

### AD-002: Persistence Strategy (2026-03-26)
**Decision:** Store rules as JSONB in PostgreSQL.
**Reason:** High performance (single PK lookup), flexible schema (easy to evolve rule conditions), and GraalVM-friendly.
**Trade-off:** Less relational rigor for internal rule structure, handled in the domain layer.
**Impact:** Simplifies the database schema and native-image compilation.

### AD-003: Deterministic Rollout (Updated 2026-03-26)
**Decision:** Use SHA-256 for consistent hashing with `double` precision [0.0, 100.0) via `ToggleHasher`.
**Performance Optimization:** Use `ThreadLocal<MessageDigest>` to reuse hashers and static `ObjectMapper` in `ToggleRuleListConverter`.
**Reason:** Ensures high-throughput evaluation and precision required for 0.1% rollouts.
**Impact:** Production-grade performance for hot paths.

### AD-004: Unified Evaluation Service (2026-03-26)
**Decision:** Maintain `ToggleService` as the unified entry point for all evaluation requests (REST, gRPC, Internal).
**Reason:** Consistency and simplicity. Avoids prefixing with "Cached" as caching is an implementation detail of the service.
**Impact:** Cleaner API surface and predictable service naming.

### AD-005: Infrastructure-Level Caching (Updated 2026-03-26)
**Decision:** Implement caching as a decorator/implementation detail of `FeatureFlagRepository` (`JpaFeatureFlagRepository`).
**Reason:** Aligns with DDD by keeping the domain repository interface pure and hiding infrastructure optimizations (Caffeine) behind the abstraction.
**Impact:** Domain and Application layers remain unaware of caching, improving testability and separation of concerns.

### AD-006: @Transactional on ToggleService.evaluate() (2026-03-31)
**Decision:** Keep `@Transactional` on `ToggleService.evaluate()` and publish `ToggleEvaluatedEvent` within the transaction via Spring's `ApplicationEventPublisher`.
**Reason:** Spring Modulith intercepts the event publication and writes an outbox entry (`EVENT_PUBLICATION`) atomically within the same transaction. The `@ApplicationModuleListener` in the analytics module then processes the event **post-commit** in its own separate transaction — analytics ingestion does NOT block the evaluation response. The only additional I/O within the evaluation transaction is a single fast local write to `EVENT_PUBLICATION`. This preserves reliable delivery (Rule 4: outbox-style dispatch) while keeping analytics processing fully async.
**Trade-off:** Each evaluation now performs one extra transactional write (outbox entry). For very high-throughput scenarios (Milestone 5 load test), this should be benchmarked. If the outbox write becomes a bottleneck, consider a separate write path or batch publication.
**Impact:** Milestone 3 checkpoint wording updated to clarify "no latency added to the evaluation *response*" (analytics processing is async post-commit).

### AD-007: Async Virtual Thread Executor with Semaphore (2026-04-01)
**Decision:** Implement `AsyncConfig` with `@EnableAsync` using `Executors.newVirtualThreadPerTaskExecutor()` and semaphore-based bulkhead (100 permits) for bounded concurrency in analytics ingestion.
**Reason:** Virtual threads are lightweight and allow high concurrency. Explicit semaphore prevents unbounded virtual thread spawning and provides backpressure when ingestion is overloaded. Observability via logging tracks semaphore acquisition/release.
**Implementation:** `EventIngestionService.ingest()` marked with `@Async("asyncExecutor")`. REST controller returns 202 ACCEPTED immediately; ingestion proceeds async on virtual thread. Semaphore queues excess tasks with automatic backpressure.
**Impact:** Decouples analytics ingestion from evaluation response latency. Improves resilience under load. Supports virtual thread transition for GraalVM native image (Milestone 5).

### AD-008: SQLSTATE-Based Duplicate Key Detection (2026-04-01)
**Decision:** Replace brittle error message parsing with SQL SQLSTATE 23505 (UNIQUE_VIOLATION) code detection in `EventIngestionService.isDuplicateKeyViolation()`.
**Reason:** Database-agnostic standard (PostgreSQL, H2, MySQL, Oracle, etc.) eliminates dependency on vendor-specific error messages. Walks the SQLException cause chain to find SQLSTATE.
**Implementation:** Added constant `SQLSTATE_UNIQUE_VIOLATION = "23505"` in EventIngestionService. Tests updated to mock proper SQLException with SQLSTATE.
**Impact:** More robust idempotency handling. Removes assumption about constraint name. Works across all SQL databases without modification.

---

## Milestone Status

| Milestone | Goal | Status |
| --------- | ---- | ------ |
| 1. The Engine | Working Rule Engine & Foundation | ✅ Complete |
| 2. High-Performance Edge | Caffeine & gRPC | ✅ Complete |
| 3. The Data Pulse | Event Ingestion + Async Processing | ✅ Complete |
| 4. The Control Plane | Management API | ⏳ Next |
| 5. Production Ready | GraalVM & Load Testing | ⏳ Planned |

---

## Active Blockers
- None.

---

## Todos
- [x] Implement Milestone 2 (Caffeine & gRPC).
- [x] Implement Milestone 3 (Data Pulse — analytics ingestion + async + observability).
- [x] Implement AsyncConfig with virtual threads + semaphore bulkhead (AD-007).
- [x] Refactor duplicate key detection to use SQLSTATE 23505 (AD-008).
- [ ] Fix AnalyticsControllerTest (missing MockMvc test dependency).
- [ ] Set up GitHub Actions for native builds.
- [ ] Create a sample gRPC client for testing.
- [ ] Benchmark evaluation path latency after outbox write (AD-006) — add to Milestone 5 load test.
