package com.platform.groundcontrol.infrastructure.http

import com.platform.groundcontrol.application.services.RolloutRuleService
import com.platform.groundcontrol.domain.enums.DataType
import com.platform.groundcontrol.domain.enums.Operator
import com.platform.groundcontrol.domain.valueobjects.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.mockito.Mock
import org.mockito.Mockito.doNothing
import org.mockito.Mockito.doThrow
import org.mockito.Mockito.verify
import org.mockito.Mockito.`when`
import org.mockito.MockitoAnnotations
import org.springframework.http.HttpStatus
import java.time.Instant
import java.util.NoSuchElementException
import java.util.UUID
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

@DisplayName("RolloutRuleController Tests")
class RolloutRuleControllerTest {

    @Mock
    private lateinit var mockRolloutRuleService: RolloutRuleService

    private lateinit var controller: RolloutRuleController

    @BeforeEach
    fun setUp() {
        MockitoAnnotations.openMocks(this)
        controller = RolloutRuleController(mockRolloutRuleService)
    }

    @Nested
    @DisplayName("Create RolloutRule Tests")
    inner class CreateTests {

        @Test
        fun `should create rollout rule successfully and return 201 CREATED`() {
            val flagCode = "test-flag"
            val ruleId = UUID.randomUUID()
            val request = CreateRolloutRule(
                percentage = 50.0,
                priority = 1,
                active = true,
                variantName = "variant-a"
            )

            val createdRule = RolloutRule(
                id = ruleId,
                featureFlagId = 1L,
                percentage = 50.0,
                priority = 1,
                active = true,
                attributeKey = null,
                attributeValue = null,
                distributionKeyAttribute = null,
                valueBool = null,
                valueString = null,
                valueInt = null,
                valuePercentage = null,
                variantName = "variant-a",
                startAt = null,
                endAt = null,
                conditions = mutableListOf()
            )

            `when`(mockRolloutRuleService.create(flagCode, request)).thenReturn(createdRule)

            val response = controller.create(flagCode, request)

            assertEquals(HttpStatus.CREATED, response.statusCode)
            assertNotNull(response.body)
            assertEquals(ruleId, response.body?.id)
            assertEquals(50.0, response.body?.percentage)
            assertEquals(1, response.body?.priority)
            verify(mockRolloutRuleService).create(flagCode, request)
        }

        @Test
        fun `should throw NoSuchElementException when flag does not exist`() {
            val flagCode = "non-existent-flag"
            val request = CreateRolloutRule(percentage = 50.0)

            `when`(mockRolloutRuleService.create(flagCode, request))
                .thenThrow(NoSuchElementException("Feature flag with code 'non-existent-flag' not found"))

            assertThrows<NoSuchElementException> {
                controller.create(flagCode, request)
            }

            verify(mockRolloutRuleService).create(flagCode, request)
        }

        @Test
        fun `should create rule with all fields populated`() {
            val flagCode = "test-flag"
            val ruleId = UUID.randomUUID()
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

            val createdRule = RolloutRule(
                id = ruleId,
                featureFlagId = 1L,
                percentage = 75.0,
                priority = 5,
                active = false,
                attributeKey = "plan",
                attributeValue = "premium",
                distributionKeyAttribute = "userId",
                valueBool = true,
                valueString = null,
                valueInt = null,
                valuePercentage = null,
                variantName = "premium-variant",
                startAt = startAt,
                endAt = endAt,
                conditions = mutableListOf(Condition("age", Operator.GREATER_THAN, 18, DataType.NUMBER))
            )

            `when`(mockRolloutRuleService.create(flagCode, request)).thenReturn(createdRule)

            val response = controller.create(flagCode, request)

            assertEquals(HttpStatus.CREATED, response.statusCode)
            assertNotNull(response.body)
            assertEquals("plan", response.body?.attributeKey)
            assertEquals("premium", response.body?.attributeValue)
            verify(mockRolloutRuleService).create(flagCode, request)
        }
    }

