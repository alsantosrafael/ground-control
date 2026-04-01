# QA Findings: Staging Area Review

## Summary

**Status: ⚠️ REMEDIATE (5 gaps, no critical violations)**

**Severity: Low-Medium** (missing test coverage, unclear scenarios, uncertain implementation details)

---

## Check Results

### ✓ Test Adequacy

**Files affected:**
- `ToggleEvaluatedEventListenerTest.java` — 2 test cases
- No test for `AnalyticsController`
- No integration tests for event → persistence flow

**Status: ⚠️ WARNING (test coverage incomplete)**

- Event listener tests: ✓ (delegates to service, idempotency key handling)
- Controller tests: ✗ (MISSING — no unit or integration tests for POST /api/v1/events)
- End-to-end scenario: ⚠️ (uncertain — untested flow: event → listener → ingestion → DB write → idempotency check)
- Edge cases: ⚠️ (uncertain — no tests for duplicate event processing, malformed requests, DB constraint violations)

**Gap 1: Missing Controller Tests**

Why: AnalyticsController has no test coverage. Controller delegates to service, but:
- Request body validation untested
- Shared API object construction untested
- 202 ACCEPTED response behavior untested
- Error handling untested (malformed JSON, null fields)

What needs:
- `@WebMvcTest(AnalyticsController.class)` test suite
- Happy path: POST valid event → 202 response
- Failure paths: invalid body → 400, null subject → 400, etc.

**Gap 2: End-to-End Event Flow Not Tested**

Why: Listener → Service → Repository → DB idempotency check is untested.
- Event ID capture and storage untested
- Idempotency key duplicate detection untested
- Metadata JSONB serialization untested

What needs:
- `@SpringBootTest` integration test (or `@DataJpaTest` if `EventIngestionService` can be tested in isolation)
- Send duplicate events with same event ID → verify stored once
- Verify metadata stored correctly as JSONB

---

### ✗ Business Logic Correctness

**Status: 🚨 CRITICAL ISSUE**

**Gap 3: Idempotency Contract Unclear**

Context: `ToggleEvaluatedEventListener` passes `event.eventId()` to `ingestionService.ingest()`, but:
- The `ingestionService.ingest()` signature is NOT VISIBLE in the diff
- We don't know if it accepts `IngestionSource + eventId` OR just the individual fields
- The test mock verifies a call but doesn't verify the ID is actually persisted

**Impact: High** — Replay-safe event handling is critical for data consistency. If the event ID is not used correctly for idempotency, duplicate events will create duplicate records.

What needs:
- Show the `EventIngestionService.ingest()` method signature
- Verify it uses `eventId` as the `idempotency_key` in the database
- Test should verify: if same event ID is replayed, `analytics_events` table has only one record

**Gap 4: Unclear Relationship Between Two Ingest Methods**

Context: Two paths to ingest analytics:
1. `AnalyticsController.ingest(AnalyticsEventRequest)` — accepts REST, calls `ingestionService.ingest(FeatureKey, Variation, Subject, Metadata)`
2. `ToggleEvaluatedEventListener.onToggleEvaluated(event)` — calls `ingestionService.ingest(IngestionSource, eventId, ...)`

Problem: These call the same service with different signatures. Either:
- Two overloaded methods exist, OR
- The signatures differ and one is wrong

**Impact: Medium** — Code smell. Suggests logic duplication or unclear intent.

What needs:
- Clarify: does `EventIngestionService` have overloaded methods?
- If so, which path is canonical and why?
- If not, one of these code paths is broken.

---

### ✓ Architecture Integrity

**Status: ✓ PASS (with caveats)**

**Module structure: ✓ Correct**
- `analytics/domain` (aggregate root likely)
- `analytics/application/services` (EventIngestionService — orchestration)
- `analytics/infrastructure/api/rest` (AnalyticsController)
- `analytics/infrastructure/messaging` (ToggleEvaluatedEventListener)
- `analytics/infrastructure/repository` (not visible, but structure suggests persistence layer)

**Layering: ✓ Correct**
- Controller stays HTTP-only, delegates to service
- Listener stays messaging-only, delegates to service
- Service owns orchestration (ingest logic)

**Spring Modulith Event Pattern: ✓ Correct**
- `@ApplicationModuleListener` used (not raw `@EventListener`)
- Event publication registry table added (outbox pattern)
- Indexes designed for retry queries and idempotency

**Gap 5: Event Publication Registry Indexes**

Context: The migration adds `EVENT_PUBLICATION` table with three indexes:
- `idx_event_publication_status` — find incomplete events
- `idx_event_publication_idempotency` — prevent duplicates (LISTENER_ID, EVENT_TYPE, STATUS)
- `idx_event_publication_completion` — cleanup queries

**Concern: ⚠️ UNCERTAIN** — Composite index `(LISTENER_ID, EVENT_TYPE, STATUS)` may not match Spring Modulith's actual query patterns.

What needs:
- Verify Spring Modulith queries actually use this combination
- If queries filter by different columns (e.g., only STATUS), index may be inefficient
- Consider running query plan analysis on actual Spring Modulith retry logic

---

### ✓ Domain Understanding

**Status: ✓ PASS**

- Domain model clean (FeatureKey, Variation, Subject, Metadata as value objects)
- Aggregate structure implicit but reasonable (analytics_events as root entity)
- Service layer owns orchestration (ingest, idempotency, replay safety)
- Event-driven vs REST both supported (two ingest paths converge)

---

### ⚠️ Scenario Coverage

**Status: ⚠️ WARNING**

**Tested scenarios:**
- ✓ Happy path: listener receives event, delegates to service
- ✓ Idempotency key passed through

**Missing scenarios:**
- ⚠️ Duplicate event (same event ID) → only stored once
- ⚠️ Event arrives out of order
- ⚠️ Listener fails mid-execution → event retried
- ⚠️ Controller receives malformed JSON
- ⚠️ Database constraint (unique idempotency key) violated
- ⚠️ Metadata JSONB field populated correctly
- ⚠️ Concurrent events with same feature key (no race condition)

---

## Violations: None (Structural)

✓ No module boundary violations
✓ No architecture rule violations (architecture-guidelines.md)
✓ No transaction safety issues (no long DB txns)
✓ No dual-write issues (event-driven + outbox pattern is correct)

---

## Gaps Summary

1. **🚨 CRITICAL:** Idempotency contract unclear — event ID usage not visible in diff
2. **Medium:** Signature mismatch on `EventIngestionService.ingest()` — two callers, unclear contract
3. **Medium:** Missing controller unit tests
4. **Medium:** Missing end-to-end event → DB idempotency test
5. **Low:** Event publication indexes may not match Spring Modulith query patterns

---

## Remediation

**Recommended next steps:**

1. **Clarify idempotency contract** (blocking):
   - Show `EventIngestionService` full implementation
   - Verify event ID is stored as `idempotency_key`
   - Add test: replay same event, verify single DB record

2. **Add controller tests** (medium):
   - Valid request → 202 ACCEPTED
   - Invalid body → 400 BAD REQUEST
   - Null subject → 400 BAD REQUEST

3. **Add integration test** (medium):
   - Event → Listener → Service → DB flow
   - Duplicate detection working

4. **Verify event publication indexes** (low):
   - Check Spring Modulith query patterns
   - Confirm index design matches usage

**Next steps:**

Option A: Auto-invoke `/tlc-spec-driven remediate` with these findings
Option B: Review findings first, then decide which items are priority

→ Your choice?
