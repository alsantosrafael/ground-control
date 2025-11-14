package com.platform.groundcontrol.infrastructure.http

import com.platform.groundcontrol.application.services.EvaluationEngineService
import com.platform.groundcontrol.application.services.FeatureFlagService
import com.platform.groundcontrol.domain.valueobjects.BulkEvaluationRequest
import com.platform.groundcontrol.domain.valueobjects.BulkEvaluationResponse
import com.platform.groundcontrol.domain.valueobjects.BulkEvaluationSummary
import com.platform.groundcontrol.domain.valueobjects.EvaluationContext
import com.platform.groundcontrol.domain.valueobjects.EvaluationResult
import org.slf4j.LoggerFactory
import org.slf4j.MDC
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import jakarta.servlet.http.HttpServletRequest
import jakarta.validation.Valid
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Size
import org.springframework.validation.annotation.Validated

@RestController
@Validated
@RequestMapping("/v1/evaluations")
class EvaluationController(
    private val evaluationEngineService: EvaluationEngineService,
    private val featureFlagService: FeatureFlagService
) {
    
    companion object {
        private val logger = LoggerFactory.getLogger(EvaluationController::class.java)

        // Request validation constants
        const val MAX_FLAG_CODE_LENGTH = 50
        const val MIN_BULK_FLAGS = 1
        const val MAX_BULK_FLAGS = 100
    }

    @PostMapping("/{code}")
    fun evaluateFlag(
        @PathVariable @NotBlank @Size(min = 1, max = MAX_FLAG_CODE_LENGTH) code: String,
        @RequestBody @Valid context: EvaluationContext,
        request: HttpServletRequest
    ): ResponseEntity<EvaluationResult> {
        val startTime = System.currentTimeMillis()

        MDC.put("endpoint", "POST /v1/evaluations/{code}")
        MDC.put("flagCode", code)
        MDC.put("subjectId", context.subjectId)

        try {
            logger.info("Evaluation request: method=POST, endpoint=/v1/evaluations/{}, flagCode={}, subjectId={}",
                code, code, context.subjectId)

            val flag = featureFlagService.getByCode(code)
            val result = evaluationEngineService.evaluate(flag, context)
            val duration = System.currentTimeMillis() - startTime

            logger.info("Evaluation response: flagCode={}, subjectId={}, result={}, enabled={}, reason={}, durationMs={}",
                code, context.subjectId, "SUCCESS", result.enabled, result.reason, duration)

            return ResponseEntity.ok(result)

        } finally {
            MDC.clear()
        }
    }

    @PostMapping("/bulk")
    fun evaluateFlags(
        @RequestBody @Valid request: BulkEvaluationRequest,
        httpRequest: HttpServletRequest
    ): ResponseEntity<BulkEvaluationResponse> {
        val startTime = System.currentTimeMillis()
        val clientIp = httpRequest.getHeader("X-Forwarded-For") ?: httpRequest.remoteAddr
        val userAgent = httpRequest.getHeader("User-Agent") ?: "unknown"

        MDC.put("endpoint", "POST /v1/evaluations/bulk")
        MDC.put("subjectId", request.context.subjectId)
        MDC.put("clientIp", clientIp)

        try {
            logger.info("Bulk evaluation request: flagCount={}, subjectId={}, clientIp={}, userAgent={}, flags={}",
                request.flagCodes.size, request.context.subjectId, clientIp, userAgent, request.flagCodes)

            val results = mutableMapOf<String, EvaluationResult>()
            val errors = mutableMapOf<String, String>()
            var successCount = 0
            var notFoundCount = 0
            var errorCount = 0

            for (flagCode in request.flagCodes) {
                try {
                    val flag = featureFlagService.getByCode(flagCode)
                    val result = evaluationEngineService.evaluate(flag, request.context)
                    results[flagCode] = result
                    successCount++
                } catch (e: NoSuchElementException) {
                    notFoundCount++
                    errors[flagCode] = "Flag not found"
                    logger.debug("Bulk evaluation: flag not found - flagCode={}", flagCode)
                } catch (e: Exception) {
                    errorCount++
                    errors[flagCode] = e.message ?: "Evaluation error"
                    logger.warn("Bulk evaluation: flag error - flagCode={}, error={}", flagCode, e.message)
                }
            }

            val duration = System.currentTimeMillis() - startTime
            logger.info("Bulk evaluation response: subjectId={}, requested={}, successful={}, notFound={}, errors={}, durationMs={}",
                request.context.subjectId, request.flagCodes.size, successCount, notFoundCount, errorCount, duration)

            return ResponseEntity.ok(
                BulkEvaluationResponse(
                    results = results,
                    errors = errors,
                    summary = BulkEvaluationSummary(
                        requested = request.flagCodes.size,
                        successful = successCount,
                        failed = notFoundCount + errorCount
                    )
                )
            )

        } finally {
            MDC.clear()
        }
    }
}