    @Nested
    @DisplayName("Update RolloutRule Tests")
    inner class UpdateTests {

        @Test
        fun `should update rollout rule successfully and return 200 OK`() {
            val flagCode = "test-flag"
            val ruleId = UUID.randomUUID()
            val request = UpdateRolloutRule(
                percentage = 75.0,
                priority = 2,
                active = false
            )

            val updatedRule = RolloutRule(
                id = ruleId,
                featureFlagId = 1L,
                percentage = 75.0,
                priority = 2,
                active = false,
                attributeKey = null,
                attributeValue = null,
                distributionKeyAttribute = null,
                valueBool = null,
                valueString = null,
                valueInt = null,
                valuePercentage = null,
                variantName = null,
                startAt = null,
                endAt = null,
                conditions = mutableListOf()
            )

            `when`(mockRolloutRuleService.update(ruleId, request)).thenReturn(updatedRule)

            val response = controller.update(flagCode, ruleId, request)

            assertEquals(HttpStatus.OK, response.statusCode)
            assertNotNull(response.body)
            assertEquals(75.0, response.body?.percentage)
            assertEquals(2, response.body?.priority)
            assertEquals(false, response.body?.active)
            verify(mockRolloutRuleService).update(ruleId, request)
        }

        @Test
        fun `should throw NoSuchElementException when rule does not exist`() {
            val flagCode = "test-flag"
            val ruleId = UUID.randomUUID()
            val request = UpdateRolloutRule(percentage = 75.0)

            `when`(mockRolloutRuleService.update(ruleId, request))
                .thenThrow(NoSuchElementException("Rollout rule with id '$ruleId' not found"))

            assertThrows<NoSuchElementException> {
                controller.update(flagCode, ruleId, request)
            }

            verify(mockRolloutRuleService).update(ruleId, request)
        }

        @Test
        fun `should update only specified fields`() {
            val flagCode = "test-flag"
            val ruleId = UUID.randomUUID()
            val request = UpdateRolloutRule(priority = 5)

            val updatedRule = RolloutRule(
                id = ruleId,
                featureFlagId = 1L,
                percentage = 50.0,
                priority = 5,
                active = true,
                attributeKey = null,
                attributeValue = null,
                distributionKeyAttribute = null,
                valueBool = null,
                valueString = null,
                valueInt = null,
                valuePercentage = null,
                variantName = null,
                startAt = null,
                endAt = null,
                conditions = mutableListOf()
            )

            `when`(mockRolloutRuleService.update(ruleId, request)).thenReturn(updatedRule)

            val response = controller.update(flagCode, ruleId, request)

            assertEquals(HttpStatus.OK, response.statusCode)
            assertEquals(5, response.body?.priority)
            verify(mockRolloutRuleService).update(ruleId, request)
        }
    }

    @Nested
    @DisplayName("Delete RolloutRule Tests")
    inner class DeleteTests {

        @Test
        fun `should delete rollout rule successfully and return 204 NO CONTENT`() {
            val flagCode = "test-flag"
            val ruleId = UUID.randomUUID()

            doNothing().`when`(mockRolloutRuleService).delete(ruleId)

            val response = controller.delete(flagCode, ruleId)

            assertEquals(HttpStatus.NO_CONTENT, response.statusCode)
            verify(mockRolloutRuleService).delete(ruleId)
        }

        @Test
        fun `should throw NoSuchElementException when rule does not exist`() {
            val flagCode = "test-flag"
            val ruleId = UUID.randomUUID()

            doThrow(NoSuchElementException("Rollout rule with id '$ruleId' not found"))
                .`when`(mockRolloutRuleService).delete(ruleId)

            assertThrows<NoSuchElementException> {
                controller.delete(flagCode, ruleId)
            }

            verify(mockRolloutRuleService).delete(ruleId)
        }
    }

    @Nested
    @DisplayName("Get RolloutRules Tests")
    inner class GetTests {

        @Test
        fun `should get all rules for flag ordered by priority and return 200 OK`() {
            val flagCode = "test-flag"
            val rule1 = createTestRule(UUID.randomUUID(), priority = 1)
            val rule2 = createTestRule(UUID.randomUUID(), priority = 2)
            val rule3 = createTestRule(UUID.randomUUID(), priority = 3)

            `when`(mockRolloutRuleService.getByFlag(flagCode)).thenReturn(listOf(rule1, rule2, rule3))

            val response = controller.getByFlag(flagCode)

            assertEquals(HttpStatus.OK, response.statusCode)
            assertNotNull(response.body)
            assertEquals(3, response.body?.size)
            assertEquals(1, response.body?.get(0)?.priority)
            assertEquals(2, response.body?.get(1)?.priority)
            assertEquals(3, response.body?.get(2)?.priority)
            verify(mockRolloutRuleService).getByFlag(flagCode)
        }

        @Test
        fun `should return empty list when flag has no rules`() {
            val flagCode = "test-flag"

            `when`(mockRolloutRuleService.getByFlag(flagCode)).thenReturn(emptyList())

            val response = controller.getByFlag(flagCode)

            assertEquals(HttpStatus.OK, response.statusCode)
            assertNotNull(response.body)
            assertEquals(0, response.body?.size)
            verify(mockRolloutRuleService).getByFlag(flagCode)
        }

        @Test
        fun `should throw NoSuchElementException when flag does not exist`() {
            val flagCode = "non-existent"

            `when`(mockRolloutRuleService.getByFlag(flagCode))
                .thenThrow(NoSuchElementException("Feature flag with code 'non-existent' not found"))

            assertThrows<NoSuchElementException> {
                controller.getByFlag(flagCode)
            }

            verify(mockRolloutRuleService).getByFlag(flagCode)
        }

        @Test
        fun `should get single rule by ID and return 200 OK`() {
            val flagCode = "test-flag"
            val ruleId = UUID.randomUUID()
            val rule = createTestRule(ruleId, priority = 1)

            `when`(mockRolloutRuleService.getById(ruleId)).thenReturn(rule)

            val response = controller.getById(flagCode, ruleId)

            assertEquals(HttpStatus.OK, response.statusCode)
            assertNotNull(response.body)
            assertEquals(ruleId, response.body?.id)
            verify(mockRolloutRuleService).getById(ruleId)
        }

        @Test
        fun `should throw NoSuchElementException when rule not found by ID`() {
            val flagCode = "test-flag"
            val ruleId = UUID.randomUUID()

            `when`(mockRolloutRuleService.getById(ruleId))
                .thenThrow(NoSuchElementException("Rollout rule with id '$ruleId' not found"))

            assertThrows<NoSuchElementException> {
                controller.getById(flagCode, ruleId)
            }

            verify(mockRolloutRuleService).getById(ruleId)
        }
    }

