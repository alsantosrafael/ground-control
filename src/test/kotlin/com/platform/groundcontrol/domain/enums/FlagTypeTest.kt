package com.platform.groundcontrol.domain.enums

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.DisplayName
import kotlin.test.assertEquals

@DisplayName("FlagType Tests")
class FlagTypeTest {

    @Test
    fun `should have all expected flag types`() {
        val expectedTypes = setOf(
            FlagType.INT,
            FlagType.STRING,
            FlagType.PERCENTAGE,
            FlagType.BOOLEAN
        )

        val actualTypes = FlagType.values().toSet()

        assertEquals(expectedTypes, actualTypes)
        assertEquals(4, FlagType.values().size)
    }

    @Test
    fun `should have correct enum values`() {
        assertEquals("INT", FlagType.INT.name)
        assertEquals("STRING", FlagType.STRING.name)
        assertEquals("PERCENTAGE", FlagType.PERCENTAGE.name)
        assertEquals("BOOLEAN", FlagType.BOOLEAN.name)
    }

    @Test
    fun `should have consistent ordinal values`() {
        assertEquals(0, FlagType.INT.ordinal)
        assertEquals(1, FlagType.STRING.ordinal)
        assertEquals(2, FlagType.PERCENTAGE.ordinal)
        assertEquals(3, FlagType.BOOLEAN.ordinal)
    }

    @Test
    fun `should allow conversion from string`() {
        assertEquals(FlagType.INT, FlagType.valueOf("INT"))
        assertEquals(FlagType.STRING, FlagType.valueOf("STRING"))
        assertEquals(FlagType.PERCENTAGE, FlagType.valueOf("PERCENTAGE"))
        assertEquals(FlagType.BOOLEAN, FlagType.valueOf("BOOLEAN"))
    }

    @Test
    fun `should be usable in when expressions`() {
        val flagType = FlagType.BOOLEAN

        val result = when (flagType) {
            FlagType.INT -> "integer"
            FlagType.STRING -> "string"
            FlagType.PERCENTAGE -> "percentage"
            FlagType.BOOLEAN -> "boolean"
        }

        assertEquals("boolean", result)
    }

    @Test
    fun `should support all enum operations`() {
        val values = FlagType.values()
        
        // Test that all values are present
        assertEquals(4, values.size)
        
        // Test that we can find each value
        values.forEach { type ->
            assertEquals(type, FlagType.valueOf(type.name))
        }
        
        // Test ordinal consistency
        values.forEachIndexed { index, type ->
            assertEquals(index, type.ordinal)
        }
    }
}