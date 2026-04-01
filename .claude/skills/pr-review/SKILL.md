---
name: pr-review
description: "PR reviewer for Ground Control Java + Spring Boot + Spring Modulith changes. Use ONLY when explicitly asked to review a pull request: 'review PR #N', 'review this PR', 'code review', or 'check this pull request'. Do NOT trigger automatically during implementation or general questions."
license: CC-BY-4.0
metadata:
  author: GroundControl Team
  version: 2.0.0
---

# PR Review — Ground Control Java/Spring Protocol

Review pull requests against Ground Control's verified stack and architecture:
- Java 21
- Spring Boot Web MVC
- Spring Modulith
- Gradle build
- JUnit 5 / Spring Boot test stack

This skill does not invent architecture rules. It applies the existing project guidance from:
- `.agents/rules/architecture-guidelines.md`
- `.agents/skills/modular-architecture.md`
- `docs/CODING-PATTERNS.md`
- `docs/integration-patterns.md`
- `docs/domain-folder-guidelines.md`
- `docs/tactical-ddd-guidance.md`

## Step 1: Initialize

1. Get PR number from context or ask the user.
2. Identify repo: `gh repo view --json nameWithOwner -q .nameWithOwner`
3. Fetch diff: `gh pr diff {PR_NUMBER}`
4. Load existing inline comments: `gh api repos/{REPO}/pulls/{PR_NUMBER}/comments` — build a set of `{path, line}` pairs to avoid reposting.
5. Read PR intent: `gh pr view {PR_NUMBER} --json title,body,headRefName`
6. Check for a linked Jira ticket in the branch name (pattern `[A-Z]+-[0-9]+`) only if requirements validation is needed and credentials are available.

## Step 2: Review Dimensions

Review across the six dimensions below. If specialized subagents are available, they may run in parallel. Otherwise, review them sequentially and produce one consolidated result.

---

## Severity Labels

- 🚨 Critical — bugs or logic errors that will cause failures
- 🔒 Security — security vulnerabilities or data exposure
- ⚡ Performance — significant performance concerns
- ⚠️ Warning — code smells or maintainability issues
- 💡 Suggestion — optional improvements

---

## Universal Rules (every subagent must follow)

1. **Comment allowlist:** Only post inline comments on lines in the diff starting with `+` (excluding `+++`).
2. **Skip duplicates:** If `{path, line}` within ±3 lines already has a comment, skip.
3. **Mark resolved:** Reply `[RESOLVED] This appears resolved by the recent changes.` on existing comments where the issue is fixed.
4. **False positive guard:** Only report findings with ≥80% confidence. Skip when uncertain.
5. **Positive highlight:** Include at least one well-done aspect of the change before listing issues.
6. **Tone:** Specific, actionable, collegial. Explain WHY something is a problem.
7. **Never** approve, request-changes, or modify files. Use `--comment` only.
8. **Marker:** Start every inline comment body with `<!-- cursor-review:{type} -->` (invisible in rendered view, used for consolidation).
9. **Scope guard:** Only flag issues clearly visible in the diff or directly inferable from changed code and nearby context.
10. **Project reality guard:** Do not require infrastructure that is not declared in this repository unless the PR itself introduces it.

---

## Review Dimension 1: Security

**Marker:** `<!-- cursor-review:security -->`
**Cap:** 5 inline comments

Load `docs/integration-patterns.md` and `docs/CODING-PATTERNS.md`.

Review for:
- hardcoded secrets, tokens, passwords, or API keys
- PII or sensitive fields in logs, exceptions, or DTOs
- missing authentication/authorization checks when protected behavior is added
- missing webhook signature validation or idempotency handling
- unsafe raw SQL/query concatenation
- overly permissive CORS or debug endpoints accidentally exposed
- leaking internal entities directly through controller responses
- exposing internal module clients/contracts across boundaries unnecessarily

**Comment format:**
```
<!-- cursor-review:security -->
🔒 Security — [Short title]
[What the issue is and why it matters]
**Recommendation:** [Specific fix]
```

---

## Subagent 2: Jira Requirements & Definition of Done

**Marker:** `<!-- cursor-review:jira -->`
**Posts:** One PR-level summary comment only — no inline comments.

1. Extract ticket ID from branch name (pattern `[A-Z]+-[0-9]+`). If none, post: "⚠️ No Jira ticket found — requirements verification skipped." and stop.
2. Fetch: `curl -su "$JIRA_USER:$JIRA_API_TOKEN" "$JIRA_BASE_URL/rest/api/2/issue/$TICKET_ID?fields=summary,description"`
3. Parse for acceptance criteria, user stories, and DoD checklist items.
4. Compare against the PR diff and post summary with `gh pr comment {PR_NUMBER} --body '...'`

