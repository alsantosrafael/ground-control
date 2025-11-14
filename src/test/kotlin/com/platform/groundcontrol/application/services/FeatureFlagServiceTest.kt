package com.platform.groundcontrol.application.services

import com.platform.groundcontrol.domain.enums.FlagType
import com.platform.groundcontrol.domain.valueobjects.CreateFeatureFlag
import com.platform.groundcontrol.domain.valueobjects.FeatureFlag
import com.platform.groundcontrol.domain.valueobjects.UpdateFeatureFlag
import com.platform.groundcontrol.domain.valueobjects.UpdateFeatureFlagState
import com.platform.groundcontrol.domain.entities.FeatureFlagEntity
import com.platform.groundcontrol.infrastructure.repositories.FeatureFlagRepository
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.assertThrows
import org.mockito.Mock
import org.mockito.Mockito.*
import org.mockito.MockitoAnnotations
import org.springframework.dao.DataIntegrityViolationException
import org.springframework.orm.ObjectOptimisticLockingFailureException
import java.time.Instant
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

@DisplayName("FeatureFlagService Tests")
class FeatureFlagServiceTest {

    @Mock
    private lateinit var mockRepository: FeatureFlagRepository

    private lateinit var service: FeatureFlagService

    @BeforeEach
    fun setUp() {
        MockitoAnnotations.openMocks(this)
        service = FeatureFlagService(mockRepository)
    }

