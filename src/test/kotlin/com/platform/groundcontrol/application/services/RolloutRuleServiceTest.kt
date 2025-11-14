package com.platform.groundcontrol.application.services

import com.platform.groundcontrol.domain.entities.FeatureFlagEntity
import com.platform.groundcontrol.domain.entities.RolloutRuleEntity
import com.platform.groundcontrol.domain.enums.DataType
import com.platform.groundcontrol.domain.enums.FlagType
import com.platform.groundcontrol.domain.enums.Operator
import com.platform.groundcontrol.domain.valueobjects.*
import com.platform.groundcontrol.infrastructure.repositories.FeatureFlagRepository
import com.platform.groundcontrol.infrastructure.repositories.RolloutRuleRepository
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.assertThrows
import org.mockito.Mock
import org.mockito.Mockito.*
import org.mockito.MockitoAnnotations
import org.mockito.ArgumentMatchers.anyList
import org.mockito.ArgumentMatchers.anyLong
import org.springframework.dao.DataIntegrityViolationException
import java.time.Instant
import java.util.Optional
import java.util.UUID
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

fun <T> anyNonNull(): T = any() ?: throw IllegalStateException("any() returned null")

@DisplayName("RolloutRuleService Tests")
class RolloutRuleServiceTest {

    @Mock
    private lateinit var mockRolloutRuleRepository: RolloutRuleRepository

    @Mock
    private lateinit var mockFeatureFlagRepository: FeatureFlagRepository

    private lateinit var service: RolloutRuleService

    @BeforeEach
    fun setUp() {
        MockitoAnnotations.openMocks(this)
        service = RolloutRuleService(mockRolloutRuleRepository, mockFeatureFlagRepository)
    }

    @Nested
    @DisplayName("Create RolloutRule Tests")
    inner class CreateRolloutRuleTests {

        @Test
        fun `should create rollout rule successfully`() {
            val flagCode = "test-flag"
            val flag = createTestFlag(id = 1L, code = flagCode)
            val request = CreateRolloutRule(
                percentage = 50.0,
                priority = 1,
                active = true,
                variantName = "variant-a"
            )

            val savedEntity = createTestRolloutRuleEntity(
                id = UUID.randomUUID(),
                featureFlagId = 1L,
                percentage = 50.0,
                priority = 1
            )

            `when`(mockFeatureFlagRepository.findByCode(flagCode)).thenReturn(flag)
            `when`(mockRolloutRuleRepository.save(any(RolloutRuleEntity::class.java))).thenReturn(savedEntity)

            val result = service.create(flagCode, request)

            assertNotNull(result)
            assertEquals(50.0, result.percentage)
            assertEquals(1, result.priority)
            assertEquals(true, result.active)

            verify(mockFeatureFlagRepository).findByCode(flagCode)
            verify(mockRolloutRuleRepository).save(any(RolloutRuleEntity::class.java))
        }

        @Test
        fun `should throw NoSuchElementException when flag does not exist`() {
            val flagCode = "non-existent-flag"
            val request = CreateRolloutRule(percentage = 50.0)

            `when`(mockFeatureFlagRepository.findByCode(flagCode)).thenReturn(null)

            val exception = assertThrows<NoSuchElementException> {
                service.create(flagCode, request)
            }

            assertEquals("Feature flag with code 'non-existent-flag' not found", exception.message)

            verify(mockFeatureFlagRepository).findByCode(flagCode)
            verify(mockRolloutRuleRepository, never()).save(any(RolloutRuleEntity::class.java))
        }

        @Test
        fun `should create rule with all fields populated`() {
            val flagCode = "test-flag"
            val flag = createTestFlag(id = 1L, code = flagCode)
            val startAt = Instant.now()
            val endAt = startAt.plusSeconds(3600)

            val request = CreateRolloutRule(
                percentage = 75.0,
                priority = 5,
                active = false,
                attributeKey = "plan",
                attributeValue = "premium",
                distributionKeyAttribute = "userId",
                valueBool = true,
                variantName = "premium-variant",
                startAt = startAt,
                endAt = endAt,
                conditions = listOf(Condition("age", Operator.GREATER_THAN, 18, DataType.NUMBER))
            )

            val savedEntity = createTestRolloutRuleEntity(
                id = UUID.randomUUID(),
                featureFlagId = 1L,
                percentage = 75.0,
                priority = 5
            )

            `when`(mockFeatureFlagRepository.findByCode(flagCode)).thenReturn(flag)
            `when`(mockRolloutRuleRepository.save(any(RolloutRuleEntity::class.java))).thenReturn(savedEntity)

            val result = service.create(flagCode, request)

            assertNotNull(result)
            verify(mockRolloutRuleRepository).save(any(RolloutRuleEntity::class.java))
        }

        @Test
        fun `should create rule with minimal fields using defaults`() {
            val flagCode = "test-flag"
            val flag = createTestFlag(id = 1L, code = flagCode)
            val request = CreateRolloutRule()

            val savedEntity = createTestRolloutRuleEntity(
                id = UUID.randomUUID(),
                featureFlagId = 1L
            )

            `when`(mockFeatureFlagRepository.findByCode(flagCode)).thenReturn(flag)
            `when`(mockRolloutRuleRepository.save(any(RolloutRuleEntity::class.java))).thenReturn(savedEntity)

            val result = service.create(flagCode, request)

            assertNotNull(result)
            assertEquals(0, result.priority)
            assertEquals(true, result.active)

            verify(mockRolloutRuleRepository).save(any(RolloutRuleEntity::class.java))
        }

        @Test
        fun `should handle race condition when flag is deleted between check and save`() {
            val flagCode = "test-flag"
            val flag = createTestFlag(id = 1L, code = flagCode)
            val request = CreateRolloutRule(percentage = 50.0)

            `when`(mockFeatureFlagRepository.findByCode(flagCode)).thenReturn(flag)

            val dbException = DataIntegrityViolationException("Foreign key constraint violation")
            `when`(mockRolloutRuleRepository.save(any(RolloutRuleEntity::class.java))).thenThrow(dbException)

            assertThrows<DataIntegrityViolationException> {
                service.create(flagCode, request)
            }

            verify(mockFeatureFlagRepository).findByCode(flagCode)
            verify(mockRolloutRuleRepository).save(any(RolloutRuleEntity::class.java))
        }
    }

