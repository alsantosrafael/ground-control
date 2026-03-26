package com.product.ground_control.toggles.infrastructure;

import com.product.ground_control.toggles.domain.FeatureFlag;

/**
 * Mapper for converting between FeatureFlag domain aggregate and Toggle entity.
 */
public class FeatureFlagMapper {

    public static FeatureFlag toDomain(Toggle entity) {
        if (entity == null) return null;
        return new FeatureFlag(
            entity.getId(),
            entity.getKey(),
            entity.getType(),
            entity.getRules(),
            entity.getDefaultValue(),
            entity.getCreatedAt(),
            entity.getUpdatedAt()
        );
    }

    public static Toggle fromDomain(FeatureFlag domain) {
        if (domain == null) return null;
        if (domain.id() == null) {
            return new Toggle(
                domain.key(),
                domain.type(),
                domain.rules(),
                domain.defaultValue(),
                domain.createdAt(),
                domain.updatedAt()
            );
        }
        return new Toggle(
            domain.id(),
            domain.key(),
            domain.type(),
            domain.rules(),
            domain.defaultValue(),
            domain.createdAt(),
            domain.updatedAt()
        );
    }
}
