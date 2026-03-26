package com.product.ground_control.toggles.infrastructure;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module;
import com.product.ground_control.toggles.domain.model.ToggleRuleDefinition;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import java.io.IOException;
import java.util.Collections;
import java.util.List;

@Converter(autoApply = true)
public class ToggleRuleListConverter implements AttributeConverter<List<ToggleRuleDefinition>, String> {

    private final ObjectMapper objectMapper = new ObjectMapper()
        .registerModule(new Jdk8Module());

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
