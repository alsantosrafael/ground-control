# System Architecture

## Rule Engine: Two-Stage Prioritized Cascade

Each feature evaluation follows a cascade of rules. Each rule is evaluated in two distinct stages:

### Stage 1: Targeting (Selection)
- Matches the context properties against defined conditions (e.g., `region == 'BR'`, `tier == 'VIP'`).
- Uses `property` as the context key.
- All conditions must match (AND logic).

### Stage 2: Distribution (Rollout)
- If the Targeting Stage matches, apply the `rolloutPercentage`.
- Uses `subject` as the distribution key (e.g., `userId`, `podId`).
- A deterministic hash is calculated: `SHA-256(featureKey + ":" + context[subject])`.
- If `hash < rolloutPercentage`, the rule matches and the `result` is returned.

**Critical Distinction**: `Distribution != FlagValueEvaluation`.
Targeting determines **who** is eligible for a rule. Distribution determines **which portion** of that eligible group receives the rule's value.

## Implementation Details
- **Hashing**: High-precision SHA-256 mapped to [0.0, 100.0).
- **Persistence**: Rules stored as JSONB for schema flexibility and high-performance retrieval.
- **Caching**: Caffeine L1 cache (Milestone 2).
- **Integrations**: gRPC for low-latency distribution (Milestone 2).
