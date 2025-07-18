package com.platform.groundcontrol.domain.entities

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.FetchType
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.Table
import java.time.Instant
import java.util.UUID

@Entity
@Table(name = "ROLLOUT_RULE")
class RolloutRuleEntity (
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    val id: UUID? = null,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "feature_flag_id", nullable = false)
    val featureFlag: FeatureFlagEntity? = null,

    @Column(name = "attribute_key")
    val attributeKey: String? = null,

    @Column(name = "attribute_value")
    val attributeValue: String? = null,

    val percentage: Double? = null,

    @Column(name = "distribution_key_attribute")
    val distributionKeyAttribute: String? = null,

    @Column(name = "value_bool")
    val valueBool: Boolean? = null,

    @Column(name = "value_string")
    val valueString: String? = null,

    @Column(name = "value_int")
    val valueInt: Int? = null,

    @Column(name = "variant_name")
    val variantName: String? = null,

    @Column(name = "start_at")
    val startAt: Instant? = null,

    @Column(name = "end_at")
    val endAt: Instant? = null,

    @Column(name = "priority", nullable = false)
    val priority: Int = 0,

    @Column(name = "active", nullable = false)
    val active: Boolean = true
) {}