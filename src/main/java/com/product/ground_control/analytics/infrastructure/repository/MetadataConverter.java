package com.product.ground_control.analytics.infrastructure.repository;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.product.ground_control.shared.api.Metadata;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Map;

/**
 * JPA converter for handling JSONB storage of analytics metadata.
 * Part of the analytics infrastructure layer.
 */
@Component
@Converter(autoApply = true)
@RequiredArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED, force = true)
public class MetadataConverter implements AttributeConverter<Metadata, String> {

    private final ObjectMapper objectMapper;

    @Override
    public String convertToDatabaseColumn(Metadata attribute) {
        if (attribute == null || attribute.isEmpty()) return "{}";
        try {
            return objectMapper.writeValueAsString(attribute.getValues());
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Error converting metadata to JSON", e);
        }
    }

    @Override
    public Metadata convertToEntityAttribute(String dbData) {
        if (dbData == null || dbData.isEmpty() || dbData.equals("{}")) return Metadata.empty();
        try {
            Map<String, Object> values = objectMapper.readValue(dbData, new TypeReference<Map<String, Object>>() {});
            return Metadata.of(values);
        } catch (IOException e) {
            throw new RuntimeException("Error converting JSON to metadata", e);
        }
    }
}
