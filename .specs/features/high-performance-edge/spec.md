# Feature Specification: High-Performance Edge (Milestone 2)

## Problem Statement
While we have a working engine, DB lookups on the evaluation path introduce unacceptable latency for high-scale environments. We need a "Nerve Center" that responds in sub-5ms through in-memory caching and binary communication (gRPC).

## Goals
- [ ] Achieve < 5ms evaluation latency (p99).
- [ ] Implement an in-memory Caffeine cache for feature rules.
- [ ] Provide a high-performance gRPC service for evaluation.
- [ ] Provide a universal REST endpoint for evaluation.

## Out of Scope
- Management UI.
- Analytics ingestion (Milestone 3).
- Advanced security/auth.

---

## User Stories

### P1: In-Memory Rule Cache ⭐ MVP
**User Story**: As a Developer, I want to cache rules in-memory so that I can avoid a database hit for every evaluation.

**Acceptance Criteria**:
1. WHEN a rule is requested THEN the system SHALL return it from the Caffeine cache.
2. WHEN a rule is updated in the DB THEN the cache SHALL be invalidated or updated.

---

### P1: gRPC Evaluation Service ⭐ MVP
**User Story**: As an Agent or High-Scale SDK, I want a gRPC endpoint so that I can evaluate flags with minimal overhead.

**Acceptance Criteria**:
1. WHEN I send a `Protobuf` request THEN the system SHALL respond with a `Protobuf` response.
2. The gRPC path SHALL exhibit lower latency and smaller payload size compared to REST.

---

### P1: REST Evaluation Endpoint ⭐ MVP
**User Story**: As a Human or Simple Script, I want a REST endpoint so that I can evaluate flags without a gRPC client.

**Acceptance Criteria**:
1. WHEN I send a `JSON` request to `/api/v1/rules/eval` THEN the system SHALL return the evaluation result.

---

## Requirement Traceability

| Requirement ID | Story                       | Status  |
| -------------- | --------------------------- | ------- |
| EDGE-01        | Caffeine Cache Integration  | Pending |
| EDGE-02        | Cache Invalidation Logic    | Pending |
| EDGE-03        | gRPC Service Definition     | Pending |
| EDGE-04        | gRPC Service Implementation | Pending |
| EDGE-05        | REST Evaluation Endpoint    | Pending |
