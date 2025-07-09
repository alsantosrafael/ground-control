package com.platform.groundcontrol.infrastructure.http

import com.platform.groundcontrol.application.services.FeatureFlagService
import com.platform.groundcontrol.domain.valueobjects.CreateFeatureFlag
import com.platform.groundcontrol.domain.valueobjects.FeatureFlag
import com.platform.groundcontrol.domain.valueobjects.UpdateFeatureFlag
import com.platform.groundcontrol.domain.valueobjects.UpdateFeatureFlagState
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PatchMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import java.net.URI

@RestController
@RequestMapping("/flags")
class FeatureFlagController(
    val featureFlagService: FeatureFlagService
) {

    @PostMapping
    fun create(@RequestBody request: CreateFeatureFlag): ResponseEntity<FeatureFlag>  {
        return ResponseEntity
            .created(URI("/flags"))
            .body(featureFlagService.create(request))
    }

    @GetMapping
    fun fetchAll(): ResponseEntity<List<FeatureFlag>> {
        return ResponseEntity
            .ok()
            .body(featureFlagService.getAll())
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