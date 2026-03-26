# Coding Conventions

## Rule Evaluation
- **Targeting vs. Distribution**: Always separate targeting logic (context comparisons) from distribution logic (hashing). 
  - `property` in `ToggleRuleCondition` refers to a context key used for comparison.
  - `subject` in `ToggleRuleDefinition` refers to a context key used for hashing/rollout.
- **Rollout Precision**: Use `Double` for `rolloutPercentage` to support high-precision canary rollouts (e.g., 0.1%).
- **Hashing**: Use `ToggleHasher` (SHA-256 with `ThreadLocal` reuse) for all deterministic rollouts. Never use `String.hashCode()` for distribution.

## Tactical DDD
- **Aggregates**: Use `FeatureFlag` (Java Record) as the root. No public setters; behavior is encapsulated in `evaluate()`.
- **Entities**: Protect state in the `Toggle` entity. No public setters. Use semantic methods (e.g., `updateRules()`, `updateDefaultValue()`).
- **Identity**: Identity generation (`UUID`) is delegated to the database via `@GeneratedValue`.
- **Mappers**: Maintain explicit mappers like `FeatureFlagMapper` between Infrastructure Entities (`Toggle`) and Domain Aggregates (`FeatureFlag`).
- **Converters**: Use `ToggleRuleListConverter` for JSONB persistence, reusing a single `ObjectMapper`.

## Testing
- **Hashing Resiliency**: Percentage-based tests in `ToggleEvaluatorTest` should use discovery loops to find stable test values rather than hardcoding IDs, ensuring tests pass even if the hashing algorithm changes.
- **Operator Validation**: Use `@ParameterizedTest` for testing new operators in `ToggleRuleCondition`.
