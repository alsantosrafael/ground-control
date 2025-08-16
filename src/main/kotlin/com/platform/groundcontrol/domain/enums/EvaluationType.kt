package com.platform.groundcontrol.domain.enums

enum class EvaluationType {
    DEFAULT,        // Standard value of flag
    ROLLOUT,        // Rollout pattern rule
    VARIANT,        // Multiple variants rule (A/B)
    ERROR,          // Failed Evaluation
    OVERRIDE        // Value set manually
}