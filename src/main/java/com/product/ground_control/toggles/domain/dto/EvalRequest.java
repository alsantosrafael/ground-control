package com.product.ground_control.toggles.domain.dto;


import java.util.Map;

public record EvalRequest(String featureKey, Map<String, String> context) {}
