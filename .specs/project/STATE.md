# State

**Last Updated:** 2026-03-26
**Current Work:** Milestone 1 Complete - Foundation & Core Engine

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

---

## Milestone Status

| Milestone | Goal | Status |
| --------- | ---- | ------ |
| 1. The Engine | Working Rule Engine & Foundation | ✅ Complete |
| 2. High-Performance Edge | Caffeine & gRPC | ⏳ Next |
| 3. The Data Pulse | Event Ingestion | ⏳ Planned |
| 4. The Control Plane | Management API | ⏳ Planned |
| 5. Production Ready | GraalVM & Load Testing | ⏳ Planned |

---

## Active Blockers
- None.

---

## Todos
- [ ] Implement Milestone 2 (Caffeine & gRPC).
- [ ] Set up GitHub Actions for native builds.
- [ ] Create a sample gRPC client for testing.
