package com.product.ground_control.toggles.infrastructure;

import com.product.ground_control.toggles.domain.FeatureType;
import com.product.ground_control.toggles.domain.model.ToggleRuleDefinition;
import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "toggles")
@Getter
@Setter(AccessLevel.PRIVATE)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class Toggle {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false, unique = true)
    private String key;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private FeatureType type;

    @Convert(converter = ToggleRuleListConverter.class)
    @Column(columnDefinition = "jsonb", nullable = false)
    private List<ToggleRuleDefinition> rules;

    @Column(name = "default_value")
    private String defaultValue;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    /**
     * Constructor for creation from domain, allowing DB-generated ID.
     */
    public Toggle(String key, FeatureType type, List<ToggleRuleDefinition> rules, String defaultValue, LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.key = key;
        this.type = type;
        this.rules = rules;
        this.defaultValue = defaultValue;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    /**
     * Business method to update toggle rules.
     * Ensures consistency of updatedAt.
     */
    public void updateRules(List<ToggleRuleDefinition> newRules) {
        this.rules = newRules;
        this.updatedAt = LocalDateTime.now();
    }

    /**
     * Business method to change default value.
     */
    public void updateDefaultValue(String newDefaultValue) {
        this.defaultValue = newDefaultValue;
        this.updatedAt = LocalDateTime.now();
    }
}
