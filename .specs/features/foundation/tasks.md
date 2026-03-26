# Tasks: Foundation (Phase 1)

**Design**: `.specs/features/foundation/design.md`
**Status**: Draft

---

## Execution Plan

### Phase 1: Infrastructure (Sequential)
- **T1**: Project dependencies (PostgreSQL, Flyway, Caffeine, gRPC).
- **T2**: Modulith module structure (`toggles`, `analytics`, `management`) and `ModularityTest`.
- **T3**: PostgreSQL/Flyway setup (`application.properties` and schema script).

### Phase 2: Domain (Sequential)
- **T4**: `FeatureRule` Aggregate Root (Domain Model).
- **T5**: Deterministic Hasher and Rule Condition logic (Value Objects).
- **T6**: `ToggleEvaluator` Domain Service (The Cascade).

### Phase 3: Persistence & Verification (Sequential)
- **T7**: JPA Repository implementation for `toggles`.
- **T8**: Integration test for the full evaluation path.

---

## Task Breakdown

### T1: Project Dependencies
- **What**: Add required libraries to `build.gradle.kts`.
- **Requirements**: `FOUND-02`.
- **Done when**: `./gradlew build` passes with new dependencies.

### T2: Module Structure
- **What**: Create empty packages for `toggles`, `analytics`, and `management`.
- **Requirements**: `FOUND-01`.
- **Done when**: `ModularityTest` passes with new empty modules.

### T3: PostgreSQL/Flyway Setup
- **What**: Add `application.properties` config and the first Flyway migration.
- **Requirements**: `FOUND-02`.
- **Done when**: Application starts without database errors (using local Postgres).

### T4: FeatureRule Aggregate Root
- **What**: Implement the `FeatureRule` class in the `toggles.domain` package.
- **Requirements**: `FOUND-03`.
- **Done when**: Unit tests verify the aggregate's invariants.

### T5: Hasher & Conditions
- **What**: Implement hashing logic for rollout percentages and condition matching.
- **Requirements**: `FOUND-05`.
- **Done when**: Unit tests prove 10% rollout is stable for the same user ID.

### T6: ToggleEvaluator (The Cascade)
- **What**: Orchestrate the evaluation from Priority 1 to N.
- **Requirements**: `FOUND-04`.
- **Done when**: Unit tests verify the "Cascade" (First match wins).

### T7: Persistence Layer
- **What**: Implement Repository and JPA Entities for `toggles`.
- **Requirements**: `FOUND-03`.
- **Done when**: Test verifies rule can be saved and reloaded.

### T8: Integration Verification
- **What**: Full integration test of the evaluation flow.
- **Requirements**: `FOUND-01, FOUND-02, FOUND-03, FOUND-04, FOUND-05`.
- **Done when**: Happy path and edge cases verified.
