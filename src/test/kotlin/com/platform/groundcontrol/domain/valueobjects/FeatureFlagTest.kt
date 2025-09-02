package com.platform.groundcontrol.domain.valueobjects

import com.platform.groundcontrol.domain.enums.FlagType
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.DisplayName
import java.time.Instant
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

@DisplayName("FeatureFlag Tests")
class FeatureFlagTest {

    @Nested
    @DisplayName("Validation Tests")
    inner class ValidationTests {

        @Test
        fun `should create valid feature flag with valid code and name`() {
            val flag = FeatureFlag(
                id = 1L,
                code = "valid-code_123",
                name = "Valid Name",
                description = "Test description",
                value = true,
                valueType = FlagType.BOOLEAN,
                enabled = true
            )

            assertEquals("valid-code_123", flag.code)
            assertEquals("Valid Name", flag.name)
            assertTrue(flag.enabled)
        }

        @Test
        fun `should throw exception for invalid code with spaces`() {
            assertThrows<IllegalArgumentException> {
                FeatureFlag(
                    id = 1L,
                    code = "invalid code",
                    name = "Valid Name",
                    description = null,
                    value = true,
                    valueType = FlagType.BOOLEAN,
                    enabled = true
                )
            }
        }

        @Test
        fun `should throw exception for invalid code with special characters`() {
            assertThrows<IllegalArgumentException> {
                FeatureFlag(
                    id = 1L,
                    code = "invalid@code",
                    name = "Valid Name",
                    description = null,
                    value = true,
                    valueType = FlagType.BOOLEAN,
                    enabled = true
                )
            }
        }

        @Test
        fun `should throw exception for code exceeding 50 characters`() {
            val longCode = "a".repeat(51)
            assertThrows<IllegalArgumentException> {
                FeatureFlag(
                    id = 1L,
                    code = longCode,
                    name = "Valid Name",
                    description = null,
                    value = true,
                    valueType = FlagType.BOOLEAN,
                    enabled = true
                )
            }
        }

        @Test
        fun `should throw exception for blank name`() {
            assertThrows<IllegalArgumentException> {
                FeatureFlag(
                    id = 1L,
                    code = "valid-code",
                    name = "",
                    description = null,
                    value = true,
                    valueType = FlagType.BOOLEAN,
                    enabled = true
                )
            }
        }

        @Test
        fun `should throw exception for name exceeding 100 characters`() {
            val longName = "a".repeat(101)
            assertThrows<IllegalArgumentException> {
                FeatureFlag(
                    id = 1L,
                    code = "valid-code",
                    name = longName,
                    description = null,
                    value = true,
                    valueType = FlagType.BOOLEAN,
                    enabled = true
                )
            }
        }

        @Test
        fun `should accept valid code with hyphens and underscores`() {
            val flag = FeatureFlag(
                id = 1L,
                code = "feature-flag_test-123",
                name = "Test Flag",
                description = null,
                value = true,
                valueType = FlagType.BOOLEAN,
                enabled = true
            )

            assertEquals("feature-flag_test-123", flag.code)
        }

        @Test
        fun `should accept name with exactly 100 characters`() {
            val exactName = "a".repeat(100)
            val flag = FeatureFlag(
                id = 1L,
                code = "valid-code",
                name = exactName,
                description = null,
                value = true,
                valueType = FlagType.BOOLEAN,
                enabled = true
            )

            assertEquals(exactName, flag.name)
        }
    }

