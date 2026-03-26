# Tactical DDD Guidance

> Mandatory note: before changing design, use [`domain-folder-guidelines.md`](./domain-folder-guidelines.md) for placement rules. If an `architecture-guidelines.md` document is introduced, treat it as stricter guidance.

## Purpose

This document describes how tactical DDD should be applied in Ground Control with Spring Boot + Spring Modulith.

It is intentionally practical: use it to model aggregates, policies, ports, events, and application services.

Important: when a module mentioned here does not exist yet in source code, treat the content as an implementation example, not current-state documentation.

## Current Ground Control state (verified)

From current repository structure:
- base package: `com.product.ground_control`
- app entry point: `GroundControlApplication`
- modulith verification/documentation test: `ModularityTests` in `src/test/java/com/product/ground_control/ModularityTest.java`

At this moment, explicit business modules (`subscription`, `billing`, `user`, `shared`) are not yet present under `src/main/java/com/product/ground_control`.

## Bounded contexts in this project

The contexts below are the recommended target split when business modules are introduced.

### Example context: `subscription`
Responsible for the subscription lifecycle:
- creation
- activation/reactivation
- cancellation
- expiration
- renewal state transitions after billing results

Example tactical concepts:
- aggregate: `Subscription`
- domain policies: `BillingCyclePolicy`, `BillingRetryPolicy`
- application services: `SubscriptionService`, `SubscriptionWriteService`, `RenewalOrchestratorService`, `SubscriptionExpiryService`

### Example context: `billing`
Responsible for charging and recording billing attempts.

Example tactical concepts:
- aggregate/entity: `BillingHistory`
- public module contract: `BillingFacade`
- async worker: `BillingWorker`
- gateway protection: `GatewayAccessControl`

### Example context: `user`
Responsible for user identity and existence checks.

Example tactical concepts:
- aggregate: `User`
- public module contract: `UserFacade`

### Example context: `shared`
Shared kernel only.

Use it for:
- enums and cross-cutting domain types (`Plan`, `SubscriptionStatus`, `BillingHistoryStatus`)
- cross-module ports (`PaymentTokenPort`)
- events and exceptions
- technical config reused by multiple modules

Do **not** treat `shared` as a dump folder for unrelated business logic.

## Tactical building blocks

## 1. Aggregates

### Example: `Subscription` aggregate in `subscription`
It owns invariants such as:
- one subscription row per user
- lifecycle transitions (`ACTIVE`, `CANCELED`, `SUSPENDED`, `INACTIVE`)
- retry counters and next retry time
- reactivation behavior

Rules:
- put state transitions inside the aggregate when they are true business behavior
- keep the aggregate consistent after each public method call
- prefer intent-revealing methods such as `reactivate`, `markAsCanceled`, `applyRenewal`, `applyBillingFailure`

### Example: `BillingHistory` billing execution record
It represents the billing attempt history and idempotency trail.

Rules:
- it must remain the source of truth for billing attempt outcomes
- idempotency must continue to be enforced at both application and database levels

### Example: `User` aggregate in `user`
Its responsibilities should stay focused on user identity and ownership checks, not subscription or billing orchestration.

## 2. Domain policies

Policies belong in the domain when they encode business rules without infrastructure concerns.

Example policies:
- `BillingCyclePolicy`: calculates the next billing cycle date
- `BillingRetryPolicy`: calculates retry backoff

Use a domain policy when:
- the rule is pure business logic
- it can be tested without Spring, database, Kafka, Redis, or HTTP
- multiple application flows need the same rule

Do not hide business rules inside controllers, listeners, or repository queries when they can live in a policy or aggregate.

## 3. Application services

Application services orchestrate use cases, transactions, and cross-module collaboration.

Example services:
- `SubscriptionWriteService`
- `SubscriptionService`
- `RenewalOrchestratorService`
- `SubscriptionExpiryService`
- `BillingFacadeImpl`
- `UserService`

Rules:
- application services may call repositories, ports, facades, and event publishers
- application services may coordinate multiple steps of a use case
- application services should not become bags of domain rules; move stable business logic into aggregates or policies
- keep transaction boundaries explicit and short

## 4. Ports and module contracts

Cross-module collaboration must happen through clear contracts.

Example contracts:
- `BillingFacade` → `subscription` depends on `billing` through this interface
- `UserFacade` → `subscription` checks user existence through this interface
- `PaymentTokenPort` → `billing` asks `subscription` for the token without creating a circular dependency

Rules:
- if another module needs something from your module, expose a contract first
- do not import another module's internal service just because it is convenient
- prefer a small, stable interface over leaking internal classes

## 5. Domain events

Use events to communicate state changes that must happen after persistence is committed.

Example events:
- `RenewalRequestedEvent`
- `BillingResultEvent`
- `SubscriptionUpdatedEvent`

Rules:
- events should describe business facts, not UI actions
- events must be safe to replay or deduplicate
- sensitive data such as raw payment tokens must not travel in events

## What belongs where

- Put business state and invariants in the aggregate.
- Put pure calculations in domain policies.
- Put orchestration and transaction control in application services.
- Put HTTP, Kafka, Redis, external gateway code, and persistence adapters in infrastructure.

For folder placement, follow [`domain-folder-guidelines.md`](./domain-folder-guidelines.md).

## Anti-patterns to avoid

- putting renewal or retry business logic directly in controllers
- letting Kafka listeners become the only place where business rules exist
- exposing internal repositories as public module APIs
- moving module-specific logic into `shared` just to reuse it quickly
- publishing integration side effects before the database commit is durable

## Spring Modulith notes

- Keep module dependencies explicit and verifiable via modulith tests.
- Place module public contracts in an API package (for example `module.api`) and keep internals private.
- Prefer domain events for asynchronous collaboration across module boundaries.
- Keep event payloads replay-safe and free of sensitive data.

## When adding a new feature

Use this checklist:
1. Identify the bounded context: `subscription`, `billing`, `user`, or truly `shared`.
2. Identify the aggregate that owns the invariant.
3. Move pure rule calculations to a domain policy if needed.
4. Add or extend an application service to orchestrate the use case.
5. If another module needs access, define or extend a port/facade.
6. Re-check the placement rules in [`domain-folder-guidelines.md`](./domain-folder-guidelines.md).
7. Re-check non-negotiable constraints from `architecture-guidelines.md` if that document exists.

If those modules do not exist yet, create the minimal package/module structure first and keep naming aligned with this guide.

## Evidence and assumptions

- **Verified now**: package root and modulith test class in this repository.
- **Example only**: concrete `subscription`, `billing`, `user`, and `shared` classes listed in this document until implemented.

## See also

- [`domain-folder-guidelines.md`](./domain-folder-guidelines.md)
- [`integration-patterns.md`](./integration-patterns.md)
- [`CODING-PATTERNS.md`](./CODING-PATTERNS.md)

