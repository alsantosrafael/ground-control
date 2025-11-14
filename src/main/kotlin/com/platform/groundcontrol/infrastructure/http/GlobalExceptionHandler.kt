package com.platform.groundcontrol.infrastructure.http

import jakarta.validation.ConstraintViolationException
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.MethodArgumentNotValidException
import org.springframework.web.bind.annotation.ControllerAdvice
import org.springframework.web.bind.annotation.ExceptionHandler
import java.time.Instant
import java.util.NoSuchElementException

@ControllerAdvice
class GlobalExceptionHandler {

    companion object {
        private val logger = LoggerFactory.getLogger(GlobalExceptionHandler::class.java)
    }

    @ExceptionHandler(NoSuchElementException::class)
    fun handleNotFoundException(ex: NoSuchElementException): ResponseEntity<ErrorResponse> {
        logger.warn("Resource not found: ${ex.message}")
        val errorResponse = ErrorResponse(
            timestamp = Instant.now(),
            status = HttpStatus.NOT_FOUND.value(),
            error = HttpStatus.NOT_FOUND.reasonPhrase,
            message = ex.message ?: "Resource not found"
        )
        return ResponseEntity(errorResponse, HttpStatus.NOT_FOUND)
    }

    @ExceptionHandler(MethodArgumentNotValidException::class)
    fun handleValidationExceptions(ex: MethodArgumentNotValidException): ResponseEntity<ErrorResponse> {
        val errors = ex.bindingResult.fieldErrors.joinToString(", ") { error ->
            "${error.field}: ${error.defaultMessage}"
        }

        logger.warn("Validation error: $errors")
        val errorResponse = ErrorResponse(
            timestamp = Instant.now(),
            status = HttpStatus.BAD_REQUEST.value(),
            error = HttpStatus.BAD_REQUEST.reasonPhrase,
            message = "Validation failed: $errors"
        )
        return ResponseEntity(errorResponse, HttpStatus.BAD_REQUEST)
    }

    @ExceptionHandler(ConstraintViolationException::class)
    fun handleConstraintViolation(ex: ConstraintViolationException): ResponseEntity<ErrorResponse> {
        val errors = ex.constraintViolations.joinToString(", ") { violation ->
            "${violation.propertyPath}: ${violation.message}"
        }

        logger.warn("Constraint violation: $errors")
        val errorResponse = ErrorResponse(
            timestamp = Instant.now(),
            status = HttpStatus.BAD_REQUEST.value(),
            error = HttpStatus.BAD_REQUEST.reasonPhrase,
            message = "Constraint violation: $errors"
        )
        return ResponseEntity(errorResponse, HttpStatus.BAD_REQUEST)
    }

    @ExceptionHandler(IllegalArgumentException::class)
    fun handleIllegalArgument(ex: IllegalArgumentException): ResponseEntity<ErrorResponse> {
        logger.warn("Invalid argument: ${ex.message}")
        val errorResponse = ErrorResponse(
            timestamp = Instant.now(),
            status = HttpStatus.BAD_REQUEST.value(),
            error = HttpStatus.BAD_REQUEST.reasonPhrase,
            message = ex.message ?: "Invalid argument"
        )
        return ResponseEntity(errorResponse, HttpStatus.BAD_REQUEST)
    }

    @ExceptionHandler(IllegalStateException::class)
    fun handleIllegalState(ex: IllegalStateException): ResponseEntity<ErrorResponse> {
        logger.error("Illegal state: ${ex.message}", ex)
        val errorResponse = ErrorResponse(
            timestamp = Instant.now(),
            status = HttpStatus.CONFLICT.value(),
            error = HttpStatus.CONFLICT.reasonPhrase,
            message = ex.message ?: "Operation conflict"
        )
        return ResponseEntity(errorResponse, HttpStatus.CONFLICT)
    }

    @ExceptionHandler(org.springframework.dao.DataIntegrityViolationException::class)
    fun handleDataIntegrityViolation(ex: org.springframework.dao.DataIntegrityViolationException): ResponseEntity<ErrorResponse> {
        val rootCause = ex.rootCause?.message ?: ex.message ?: ""

        // Parse user-friendly message from database constraint
        val userMessage = when {
            rootCause.contains("unique constraint", ignoreCase = true) ||
            rootCause.contains("duplicate key", ignoreCase = true) -> {
                when {
                    rootCause.contains("code", ignoreCase = true) ->
                        "A feature flag with this code already exists. Please use a different code."
                    rootCause.contains("name", ignoreCase = true) ->
                        "A feature flag with this name already exists. Please use a different name."
                    else ->
                        "This resource already exists. Please check your input and try again."
                }
            }
            rootCause.contains("foreign key", ignoreCase = true) ||
            rootCause.contains("violates foreign key constraint", ignoreCase = true) -> {
                "Cannot perform this operation because it references data that is being used elsewhere."
            }
            rootCause.contains("not-null", ignoreCase = true) ||
            rootCause.contains("null value", ignoreCase = true) -> {
                "A required field is missing. Please provide all required information."
            }
            rootCause.contains("check constraint", ignoreCase = true) -> {
                "The provided value does not meet the required criteria. Please check your input."
            }
            else -> {
                "Unable to complete the operation due to a data constraint. Please review your input."
            }
        }

        logger.warn("Data integrity violation: {} - Root cause: {}", userMessage, rootCause)

        val errorResponse = ErrorResponse(
            timestamp = Instant.now(),
            status = HttpStatus.CONFLICT.value(),
            error = "Conflict",
            message = userMessage
        )
        return ResponseEntity(errorResponse, HttpStatus.CONFLICT)
    }

    @ExceptionHandler(org.springframework.orm.ObjectOptimisticLockingFailureException::class)
    fun handleOptimisticLocking(ex: org.springframework.orm.ObjectOptimisticLockingFailureException): ResponseEntity<ErrorResponse> {
        logger.warn("Optimistic locking failure: {}", ex.message)
        val errorResponse = ErrorResponse(
            timestamp = Instant.now(),
            status = HttpStatus.CONFLICT.value(),
            error = "Conflict",
            message = "This resource was modified by another user. Please refresh and try again."
        )
        return ResponseEntity(errorResponse, HttpStatus.CONFLICT)
    }

    @ExceptionHandler(Exception::class)
    fun handleGenericException(ex: Exception): ResponseEntity<ErrorResponse> {
        logger.error("Unexpected error: ${ex.message}", ex)
        val errorResponse = ErrorResponse(
            timestamp = Instant.now(),
            status = HttpStatus.INTERNAL_SERVER_ERROR.value(),
            error = HttpStatus.INTERNAL_SERVER_ERROR.reasonPhrase,
            message = "An unexpected error occurred. Please contact support."
        )
        return ResponseEntity(errorResponse, HttpStatus.INTERNAL_SERVER_ERROR)
    }
}

data class ErrorResponse(
    val timestamp: Instant,
    val status: Int,
    val error: String,
    val message: String?
)