    @Nested
    @DisplayName("Create Feature Flag - Proactive Validation Tests")
    inner class CreateProactiveValidationTests {

        @Test
        fun `should create feature flag successfully when code and name are unique`() {
            val request = CreateFeatureFlag(
                code = "new-feature",
                name = "New Feature",
                description = "A new feature",
                value = true,
                valueType = FlagType.BOOLEAN,
                enabled = true,
                dueAt = null
            )

            val savedEntity = createTestEntity(
                id = 1L,
                code = request.code,
                name = request.name
            )

            `when`(mockRepository.existsByCode(request.code)).thenReturn(false)
            `when`(mockRepository.existsByName(request.name)).thenReturn(false)
            `when`(mockRepository.save(any(FeatureFlagEntity::class.java))).thenReturn(savedEntity)

            val result = service.create(request)

            assertNotNull(result)
            assertEquals(1L, result.id)
            assertEquals(request.code, result.code)
            assertEquals(request.name, result.name)

            verify(mockRepository).existsByCode(request.code)
            verify(mockRepository).existsByName(request.name)
            verify(mockRepository).save(any(FeatureFlagEntity::class.java))
        }

        @Test
        fun `should throw IllegalStateException when code already exists`() {
            val request = CreateFeatureFlag(
                code = "existing-code",
                name = "New Feature",
                description = "A new feature",
                value = true,
                valueType = FlagType.BOOLEAN,
                enabled = true,
                dueAt = null
            )

            `when`(mockRepository.existsByCode(request.code)).thenReturn(true)

            val exception = assertThrows<IllegalStateException> {
                service.create(request)
            }

            assertEquals(
                "A feature flag with code 'existing-code' already exists. Please use a different code.",
                exception.message
            )

            verify(mockRepository).existsByCode(request.code)
            verify(mockRepository, never()).existsByName(anyString())
            verify(mockRepository, never()).save(any(FeatureFlagEntity::class.java))
        }

        @Test
        fun `should throw IllegalStateException when name already exists`() {
            val request = CreateFeatureFlag(
                code = "new-code",
                name = "Existing Name",
                description = "A new feature",
                value = true,
                valueType = FlagType.BOOLEAN,
                enabled = true,
                dueAt = null
            )

            `when`(mockRepository.existsByCode(request.code)).thenReturn(false)
            `when`(mockRepository.existsByName(request.name)).thenReturn(true)

            val exception = assertThrows<IllegalStateException> {
                service.create(request)
            }

            assertEquals(
                "A feature flag with name 'Existing Name' already exists. Please use a different name.",
                exception.message
            )

            verify(mockRepository).existsByCode(request.code)
            verify(mockRepository).existsByName(request.name)
            verify(mockRepository, never()).save(any(FeatureFlagEntity::class.java))
        }

        @Test
        fun `should handle race condition when duplicate is inserted between check and save`() {
            val request = CreateFeatureFlag(
                code = "race-condition-code",
                name = "Race Condition Feature",
                description = "Testing race condition",
                value = true,
                valueType = FlagType.BOOLEAN,
                enabled = true,
                dueAt = null
            )

            // Proactive check passes (no duplicate found)
            `when`(mockRepository.existsByCode(request.code)).thenReturn(false)
            `when`(mockRepository.existsByName(request.name)).thenReturn(false)

            // But database constraint fails (duplicate was inserted by another thread)
            val dbException = DataIntegrityViolationException("Unique constraint violation")
            `when`(mockRepository.save(any(FeatureFlagEntity::class.java))).thenThrow(dbException)

            assertThrows<DataIntegrityViolationException> {
                service.create(request)
            }

            verify(mockRepository).existsByCode(request.code)
            verify(mockRepository).existsByName(request.name)
            verify(mockRepository).save(any(FeatureFlagEntity::class.java))
        }

        @Test
        fun `should validate code before name to fail fast on code duplicates`() {
            val request = CreateFeatureFlag(
                code = "duplicate-code",
                name = "Duplicate Name",
                description = "Both are duplicates",
                value = true,
                valueType = FlagType.BOOLEAN,
                enabled = true,
                dueAt = null
            )

            `when`(mockRepository.existsByCode(request.code)).thenReturn(true)
            // Name check is also true, but should never be called
            `when`(mockRepository.existsByName(request.name)).thenReturn(true)

            val exception = assertThrows<IllegalStateException> {
                service.create(request)
            }

            // Should fail on code check first
            assertEquals(
                "A feature flag with code 'duplicate-code' already exists. Please use a different code.",
                exception.message
            )

            verify(mockRepository).existsByCode(request.code)
            verify(mockRepository, never()).existsByName(anyString())
        }

        @Test
        fun `should create feature flag with special characters in code and name`() {
            val request = CreateFeatureFlag(
                code = "feature-with-dashes_and_underscores",
                name = "Feature with Spaces & Special!",
                description = "Testing special characters",
                value = "special",
                valueType = FlagType.STRING,
                enabled = true,
                dueAt = null
            )

            val savedEntity = createTestEntity(
                id = 1L,
                code = request.code,
                name = request.name,
                valueType = FlagType.STRING,
                value = "special"
            )

            `when`(mockRepository.existsByCode(request.code)).thenReturn(false)
            `when`(mockRepository.existsByName(request.name)).thenReturn(false)
            `when`(mockRepository.save(any(FeatureFlagEntity::class.java))).thenReturn(savedEntity)

            val result = service.create(request)

            assertNotNull(result)
            assertEquals(request.code, result.code)
            assertEquals(request.name, result.name)
        }

        @Test
        fun `should create feature flag with dueAt timestamp`() {
            val dueAt = Instant.now().plusSeconds(3600)
            val request = CreateFeatureFlag(
                code = "expiring-feature",
                name = "Expiring Feature",
                description = "Feature that expires",
                value = true,
                valueType = FlagType.BOOLEAN,
                enabled = true,
                dueAt = dueAt
            )

            val savedEntity = createTestEntity(
                id = 1L,
                code = request.code,
                name = request.name,
                dueAt = dueAt
            )

            `when`(mockRepository.existsByCode(request.code)).thenReturn(false)
            `when`(mockRepository.existsByName(request.name)).thenReturn(false)
            `when`(mockRepository.save(any(FeatureFlagEntity::class.java))).thenReturn(savedEntity)

            val result = service.create(request)

            assertNotNull(result)
            assertEquals(dueAt, result.dueAt)
        }
    }

