# Feature Specification: Foundation (Phase 1)

## Problem Statement
We need to establish the architectural backbone of Ground Control. This includes the module boundaries (Spring Modulith), persistence configuration (PostgreSQL), and the core domain logic for evaluating feature toggles (Rule Engine).

## Goals
- [ ] Establish explicit module boundaries for `toggles`, `analytics`, and `management`.
- [ ] Configure PostgreSQL with Flyway for schema migrations.
- [ ] Implement the `FeatureRule` Aggregate with support for Boolean/String values and Deterministic Rollout.

## Out of Scope
- gRPC implementation details (Protobuf files only).
- In-memory caching (Caffeine).
- Management UI or complex analytics ingestion.

---

## User Stories

### P1: Core Modulith Setup ⭐ MVP
**User Story**: As a Developer, I want a clean multi-module structure so that I can maintain domain isolation and GraalVM compatibility.

**Acceptance Criteria**:
1. WHEN I run `ModularityTest` THEN it SHALL pass for the new modules (`toggles`, `analytics`, `management`).
2. WHEN the application starts THEN PostgreSQL connectivity SHALL be verified.

---

### P1: FeatureRule Aggregate ⭐ MVP
**User Story**: As a PM/Agent, I want to define rules with priorities and percentages so that I can control rollouts precisely.

**Acceptance Criteria**:
1. WHEN a user ID is hashed THEN the rollout percentage SHALL be deterministic.
2. WHEN multiple rules exist THEN the engine SHALL evaluate them in Priority Order (Cascade).

---

## Requirement Traceability

| Requirement ID | Story                       | Status  |
| -------------- | --------------------------- | ------- |
| FOUND-01       | Core Modulith Setup         | Pending |
| FOUND-02       | PostgreSQL/Flyway Config    | Pending |
| FOUND-03       | FeatureRule Domain Model    | Pending |
| FOUND-04       | Rule Evaluator (Cascade)    | Pending |
| FOUND-05       | Deterministic Hashing Logic | Pending |
