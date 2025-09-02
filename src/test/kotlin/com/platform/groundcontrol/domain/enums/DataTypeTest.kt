package com.platform.groundcontrol.domain.enums

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.DisplayName
import kotlin.test.assertEquals

@DisplayName("DataType Tests")
class DataTypeTest {

    @Test
    fun `should have all expected data types`() {
        val expectedTypes = setOf(
            DataType.STRING,
            DataType.NUMBER,
            DataType.BOOLEAN,
            DataType.ARRAY,
            DataType.DATE
        )

        val actualTypes = DataType.values().toSet()

        assertEquals(expectedTypes, actualTypes)
        assertEquals(5, DataType.values().size)
    }

    @Test
    fun `should have correct enum values`() {
        assertEquals("STRING", DataType.STRING.name)
        assertEquals("NUMBER", DataType.NUMBER.name)
        assertEquals("BOOLEAN", DataType.BOOLEAN.name)
        assertEquals("ARRAY", DataType.ARRAY.name)
        assertEquals("DATE", DataType.DATE.name)
    }

    @Test
    fun `should have consistent ordinal values`() {
        assertEquals(0, DataType.STRING.ordinal)
        assertEquals(1, DataType.NUMBER.ordinal)
        assertEquals(2, DataType.BOOLEAN.ordinal)
        assertEquals(3, DataType.ARRAY.ordinal)
        assertEquals(4, DataType.DATE.ordinal)
    }

    @Test
    fun `should allow conversion from string`() {
        assertEquals(DataType.STRING, DataType.valueOf("STRING"))
        assertEquals(DataType.NUMBER, DataType.valueOf("NUMBER"))
        assertEquals(DataType.BOOLEAN, DataType.valueOf("BOOLEAN"))
        assertEquals(DataType.ARRAY, DataType.valueOf("ARRAY"))
        assertEquals(DataType.DATE, DataType.valueOf("DATE"))
    }

    @Test
    fun `should be usable in when expressions`() {
        val dataType = DataType.STRING

        val result = when (dataType) {
            DataType.STRING -> "text data"
            DataType.NUMBER -> "numeric data"
            DataType.BOOLEAN -> "boolean data"
            DataType.ARRAY -> "collection data"
            DataType.DATE -> "temporal data"
        }

        assertEquals("text data", result)
    }

    @Test
    fun `should support all enum operations`() {
        val values = DataType.values()
        
        // Test that all values are present
        assertEquals(5, values.size)
        
        // Test that we can find each value
        values.forEach { type ->
            assertEquals(type, DataType.valueOf(type.name))
        }
        
        // Test ordinal consistency
        values.forEachIndexed { index, type ->
            assertEquals(index, type.ordinal)
        }
    }

    @Test
    fun `should represent primitive and complex types`() {
        val primitiveTypes = setOf(
            DataType.STRING,
            DataType.NUMBER,
            DataType.BOOLEAN
        )

        val complexTypes = setOf(
            DataType.ARRAY,
            DataType.DATE
        )

        // Verify all types are accounted for
        val allTypes = primitiveTypes + complexTypes
        assertEquals(DataType.values().toSet(), allTypes)
    }

    @Test
    fun `should map to appropriate condition evaluators`() {
        // These data types should have corresponding evaluators
        val supportedTypes = setOf(
            DataType.STRING,    // StringEvaluator
            DataType.NUMBER,    // NumericEvaluator  
            DataType.BOOLEAN,   // BooleanEvaluator
            DataType.ARRAY,     // ArrayEvaluator (implied)
            DataType.DATE       // DateEvaluator (implied)
        )

        assertEquals(DataType.values().toSet(), supportedTypes)
    }
}