    @Nested
    @DisplayName("Update RolloutRule Tests")
    inner class UpdateRolloutRuleTests {

        @Test
        fun `should update rollout rule successfully`() {
            val ruleId = UUID.randomUUID()
            val existingEntity = createTestRolloutRuleEntity(
                id = ruleId,
                featureFlagId = 1L,
                percentage = 50.0,
                priority = 1
            )

            val request = UpdateRolloutRule(
                percentage = 75.0,
                priority = 2,
                active = false
            )

            `when`(mockRolloutRuleRepository.findById(ruleId)).thenReturn(Optional.of(existingEntity))
            `when`(mockRolloutRuleRepository.save(any(RolloutRuleEntity::class.java))).thenReturn(existingEntity)

            val result = service.update(ruleId, request)

            assertNotNull(result)

            verify(mockRolloutRuleRepository).findById(ruleId)
            verify(mockRolloutRuleRepository).save(any(RolloutRuleEntity::class.java))
        }

        @Test
        fun `should throw NoSuchElementException when rule does not exist`() {
            val ruleId = UUID.randomUUID()
            val request = UpdateRolloutRule(percentage = 75.0)

            `when`(mockRolloutRuleRepository.findById(ruleId)).thenReturn(Optional.empty())

            val exception = assertThrows<NoSuchElementException> {
                service.update(ruleId, request)
            }

            assertEquals("Rollout rule with id '$ruleId' not found", exception.message)

            verify(mockRolloutRuleRepository).findById(ruleId)
            verify(mockRolloutRuleRepository, never()).save(any(RolloutRuleEntity::class.java))
        }

        @Test
        fun `should update only specified fields`() {
            val ruleId = UUID.randomUUID()
            val existingEntity = createTestRolloutRuleEntity(
                id = ruleId,
                featureFlagId = 1L,
                percentage = 50.0,
                priority = 1,
                active = true
            )

            val request = UpdateRolloutRule(priority = 5)

            `when`(mockRolloutRuleRepository.findById(ruleId)).thenReturn(Optional.of(existingEntity))
            `when`(mockRolloutRuleRepository.save(any(RolloutRuleEntity::class.java))).thenReturn(existingEntity)

            service.update(ruleId, request)

            verify(mockRolloutRuleRepository).save(any(RolloutRuleEntity::class.java))
        }

        @Test
        fun `should handle race condition in update`() {
            val ruleId = UUID.randomUUID()
            val existingEntity = createTestRolloutRuleEntity(
                id = ruleId,
                featureFlagId = 1L
            )

            val request = UpdateRolloutRule(percentage = 75.0)

            `when`(mockRolloutRuleRepository.findById(ruleId)).thenReturn(Optional.of(existingEntity))

            val dbException = DataIntegrityViolationException("Constraint violation")
            `when`(mockRolloutRuleRepository.save(any(RolloutRuleEntity::class.java))).thenThrow(dbException)

            assertThrows<DataIntegrityViolationException> {
                service.update(ruleId, request)
            }

            verify(mockRolloutRuleRepository).save(any(RolloutRuleEntity::class.java))
        }
    }

