package com.platform.groundcontrol.domain.entities

import com.platform.groundcontrol.domain.valueobjects.Condition
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.FetchType
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.Table
import org.hibernate.annotations.JdbcTypeCode
import org.hibernate.type.SqlTypes
import org.hibernate.annotations.CreationTimestamp
import org.hibernate.annotations.UpdateTimestamp
import java.time.Instant
import java.util.UUID

@Entity
@Table(name = "ROLLOUT_RULE")
class RolloutRuleEntity (
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    var id: UUID? = null,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "feature_flag_id", nullable = false)
    var featureFlag: FeatureFlagEntity? = null,

    @Column(name = "attribute_key")
    var attributeKey: String? = null,

    @Column(name = "attribute_value")
    var attributeValue: String? = null,

    var percentage: Double? = null,

    @Column(name = "distribution_key_attribute")
    var distributionKeyAttribute: String? = null,

    @Column(name = "value_bool")
    var valueBool: Boolean? = null,

    @Column(name = "value_string")
    var valueString: String? = null,

    @Column(name = "value_int")
    var valueInt: Int? = null,

    @Column(name = "value_percentage")
    var valuePercentage: Double? = null,

    @Column(name = "variant_name")
    var variantName: String? = null,

    @Column(name = "start_at")
    var startAt: Instant? = null,

    @Column(name = "end_at")
    var endAt: Instant? = null,

    @Column(name = "priority", nullable = false)
    var priority: Int = 0,

    @Column(name = "active", nullable = false)
    var active: Boolean = true,

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "conditions")
    var conditions: MutableList<Condition> = mutableListOf(),

    @CreationTimestamp
    @Column(updatable = false, name = "created_at")
    var createdAt: Instant = Instant.now(),

    @UpdateTimestamp
    @Column(name = "updated_at")
    var updatedAt: Instant = Instant.now(),
) {}