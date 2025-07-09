package com.platform.groundcontrol.application.configuration

import org.springdoc.core.models.GroupedOpenApi
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class OpenApiConfig {

    @Bean
    fun publicApi(): GroupedOpenApi = GroupedOpenApi.builder()
        .group("public")
        .pathsToMatch("/**")
        .build()
}