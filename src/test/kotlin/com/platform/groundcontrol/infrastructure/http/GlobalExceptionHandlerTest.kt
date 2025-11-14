package com.platform.groundcontrol.infrastructure.http

import jakarta.validation.ConstraintViolation
import jakarta.validation.ConstraintViolationException
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.DisplayName
import org.mockito.Mockito.*
import org.springframework.dao.DataIntegrityViolationException
import org.springframework.http.HttpStatus
import org.springframework.orm.ObjectOptimisticLockingFailureException
import org.springframework.validation.BindingResult
import org.springframework.validation.FieldError
import org.springframework.web.bind.MethodArgumentNotValidException
import java.sql.SQLException
import java.util.NoSuchElementException
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

@DisplayName("GlobalExceptionHandler Tests")
class GlobalExceptionHandlerTest {

    private lateinit var handler: GlobalExceptionHandler

    @BeforeEach
    fun setUp() {
        handler = GlobalExceptionHandler()
    }

    @Nested
    @DisplayName("NoSuchElementException Handler Tests")
    inner class NoSuchElementExceptionTests {

        @Test
        fun `should return 404 NOT_FOUND with custom message`() {
            val exception = NoSuchElementException("Feature flag with code 'my-flag' not found")

            val response = handler.handleNotFoundException(exception)

            assertEquals(HttpStatus.NOT_FOUND, response.statusCode)
            assertEquals(404, response.body?.status)
            assertEquals("Not Found", response.body?.error)
            assertEquals("Feature flag with code 'my-flag' not found", response.body?.message)
            assertNotNull(response.body?.timestamp)
        }

        @Test
        fun `should return 404 with default message when exception message is null`() {
            val exception = NoSuchElementException(null as String?)

            val response = handler.handleNotFoundException(exception)

            assertEquals(HttpStatus.NOT_FOUND, response.statusCode)
            assertEquals("Resource not found", response.body?.message)
        }
    }

    @Nested
    @DisplayName("MethodArgumentNotValidException Handler Tests")
    inner class MethodArgumentNotValidExceptionTests {

        @Test
        fun `should return 400 BAD_REQUEST with validation errors`() {
            val bindingResult = mock(BindingResult::class.java)
            val fieldError1 = FieldError("featureFlag", "code", "must not be blank")
            val fieldError2 = FieldError("featureFlag", "name", "must not be blank")

            `when`(bindingResult.fieldErrors).thenReturn(listOf(fieldError1, fieldError2))

            val methodParameter = mock(org.springframework.core.MethodParameter::class.java)
            val exception = MethodArgumentNotValidException(methodParameter, bindingResult)

            val response = handler.handleValidationExceptions(exception)

            assertEquals(HttpStatus.BAD_REQUEST, response.statusCode)
            assertEquals(400, response.body?.status)
            assertEquals("Bad Request", response.body?.error)
            assertEquals("Validation failed: code: must not be blank, name: must not be blank", response.body?.message)
        }

        @Test
        fun `should handle single field validation error`() {
            val bindingResult = mock(BindingResult::class.java)
            val fieldError = FieldError("featureFlag", "code", "size must be between 1 and 50")

            `when`(bindingResult.fieldErrors).thenReturn(listOf(fieldError))

            val methodParameter = mock(org.springframework.core.MethodParameter::class.java)
            val exception = MethodArgumentNotValidException(methodParameter, bindingResult)

            val response = handler.handleValidationExceptions(exception)

            assertEquals(HttpStatus.BAD_REQUEST, response.statusCode)
            assertEquals("Validation failed: code: size must be between 1 and 50", response.body?.message)
        }
    }

