package com.platform.groundcontrol.domain.valueobjects

import com.platform.groundcontrol.infrastructure.http.EvaluationController
import jakarta.validation.Valid
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Size

data class BulkEvaluationRequest(
    @field:Size(
        min = EvaluationController.MIN_BULK_FLAGS,
        max = EvaluationController.MAX_BULK_FLAGS,
        message = "Flag codes list must contain between ${EvaluationController.MIN_BULK_FLAGS} and ${EvaluationController.MAX_BULK_FLAGS} items"
    )
    val flagCodes: List<@NotBlank @Size(min = 1, max = EvaluationController.MAX_FLAG_CODE_LENGTH) String>,
    @field:Valid
    val context: EvaluationContext
)

data class BulkEvaluationResponse(
    val results: Map<String, EvaluationResult>,
    val errors: Map<String, String>,
    val summary: BulkEvaluationSummary
)

data class BulkEvaluationSummary(
    val requested: Int,
    val successful: Int,
    val failed: Int
)