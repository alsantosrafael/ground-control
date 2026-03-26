package com.product.ground_control.toggles.infrastructure;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.product.ground_control.toggles.application.configuration.CacheConfig;
import com.product.ground_control.toggles.domain.FeatureFlagRepository;
import com.product.ground_control.toggles.domain.FeatureType;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import com.product.ground_control.toggles.domain.entity.Toggle;
import com.product.ground_control.toggles.infrastructure.repository.JpaFeatureFlagRepository;
import com.product.ground_control.toggles.infrastructure.repository.ToggleRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = {CacheConfig.class, JpaFeatureFlagRepository.class})
@EnableCaching
class JpaFeatureFlagRepositoryTest {

    @MockitoBean
    private ToggleRepository toggleRepository;

    @Autowired
    private FeatureFlagRepository repository;

    @Autowired
    private CacheManager cacheManager;

    @BeforeEach
    void setUp() {
        cacheManager.getCache(CacheConfig.FEATURE_RULES_CACHE).clear();
    }

    @Test
    void shouldCacheFeatureFlag() {
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

        when(toggleRepository.findByKey(key)).thenReturn(Optional.of(toggle));

        // First call
        Optional<Toggle> result1 = repository.findByKey(key);
        assertEquals("false", result1.get().getDefaultValue());
        verify(toggleRepository, times(1)).findByKey(key);

        // Second call
        Optional<Toggle> result2 = repository.findByKey(key);
        assertEquals("false", result2.get().getDefaultValue());
        verify(toggleRepository, times(1)).findByKey(key);

        assertNotNull(cacheManager.getCache(CacheConfig.FEATURE_RULES_CACHE).get(key));
    }
}