    @Nested
    @DisplayName("ConstraintViolationException Handler Tests")
    inner class ConstraintViolationExceptionTests {

        @Test
        fun `should return 400 BAD_REQUEST with constraint violations`() {
            val violation1 = mock(ConstraintViolation::class.java) as ConstraintViolation<*>
            val violation2 = mock(ConstraintViolation::class.java) as ConstraintViolation<*>

            val path1 = mock(jakarta.validation.Path::class.java)
            val path2 = mock(jakarta.validation.Path::class.java)

            `when`(path1.toString()).thenReturn("code")
            `when`(path2.toString()).thenReturn("enabled")
            `when`(violation1.propertyPath).thenReturn(path1)
            `when`(violation1.message).thenReturn("must not be empty")
            `when`(violation2.propertyPath).thenReturn(path2)
            `when`(violation2.message).thenReturn("must not be null")

            val exception = ConstraintViolationException(setOf(violation1, violation2))

            val response = handler.handleConstraintViolation(exception)

            assertEquals(HttpStatus.BAD_REQUEST, response.statusCode)
            assertEquals(400, response.body?.status)
            assertEquals("Bad Request", response.body?.error)
            // Message should contain both violations (order may vary)
            val message = response.body?.message ?: ""
            assert(message.contains("code: must not be empty"))
            assert(message.contains("enabled: must not be null"))
        }
    }

    @Nested
    @DisplayName("IllegalArgumentException Handler Tests")
    inner class IllegalArgumentExceptionTests {

        @Test
        fun `should return 400 BAD_REQUEST with exception message`() {
            val exception = IllegalArgumentException("Invalid flag type provided")

            val response = handler.handleIllegalArgument(exception)

            assertEquals(HttpStatus.BAD_REQUEST, response.statusCode)
            assertEquals(400, response.body?.status)
            assertEquals("Bad Request", response.body?.error)
            assertEquals("Invalid flag type provided", response.body?.message)
        }

        @Test
        fun `should return default message when exception message is null`() {
            val exception = IllegalArgumentException(null as String?)

            val response = handler.handleIllegalArgument(exception)

            assertEquals(HttpStatus.BAD_REQUEST, response.statusCode)
            assertEquals("Invalid argument", response.body?.message)
        }
    }

    @Nested
    @DisplayName("IllegalStateException Handler Tests")
    inner class IllegalStateExceptionTests {

        @Test
        fun `should return 409 CONFLICT for duplicate code`() {
            val exception = IllegalStateException("A feature flag with code 'my-flag' already exists. Please use a different code.")

            val response = handler.handleIllegalState(exception)

            assertEquals(HttpStatus.CONFLICT, response.statusCode)
            assertEquals(409, response.body?.status)
            assertEquals("Conflict", response.body?.error)
            assertEquals("A feature flag with code 'my-flag' already exists. Please use a different code.", response.body?.message)
        }

        @Test
        fun `should return 409 CONFLICT for duplicate name`() {
            val exception = IllegalStateException("A feature flag with name 'My Flag' already exists. Please use a different name.")

            val response = handler.handleIllegalState(exception)

            assertEquals(HttpStatus.CONFLICT, response.statusCode)
            assertEquals(409, response.body?.status)
            assertEquals("A feature flag with name 'My Flag' already exists. Please use a different name.", response.body?.message)
        }

        @Test
        fun `should return default message when exception message is null`() {
            val exception = IllegalStateException(null as String?)

            val response = handler.handleIllegalState(exception)

            assertEquals(HttpStatus.CONFLICT, response.statusCode)
            assertEquals("Operation conflict", response.body?.message)
        }
    }

    @Nested
    @DisplayName("DataIntegrityViolationException Handler Tests - Unique Constraints")
    inner class DataIntegrityUniqueConstraintTests {

        @Test
        fun `should handle unique constraint violation for code`() {
            val rootCause = SQLException("ERROR: duplicate key value violates unique constraint \"uk_feature_flag_code\"")
            val exception = DataIntegrityViolationException("could not execute statement", rootCause)

            val response = handler.handleDataIntegrityViolation(exception)

            assertEquals(HttpStatus.CONFLICT, response.statusCode)
            assertEquals(409, response.body?.status)
            assertEquals("Conflict", response.body?.error)
            assertEquals("A feature flag with this code already exists. Please use a different code.", response.body?.message)
        }

        @Test
        fun `should handle unique constraint violation for name`() {
            val rootCause = SQLException("ERROR: duplicate key value violates unique constraint \"uk_feature_flag_name\"")
            val exception = DataIntegrityViolationException("could not execute statement", rootCause)

            val response = handler.handleDataIntegrityViolation(exception)

            assertEquals(HttpStatus.CONFLICT, response.statusCode)
            assertEquals("A feature flag with this name already exists. Please use a different name.", response.body?.message)
        }

        @Test
        fun `should handle generic duplicate key error`() {
            val rootCause = SQLException("Duplicate key error on unknown column")
            val exception = DataIntegrityViolationException("could not execute statement", rootCause)

            val response = handler.handleDataIntegrityViolation(exception)

            assertEquals(HttpStatus.CONFLICT, response.statusCode)
            assertEquals("This resource already exists. Please check your input and try again.", response.body?.message)
        }

        @Test
        fun `should handle unique constraint with case-insensitive matching`() {
            val rootCause = SQLException("ERROR: UNIQUE CONSTRAINT violation on CODE column")
            val exception = DataIntegrityViolationException("could not execute statement", rootCause)

            val response = handler.handleDataIntegrityViolation(exception)

            assertEquals(HttpStatus.CONFLICT, response.statusCode)
            assertEquals("A feature flag with this code already exists. Please use a different code.", response.body?.message)
        }
    }

