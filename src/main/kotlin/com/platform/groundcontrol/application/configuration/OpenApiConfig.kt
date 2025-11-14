package com.platform.groundcontrol.application.configuration

import io.swagger.v3.oas.models.OpenAPI
import io.swagger.v3.oas.models.info.Contact
import io.swagger.v3.oas.models.info.Info
import io.swagger.v3.oas.models.info.License
import io.swagger.v3.oas.models.servers.Server
import io.swagger.v3.oas.models.tags.Tag
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class OpenApiConfig {

    @Value("\${spring.application.name}")
    private lateinit var applicationName: String

    @Bean
    fun openAPI(): OpenAPI {
        return OpenAPI()
            .info(
                Info()
                    .title("Ground Control - Feature Flag Management API")
                    .description("""
                        **Ground Control** is a comprehensive feature flag management system that allows you to:
                        
                        - 🚩 **Manage Feature Flags**: Create, update, enable/disable feature flags
                        - 🎯 **Target Users**: Use rollout rules with conditions to target specific users or segments  
                        - 📊 **Gradual Rollouts**: Implement percentage-based gradual rollouts
                        - ⏰ **Time-based Control**: Schedule flags with start/end times
                        - 🔄 **Real-time Evaluation**: Fast flag evaluation with Redis caching
                        - 📈 **Monitoring**: Built-in metrics and observability
                        
                        ## Getting Started

                        1. **Create a feature flag** using the POST `/v1/flags` endpoint
                        2. **Evaluate the flag** using POST `/v1/evaluations/{flagCode}`
                        3. **Add rollout rules** for targeting specific users or gradual rollouts
                        4. **Monitor usage** through the `/actuator/metrics` endpoint

                        **Note**: All API endpoints are versioned with `/v1/` prefix.
                        
                        ## Flag Evaluation
                        
                        Flags are evaluated in this order:
                        1. Check if flag is enabled and not expired
                        2. Evaluate rollout rules by priority (lowest priority first)
                        3. Check rule conditions (all must match)
                        4. Check percentage rollout
                        5. Return rule value or flag default value
                        
                        ## Supported Data Types
                        
                        - **BOOLEAN**: true/false values
                        - **STRING**: Text values
                        - **NUMBER**: Numeric values (int/double)
                        - **PERCENTAGE**: Percentage values (0-100)
                        
                        ## Condition Operators
                        
                        - **String**: EQUALS, NOT_EQUALS, CONTAINS, STARTS_WITH, ENDS_WITH, REGEX_MATCH
                        - **Number**: EQUALS, NOT_EQUALS, GREATER_THAN, GREATER_EQUAL, LESS_THAN, LESS_EQUAL
                        - **Boolean**: EQUALS, NOT_EQUALS
                        - **Array**: IN, NOT_IN
                    """.trimIndent())
                    .version("1.0.0")
                    .contact(
                        Contact()
                            .name("Ground Control Team")
                            .email("support@groundcontrol.dev")
                            .url("https://github.com/your-org/groundcontrol")
                    )
                    .license(
                        License()
                            .name("MIT License")
                            .url("https://opensource.org/licenses/MIT")
                    )
            )
            .servers(
                listOf(
                    Server().url("http://localhost:8080/v1").description("Local development server (v1)"),
                    Server().url("https://api.groundcontrol.dev/v1").description("Production server (v1)")
                )
            )
            .tags(
                listOf(
                    Tag().name("Feature Flags").description("Manage feature flags"),
                    Tag().name("Rollout Rules").description("Manage rollout rules for targeted flag delivery"),
                    Tag().name("Evaluation").description("Evaluate feature flags for users"),
                    Tag().name("Health & Monitoring").description("Health checks and system metrics")
                )
            )
    }
}