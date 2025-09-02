package com.platform.groundcontrol.domain.enums

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.DisplayName
import kotlin.test.assertEquals

@DisplayName("Reason Tests")
class ReasonTest {

    @Test
    fun `should have all expected reason types`() {
        val expectedReasons = setOf(
            Reason.FLAG_DISABLED,
            Reason.FLAG_EXPIRED,
            Reason.DEFAULT,
            Reason.RULE_MATCH,
            Reason.MANUAL
        )

        val actualReasons = Reason.values().toSet()

        assertEquals(expectedReasons, actualReasons)
        assertEquals(5, Reason.values().size)
    }

    @Test
    fun `should have correct enum values`() {
        assertEquals("FLAG_DISABLED", Reason.FLAG_DISABLED.name)
        assertEquals("FLAG_EXPIRED", Reason.FLAG_EXPIRED.name)
        assertEquals("DEFAULT", Reason.DEFAULT.name)
        assertEquals("RULE_MATCH", Reason.RULE_MATCH.name)
        assertEquals("MANUAL", Reason.MANUAL.name)
    }

    @Test
    fun `should have consistent ordinal values`() {
        assertEquals(0, Reason.FLAG_DISABLED.ordinal)
        assertEquals(1, Reason.FLAG_EXPIRED.ordinal)
        assertEquals(2, Reason.DEFAULT.ordinal)
        assertEquals(3, Reason.RULE_MATCH.ordinal)
        assertEquals(4, Reason.MANUAL.ordinal)
    }

    @Test
    fun `should allow conversion from string`() {
        assertEquals(Reason.FLAG_DISABLED, Reason.valueOf("FLAG_DISABLED"))
        assertEquals(Reason.FLAG_EXPIRED, Reason.valueOf("FLAG_EXPIRED"))
        assertEquals(Reason.DEFAULT, Reason.valueOf("DEFAULT"))
        assertEquals(Reason.RULE_MATCH, Reason.valueOf("RULE_MATCH"))
        assertEquals(Reason.MANUAL, Reason.valueOf("MANUAL"))
    }

    @Test
    fun `should be usable in when expressions`() {
        val reason = Reason.RULE_MATCH

        val result = when (reason) {
            Reason.FLAG_DISABLED -> "flag is disabled"
            Reason.FLAG_EXPIRED -> "flag has expired"
            Reason.DEFAULT -> "using default value"
            Reason.RULE_MATCH -> "matched a rollout rule"
            Reason.MANUAL -> "manually set"
        }

        assertEquals("matched a rollout rule", result)
    }

    @Test
    fun `should represent evaluation outcomes correctly`() {
        // Group reasons by evaluation outcome
        val failureReasons = setOf(
            Reason.FLAG_DISABLED,
            Reason.FLAG_EXPIRED
        )

        val successReasons = setOf(
            Reason.DEFAULT,
            Reason.RULE_MATCH,
            Reason.MANUAL
        )

        // Verify categorization makes sense for flag evaluation logic
        val allReasons = failureReasons + successReasons
        assertEquals(Reason.values().toSet(), allReasons)
    }

    @Test
    fun `should support all enum operations`() {
        val values = Reason.values()
        
        // Test that all values are present
        assertEquals(5, values.size)
        
        // Test that we can find each value
        values.forEach { reason ->
            assertEquals(reason, Reason.valueOf(reason.name))
        }
        
        // Test ordinal consistency
        values.forEachIndexed { index, reason ->
            assertEquals(index, reason.ordinal)
        }
    }

    @Test
    fun `should provide semantic meaning for evaluation results`() {
        // Test that reasons correspond to evaluation engine logic
        val evaluationReasons = mapOf(
            Reason.FLAG_DISABLED to "Feature flag is globally disabled",
            Reason.FLAG_EXPIRED to "Feature flag has passed its expiration date",
            Reason.DEFAULT to "No rollout rules matched, using flag's default value",
            Reason.RULE_MATCH to "A rollout rule condition was satisfied",
            Reason.MANUAL to "Value was manually overridden"
        )

        // Verify all reasons have semantic meanings
        assertEquals(Reason.values().size, evaluationReasons.size)
        
        Reason.values().forEach { reason ->
            assertEquals(reason, evaluationReasons.keys.find { it == reason })
        }
    }
}