    @Nested
    @DisplayName("Enable/Disable Tests")
    inner class EnableDisableTests {

        @Test
        fun `should enable disabled flag`() {
            val flag = FeatureFlag(
                id = 1L,
                code = "test-flag",
                name = "Test Flag",
                description = null,
                value = true,
                valueType = FlagType.BOOLEAN,
                enabled = false
            )
            val originalUpdatedAt = flag.updatedAt

            Thread.sleep(10) // Ensure time difference
            flag.enable()

            assertTrue(flag.enabled)
            assertTrue(flag.updatedAt.isAfter(originalUpdatedAt))
        }

        @Test
        fun `should not update timestamp when enabling already enabled flag`() {
            val flag = FeatureFlag(
                id = 1L,
                code = "test-flag",
                name = "Test Flag",
                description = null,
                value = true,
                valueType = FlagType.BOOLEAN,
                enabled = true
            )
            val originalUpdatedAt = flag.updatedAt

            flag.enable()

            assertTrue(flag.enabled)
            assertEquals(originalUpdatedAt, flag.updatedAt)
        }

        @Test
        fun `should disable enabled flag`() {
            val flag = FeatureFlag(
                id = 1L,
                code = "test-flag",
                name = "Test Flag",
                description = null,
                value = true,
                valueType = FlagType.BOOLEAN,
                enabled = true
            )
            val originalUpdatedAt = flag.updatedAt

            Thread.sleep(10) // Ensure time difference
            flag.disable()

            assertFalse(flag.enabled)
            assertTrue(flag.updatedAt.isAfter(originalUpdatedAt))
        }

        @Test
        fun `should not update timestamp when disabling already disabled flag`() {
            val flag = FeatureFlag(
                id = 1L,
                code = "test-flag",
                name = "Test Flag",
                description = null,
                value = true,
                valueType = FlagType.BOOLEAN,
                enabled = false
            )
            val originalUpdatedAt = flag.updatedAt

            flag.disable()

            assertFalse(flag.enabled)
            assertEquals(originalUpdatedAt, flag.updatedAt)
        }
    }

    @Nested
    @DisplayName("Update Details Tests")
    inner class UpdateDetailsTests {

        @Test
        fun `should update name when different`() {
            val flag = FeatureFlag(
                id = 1L,
                code = "test-flag",
                name = "Original Name",
                description = "Original Description",
                value = true,
                valueType = FlagType.BOOLEAN,
                enabled = true
            )
            val originalUpdatedAt = flag.updatedAt

            Thread.sleep(10)
            flag.updateDetails(newName = "New Name", newCode = null, newDescription = null, newDueAt = null)

            assertEquals("New Name", flag.name)
            assertEquals("test-flag", flag.code) // unchanged
            assertNull(flag.description) // changed to null
            assertTrue(flag.updatedAt.isAfter(originalUpdatedAt))
        }

        @Test
        fun `should update code when different`() {
            val flag = FeatureFlag(
                id = 1L,
                code = "original-code",
                name = "Test Name",
                description = null,
                value = true,
                valueType = FlagType.BOOLEAN,
                enabled = true
            )
            val originalUpdatedAt = flag.updatedAt

            Thread.sleep(10)
            flag.updateDetails(newName = null, newCode = "new-code", newDescription = null, newDueAt = null)

            assertEquals("new-code", flag.code)
            assertTrue(flag.updatedAt.isAfter(originalUpdatedAt))
        }

        @Test
        fun `should update description when different`() {
            val flag = FeatureFlag(
                id = 1L,
                code = "test-flag",
                name = "Test Name",
                description = "Original Description",
                value = true,
                valueType = FlagType.BOOLEAN,
                enabled = true
            )
            val originalUpdatedAt = flag.updatedAt

            Thread.sleep(10)
            flag.updateDetails(newName = null, newCode = null, newDescription = "New Description", newDueAt = null)

            assertEquals("New Description", flag.description)
            assertTrue(flag.updatedAt.isAfter(originalUpdatedAt))
        }

        @Test
        fun `should update dueAt when different`() {
            val flag = FeatureFlag(
                id = 1L,
                code = "test-flag",
                name = "Test Name",
                description = null,
                value = true,
                valueType = FlagType.BOOLEAN,
                enabled = true,
                dueAt = Instant.now()
            )
            val originalUpdatedAt = flag.updatedAt
            val newDueAt = Instant.now().plusSeconds(3600)

            Thread.sleep(10)
            flag.updateDetails(newName = null, newCode = null, newDescription = null, newDueAt = newDueAt)

            assertEquals(newDueAt, flag.dueAt)
            assertTrue(flag.updatedAt.isAfter(originalUpdatedAt))
        }

        @Test
        fun `should not update timestamp when no changes made`() {
            val flag = FeatureFlag(
                id = 1L,
                code = "test-flag",
                name = "Test Name",
                description = "Test Description",
                value = true,
                valueType = FlagType.BOOLEAN,
                enabled = true
            )
            val originalUpdatedAt = flag.updatedAt

            flag.updateDetails(
                newName = "Test Name", // same
                newCode = "test-flag", // same
                newDescription = "Test Description", // same
                newDueAt = null // same
            )

            assertEquals(originalUpdatedAt, flag.updatedAt)
        }

        @Test
        fun `should update multiple fields at once`() {
            val flag = FeatureFlag(
                id = 1L,
                code = "original-code",
                name = "Original Name",
                description = "Original Description",
                value = true,
                valueType = FlagType.BOOLEAN,
                enabled = true
            )
            val originalUpdatedAt = flag.updatedAt
            val newDueAt = Instant.now().plusSeconds(3600)

            Thread.sleep(10)
            flag.updateDetails(
                newName = "New Name",
                newCode = "new-code",
                newDescription = "New Description",
                newDueAt = newDueAt
            )

            assertEquals("New Name", flag.name)
            assertEquals("new-code", flag.code)
            assertEquals("New Description", flag.description)
            assertEquals(newDueAt, flag.dueAt)
            assertTrue(flag.updatedAt.isAfter(originalUpdatedAt))
        }
    }