    @Nested
    @DisplayName("Reorder RolloutRules Tests")
    inner class ReorderTests {

        @Test
        fun `should reorder rules successfully and return 204 NO CONTENT`() {
            val flagCode = "test-flag"
            val rule1Id = UUID.randomUUID()
            val rule2Id = UUID.randomUUID()
            val rule3Id = UUID.randomUUID()

            val newOrder = listOf(rule3Id, rule1Id, rule2Id)
            val request = ReorderRolloutRules(newOrder)

            doNothing().`when`(mockRolloutRuleService).reorder(flagCode, request)

            val response = controller.reorder(flagCode, request)

            assertEquals(HttpStatus.NO_CONTENT, response.statusCode)
            verify(mockRolloutRuleService).reorder(flagCode, request)
        }

        @Test
        fun `should throw NoSuchElementException when flag does not exist`() {
            val flagCode = "non-existent"
            val request = ReorderRolloutRules(listOf(UUID.randomUUID()))

            doThrow(NoSuchElementException("Feature flag with code 'non-existent' not found"))
                .`when`(mockRolloutRuleService).reorder(flagCode, request)

            assertThrows<NoSuchElementException> {
                controller.reorder(flagCode, request)
            }

            verify(mockRolloutRuleService).reorder(flagCode, request)
        }

        @Test
        fun `should throw IllegalArgumentException when rule IDs do not belong to flag`() {
            val flagCode = "test-flag"
            val validRuleId = UUID.randomUUID()
            val invalidRuleId = UUID.randomUUID()
            val request = ReorderRolloutRules(listOf(validRuleId, invalidRuleId))

            doThrow(IllegalArgumentException("Invalid rule IDs: [$invalidRuleId]"))
                .`when`(mockRolloutRuleService).reorder(flagCode, request)

            assertThrows<IllegalArgumentException> {
                controller.reorder(flagCode, request)
            }

            verify(mockRolloutRuleService).reorder(flagCode, request)
        }

        @Test
        fun `should throw IllegalArgumentException when not all rules are provided for reorder`() {
            val flagCode = "test-flag"
            val rule1Id = UUID.randomUUID()
            val rule2Id = UUID.randomUUID()
            val request = ReorderRolloutRules(listOf(rule1Id, rule2Id))

            doThrow(IllegalArgumentException("Must provide all rule IDs for reordering. Expected 3 rule IDs, but got 2"))
                .`when`(mockRolloutRuleService).reorder(flagCode, request)

            assertThrows<IllegalArgumentException> {
                controller.reorder(flagCode, request)
            }

            verify(mockRolloutRuleService).reorder(flagCode, request)
        }
    }

    private fun createTestRule(id: UUID, priority: Int = 0): RolloutRule {
        return RolloutRule(
            id = id,
            featureFlagId = 1L,
            percentage = null,
            priority = priority,
            active = true,
            attributeKey = null,
            attributeValue = null,
            distributionKeyAttribute = null,
            valueBool = null,
            valueString = null,
            valueInt = null,
            valuePercentage = null,
            variantName = null,
            startAt = null,
            endAt = null,
            conditions = mutableListOf()
        )
    }
}
