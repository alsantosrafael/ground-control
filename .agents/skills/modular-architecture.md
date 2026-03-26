---
name: modular-architecture
description: Ground Control modular architecture expert for Java + Spring Boot + Spring Modulith. Use when creating modules, assessing architecture compliance, evaluating boundaries, or planning module evolution.
---

# Modular Architecture Expert

You are an expert in Ground Control modular architecture. This skill provides practical rules for module design, compliance assessment, and module evolution in Java + Spring Boot + Spring Modulith.

## Core Philosophy

- **Modules = bounded contexts**: each module owns business logic and data boundaries
- **Public API first**: expose module contracts, keep internals private
- **Events over tight coupling**: prefer domain events for async cross-module integration
- **Framework at the edges**: domain logic should not depend on transport/persistence details

## Module Structure (recommended)

```
src/main/java/com/product/ground_control/{module}/
├── api/                              # Published interfaces and module contracts
├── application/                      # Use-case orchestration, transaction boundaries
│   ├── service/
│   └── dto/
├── domain/                           # Aggregates, value objects, policies, repository ports
│   ├── model/
│   ├── policy/
│   └── repository/
└── infrastructure/                   # Controllers, persistence adapters, messaging, external clients
    ├── http/
    ├── persistence/
    ├── messaging/
    └── client/
```

If a module does not exist in source yet, treat the structure above as an example target.

## The 10 Principles

| # | Principle | Criticality | Key Rule |
|---|-----------|-------------|----------|
| 1 | **Well-Defined Boundaries** | High | Expose only public module API packages/interfaces |
| 2 | **Composability** | Medium | Modules work independently or together |
| 3 | **Independence** | High | No shared mutable state; test in isolation |
| 4 | **Individual Scale** | Medium | Module-specific resource configurations |
| 5 | **Explicit Communication** | High | All inter-module contracts via interfaces/DTOs |
| 6 | **Replaceability** | Medium | Interface-based dependencies where needed |
| 7 | **Deployment Independence** | Medium | No deployment assumptions in modules |
| 8 | **State Isolation** | 🔴 CRITICAL | Module-prefixed entity names; no shared DB tables |
| 9 | **Observability** | High | Module-specific logging, metrics, health checks |
| 10 | **Fail Independence** | High | Circuit breakers; failures don't cascade |

## Top 8 Critical Violations

1. 🔴 **Duplicate table/entity names across modules** — use module-prefixed names such as `billing_plan`, `rollout_rule`
2. 🔴 **Cross-module database access** — one module reading another module's repositories/tables directly
3. 🔴 **Shared persistence ownership confusion** — no clear repository ownership per module
4. 🟠 **Fat controllers** — business logic in controllers instead of services
5. 🟠 **Repository injection in controllers** — controllers must only inject services
6. 🟠 **Missing transactional boundaries on writes** — write orchestration must be explicitly transactional
7. 🟠 **Exporting internal services** — modules must expose API contracts, not internals
8. 🟠 **Facade containing logic** — facades must be pure delegation to services; all querying and mapping belongs in services

## Decision Tree: What To Do

```
TASK TYPE                              → ACTION
────────────────────────────────────────────────────────────────
Creating a new module                  → define module API, then application/domain/infrastructure
Assessing architecture compliance      → run Modulith verification + review boundary checklist
Evaluating module split                → check cohesion, data ownership, and change frequency
Designing integration boundaries       → prefer API/events over direct DB calls
Improving resilience/observability     → add retry/circuit breaker + metrics + structured logs
```

## Use Case Instructions

### Creating a New Module

Follow this process:
1. Gather requirements (module responsibility, business invariants, integrations)
2. Define public module API first (`api/` interfaces, contracts)
3. Design aggregates/policies in `domain/`
4. Add orchestration in `application/` with explicit transactions
5. Add adapters in `infrastructure/` (web, messaging, persistence, clients)
6. Verify boundaries with Modulith tests and package dependencies

### Evaluating Whether to Split a Module

Split only when data ownership, change cadence, and failure domains justify separation.

Use these checks:
- Is there a distinct aggregate boundary?
- Can the candidate module evolve independently?
- Does separation reduce coupling without harming flow clarity?

### Managing Persistence Ownership

Use these patterns:
- each module owns its entities/repositories
- cross-module reads happen via API or event-carried data
- shared kernel contains only truly shared primitives/contracts

### Assessing Architecture Compliance

Run module boundary checks and produce prioritized findings:
- P0: data ownership or boundary violations
- P1: layering and contract violations
- P2: observability/resilience improvements

### Understanding a Specific Principle

Explain:
- what the principle means in Ground Control
- what concrete code smells violate it
- what refactor preserves behavior while restoring boundary clarity

## Quick Anti-Pattern Check

Before generating any code, verify:
- [ ] Entity/table names reflect module ownership
- [ ] No duplicate entity/table names across modules
- [ ] Controllers only inject services (not repositories)
- [ ] Write operations use explicit `@Transactional` boundaries
- [ ] Modules expose API contracts, not internal services/repositories
- [ ] Cross-module communication via HTTP/events (never direct DB access)
- [ ] Shared kernel is minimal and intentional
- [ ] Modulith verification tests remain green when module boundaries change
