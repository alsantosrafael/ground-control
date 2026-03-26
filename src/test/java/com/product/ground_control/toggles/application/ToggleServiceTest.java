package com.product.ground_control.toggles.application;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

import com.product.ground_control.toggles.application.services.ToggleService;
import com.product.ground_control.toggles.domain.FeatureFlagRepository;
import com.product.ground_control.toggles.domain.FeatureType;
import com.product.ground_control.toggles.domain.entity.Toggle;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ToggleServiceTest {

    @Mock
    private FeatureFlagRepository repository;

    @InjectMocks
    private ToggleService service;

    @Test
    void shouldEvaluateFeatureFlag() {
        String key = "test_feature";
        Toggle toggle = new Toggle(
            UUID.randomUUID(),
            key,
            FeatureType.BOOLEAN,
            List.of(),
            "false",
            LocalDateTime.now(),
            LocalDateTime.now()
        );

        when(repository.findByKey(key)).thenReturn(Optional.of(toggle));

        var result = service.evaluate(key, Map.of());
        assertTrue(result.isPresent());
        assertEquals("false", result.get().value());
        assertEquals("BOOLEAN", result.get().type());
        assertEquals(key, result.get().featureKey());
    }

    @Test
    void shouldReturnEmptyWhenFeatureNotFound() {
        String key = "non_existent";
        when(repository.findByKey(key)).thenReturn(Optional.empty());

        var result = service.evaluate(key, Map.of());
        assertTrue(result.isEmpty());
    }
}