    @Nested
    @DisplayName("Expiration Tests")
    inner class ExpirationTests {

        @Test
        fun `should return false when dueAt is null`() {
            val flag = FeatureFlag(
                id = 1L,
                code = "test-flag",
                name = "Test Flag",
                description = null,
                value = true,
                valueType = FlagType.BOOLEAN,
                enabled = true,
                dueAt = null
            )

            assertFalse(flag.isExpired())
        }

        @Test
        fun `should return true when dueAt is in the past`() {
            val pastInstant = Instant.now().minusSeconds(3600)
            val flag = FeatureFlag(
                id = 1L,
                code = "test-flag",
                name = "Test Flag",
                description = null,
                value = true,
                valueType = FlagType.BOOLEAN,
                enabled = true,
                dueAt = pastInstant
            )

            assertTrue(flag.isExpired())
        }

        @Test
        fun `should return false when dueAt is in the future`() {
            val futureInstant = Instant.now().plusSeconds(3600)
            val flag = FeatureFlag(
                id = 1L,
                code = "test-flag",
                name = "Test Flag",
                description = null,
                value = true,
                valueType = FlagType.BOOLEAN,
                enabled = true,
                dueAt = futureInstant
            )

            assertFalse(flag.isExpired())
        }

        @Test
        fun `should check expiration with provided time`() {
            val dueAt = Instant.parse("2023-01-01T00:00:00Z")
            val flag = FeatureFlag(
                id = 1L,
                code = "test-flag",
                name = "Test Flag",
                description = null,
                value = true,
                valueType = FlagType.BOOLEAN,
                enabled = true,
                dueAt = dueAt
            )

            val checkTime = Instant.parse("2023-01-01T00:00:01Z")
            assertTrue(flag.isExpired(checkTime))

            val earlierCheckTime = Instant.parse("2022-12-31T23:59:59Z")
            assertFalse(flag.isExpired(earlierCheckTime))
        }
    }

