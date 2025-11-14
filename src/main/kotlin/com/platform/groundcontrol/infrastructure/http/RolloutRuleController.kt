package com.platform.groundcontrol.infrastructure.http

import com.platform.groundcontrol.application.services.RolloutRuleService
import com.platform.groundcontrol.domain.valueobjects.CreateRolloutRule
import com.platform.groundcontrol.domain.valueobjects.ReorderRolloutRules
import com.platform.groundcontrol.domain.valueobjects.RolloutRule
import com.platform.groundcontrol.domain.valueobjects.UpdateRolloutRule
import jakarta.validation.Valid
import org.springframework.http.ResponseEntity
import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import java.net.URI
import java.util.UUID

@RestController
@Validated
@RequestMapping("/v1/flags/{flagCode}/rules")
class RolloutRuleController(
    val rolloutRuleService: RolloutRuleService
) {

    @PostMapping
    fun create(
        @PathVariable("flagCode") flagCode: String,
        @RequestBody @Valid request: CreateRolloutRule
    ): ResponseEntity<RolloutRule> {
        val created = rolloutRuleService.create(flagCode, request)
        return ResponseEntity
            .created(URI("/v1/flags/$flagCode/rules/${created.id}"))
            .body(created)
    }

    @PutMapping("/{ruleId}")
    fun update(
        @PathVariable("flagCode") flagCode: String,
        @PathVariable("ruleId") ruleId: UUID,
        @RequestBody @Valid request: UpdateRolloutRule
    ): ResponseEntity<RolloutRule> {
        return ResponseEntity
            .ok()
            .body(rolloutRuleService.update(ruleId, request))
    }

    @DeleteMapping("/{ruleId}")
    fun delete(
        @PathVariable("flagCode") flagCode: String,
        @PathVariable("ruleId") ruleId: UUID
    ): ResponseEntity<Void> {
        rolloutRuleService.delete(ruleId)
        return ResponseEntity.noContent().build()
    }

    @GetMapping
    fun getByFlag(@PathVariable("flagCode") flagCode: String): ResponseEntity<List<RolloutRule>> {
        return ResponseEntity
            .ok()
            .body(rolloutRuleService.getByFlag(flagCode))
    }

    @GetMapping("/{ruleId}")
    fun getById(
        @PathVariable("flagCode") flagCode: String,
        @PathVariable("ruleId") ruleId: UUID
    ): ResponseEntity<RolloutRule> {
        return ResponseEntity
            .ok()
            .body(rolloutRuleService.getById(ruleId))
    }

    @PostMapping("/reorder")
    fun reorder(
        @PathVariable("flagCode") flagCode: String,
        @RequestBody @Valid request: ReorderRolloutRules
    ): ResponseEntity<Void> {
        rolloutRuleService.reorder(flagCode, request)
        return ResponseEntity.noContent().build()
    }
}
