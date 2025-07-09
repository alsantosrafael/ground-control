package com.platform.groundcontrol.infrastructure.entities

import com.platform.groundcontrol.domain.valueobjects.FeatureFlag
import com.platform.groundcontrol.domain.valueobjects.FeatureFlagCode
import com.platform.groundcontrol.domain.valueobjects.FeatureFlagId
import com.platform.groundcontrol.domain.valueobjects.FeatureFlagName
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.Table
import org.hibernate.annotations.CreationTimestamp
import org.hibernate.annotations.UpdateTimestamp
import java.time.Instant
import java.util.UUID

@Entity
@Table(name = "FEATURE_FLAG")
class FeatureFlagEntity(
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    var id: UUID? = null,
    @Column(nullable = false, unique = true)
    var code: String = "",
    @Column(nullable = false, unique = true)
    var name: String = "",
    @Column(nullable = true)
    var description: String? = null,
    @Column(nullable = false)
    var isEnabled: Boolean = false,
    @Column(nullable = true)
    var previousEnabledState: Boolean? = null,
    @CreationTimestamp
    @Column(updatable = false)
    var createdAt: Instant = Instant.now(),
    @UpdateTimestamp
    var updatedAt: Instant = Instant.now(),
    @Column(nullable = true)
    var dueAt: Instant? = null
) {


    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as FeatureFlagEntity

        if (isEnabled != other.isEnabled) return false
        if (previousEnabledState != other.previousEnabledState) return false
        if (id != other.id) return false
        if (name != other.name) return false
        if (code != other.code) return false
        if (description != other.description) return false
        if (createdAt != other.createdAt) return false
        if (updatedAt != other.updatedAt) return false
        if (dueAt != other.dueAt) return false

        return true
    }

    override fun hashCode(): Int {
        return code.hashCode()
    }

}

fun FeatureFlagEntity.toDomain(): FeatureFlag {
    return FeatureFlag(
        id = FeatureFlagId(this.id!!),
        code = FeatureFlagCode(this.name),
        name = FeatureFlagName(this.name),
        description = this.description,
        initialEnabled = this.isEnabled,
        dueAt = this.dueAt
    ).apply {

        if (this@toDomain.previousEnabledState != null &&
            this@toDomain.previousEnabledState != this@toDomain.isEnabled) {

            this.previousEnabledState = this@toDomain.previousEnabledState!!

            // Then apply the current state
            if (this@toDomain.isEnabled) {
                this.enable()
            } else {
                this.disable()
            }
        }

        this.updatedAt = this@toDomain.updatedAt
    }
}