    @Nested
    @DisplayName("Delete RolloutRule Tests")
    inner class DeleteRolloutRuleTests {

        @Test
        fun `should delete rollout rule successfully`() {
            val ruleId = UUID.randomUUID()

            `when`(mockRolloutRuleRepository.existsById(ruleId)).thenReturn(true)
            doNothing().`when`(mockRolloutRuleRepository).deleteById(ruleId)

            service.delete(ruleId)

            verify(mockRolloutRuleRepository).existsById(ruleId)
            verify(mockRolloutRuleRepository).deleteById(ruleId)
        }

        @Test
        fun `should throw NoSuchElementException when rule does not exist`() {
            val ruleId = UUID.randomUUID()

            `when`(mockRolloutRuleRepository.existsById(ruleId)).thenReturn(false)

            val exception = assertThrows<NoSuchElementException> {
                service.delete(ruleId)
            }

            assertEquals("Rollout rule with id '$ruleId' not found", exception.message)

            verify(mockRolloutRuleRepository).existsById(ruleId)
            verify(mockRolloutRuleRepository, never()).deleteById(any())
        }
    }

    @Nested
    @DisplayName("Get RolloutRules Tests")
    inner class GetRolloutRulesTests {

        @Test
        fun `should get all rules for flag ordered by priority`() {
            val flagCode = "test-flag"
            val flag = createTestFlag(id = 1L, code = flagCode)

            val rules = listOf(
                createTestRolloutRuleEntity(UUID.randomUUID(), 1L, priority = 1),
                createTestRolloutRuleEntity(UUID.randomUUID(), 1L, priority = 2),
                createTestRolloutRuleEntity(UUID.randomUUID(), 1L, priority = 3)
            )

            `when`(mockFeatureFlagRepository.findByCode(flagCode)).thenReturn(flag)
            `when`(mockRolloutRuleRepository.findByFeatureFlagIdOrderByPriorityAsc(1L)).thenReturn(rules)

            val result = service.getByFlag(flagCode)

            assertEquals(3, result.size)

            verify(mockFeatureFlagRepository).findByCode(flagCode)
            verify(mockRolloutRuleRepository).findByFeatureFlagIdOrderByPriorityAsc(1L)
        }

        @Test
        fun `should throw NoSuchElementException when flag does not exist for getByFlag`() {
            val flagCode = "non-existent"

            `when`(mockFeatureFlagRepository.findByCode(flagCode)).thenReturn(null)

            val exception = assertThrows<NoSuchElementException> {
                service.getByFlag(flagCode)
            }

            assertEquals("Feature flag with code 'non-existent' not found", exception.message)

            verify(mockFeatureFlagRepository).findByCode(flagCode)
            verify(mockRolloutRuleRepository, never()).findByFeatureFlagIdOrderByPriorityAsc(anyLong())
        }

        @Test
        fun `should get single rule by ID`() {
            val ruleId = UUID.randomUUID()
            val entity = createTestRolloutRuleEntity(ruleId, 1L)

            `when`(mockRolloutRuleRepository.findById(ruleId)).thenReturn(Optional.of(entity))

            val result = service.getById(ruleId)

            assertNotNull(result)
            assertEquals(ruleId, result.id)

            verify(mockRolloutRuleRepository).findById(ruleId)
        }

        @Test
        fun `should throw NoSuchElementException when rule not found by ID`() {
            val ruleId = UUID.randomUUID()

            `when`(mockRolloutRuleRepository.findById(ruleId)).thenReturn(Optional.empty())

            val exception = assertThrows<NoSuchElementException> {
                service.getById(ruleId)
            }

            assertEquals("Rollout rule with id '$ruleId' not found", exception.message)

            verify(mockRolloutRuleRepository).findById(ruleId)
        }
    }

