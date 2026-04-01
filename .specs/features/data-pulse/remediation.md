# Remediation: Analytics Module Test & API Gaps

## Scope

Fix 5 QA gaps found in the analytics module (Event listener + Controller + Tests).

**Scope Size:** Medium (4 files, clear fixes)

---

## Analysis

### What Works ✓

EventIngestionService is **correctly implemented**:
- Single `ingest(IngestionSource, UUID idempotencyKey, ...)` method
- Delegates to `ingestFromEvent()` (event ID as key) and `ingestFromRestApi()` (client key)
- Duplicate detection via UNIQUE constraint on `idempotency_key`
- Async execution + transaction safety via Spring Modulith
- `@Async("asyncExecutor")` prevents blocking

### What's Broken ✗

**AnalyticsController calls the service with the WRONG signature:**
- Current: `ingestionService.ingest(FeatureKey, Variation, Subject, Metadata)` — missing IngestionSource and idempotencyKey
- Expected: `ingestionService.ingest(IngestionSource.REST_API, clientProvidedKey, ...)`

**Tests missing:**
1. AnalyticsController unit tests (202 response, error handling)
2. Integration test (controller → listener → service → DB → idempotency)

---

## Execution Plan

### Step 1: Fix AnalyticsController
- **File:** `src/main/java/.../analytics/infrastructure/api/rest/AnalyticsController.java`
- **What:** Add REST API idempotency key (from X-Idempotency-Key header or generate UUID)
- **Verify:** Controller passes `IngestionSource.REST_API + idempotencyKey` to service

### Step 2: Add AnalyticsController Unit Tests
- **File:** `src/test/java/.../analytics/infrastructure/api/AnalyticsControllerTest.java`
- **What:**
  - Happy path: POST valid event → 202 ACCEPTED
  - Error: missing subject → 400
  - Error: malformed JSON → 400
  - Verify `EventIngestionService.ingest()` called with correct source + key
- **Scope:** `@WebMvcTest(AnalyticsController.class)` with MockMvc

### Step 3: Add Integration Test
- **File:** `src/test/java/.../EvaluationIntegrationTest.java` (or new file)
- **What:**
  - Send event via listener → verify stored in `analytics_events`
  - Send duplicate event (same event ID) → verify still one record
  - Verify `idempotency_key` stored correctly
  - Verify metadata JSONB populated
- **Scope:** `@SpringBootTest` with TestContainer or in-memory DB

### Step 4: Verify Event Publication Indexes
- **File:** `src/main/resources/db/migration/V2__create_analytics_table.sql`
- **What:** Confirm composite index matches Spring Modulith query patterns
- **Risk:** Low — indexes are performance-only, not correctness

---

## Atomic Steps (to Execute)

1. Update `AnalyticsController` to accept and use idempotency key (IngestionSource.REST_API)
2. Add `AnalyticsControllerTest.java` with 3-4 test cases
3. Add integration test to `EvaluationIntegrationTest.java` for event → DB flow + idempotency
4. Run tests; verify all pass

---

## Success Criteria

- ✓ AnalyticsController compiles and passes unit tests
- ✓ Controller tests: 85%+ coverage
- ✓ Integration test: duplicate events detected, single record stored
- ✓ No QA findings remaining

---

## Risks & Mitigations

| Risk | Impact | Mitigation |
|------|--------|-----------|
| Idempotency key generation strategy | Medium | Use UUID.randomUUID() or X-Idempotency-Key header |
| Integration test DB setup | Medium | Use TestContainer or Spring's test properties for H2 |
| Spring Modulith event retry patterns | Low | Current implementation is sound; indexes verified by test |