    @Nested
    @DisplayName("DataIntegrityViolationException Handler Tests - Foreign Key Constraints")
    inner class DataIntegrityForeignKeyConstraintTests {

        @Test
        fun `should handle foreign key constraint violation`() {
            val rootCause = SQLException("ERROR: insert or update on table violates foreign key constraint \"fk_rollout_rule_feature_flag\"")
            val exception = DataIntegrityViolationException("could not execute statement", rootCause)

            val response = handler.handleDataIntegrityViolation(exception)

            assertEquals(HttpStatus.CONFLICT, response.statusCode)
            assertEquals(409, response.body?.status)
            assertEquals("Cannot perform this operation because it references data that is being used elsewhere.", response.body?.message)
        }

        @Test
        fun `should handle foreign key constraint with case-insensitive matching`() {
            val rootCause = SQLException("violates FOREIGN KEY constraint on table")
            val exception = DataIntegrityViolationException("could not execute statement", rootCause)

            val response = handler.handleDataIntegrityViolation(exception)

            assertEquals(HttpStatus.CONFLICT, response.statusCode)
            assertEquals("Cannot perform this operation because it references data that is being used elsewhere.", response.body?.message)
        }
    }

    @Nested
    @DisplayName("DataIntegrityViolationException Handler Tests - Not-Null Constraints")
    inner class DataIntegrityNotNullConstraintTests {

        @Test
        fun `should handle not-null constraint violation`() {
            val rootCause = SQLException("ERROR: null value in column \"code\" violates not-null constraint")
            val exception = DataIntegrityViolationException("could not execute statement", rootCause)

            val response = handler.handleDataIntegrityViolation(exception)

            assertEquals(HttpStatus.CONFLICT, response.statusCode)
            assertEquals("A required field is missing. Please provide all required information.", response.body?.message)
        }

        @Test
        fun `should handle null value constraint with case-insensitive matching`() {
            val rootCause = SQLException("NULL VALUE in column violates constraint")
            val exception = DataIntegrityViolationException("could not execute statement", rootCause)

            val response = handler.handleDataIntegrityViolation(exception)

            assertEquals(HttpStatus.CONFLICT, response.statusCode)
            assertEquals("A required field is missing. Please provide all required information.", response.body?.message)
        }
    }

    @Nested
    @DisplayName("DataIntegrityViolationException Handler Tests - Check Constraints")
    inner class DataIntegrityCheckConstraintTests {

        @Test
        fun `should handle check constraint violation`() {
            val rootCause = SQLException("ERROR: new row violates check constraint \"ck_percentage_range\"")
            val exception = DataIntegrityViolationException("could not execute statement", rootCause)

            val response = handler.handleDataIntegrityViolation(exception)

            assertEquals(HttpStatus.CONFLICT, response.statusCode)
            assertEquals("The provided value does not meet the required criteria. Please check your input.", response.body?.message)
        }

        @Test
        fun `should handle check constraint with case-insensitive matching`() {
            val rootCause = SQLException("violates CHECK CONSTRAINT on table")
            val exception = DataIntegrityViolationException("could not execute statement", rootCause)

            val response = handler.handleDataIntegrityViolation(exception)

            assertEquals(HttpStatus.CONFLICT, response.statusCode)
            assertEquals("The provided value does not meet the required criteria. Please check your input.", response.body?.message)
        }
    }

