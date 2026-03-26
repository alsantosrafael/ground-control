package com.product.ground_control.toggles.infrastructure.repository;

import java.util.Optional;
import java.util.UUID;

import com.product.ground_control.toggles.domain.entity.Toggle;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ToggleRepository extends JpaRepository<Toggle, UUID> {
    Optional<Toggle> findByKey(String key);
}
