# Domain and Folder Guidelines

> Mandatory note: folder decisions in this repository should align with [`tactical-ddd-guidance.md`](./tactical-ddd-guidance.md). If an `architecture-guidelines.md` document is introduced, follow it as a stricter source of truth.

## Purpose

This document defines where new code should live in Ground Control.

It separates:
- what is **verified in this repository today**
- what is a **recommended module layout example** for upcoming domain modules

## Current Ground Control state (verified)

Verified from source files in this repository:
- Base package: `com.product.ground_control`
- Current app entry point: `src/main/java/com/product/ground_control/GroundControlApplication.java`
- Modulith boundary validation test: `src/test/java/com/product/ground_control/ModularityTest.java`

At the moment, explicit business modules like `billing`, `subscription`, `user`, and `shared` are not present under `src/main/java/com/product/ground_control`.

## Golden rule

**Organize by module first, then by layer.**

That means:
- start from the business capability (module)
- only then choose `application`, `domain`, or `infrastructure`
- avoid cross-cutting folders that mix multiple modules

## Recommended module layout (example when modules are introduced)

Use this as the target shape when adding domain modules:

```text
src/main/java/com/product/ground_control/
├── subscription/               # example module
│   ├── api/                    # published module contract
│   ├── application/
│   │   ├── service/
│   │   └── dto/
│   ├── domain/
│   │   ├── model/
│   │   ├── policy/
│   │   └── repository/
│   └── infrastructure/
│       ├── web/
│       ├── messaging/
│       └── persistence/
├── billing/                    # example module
├── user/                       # example module
└── shared/                     # example shared kernel (strictly limited)
```

If a module does not exist yet, keep examples generic and avoid pretending classes already exist.

## What goes in each layer

### `domain/`
Put business concepts here.

Allowed contents:
- entities and aggregates
- repository contracts (interfaces)
- value objects
- pure domain policies/calculations

Do not put here:
- controllers
- Kafka/SQS listeners
- HTTP clients
- Spring MVC request/response models
- JPA adapter plumbing

### `application/`
Put use-case orchestration here.

Allowed contents:
- application services
- use-case DTOs
- transaction boundaries
- orchestration across repositories, ports, and events

### `infrastructure/`
Put framework and adapter code here.

Allowed contents:
- REST controllers
- messaging consumers/producers
- persistence adapters and Spring Data implementations
- external API/gateway adapters

## Public entry points by module (guideline)

When a module is added, define a clear public API package (for example `module/api`) and keep internals private.

Rules:
- other modules depend on public contracts, not internal services/repositories
- cross-module communication should happen via module API or events
- avoid direct table/repository access across module boundaries

## Placement rules for new code

### Add to `domain` when
- code expresses business meaning/invariants
- it should be testable without Spring/container

### Add to `application` when
- code orchestrates a use case end-to-end
- it controls transaction boundaries
- it coordinates multiple collaborators

### Add to `infrastructure` when
- code is framework/transport/persistence integration
- it translates external protocols into application calls

## Dependency direction

Preferred direction:
- `infrastructure -> application -> domain`
- cross-module calls through public module API or events
- `shared` only for truly shared kernel elements

Avoid:
- importing another module's `infrastructure` package
- exposing repositories as module public API
- dumping unrelated business logic into `shared`

## Naming guidance

Use names that reveal role:
- `*Service` for application orchestration
- `*Facade` or `*Api` for public module contracts
- `*Repository` for repository contracts/adapters
- `*Controller` for HTTP adapters
- `*Listener` / `*Publisher` for messaging adapters
- `*Policy` for pure domain calculations

## Test folder alignment

Mirror production structure in `src/test/java`.

Keep `src/test/java/com/product/ground_control/ModularityTest.java` as a boundary safety net as modules are introduced.

## Review checklist before merging

1. Did I choose the correct module first?
2. Did I choose the correct layer second?
3. Am I using public module contracts instead of internals?
4. Should this logic live in a domain aggregate or policy?
5. Did I avoid placing module-specific business logic in `shared`?

## Evidence and assumptions

- **Verified now**: base package, app class, and modulith test class listed above.
- **Assumed/example only**: `subscription`, `billing`, `user`, and `shared` modules unless added in source.

## See also

- [`tactical-ddd-guidance.md`](./tactical-ddd-guidance.md)
- [`integration-patterns.md`](./integration-patterns.md)
- [`CODING-PATTERNS.md`](./CODING-PATTERNS.md)