**Summary format:**
```markdown
## Review Dimension 2: Requirements & Scope
## 📋 Requirements Review: {TICKET_ID}
**Marker:** `<!-- cursor-review:requirements -->`
**Posts:** One PR-level summary comment only — no inline comments.
### ❌ Missing or Incomplete
If Jira access is available:
1. Extract ticket ID from branch name (pattern `[A-Z]+-[0-9]+`).
2. Fetch ticket details from Jira.
3. Compare acceptance criteria and DoD against the PR diff.

If Jira access is NOT available:
- compare the PR title/body against the actual diff
- flag scope drift, missing user-visible behavior, or hidden extra changes
- state clearly that requirements validation was limited to PR metadata

---

<!-- cursor-review:requirements -->
## 📋 Requirements Review
**Marker:** `<!-- cursor-review:e2e -->`
**Cap:** 5 inline comments

### 🔲 Definition of Done / Scope
### 💬 Notes

If Jira was checked, include `{TICKET_ID}` in the heading.
If not, say: `⚠️ Jira not available — reviewed against PR title/body only.`
```

---

## Review Dimension 3: Tests

**Marker:** `<!-- cursor-review:tests -->`
**Cap:** 5 inline comments

Load `docs/CODING-PATTERNS.md`.

Review for:
- missing tests for newly added business logic, controllers, integrations, or regressions
- wrong Spring test scope (`@SpringBootTest` used when `@WebMvcTest` / `@DataJpaTest` would be enough, or vice versa)
- weak assertions that only check status but not response body or side effects
- over-mocking, unused stubs, blanket `any()` usage hiding broken behavior
- missing failure-path tests for validation, exceptions, retries, or boundary conditions
- tests that couple to module internals instead of public behavior

Prefer Spring/JUnit-native guidance:
- controller tests: `@WebMvcTest` + MockMvc when appropriate
- persistence tests: `@DataJpaTest`
- integration tests: `@SpringBootTest` only when full context is needed

**Comment format:**
```
<!-- cursor-review:tests -->
[🚨/⚠️/💡] — [Short title]
[Description of the gap or anti-pattern]
**Recommendation:** [Specific Spring/JUnit test pattern to follow]
**Comment format:**
```
<!-- cursor-review:e2e -->
[🚨/⚠️/💡] — [Short title]
## Review Dimension 4: Architecture & Coding Patterns
[What in the diff violates it]
**Recommendation:** [Exact fix, code snippet if < 6 lines]
```

Load:
- `.agents/rules/architecture-guidelines.md`
- `.agents/skills/modular-architecture.md`
- `docs/CODING-PATTERNS.md`
- `docs/domain-folder-guidelines.md`
- `docs/tactical-ddd-guidance.md`

Review for:
- module boundary violations or cross-module repository/entity access
- wrong placement across `domain`, `application`, `infrastructure`
- fat controllers or repositories injected directly into controllers
- domain logic leaking into controllers/listeners/persistence adapters
- ORM leakage into services (`EntityManager`, JPQL, criteria, persistence details)
- missing or misplaced `@Transactional` boundaries on write orchestration
- entities/DB objects without module-aware naming when modules are present
- shared-kernel abuse (`shared` becoming a dump folder)
- public API leakage of internal services instead of clear module contracts
- wildcard imports, bad package naming, or style drift that harms maintainability

## Subagent 5: Regression & Hallucination Detection

**Marker:** `<!-- cursor-review:regression -->`
**Cap:** 5 inline comments

[What in the diff violates it and why it matters]

**Comment format:**
```
<!-- cursor-review:regression -->
[🚨/⚠️/💡] — [Short title]
## Review Dimension 5: Regression & Hallucination Detection
[Specific description with quoted evidence from the diff]
**Recommendation:** [Exact fix]
```

Review the PR diff for code changes unrelated to the stated purpose or showing AI-generated artifacts. Look for:
- deleted or modified code unrelated to the PR intent (🚨 Critical)
- phantom imports or references to non-existent classes/methods (🚨 Critical)
- wrong Spring annotations or incompatible combinations
- copy-pasted code that duplicates existing behavior in the same module
- `TODO`/`FIXME` left in production paths without issue tracking
- swallowed exceptions, weakened validation, or removed guards
- dead code, unused beans, unreachable branches, or unused DTOs created by the PR
- changes that contradict the actual repo stack (e.g. Node/Nest/TypeORM idioms appearing in Java code)

## Subagent 6: Performance

**Marker:** `<!-- cursor-review:performance -->`
**Cap:** 3 inline comments
Type: [unrelated-change | phantom-symbol | hallucination | duplicate | regression | dead-code]
Load `docs/coding-patterns.md` (Repository Pattern and Transaction Management sections). Only flag issues **clearly visible in the diff** — no speculation. Look for: N+1 query patterns (repository lookup inside a loop), unbounded `find()` with no pagination, missing `relations` causing lazy-load N+1, sequential `await` for independent operations that could use `Promise.all`, and multiple `repository.save()` calls without `@Transactional`.

**Comment format:**
```
<!-- cursor-review:performance -->
⚡ Performance — [Short title]
## Review Dimension 6: Performance
**Recommendation:** [Fix with short code sketch if < 6 lines]
```

---
Load `docs/CODING-PATTERNS.md` and `docs/integration-patterns.md`. Only flag issues **clearly visible in the diff**.

Look for:
- repository/database lookup inside loops (N+1)
- unbounded reads where pagination/limits are clearly needed
- repeated `save()` / flush operations in a write flow without an explicit transaction
- long-running HTTP/external client calls inside open transactions
- sequential remote calls that could be batched or reduced
- unnecessary object mapping/conversion in tight loops when visible in the diff
- expensive logging or serialization on hot paths
## Step 3: Consolidation

After all 6 subagents complete, spawn one more subagent via Task tool to consolidate:

1. `gh api repos/{REPO}/pulls/{PR_NUMBER}/comments` — fetch all inline comments.
2. Filter to those starting with `<!-- cursor-review: -->` and parse the type from the marker.
3. Fetch PR-level comments for the `<!-- cursor-review:jira -->` summary.
4. Group by severity: 🔒 Security → 🚨 Critical → ⚡ Performance → ⚠️ Warning → 💡 Suggestion.
5. Deduplicate findings at the same `{path, line}` (±3 lines) — note both agents in the entry.
6. Collect one positive highlight per agent.
7. Post: `gh pr review {PR_NUMBER} --comment --body '...'`

**Summary format:**
```markdown
## 🤖 Cursor AI Review Summary

