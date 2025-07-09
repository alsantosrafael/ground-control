package com.platform.groundcontrol.infrastructure.http

import com.platform.groundcontrol.application.services.FeatureFlagService
import com.platform.groundcontrol.domain.valueobjects.CreateFeatureFlag
import com.platform.groundcontrol.domain.valueobjects.FeatureFlag
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
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
            .body(featureFlagService.fetchAll())
    }
}