package com.product.ground_control.toggles.domain.model;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Collections;
import java.util.List;

/**
 * JPA converter for handling JSONB storage of rule definitions.
 * Part of the toggles domain model.
 */
@Component
@Converter(autoApply = true)
@RequiredArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED, force = true)
public class ToggleRuleListConverter implements AttributeConverter<List<ToggleRuleDefinition>, String> {

    private final ObjectMapper objectMapper;

    @Override
    public String convertToDatabaseColumn(List<ToggleRuleDefinition> attribute) {
        if (attribute == null) return "[]";
        try {
            return objectMapper.writeValueAsString(attribute);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Error converting rule list to JSON", e);
        }
    }

    @Override
    public List<ToggleRuleDefinition> convertToEntityAttribute(String dbData) {
        if (dbData == null || dbData.isEmpty()) return Collections.emptyList();
        try {
            return objectMapper.readValue(dbData, new TypeReference<List<ToggleRuleDefinition>>() {});
        } catch (IOException e) {
            throw new RuntimeException("Error converting JSON to rule list", e);
        }
    }
}