    @Nested
    @DisplayName("Reorder RolloutRules Tests")
    inner class ReorderRolloutRulesTests {

        @Test
        fun `should reorder rules successfully`() {
            val flagCode = "test-flag"
            val flag = createTestFlag(id = 1L, code = flagCode)

            val rule1 = createTestRolloutRuleEntity(UUID.randomUUID(), 1L, priority = 0)
            val rule2 = createTestRolloutRuleEntity(UUID.randomUUID(), 1L, priority = 1)
            val rule3 = createTestRolloutRuleEntity(UUID.randomUUID(), 1L, priority = 2)

            val rules = listOf(rule1, rule2, rule3)
            val newOrder = listOf(rule3.id!!, rule1.id!!, rule2.id!!)
            val request = ReorderRolloutRules(newOrder)

            `when`(mockFeatureFlagRepository.findByCode(flagCode)).thenReturn(flag)
            `when`(mockRolloutRuleRepository.findByFeatureFlagIdOrderByPriorityAsc(1L)).thenReturn(rules)
            `when`(mockRolloutRuleRepository.saveAll(anyList<RolloutRuleEntity>())).thenReturn(rules)

            service.reorder(flagCode, request)

            verify(mockFeatureFlagRepository).findByCode(flagCode)
            verify(mockRolloutRuleRepository).findByFeatureFlagIdOrderByPriorityAsc(1L)
            verify(mockRolloutRuleRepository).saveAll(anyList<RolloutRuleEntity>())
        }

        @Test
        fun `should throw NoSuchElementException when flag does not exist for reorder`() {
            val flagCode = "non-existent"
            val request = ReorderRolloutRules(listOf(UUID.randomUUID()))

            `when`(mockFeatureFlagRepository.findByCode(flagCode)).thenReturn(null)

            val exception = assertThrows<NoSuchElementException> {
                service.reorder(flagCode, request)
            }

            assertEquals("Feature flag with code 'non-existent' not found", exception.message)

            verify(mockFeatureFlagRepository).findByCode(flagCode)
            verify(mockRolloutRuleRepository, never()).findByFeatureFlagIdOrderByPriorityAsc(anyLong())
        }

        @Test
        fun `should throw IllegalArgumentException when rule IDs do not belong to flag`() {
            val flagCode = "test-flag"
            val flag = createTestFlag(id = 1L, code = flagCode)

            val rule1 = createTestRolloutRuleEntity(UUID.randomUUID(), 1L)
            val rule2 = createTestRolloutRuleEntity(UUID.randomUUID(), 1L)
            val rules = listOf(rule1, rule2)

            val invalidRuleId = UUID.randomUUID()
            val request = ReorderRolloutRules(listOf(rule1.id!!, invalidRuleId))

            `when`(mockFeatureFlagRepository.findByCode(flagCode)).thenReturn(flag)
            `when`(mockRolloutRuleRepository.findByFeatureFlagIdOrderByPriorityAsc(1L)).thenReturn(rules)

            val exception = assertThrows<IllegalArgumentException> {
                service.reorder(flagCode, request)
            }

            assert(exception.message!!.contains("Invalid rule IDs"))

            verify(mockRolloutRuleRepository, never()).saveAll(anyList())
        }

        @Test
        fun `should throw IllegalArgumentException when not all rules are included in reorder`() {
            val flagCode = "test-flag"
            val flag = createTestFlag(id = 1L, code = flagCode)

            val rule1 = createTestRolloutRuleEntity(UUID.randomUUID(), 1L)
            val rule2 = createTestRolloutRuleEntity(UUID.randomUUID(), 1L)
            val rule3 = createTestRolloutRuleEntity(UUID.randomUUID(), 1L)
            val rules = listOf(rule1, rule2, rule3)

            val request = ReorderRolloutRules(listOf(rule1.id!!, rule2.id!!))

            `when`(mockFeatureFlagRepository.findByCode(flagCode)).thenReturn(flag)
            `when`(mockRolloutRuleRepository.findByFeatureFlagIdOrderByPriorityAsc(1L)).thenReturn(rules)

            val exception = assertThrows<IllegalArgumentException> {
                service.reorder(flagCode, request)
            }

            assert(exception.message!!.contains("Must provide all rule IDs for reordering"))

            verify(mockRolloutRuleRepository, never()).saveAll(anyList())
        }
    }

    private fun createTestFlag(
        id: Long,
        code: String,
        name: String = "Test Flag"
    ): FeatureFlagEntity {
        return FeatureFlagEntity(
            id = id,
            code = code,
            name = name,
            description = "Test description",
            defaultBoolValue = true,
            defaultStringValue = null,
            defaultIntValue = null,
            defaultPercentageValue = null,
            flagType = FlagType.BOOLEAN,
            enabled = true,
            dueAt = null
        )
    }

    private fun createTestRolloutRuleEntity(
        id: UUID,
        featureFlagId: Long,
        percentage: Double? = null,
        priority: Int = 0,
        active: Boolean = true
    ): RolloutRuleEntity {
        val flag = createTestFlag(featureFlagId, "test-flag")
        return RolloutRuleEntity(
            id = id,
            featureFlag = flag,
            attributeKey = null,
            attributeValue = null,
            percentage = percentage,
            distributionKeyAttribute = null,
            valueBool = null,
            valueString = null,
            valueInt = null,
            valuePercentage = null,
            variantName = null,
            startAt = null,
            endAt = null,
            priority = priority,
            active = active,
            conditions = mutableListOf()
        )
    }
}