After reviewing all 6 dimensions, consolidate into one PR-level summary comment or review comment.
|---|---|
| **Subagents invoked** | {N} of 6 (Security · Requirements · E2E Coverage · Architecture · Regression · Performance) |
2. Filter to those starting with `<!-- cursor-review:` and parse the type from the marker.
3. Fetch any PR-level requirements review comment if one was posted.
4. Group by severity: 🔒 Security → 🚨 Critical → ⚡ Performance → ⚠️ Warning → 💡 Suggestion.

6. Collect one positive highlight per review dimension.

### 🔒 Security ({N})
- [`path/file.ts:L42`] Finding title

## 🤖 Ground Control AI Review Summary
### ⚡ Performance ({N})
### ⚠️ Warnings ({N})
### 💡 Suggestions ({N})
| **Review dimensions** | 6 (Security · Requirements · Tests · Architecture · Regression · Performance) |
| **Skills loaded** | `.agents/skills/pr-review/SKILL.md`, `.agents/skills/modular-architecture.md` |
| **Docs loaded** | `.agents/rules/architecture-guidelines.md`, `docs/CODING-PATTERNS.md`, `docs/integration-patterns.md`, `docs/domain-folder-guidelines.md`, `docs/tactical-ddd-guidance.md` |
- [One positive highlight per agent]

---
> See inline comments for details and recommendations.
```
- [`src/main/java/.../MyService.java:L42`] Finding title
If no findings across all agents: post `✅ No issues found across all review dimensions.` but still include the metadata table.

## Java + Spring Boot Review Heuristics

Use these as concrete reminders while reviewing:

- **Controllers** should stay HTTP-only and delegate to services.
- **Services** should own orchestration and explicit transactions.
- **Repositories** should encapsulate JPA details; services should not build queries.
- **DTOs** should stay separate from entities.
- **Spring Modulith** boundaries should prefer API packages and events over direct internal access.
- **Outbound integrations** should stay inside dedicated clients/adapters with timeouts, retries, and error translation.
- **Tests** should prefer the smallest Spring scope needed and avoid over-mocking.

## Things to Avoid Flagging Without Strong Evidence

- missing infrastructure that the repository does not currently declare
- speculative scalability issues not visible in the diff
- requests for architecture patterns that conflict with current verified project state
- style-only nits unless they violate documented coding patterns or harm readability materially

