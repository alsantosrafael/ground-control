# State

**Last Updated:** 2026-03-26
**Current Work:** Milestone 1 Complete - Foundation & Core Engine

---

## Recent Decisions (Last 60 days)

### AD-001: Rule Engine Design (Updated 2026-03-26)
**Decision:** Use a Two-Stage Prioritized Cascade.
1. **Targeting Stage:** Evaluate boolean conditions against context properties (e.g., `region == 'US'`).
2. **Distribution Stage:** Apply deterministic rollout percentage based on a `subject` (e.g., `userId`).
**Core Principle:** Distribution != FlagValueEvaluation. Distribution is a gate within a rule, not a replacement for targeting.
**Reason:** Allows for complex rollouts like "10% of VIP users get the new UI" without mixing comparison logic with hashing logic.
**Impact:** Clearer domain model, better observability into why a flag was or wasn't enabled.

### AD-002: Persistence Strategy (2026-03-26)
**Decision:** Store rules as JSONB in PostgreSQL.
**Reason:** High performance (single PK lookup), flexible schema (easy to evolve rule conditions), and GraalVM-friendly.
**Trade-off:** Less relational rigor for internal rule structure, handled in the domain layer.
**Impact:** Simplifies the database schema and native-image compilation.

### AD-003: Deterministic Rollout (Updated 2026-03-26)
**Decision:** Use SHA-256 for consistent hashing with `double` precision [0.0, 100.0).
**Reason:** `String.hashCode` lacks the uniformity required for precise decimal rollouts (e.g., 0.5%). SHA-256 ensures a "truly correct" distribution.
**Impact:** Support for high-precision canary rollouts.

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