    @Nested
    @DisplayName("Rollout Rules Tests")
    inner class RolloutRulesTests {

        @Test
        fun `should add rollout rule and update timestamp`() {
            val flag = FeatureFlag(
                id = 1L,
                code = "test-flag",
                name = "Test Flag",
                description = null,
                value = true,
                valueType = FlagType.BOOLEAN,
                enabled = true
            )
            val originalUpdatedAt = flag.updatedAt
            val rolloutRule = RolloutRule(
                id = null,
                featureFlagId = 1L,
                attributeKey = null,
                attributeValue = null,
                percentage = 50.0,
                distributionKeyAttribute = null,
                valueBool = true,
                valueString = null,
                valueInt = null,
                valuePercentage = null,
                variantName = "test-variant"
            )

            Thread.sleep(10)
            flag.addRolloutRule(rolloutRule)

            assertEquals(1, flag.rolloutRules.size)
            assertEquals(rolloutRule, flag.rolloutRules[0])
            assertTrue(flag.updatedAt.isAfter(originalUpdatedAt))
        }

        @Test
        fun `should remove rollout rule and update timestamp`() {
            val rolloutRule = RolloutRule(
                id = null,
                featureFlagId = 1L,
                attributeKey = null,
                attributeValue = null,
                percentage = 50.0,
                distributionKeyAttribute = null,
                valueBool = true,
                valueString = null,
                valueInt = null,
                valuePercentage = null,
                variantName = "test-variant"
            )
            val flag = FeatureFlag(
                id = 1L,
                code = "test-flag",
                name = "Test Flag",
                description = null,
                value = true,
                valueType = FlagType.BOOLEAN,
                enabled = true,
                rolloutRules = mutableListOf(rolloutRule)
            )
            val originalUpdatedAt = flag.updatedAt

            Thread.sleep(10)
            flag.removeRolloutRule(rolloutRule)

            assertEquals(0, flag.rolloutRules.size)
            assertTrue(flag.updatedAt.isAfter(originalUpdatedAt))
        }

        @Test
        fun `should handle multiple rollout rules`() {
            val flag = FeatureFlag(
                id = 1L,
                code = "test-flag",
                name = "Test Flag",
                description = null,
                value = true,
                valueType = FlagType.BOOLEAN,
                enabled = true
            )

            val rule1 = RolloutRule(
                id = null,
                featureFlagId = 1L,
                attributeKey = null,
                attributeValue = null,
                percentage = 25.0,
                distributionKeyAttribute = null,
                valueBool = true,
                valueString = null,
                valueInt = null,
                valuePercentage = null,
                variantName = "variant-1"
            )

            val rule2 = RolloutRule(
                id = null,
                featureFlagId = 1L,
                attributeKey = null,
                attributeValue = null,
                percentage = 75.0,
                distributionKeyAttribute = null,
                valueBool = false,
                valueString = null,
                valueInt = null,
                valuePercentage = null,
                variantName = "variant-2"
            )

            flag.addRolloutRule(rule1)
            flag.addRolloutRule(rule2)

            assertEquals(2, flag.rolloutRules.size)
            assertTrue(flag.rolloutRules.contains(rule1))
            assertTrue(flag.rolloutRules.contains(rule2))
        }
    }

    @Nested
    @DisplayName("Different Value Types Tests")
    inner class ValueTypesTests {

        @Test
        fun `should handle boolean flag type`() {
            val flag = FeatureFlag(
                id = 1L,
                code = "boolean-flag",
                name = "Boolean Flag",
                description = null,
                value = true,
                valueType = FlagType.BOOLEAN,
                enabled = true
            )

            assertEquals(FlagType.BOOLEAN, flag.valueType)
            assertEquals(true, flag.value)
        }

        @Test
        fun `should handle string flag type`() {
            val flag = FeatureFlag(
                id = 1L,
                code = "string-flag",
                name = "String Flag",
                description = null,
                value = "test-value",
                valueType = FlagType.STRING,
                enabled = true
            )

            assertEquals(FlagType.STRING, flag.valueType)
            assertEquals("test-value", flag.value)
        }

        @Test
        fun `should handle int flag type`() {
            val flag = FeatureFlag(
                id = 1L,
                code = "int-flag",
                name = "Int Flag",
                description = null,
                value = 42,
                valueType = FlagType.INT,
                enabled = true
            )

            assertEquals(FlagType.INT, flag.valueType)
            assertEquals(42, flag.value)
        }

        @Test
        fun `should handle percentage flag type`() {
            val flag = FeatureFlag(
                id = 1L,
                code = "percentage-flag",
                name = "Percentage Flag",
                description = null,
                value = 75.5,
                valueType = FlagType.PERCENTAGE,
                enabled = true
            )

            assertEquals(FlagType.PERCENTAGE, flag.valueType)
            assertEquals(75.5, flag.value)
        }
    }
}