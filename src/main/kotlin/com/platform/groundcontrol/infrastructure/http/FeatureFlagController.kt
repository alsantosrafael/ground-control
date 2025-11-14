package com.platform.groundcontrol.infrastructure.http

import com.platform.groundcontrol.application.services.FeatureFlagService
import com.platform.groundcontrol.domain.valueobjects.CreateFeatureFlag
import com.platform.groundcontrol.domain.valueobjects.FeatureFlag
import com.platform.groundcontrol.domain.valueobjects.FindByCodes
import com.platform.groundcontrol.domain.valueobjects.UpdateFeatureFlag
import com.platform.groundcontrol.domain.valueobjects.UpdateFeatureFlagState
import jakarta.validation.Valid
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.web.PageableDefault
import org.springframework.http.ResponseEntity
import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PatchMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import java.net.URI

@RestController
@Validated
@RequestMapping("/v1/flags")
class FeatureFlagController(
    val featureFlagService: FeatureFlagService
) {

    @PostMapping
    fun create(@RequestBody @Valid request: CreateFeatureFlag): ResponseEntity<FeatureFlag>  {
        return ResponseEntity
            .created(URI("/v1/flags"))
            .body(featureFlagService.create(request))
    }


    @GetMapping("/by-codes")
    fun fetchAllByCodes(@RequestParam codes: List<String>): ResponseEntity<FindByCodes> {
        return ResponseEntity
            .ok(featureFlagService.getAllByCodes(codes))
    }

    @GetMapping
    fun fetchAll(@PageableDefault(size = 20, sort = ["updatedAt"]) pageable: Pageable): ResponseEntity<Page<FeatureFlag>> {
        return ResponseEntity
            .ok()
            .body(featureFlagService.getAll(pageable))
    }

    @GetMapping("/{code}")
    fun fetchByCode(@PathVariable("code") code: String): ResponseEntity<FeatureFlag> {
        return ResponseEntity
            .ok()
            .body(featureFlagService.getByCode(code))
    }

    @PutMapping("/{code}")
    fun update(@PathVariable("code") code: String, @RequestBody request: UpdateFeatureFlag ): ResponseEntity<Void> {
        featureFlagService.update(code, request)
        return ResponseEntity.noContent().build()
    }

    @PatchMapping("/{code}/change-state")
    fun updateState(@PathVariable("code") code: String, @RequestBody request: UpdateFeatureFlagState): ResponseEntity<Void> {
        featureFlagService.updateFeatureFlagStatus(code, request)
        return ResponseEntity.noContent().build()
    }
}