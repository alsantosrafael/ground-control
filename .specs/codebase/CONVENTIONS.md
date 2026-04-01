# Ground Control Engineering Conventions

This document captures the architectural standards and coding conventions for the Ground Control project, based on the reference patterns in the `toggles` module.

## 🏗️ Module Architecture (Ports & Adapters)

Every module in Ground Control follows a strict **Hexagonal / Clean Architecture** structure within the Spring Modulith.

### 1. Layers & Responsibilities

| Layer | Package | Responsibility |
| :--- | :--- | :--- |
| **Domain** | `{module}.domain` | **Root**: Repository ports (interfaces). No framework dependencies. |
| **Domain (Entity)** | `{module}.domain.entity` | **Entities**: DDD aggregates. Minimal setters, protected constructors. |
| **Domain (Model)** | `{module}.domain.model` | **Value Objects**: Converters, specific domain data types. |
| **Application** | `{module}.application.services` | **Services**: Orchestrate use cases, inject repository interfaces. (Pluralized naming: `services`). |
| **Application** | `{module}.application.dto` | **DTOs**: Application-level data transfer objects. |
| **Infrastructure** | `{module}.infrastructure.repository` | **Adapters**: JpaRepository implementations and Spring Data interfaces. |
| **Infrastructure** | `{module}.infrastructure.api.rest` | **Delivery**: Controllers (REST). |
| **Infrastructure** | `{module}.infrastructure.messaging` | **Messaging**: Event listeners and publishing logic. |

---

### 2. Entity Standards

*   **Lombok Guards**: Use `@Setter(AccessLevel.PRIVATE)` and `@NoArgsConstructor(access = AccessLevel.PROTECTED)` to ensure domain integrity and Hibernate compatibility.
*   **ID Generation**: Use `@GeneratedValue(strategy = GenerationType.UUID)` for automatic UUID management.
*   **Time Handling**: Use `LocalDateTime` for all core timestamps.
*   **Encapsulation**: Entities should only be modified through explicit business methods (e.g., `updateRules`, `ingest`).

---

### 3. Repository Pattern

The domain model must be unaware of the persistence implementation.
1.  Define the interface (e.g., `AnalyticsEventRepository`) in the `domain` package.
2.  Define the Spring Data interface (e.g., `JpaAnalyticsEventRepository`) in the `infrastructure.repository` package.
3.  Implement the domain interface as an "Adapter" that delegates to the Spring Data interface.

---

### 4. Modularity (Spring Modulith)

*   **API Package**: Cross-module contracts (e.g. Events, shared DTOs) MUST reside in a subpackage named `api` (e.g., `com.product.ground_control.toggles.api`).
*   **Transactional Boundaries**: All write operations MUST be explicitly orchestrated in a service with `@Transactional`.
*   **Asynchronous Communication**: Prefer `EventPublicationRegistry` for cross-module events via `@ApplicationModuleListener`.