    @Nested
    @DisplayName("Get Feature Flag Tests")
    inner class GetFeatureFlagTests {

        @Test
        fun `should return feature flag when code exists`() {
            val code = "existing-feature"
            val entity = createTestEntity(id = 1L, code = code, name = "Existing Feature")

            `when`(mockRepository.findByCodeWithRules(code)).thenReturn(entity)

            val result = service.getByCode(code)

            assertNotNull(result)
            assertEquals(code, result.code)

            verify(mockRepository).findByCodeWithRules(code)
        }

        @Test
        fun `should throw NoSuchElementException when code does not exist`() {
            val code = "non-existent"

            `when`(mockRepository.findByCodeWithRules(code)).thenReturn(null)

            val exception = assertThrows<NoSuchElementException> {
                service.getByCode(code)
            }

            assertEquals("Feature flag with code 'non-existent' not found", exception.message)

            verify(mockRepository).findByCodeWithRules(code)
        }

        @Test
        fun `should retrieve all feature flags by codes`() {
            val codes = listOf("feature-1", "feature-2", "feature-3")
            val entities = listOf(
                createTestEntity(id = 1L, code = "feature-1", name = "Feature 1"),
                createTestEntity(id = 2L, code = "feature-2", name = "Feature 2")
            )

            `when`(mockRepository.findByCodeInWithRules(codes)).thenReturn(entities)

            val result = service.getAllByCodes(codes)

            assertEquals(2, result.featureFlags.size)
            assertEquals(1, result.notFoundCodes.size)
            assertEquals("feature-3", result.notFoundCodes[0])

            verify(mockRepository).findByCodeInWithRules(codes)
        }
    }

