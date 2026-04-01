# Tasks: The Data Pulse (Milestone 3)

**Design**: `.specs/features/data-pulse/design.md`
**Status**: ⏳ Next

---

## 📅 Milestone 3: The Data Pulse (Event Ingestion)

### Phase 1: Persistence & Reliable Messaging (Sequential)
- [ ] **T1**: Create PostgreSQL database migration for `analytics_events` table.
- [ ] **T2**: Implement `AnalyticsEvent` domain entity and `AnalyticsEventRepository` (Module-Private).
- [ ] **T3**: Configure `Spring Modulith Event Publication Registry` for reliable delivery.

### Phase 2: Ingestion & APIs (Parallel OK)
- [ ] **T4**: Implement `AnalyticsController` for `POST /api/v1/events`.
- [ ] **T5**: Implement `EventIngestionService` with `@Transactional` boundaries.
- [ ] **T6**: Implementation of `AnalyticsModule` structure (`api/`, `application/`, `domain/`, `infrastructure/`).

### Phase 3: Engine Integration (Sequential)
- [ ] **T7**: Create `ToggleEvaluatedEvent` in `com.product.ground_control.toggles.api`.
- [ ] **T8**: Modify `ToggleService` to publish the event upon evaluation.
- [ ] **T9**: Implement `@ApplicationModuleListener` in `analytics` to consume events asynchronously.

### Phase 4: Validation (Sequential)
- [x] **T10**: E2E Integration test for REST ingestion to DB.
- [ ] **T11**: **SLA Performance benchmark**: Verify `202 Accepted` response < 10ms.
- [x] **T12**: **Modularity check**: Run `ModularityTest` to verify boundary compliance.
- [x] **T13**: **Reliability check**: Verify Event Publication Registry persistence.

---

## Phase 5: PR Review Fixes (2026-03-31)

### Critical
- [x] **R-01**: Fix `LocalDateTime` → `Instant` in `AnalyticsEvent` (schema mismatch with `TIMESTAMPTZ`)
- [x] **R-02**: Remove debug try/catch from `ModularityTest`

### Architecture
- [x] **R-03**: Remove `deleteAll()`/`findAll()` from `AnalyticsEventRepository` domain port; update integration test to use `EventRepository` directly
- [x] **R-04**: Fix `MetadataConverter` to use constructor injection (align with `ToggleRuleListConverter` pattern)
- [x] **R-05**: Remove duplicate `from()` factory method aliases on VOs; standardize on `of()`

### Security
- [x] **R-06**: Add `@Valid` + `@NotBlank` to `AnalyticsEventRequest`; update controller; add `spring-boot-starter-validation` to build

### Tests
- [x] **R-07**: Fix fragile `hasSize(2)` assertion → targeted `anySatisfy` + `hasSizeGreaterThanOrEqualTo`
- [x] **R-08**: Add unit tests for `EventIngestionService` idempotency logic (`EventIngestionServiceTest`)
- [x] **R-09**: Add `@WebMvcTest` for `AnalyticsController` (`AnalyticsControllerTest`)

### Docs & Decisions
- [x] **R-10**: Fix inaccurate "backwards compatibility" comment in `AnalyticsController`
- [x] **R-11**: Document AD-006 in STATE.md (performance tradeoff: outbox write cost vs. async delivery)
- [x] **R-12**: Update ROADMAP.md Milestone 3 checkpoint wording to clarify async semantics

## Phase 6: Second PR Review Fixes (2026-04-01)

### Security
- [x] **R2-01**: Remove `subject.getIdentifier()` from INFO logs in `EventIngestionService` (demoted to DEBUG to avoid PII in production log aggregators)

### Architecture
- [x] **R2-02**: Fix domain→infra layering violation in `AnalyticsEvent` — `@Converter(autoApply = true)` on `MetadataConverter` eliminates the `@Convert` annotation and the infrastructure import from the domain entity
- [x] **R2-03**: Remove dead `@Embeddable` from `Metadata` (stored via converter, not embedded; annotation was misleading)
- [x] **R2-04**: Move `EvaluationIntegrationTest` from `toggles.api` package to root `com.product.ground_control` package — cross-module E2E tests live at the root level, not inside a specific module

### Tests
- [x] **R2-05**: Convert `AnalyticsControllerTest` from `@SpringBootTest` to `@WebMvcTest(AnalyticsController.class)` — removes Flyway workaround, boots only web slice
- [x] **R2-06**: Add `ToggleEvaluatedEventListenerTest` covering delegation to `EventIngestionService` and idempotency key contract

### Dead Code
- [x] **R2-07**: Remove unused `AnalyticsEvent.create(String, String, String, Map)` factory — only the VO overload is used

### Requirements
- [x] **R2-08**: Add `spring.threads.virtual.enabled=true` to `application.properties` (DP-04 compliance — makes Virtual Thread intent explicit)
