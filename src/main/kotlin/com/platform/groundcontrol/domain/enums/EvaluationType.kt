package com.platform.groundcontrol.domain.enums

enum class EvaluationType {
    DEFAULT,        // Valor padrão da flag
    ROLLOUT,        // Valor de uma regra de rollout
    VARIANT,        // Valor de uma variante A/B/n
    ERROR,          // Avaliação falhou
    OVERRIDE        // Valor sobrescrito manualmente (se implementar no futuro)
}