    @Nested
    @DisplayName("DataIntegrityViolationException Handler Tests - Edge Cases")
    inner class DataIntegrityEdgeCaseTests {

        @Test
        fun `should handle exception with no root cause`() {
            val exception = DataIntegrityViolationException("Database error occurred")

            val response = handler.handleDataIntegrityViolation(exception)

            assertEquals(HttpStatus.CONFLICT, response.statusCode)
            assertEquals("Unable to complete the operation due to a data constraint. Please review your input.", response.body?.message)
        }

        @Test
        fun `should handle exception with null root cause message`() {
            val rootCause = SQLException(null as String?)
            val exception = DataIntegrityViolationException("could not execute statement", rootCause)

            val response = handler.handleDataIntegrityViolation(exception)

            assertEquals(HttpStatus.CONFLICT, response.statusCode)
            assertEquals("Unable to complete the operation due to a data constraint. Please review your input.", response.body?.message)
        }

        @Test
        fun `should handle unknown constraint type`() {
            val rootCause = SQLException("Some unknown database error occurred")
            val exception = DataIntegrityViolationException("could not execute statement", rootCause)

            val response = handler.handleDataIntegrityViolation(exception)

            assertEquals(HttpStatus.CONFLICT, response.statusCode)
            assertEquals("Unable to complete the operation due to a data constraint. Please review your input.", response.body?.message)
        }

        @Test
        fun `should prioritize unique constraint when multiple keywords present`() {
            val rootCause = SQLException("ERROR: duplicate key value violates unique constraint and foreign key constraint")
            val exception = DataIntegrityViolationException("could not execute statement", rootCause)

            val response = handler.handleDataIntegrityViolation(exception)

            // Should match unique constraint first (order matters in when expression)
            assertEquals(HttpStatus.CONFLICT, response.statusCode)
            assert(response.body?.message?.contains("already exists") == true)
        }
    }

    @Nested
    @DisplayName("ObjectOptimisticLockingFailureException Handler Tests")
    inner class OptimisticLockingTests {

        @Test
        fun `should return 409 CONFLICT for optimistic locking failure`() {
            val exception = ObjectOptimisticLockingFailureException("Feature flag was modified", null)

            val response = handler.handleOptimisticLocking(exception)

            assertEquals(HttpStatus.CONFLICT, response.statusCode)
            assertEquals(409, response.body?.status)
            assertEquals("Conflict", response.body?.error)
            assertEquals("This resource was modified by another user. Please refresh and try again.", response.body?.message)
            assertNotNull(response.body?.timestamp)
        }

        @Test
        fun `should handle concurrent update scenario`() {
            // Simulates two users updating the same feature flag simultaneously
            val exception = ObjectOptimisticLockingFailureException(
                "Object of class [FeatureFlagEntity] with identifier [1]: optimistic locking failed",
                null
            )

            val response = handler.handleOptimisticLocking(exception)

            assertEquals(HttpStatus.CONFLICT, response.statusCode)
            assertEquals("This resource was modified by another user. Please refresh and try again.", response.body?.message)
        }
    }

    @Nested
    @DisplayName("Generic Exception Handler Tests")
    inner class GenericExceptionTests {

        @Test
        fun `should return 500 INTERNAL_SERVER_ERROR for generic exception`() {
            val exception = Exception("Unexpected error occurred")

            val response = handler.handleGenericException(exception)

            assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.statusCode)
            assertEquals(500, response.body?.status)
            assertEquals("Internal Server Error", response.body?.error)
            assertEquals("An unexpected error occurred. Please contact support.", response.body?.message)
            assertNotNull(response.body?.timestamp)
        }

