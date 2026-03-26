# System Architecture

## Rule Engine: Two-Stage Prioritized Cascade

Each feature evaluation follows a cascade of rules defined in the `FeatureFlag` aggregate. Each rule (`ToggleRuleDefinition`) is evaluated in two distinct stages:

### Stage 1: Targeting (Selection)
- Matches the context properties against defined conditions in `ToggleRuleCondition` (e.g., `region == 'BR'`, `tier == 'VIP'`).
- Uses the `property` field as the context key.
- All conditions must match (AND logic) for the targeting to be successful.

### Stage 2: Distribution (Rollout)
- If the Targeting Stage matches, apply the `rolloutPercentage`.
- Uses the `subject` field as the distribution key (e.g., `userId`, `podId`).
- A deterministic hash is calculated via `ToggleHasher`: `SHA-256(featureKey + ":" + context[subject])`.
- If `hash < rolloutPercentage`, the rule matches and the `result` value is returned.

**Critical Distinction**: `Distribution != FlagValueEvaluation`.
Targeting determines **who** is eligible for a rule. Distribution determines **which portion** of that eligible group receives the rule's value.

## Implementation Details
- **Aggregate Root**: `FeatureFlag` (Domain Layer).
- **Entity**: `Toggle` (Infrastructure Layer).
- **Hashing**: High-precision SHA-256 mapped to [0.0, 100.0) via `ToggleHasher`.
- **Persistence**: Rules stored as JSONB via `ToggleRuleListConverter` for schema flexibility.
- **Caching**: Caffeine L1 cache (Milestone 2).
- **Integrations**: gRPC for low-latency distribution (Milestone 2).