    @Nested
    @DisplayName("Update Feature Flag Tests")
    inner class UpdateFeatureFlagTests {

        @Test
        fun `should update feature flag successfully`() {
            val code = "feature-to-update"
            val existingEntity = createTestEntity(
                id = 1L,
                code = code,
                name = "Old Name"
            )

            val updateRequest = UpdateFeatureFlag(
                code = "new-code",
                name = "New Name",
                description = "Updated description",
                dueAt = null
            )

            `when`(mockRepository.findByCode(code)).thenReturn(existingEntity)
            `when`(mockRepository.existsByCode("new-code")).thenReturn(false)
            `when`(mockRepository.existsByName("New Name")).thenReturn(false)
            `when`(mockRepository.save(any(FeatureFlagEntity::class.java))).thenReturn(existingEntity)

            service.update(code, updateRequest)

            verify(mockRepository).findByCode(code)
            verify(mockRepository).existsByCode("new-code")
            verify(mockRepository).existsByName("New Name")
            verify(mockRepository).save(any(FeatureFlagEntity::class.java))
        }

        @Test
        fun `should throw IllegalStateException when updating to duplicate code`() {
            val code = "original-code"
            val existingEntity = createTestEntity(
                id = 1L,
                code = code,
                name = "Original Name"
            )

            val updateRequest = UpdateFeatureFlag(
                code = "duplicate-code",
                name = "New Name",
                description = "Updated description",
                dueAt = null
            )

            `when`(mockRepository.findByCode(code)).thenReturn(existingEntity)
            `when`(mockRepository.existsByCode("duplicate-code")).thenReturn(true)

            val exception = assertThrows<IllegalStateException> {
                service.update(code, updateRequest)
            }

            assertEquals(
                "A feature flag with code 'duplicate-code' already exists. Please use a different code.",
                exception.message
            )

            verify(mockRepository).findByCode(code)
            verify(mockRepository).existsByCode("duplicate-code")
            verify(mockRepository, never()).existsByName(anyString())
            verify(mockRepository, never()).save(any(FeatureFlagEntity::class.java))
        }

        @Test
        fun `should throw IllegalStateException when updating to duplicate name`() {
            val code = "original-code"
            val existingEntity = createTestEntity(
                id = 1L,
                code = code,
                name = "Original Name"
            )

            val updateRequest = UpdateFeatureFlag(
                code = "new-code",
                name = "Duplicate Name",
                description = "Updated description",
                dueAt = null
            )

            `when`(mockRepository.findByCode(code)).thenReturn(existingEntity)
            `when`(mockRepository.existsByCode("new-code")).thenReturn(false)
            `when`(mockRepository.existsByName("Duplicate Name")).thenReturn(true)

            val exception = assertThrows<IllegalStateException> {
                service.update(code, updateRequest)
            }

            assertEquals(
                "A feature flag with name 'Duplicate Name' already exists. Please use a different name.",
                exception.message
            )

            verify(mockRepository).findByCode(code)
            verify(mockRepository).existsByCode("new-code")
            verify(mockRepository).existsByName("Duplicate Name")
            verify(mockRepository, never()).save(any(FeatureFlagEntity::class.java))
        }

        @Test
        fun `should not check for duplicates when code is not being changed`() {
            val code = "same-code"
            val existingEntity = createTestEntity(
                id = 1L,
                code = code,
                name = "Original Name"
            )

            val updateRequest = UpdateFeatureFlag(
                code = code, // Same code as original
                name = "New Name",
                description = "Updated description",
                dueAt = null
            )

            `when`(mockRepository.findByCode(code)).thenReturn(existingEntity)
            `when`(mockRepository.existsByName("New Name")).thenReturn(false)
            `when`(mockRepository.save(any(FeatureFlagEntity::class.java))).thenReturn(existingEntity)

            service.update(code, updateRequest)

            verify(mockRepository).findByCode(code)
            verify(mockRepository, never()).existsByCode(anyString()) // Should not check code
            verify(mockRepository).existsByName("New Name")
            verify(mockRepository).save(any(FeatureFlagEntity::class.java))
        }

        @Test
        fun `should not check for duplicates when name is not being changed`() {
            val code = "some-code"
            val existingEntity = createTestEntity(
                id = 1L,
                code = code,
                name = "Same Name"
            )

            val updateRequest = UpdateFeatureFlag(
                code = "new-code",
                name = "Same Name", // Same name as original
                description = "Updated description",
                dueAt = null
            )

            `when`(mockRepository.findByCode(code)).thenReturn(existingEntity)
            `when`(mockRepository.existsByCode("new-code")).thenReturn(false)
            `when`(mockRepository.save(any(FeatureFlagEntity::class.java))).thenReturn(existingEntity)

            service.update(code, updateRequest)

            verify(mockRepository).findByCode(code)
            verify(mockRepository).existsByCode("new-code")
            verify(mockRepository, never()).existsByName(anyString()) // Should not check name
            verify(mockRepository).save(any(FeatureFlagEntity::class.java))
        }

        @Test
        fun `should update only description without checking duplicates`() {
            val code = "some-code"
            val existingEntity = createTestEntity(
                id = 1L,
                code = code,
                name = "Some Name"
            )

            val updateRequest = UpdateFeatureFlag(
                code = null, // Not updating code
                name = null, // Not updating name
                description = "New description only",
                dueAt = null
            )

            `when`(mockRepository.findByCode(code)).thenReturn(existingEntity)
            `when`(mockRepository.save(any(FeatureFlagEntity::class.java))).thenReturn(existingEntity)

            service.update(code, updateRequest)

            verify(mockRepository).findByCode(code)
            verify(mockRepository, never()).existsByCode(anyString())
            verify(mockRepository, never()).existsByName(anyString())
            verify(mockRepository).save(any(FeatureFlagEntity::class.java))
        }

        @Test
        fun `should handle race condition in update when duplicate is created between check and save`() {
            val code = "original-code"
            val existingEntity = createTestEntity(
                id = 1L,
                code = code,
                name = "Original Name"
            )

            val updateRequest = UpdateFeatureFlag(
                code = "new-code",
                name = "New Name",
                description = "Updated",
                dueAt = null
            )

            `when`(mockRepository.findByCode(code)).thenReturn(existingEntity)
            `when`(mockRepository.existsByCode("new-code")).thenReturn(false)
            `when`(mockRepository.existsByName("New Name")).thenReturn(false)

            // Race condition: duplicate created by another thread between check and save
            val dbException = DataIntegrityViolationException("Unique constraint violation")
            `when`(mockRepository.save(any(FeatureFlagEntity::class.java))).thenThrow(dbException)

            assertThrows<DataIntegrityViolationException> {
                service.update(code, updateRequest)
            }

            verify(mockRepository).findByCode(code)
            verify(mockRepository).existsByCode("new-code")
            verify(mockRepository).existsByName("New Name")
            verify(mockRepository).save(any(FeatureFlagEntity::class.java))
        }

        @Test
        fun `should throw NoSuchElementException when updating non-existent flag`() {
            val code = "non-existent"
            val updateRequest = UpdateFeatureFlag(
                code = "new-code",
                name = "New Name",
                description = "Updated description",
                dueAt = null
            )

            `when`(mockRepository.findByCode(code)).thenReturn(null)

            val exception = assertThrows<NoSuchElementException> {
                service.update(code, updateRequest)
            }

            assertEquals("Feature flag with code 'non-existent' not found", exception.message)

            verify(mockRepository).findByCode(code)
            verify(mockRepository, never()).save(any(FeatureFlagEntity::class.java))
        }

        @Test
        fun `should enable feature flag`() {
            val code = "flag-to-enable"
            val existingEntity = createTestEntity(
                id = 1L,
                code = code,
                name = "Flag",
                enabled = false
            )

            val stateUpdate = UpdateFeatureFlagState(isEnabled = true)

            `when`(mockRepository.findByCode(code)).thenReturn(existingEntity)
            `when`(mockRepository.save(any(FeatureFlagEntity::class.java))).thenReturn(existingEntity)

            service.updateFeatureFlagStatus(code, stateUpdate)

            verify(mockRepository).findByCode(code)
            verify(mockRepository).save(any(FeatureFlagEntity::class.java))
        }

        @Test
        fun `should disable feature flag`() {
            val code = "flag-to-disable"
            val existingEntity = createTestEntity(
                id = 1L,
                code = code,
                name = "Flag",
                enabled = true
            )

            val stateUpdate = UpdateFeatureFlagState(isEnabled = false)

            `when`(mockRepository.findByCode(code)).thenReturn(existingEntity)
            `when`(mockRepository.save(any(FeatureFlagEntity::class.java))).thenReturn(existingEntity)

            service.updateFeatureFlagStatus(code, stateUpdate)

            verify(mockRepository).findByCode(code)
            verify(mockRepository).save(any(FeatureFlagEntity::class.java))
        }

        @Test
        fun `should throw NoSuchElementException when updating status of non-existent flag`() {
            val code = "non-existent"
            val stateUpdate = UpdateFeatureFlagState(isEnabled = true)

            `when`(mockRepository.findByCode(code)).thenReturn(null)

            val exception = assertThrows<NoSuchElementException> {
                service.updateFeatureFlagStatus(code, stateUpdate)
            }

            assertEquals("Feature flag with code 'non-existent' not found", exception.message)

            verify(mockRepository).findByCode(code)
            verify(mockRepository, never()).save(any(FeatureFlagEntity::class.java))
        }
    }