        @Test
        fun `should handle runtime exceptions`() {
            val exception = RuntimeException("Something went wrong")

            val response = handler.handleGenericException(exception)

            assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.statusCode)
            assertEquals("An unexpected error occurred. Please contact support.", response.body?.message)
        }

        @Test
        fun `should handle null pointer exceptions`() {
            val exception = NullPointerException("Null value encountered")

            val response = handler.handleGenericException(exception)

            assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.statusCode)
            assertEquals("An unexpected error occurred. Please contact support.", response.body?.message)
        }
    }

    @Nested
    @DisplayName("ErrorResponse Structure Tests")
    inner class ErrorResponseTests {

        @Test
        fun `should create ErrorResponse with all required fields`() {
            val exception = NoSuchElementException("Test error")

            val response = handler.handleNotFoundException(exception)

            val errorResponse = response.body
            assertNotNull(errorResponse)
            assertNotNull(errorResponse.timestamp)
            assertEquals(404, errorResponse.status)
            assertEquals("Not Found", errorResponse.error)
            assertEquals("Test error", errorResponse.message)
        }

        @Test
        fun `should have consistent ErrorResponse structure across all handlers`() {
            val exceptions = listOf(
                handler.handleNotFoundException(NoSuchElementException("test")),
                handler.handleIllegalArgument(IllegalArgumentException("test")),
                handler.handleIllegalState(IllegalStateException("test")),
                handler.handleDataIntegrityViolation(DataIntegrityViolationException("test")),
                handler.handleOptimisticLocking(ObjectOptimisticLockingFailureException("test", null)),
                handler.handleGenericException(Exception("test"))
            )

            exceptions.forEach { response ->
                assertNotNull(response.body)
                assertNotNull(response.body?.timestamp)
                assertNotNull(response.body?.status)
                assertNotNull(response.body?.error)
                assertNotNull(response.body?.message)
            }
        }
    }

    @Nested
    @DisplayName("Real-World Scenario Tests")
    inner class RealWorldScenarioTests {

        @Test
        fun `should handle duplicate feature flag creation via database constraint`() {
            // Scenario: User tries to create a feature flag with code that already exists
            // Proactive check was bypassed somehow (race condition)
            val rootCause = SQLException(
                "ERROR: duplicate key value violates unique constraint \"uk_feature_flag_code\"\n" +
                "Detail: Key (code)=(my-feature) already exists."
            )
            val exception = DataIntegrityViolationException("could not execute statement", rootCause)

            val response = handler.handleDataIntegrityViolation(exception)

            assertEquals(HttpStatus.CONFLICT, response.statusCode)
            assertEquals("A feature flag with this code already exists. Please use a different code.", response.body?.message)
        }

        @Test
        fun `should handle rollout rule creation without valid feature flag`() {
            // Scenario: User tries to create a rollout rule for non-existent feature flag
            val rootCause = SQLException(
                "ERROR: insert or update on table \"rollout_rule\" violates foreign key constraint \"fk_rollout_rule_feature_flag\"\n" +
                "Detail: Key (feature_flag_id)=(999) is not present in table \"feature_flag\"."
            )
            val exception = DataIntegrityViolationException("could not execute statement", rootCause)

            val response = handler.handleDataIntegrityViolation(exception)

            assertEquals(HttpStatus.CONFLICT, response.statusCode)
            assertEquals("Cannot perform this operation because it references data that is being used elsewhere.", response.body?.message)
        }

        @Test
        fun `should handle feature flag update with missing required field`() {
            // Scenario: User tries to update a feature flag but forgets to provide required field
            val rootCause = SQLException("ERROR: null value in column \"name\" violates not-null constraint")
            val exception = DataIntegrityViolationException("could not execute statement", rootCause)

            val response = handler.handleDataIntegrityViolation(exception)

            assertEquals(HttpStatus.CONFLICT, response.statusCode)
            assertEquals("A required field is missing. Please provide all required information.", response.body?.message)
        }

        @Test
        fun `should handle concurrent feature flag updates`() {
            // Scenario: Two users try to update the same feature flag at the same time
            val exception = ObjectOptimisticLockingFailureException(
                "Row was updated or deleted by another transaction",
                null
            )

            val response = handler.handleOptimisticLocking(exception)

            assertEquals(HttpStatus.CONFLICT, response.statusCode)
            assertEquals("This resource was modified by another user. Please refresh and try again.", response.body?.message)
        }

        @Test
        fun `should handle invalid percentage value in rollout rule`() {
            // Scenario: User tries to create a rollout rule with percentage > 100
            val rootCause = SQLException(
                "ERROR: new row for relation \"rollout_rule\" violates check constraint \"ck_percentage_range\"\n" +
                "Detail: Failing row contains percentage value 150.0"
            )
            val exception = DataIntegrityViolationException("could not execute statement", rootCause)

            val response = handler.handleDataIntegrityViolation(exception)

            assertEquals(HttpStatus.CONFLICT, response.statusCode)
            assertEquals("The provided value does not meet the required criteria. Please check your input.", response.body?.message)
        }
    }
}
