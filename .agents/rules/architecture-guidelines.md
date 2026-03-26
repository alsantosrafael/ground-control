---
description: Core architecture principles
globs: []
alwaysApply: true
---

# Architecture Guidelines

> ## Non-negotiable rule
> **`architecture-guidelines.md` should always be followed.**
>
> If a shortcut conflicts with this document, the shortcut is wrong. Update the architecture deliberately, then update this document and the related guidance files.

## Purpose

This is the normative architecture ruleset for `ground-control`.

Use it together with:
- [`tactical-ddd-guidance.md`](../../docs/tactical-ddd-guidance.md) for modeling decisions
- [`domain-folder-guidelines.md`](../../docs/domain-folder-guidelines.md) for package and folder placement
- [`integration-patterns.md`](../../docs/integration-patterns.md) for adapter and resilience patterns

## Current repository baseline (verified)

- Base package: `com.product.ground_control`
- Application entrypoint: `src/main/java/com/product/ground_control/GroundControlApplication.java`
- Modulith verification test: `src/test/java/com/product/ground_control/ModularityTest.java`

If domain modules are not present yet, treat module names and contracts below as target examples, not current-state claims.

## 1. Preserve module boundaries

Rules:
- each business capability stays in its own module (for example: `rollout`, `billing`, `rollout-rule`)
- cross-module collaboration must happen through explicit module APIs/ports/events
- do not depend on another module's internal services, controllers, listeners, or repositories
- keep `shared` small and intentional

Why this matters in this project:
- the build already verifies modular structure via `ModularityTests`
- module boundaries should stay enforceable as modules are introduced

## 2. Keep business logic in the right place

Rules:
- aggregates own business state transitions and invariants
- pure calculations belong in domain policies
- application services orchestrate use cases and transactions
- infrastructure adapts HTTP, Kafka, Redis, external gateways, and framework concerns

Examples (target patterns):
- aggregates own lifecycle transitions and invariants
- domain policies hold pure calculations
- application services orchestrate use cases and transactions
- infrastructure adapters handle HTTP/events/persistence concerns

## 3. Never hold long database transactions over gateway I/O

Rules:
- keep write transactions short
- do not keep a DB connection open during external HTTP calls
- where necessary, split the workflow into short transactional segments
- preserve transaction propagation intentionally, even under virtual threads

Why this matters here:
- keeping transactions short reduces lock contention and failure blast radius
- split workflows are easier to retry safely and observe

## 4. Use events and outbox-style dispatch instead of dual-write

Rules:
- if a DB change must trigger external delivery, publish an event and let the outbox/module listener deliver it
- do not write to the database and Kafka manually in the same imperative block as a substitute for reliable delivery
- prefer business events that can be retried safely

Why this matters here:
- outbox/event-first integration prevents lost updates during failures
- Spring Modulith event publication provides a clean module integration model

## 5. Cache must never get ahead of the database

Rules:
- publish cache update events from within a transaction
- perform cache writes after commit, not before
- Redis is a read optimization, never the source of truth
- define TTLs based on business state, not arbitrary guesses

Why this matters here:
- post-commit cache updates avoid serving states not yet committed in the database
- cache should stay a performance optimization, not an authority

## 6. Scheduler flows must be safe under concurrency and multiple pods

Rules:
- scheduled workflows must use distributed locking when duplicate execution across instances is harmful
- also keep a database-level correctness guard for defense in depth
- use atomic updates/CAS-style conditions for state transitions that may race
- align time precision with database precision when timestamps are part of concurrency guards

Why this matters here:
- distributed schedulers can duplicate work without lock and atomic guards
- database-level safeguards protect correctness when infrastructure guarantees are imperfect

## 7. Every async consumer must be idempotent or conflict-safe

Rules:
- Kafka consumers must assume redelivery can happen
- use idempotency keys, compare-and-swap updates, or other replay-safe patterns
- stale or duplicate messages must be ignored safely
- never rely on exactly-once behavior from infrastructure alone

Why this matters here:
- redelivery is normal in async systems; handlers must be replay-safe

## 8. Sensitive payment data must not leak through messaging

Rules:
- do not place raw payment tokens in Kafka events
- keep tokens encrypted at rest
- fetch sensitive values through a dedicated port when needed at execution time

Why this matters here:
- privacy and compliance require strict isolation of sensitive fields

## 9. Resilience patterns are part of the architecture, not optional extras

Rules:
- preserve bounded gateway concurrency
- keep retry and circuit breaker behavior intentional and observable
- prefer natural backpressure over artificial local rejection when compatible with virtual threads
- expose metrics and logs that explain the control flow

Why this matters here:
- resilience controls prevent dependency outages from cascading across modules

## 10. Database shape changes are migration-first

Rules:
- schema evolution must go through Flyway migrations
- keep `ddl-auto=none` assumptions intact
- enforce critical invariants both in code and in the database when appropriate
- treat indexes and unique constraints as part of the architecture

Why this matters here:
- migration-first schema changes reduce rollout and rollback risk

## 11. Prefer evolvable public APIs over convenience coupling

Rules:
- if a module capability may later move to another service, call it through a stable contract now
- avoid letting tests normalize illegal coupling between modules
- document new public contracts in code and in these docs when they are introduced

This project should preserve this direction by keeping module contracts stable and explicit.

## 12. Update the docs when the architecture changes

Rules:
- if you add a new module, update all three documents
- if you add a new public contract, update the module and folder guidance
- if you change an invariant or aggregate boundary, update the tactical DDD guidance
- if you intentionally break a rule, capture the decision explicitly before merging

## Architecture review checklist

Before merging a significant change, ask:
1. Does this preserve module boundaries?
2. Does it avoid dual-write?
3. Is the transaction boundary short and explicit?
4. Is the async path replay-safe?
5. Is cache updated only after commit?
6. Are sensitive values kept out of events?
7. Did I place code according to [`domain-folder-guidelines.md`](../../docs/domain-folder-guidelines.md)?
8. Did I model the behavior according to [`tactical-ddd-guidance.md`](../../docs/tactical-ddd-guidance.md)?

## See also

- [`tactical-ddd-guidance.md`](../../docs/tactical-ddd-guidance.md)
- [`domain-folder-guidelines.md`](../../docs/domain-folder-guidelines.md)
- [`CODING-PATTERNS.md`](../../docs/CODING-PATTERNS.md)
- [`integration-patterns.md`](../../docs/integration-patterns.md)
- `src/test/java/com/product/ground_control/ModularityTest.java`

