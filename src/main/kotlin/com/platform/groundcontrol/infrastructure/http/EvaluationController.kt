package com.platform.groundcontrol.infrastructure.http

import com.platform.groundcontrol.application.services.EvaluationEngineService
import com.platform.groundcontrol.application.services.FeatureFlagService
import com.platform.groundcontrol.domain.valueobjects.EvaluationContext
import com.platform.groundcontrol.domain.valueobjects.EvaluationResult
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/evaluations")
class EvaluationController(
    private val evaluationEngineService: EvaluationEngineService,
    private val featureFlagService: FeatureFlagService
) {

    @PostMapping("/{code}")
    fun evaluateFlag(
        @PathVariable code: String,
        @RequestBody context: EvaluationContext
    ): ResponseEntity<EvaluationResult> {
        return try {
            val flag = featureFlagService.getByCode(code)
            val result = evaluationEngineService.evaluate(flag, context)
            ResponseEntity.ok(result)
        } catch (e: NoSuchElementException) {
            ResponseEntity.notFound().build()
        } catch (e: Exception) {
            ResponseEntity.internalServerError().build()
        }
    }

    @PostMapping("/bulk")
    fun evaluateFlags(
        @RequestBody request: BulkEvaluationRequest
    ): ResponseEntity<Map<String, EvaluationResult>> {
        return try {
            val results = mutableMapOf<String, EvaluationResult>()
            
            for (flagCode in request.flagCodes) {
                try {
                    val flag = featureFlagService.getByCode(flagCode)
                    val result = evaluationEngineService.evaluate(flag, request.context)
                    results[flagCode] = result
                } catch (e: NoSuchElementException) {
                    continue
                }
            }
            
            ResponseEntity.ok(results)
        } catch (e: Exception) {
            ResponseEntity.internalServerError().build()
        }
    }
}

data class BulkEvaluationRequest(
    val flagCodes: List<String>,
    val context: EvaluationContext
)