    @Nested
    @DisplayName("Edge Cases and Boundary Tests")
    inner class EdgeCasesTests {

        @Test
        fun `should handle empty code string validation at service layer`() {
            // Note: This would typically be caught by validation annotations,
            // but we test the service layer's behavior
            val request = CreateFeatureFlag(
                code = "",
                name = "Empty Code Feature",
                description = "Testing empty code",
                value = true,
                valueType = FlagType.BOOLEAN,
                enabled = true,
                dueAt = null
            )

            `when`(mockRepository.existsByCode("")).thenReturn(false)
            `when`(mockRepository.existsByName(request.name)).thenReturn(false)
            `when`(mockRepository.save(any(FeatureFlagEntity::class.java)))
                .thenReturn(createTestEntity(id = 1L, code = "", name = request.name))

            val exception = assertThrows<IllegalArgumentException> {
                service.create(request)
            }
            assertEquals(exception.message, "Feature flag code must contain only letters, numbers, hyphens, or underscores.")
        }

        @Test
        fun `should handle very long code string`() {
            val longCode = "a".repeat(255)
            val request = CreateFeatureFlag(
                code = longCode,
                name = "Long Code Feature",
                description = "Testing long code",
                value = true,
                valueType = FlagType.BOOLEAN,
                enabled = true,
                dueAt = null
            )

            `when`(mockRepository.existsByCode(longCode)).thenReturn(false)
            `when`(mockRepository.existsByName(request.name)).thenReturn(false)
            `when`(mockRepository.save(any(FeatureFlagEntity::class.java)))
                .thenReturn(createTestEntity(id = 1L, code = longCode, name = request.name))

            val exception = assertThrows<IllegalArgumentException> {
                service.create(request)
            }

            assertEquals(exception.message,  "Feature flag code cannot exceed 50 characters.")
        }

        @Test
        fun `should handle case-sensitive code duplicates`() {
            // Testing if "Feature" and "feature" are treated as different codes
            val request1 = CreateFeatureFlag(
                code = "Feature",
                name = "Feature 1",
                description = "First feature",
                value = true,
                valueType = FlagType.BOOLEAN,
                enabled = true,
                dueAt = null
            )

            val request2 = CreateFeatureFlag(
                code = "feature",
                name = "Feature 2",
                description = "Second feature",
                value = true,
                valueType = FlagType.BOOLEAN,
                enabled = true,
                dueAt = null
            )

            // First creation succeeds
            `when`(mockRepository.existsByCode("Feature")).thenReturn(false)
            `when`(mockRepository.existsByName("Feature 1")).thenReturn(false)
            `when`(mockRepository.save(any(FeatureFlagEntity::class.java)))
                .thenReturn(createTestEntity(id = 1L, code = "Feature", name = "Feature 1"))

            val result1 = service.create(request1)
            assertNotNull(result1)

            // Second creation should also succeed (case-sensitive)
            `when`(mockRepository.existsByCode("feature")).thenReturn(false)
            `when`(mockRepository.existsByName("Feature 2")).thenReturn(false)
            `when`(mockRepository.save(any(FeatureFlagEntity::class.java)))
                .thenReturn(createTestEntity(id = 2L, code = "feature", name = "Feature 2"))

            val result2 = service.create(request2)
            assertNotNull(result2)
        }

        @Test
        fun `should handle different value types correctly`() {
            val requests = listOf(
                CreateFeatureFlag("bool-flag", "Bool Flag", "Boolean", true, FlagType.BOOLEAN, true, null),
                CreateFeatureFlag("string-flag", "String Flag", "String", "test", FlagType.STRING, true, null),
                CreateFeatureFlag("int-flag", "Int Flag", "Integer", 42, FlagType.INT, true, null),
                CreateFeatureFlag("percentage-flag", "Percentage Flag", "Percentage", 75.5, FlagType.PERCENTAGE, true, null)
            )

            requests.forEachIndexed { index, request ->
                `when`(mockRepository.existsByCode(request.code)).thenReturn(false)
                `when`(mockRepository.existsByName(request.name)).thenReturn(false)
                `when`(mockRepository.save(any(FeatureFlagEntity::class.java)))
                    .thenReturn(createTestEntity(
                        id = index.toLong() + 1,
                        code = request.code,
                        name = request.name,
                        valueType = request.valueType,
                        value = request.value
                    ))
                val exception = assertThrows<IllegalArgumentException> {
                    service.create(request)
                }
                assertEquals(exception.message, "Feature flag code must contain only letters, numbers, hyphens, or underscores.")

            }
        }

        @Test
        fun `should handle null description`() {
            val request = CreateFeatureFlag(
                code = "no-description",
                name = "No Description Feature",
                description = null,
                value = true,
                valueType = FlagType.BOOLEAN,
                enabled = true,
                dueAt = null
            )

            `when`(mockRepository.existsByCode(request.code)).thenReturn(false)
            `when`(mockRepository.existsByName(request.name)).thenReturn(false)
            `when`(mockRepository.save(any(FeatureFlagEntity::class.java)))
                .thenReturn(createTestEntity(id = 1L, code = request.code, name = request.name, description = null))

            val result = service.create(request)

            assertNotNull(result)
            assertEquals(null, result.description)
        }
    }

    private fun createTestEntity(
        id: Long,
        code: String,
        name: String,
        description: String? = "Test description",
        value: Any? = true,
        valueType: FlagType = FlagType.BOOLEAN,
        enabled: Boolean = true,
        dueAt: Instant? = null
    ): FeatureFlagEntity {
        return FeatureFlagEntity(
            id = id,
            code = code,
            name = name,
            description = description,
            defaultBoolValue = if (valueType == FlagType.BOOLEAN) value as? Boolean else null,
            defaultStringValue = if (valueType == FlagType.STRING) value as? String else null,
            defaultIntValue = if (valueType == FlagType.INT) value as? Int else null,
            defaultPercentageValue = if (valueType == FlagType.PERCENTAGE) value as? Double else null,
            flagType = valueType,
            enabled = enabled,
            dueAt = dueAt
        )
    }
}
