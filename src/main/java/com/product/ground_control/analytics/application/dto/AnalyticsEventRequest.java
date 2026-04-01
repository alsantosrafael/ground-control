package com.product.ground_control.analytics.application.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.Map;

/**
 * Request DTO for analytics event ingestion.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AnalyticsEventRequest {

    @NotBlank
    private String featureKey;

    @NotBlank
    private String variation;

    @NotBlank
    private String subject;

    private Map<String, Object> metadata;
}
