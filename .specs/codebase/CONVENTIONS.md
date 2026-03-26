# Coding Conventions

## Rule Evaluation
- **Targeting vs. Distribution**: Always separate targeting logic (context comparisons) from distribution logic (hashing). 
  - `property` refers to a context key used for comparison.
  - `subject` refers to a context key used for hashing/rollout.
- **Rollout Precision**: Use `Double` for percentages to support high-precision canary rollouts (e.g., 0.1%).
- **Hashing**: Use `ToggleHasher` (SHA-256) for all deterministic rollouts. Never use `String.hashCode()` for distribution.

## Tactical DDD
- **Entities**: Protect state. No public setters. Use semantic methods (e.g., `updateRules()`).
- **Value Objects**: Use Java Records.
- **Mappers**: Maintain explicit mappers between Infrastructure Entities and Domain Aggregates.

## Testing
- **Hashing Resiliency**: Percentage-based tests should use discovery loops to find stable test values rather than hardcoding IDs, ensuring tests pass even if the hashing algorithm changes.
