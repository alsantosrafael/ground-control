package com.product.ground_control.toggles.domain.model;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import java.io.IOException;
import java.util.Collections;
import java.util.List;

/**
 * JPA converter for handling JSONB storage of rule definitions.
 * Reuses a single ObjectMapper instance for performance.
 */
@Converter(autoApply = true)
public class ToggleRuleListConverter implements AttributeConverter<List<ToggleRuleDefinition>, String> {

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper()
        .registerModule(new Jdk8Module());

    @Override
    public String convertToDatabaseColumn(List<ToggleRuleDefinition> attribute) {
        if (attribute == null) return "[]";
        try {
            return OBJECT_MAPPER.writeValueAsString(attribute);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Error converting rule list to JSON", e);
        }
    }

    @Override
    public List<ToggleRuleDefinition> convertToEntityAttribute(String dbData) {
        if (dbData == null || dbData.isEmpty()) return Collections.emptyList();
        try {
            return OBJECT_MAPPER.readValue(dbData, new TypeReference<List<ToggleRuleDefinition>>() {});
        } catch (IOException e) {
            throw new RuntimeException("Error converting JSON to rule list", e);
        }
    }
}
