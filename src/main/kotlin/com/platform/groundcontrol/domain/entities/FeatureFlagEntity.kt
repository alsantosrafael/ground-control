package com.platform.groundcontrol.domain.entities

import com.platform.groundcontrol.domain.enums.FlagType
import jakarta.persistence.CascadeType
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.FetchType
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.OneToMany
import jakarta.persistence.Table
import org.hibernate.annotations.CreationTimestamp
import org.hibernate.annotations.UpdateTimestamp
import java.time.Instant

@Entity
@Table(name = "FEATURE_FLAG")
class FeatureFlagEntity(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null,

    @Column(nullable = false, unique = true)
    var code: String = "",

    @Column(nullable = false, unique = true)
    var name: String = "",

    @Column(nullable = true)
    var description: String? = null,

    @Column(nullable = false)
    var enabled: Boolean = false,

    @Enumerated(EnumType.STRING)
    @Column(name = "flag_type", nullable = false)
    var flagType: FlagType? = FlagType.BOOLEAN,

    @Column(name = "default_bool_value")
    var defaultBoolValue: Boolean? = null,

    @Column(name = "default_int_value")
    var defaultIntValue: Int? = null,

    @Column(name = "default_string_value")
    var defaultStringValue: String? = null,

    @Column(name = "default_percentage_value")
    var defaultPercentageValue: Double? = null,

    @CreationTimestamp
    @Column(updatable = false, name = "created_at")
    var createdAt: Instant = Instant.now(),

    @UpdateTimestamp
    @Column(name = "updated_at")
    var updatedAt: Instant = Instant.now(),

    @Column(nullable = true, name = "due_at")
    var dueAt: Instant? = null,

    @OneToMany(mappedBy = "featureFlag", cascade = [CascadeType.ALL], fetch = FetchType.LAZY, orphanRemoval = true)
    var rolloutRules: MutableList<RolloutRuleEntity> = mutableListOf() // var because JPA needs to set the list and update it
) {

    override fun hashCode(): Int {
        return code.hashCode()
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as FeatureFlagEntity

        if (enabled != other.enabled) return false
        if (defaultBoolValue != other.defaultBoolValue) return false
        if (defaultIntValue != other.defaultIntValue) return false
        if (defaultPercentageValue != other.defaultPercentageValue) return false
        if (id != other.id) return false
        if (code != other.code) return false
        if (name != other.name) return false
        if (description != other.description) return false
        if (flagType != other.flagType) return false
        if (defaultStringValue != other.defaultStringValue) return false
        if (createdAt != other.createdAt) return false
        if (updatedAt != other.updatedAt) return false
        if (dueAt != other.dueAt) return false

        return true
    }
}


