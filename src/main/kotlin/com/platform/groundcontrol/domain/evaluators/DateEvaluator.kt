package com.platform.groundcontrol.domain.evaluators

import com.platform.groundcontrol.domain.enums.DataType
import com.platform.groundcontrol.domain.enums.Operator
import org.springframework.stereotype.Component
import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter

@Component
class DateEvaluator : ConditionEvaluator {
    override fun canHandle(operator: Operator, dataType: DataType): Boolean {
        return dataType == DataType.DATE && operator in listOf(
            Operator.EQUALS,
            Operator.NOT_EQUALS,
            Operator.GREATER_THAN,     // after
            Operator.GREATER_EQUAL,    // after or equal
            Operator.LESS_THAN,        // before
            Operator.LESS_EQUAL        // before or equal
        )
    }

    override fun evaluate(attributeValue: Any, conditionValue: Any, operator: Operator): Boolean {
        val attributeInstant = convertToInstant(attributeValue) ?: return false
        val conditionInstant = convertToInstant(conditionValue) ?: return false

        return when (operator) {
            Operator.EQUALS -> attributeInstant == conditionInstant
            Operator.NOT_EQUALS -> attributeInstant != conditionInstant
            Operator.GREATER_THAN -> attributeInstant.isAfter(conditionInstant)
            Operator.GREATER_EQUAL -> !attributeInstant.isBefore(conditionInstant)
            Operator.LESS_THAN -> attributeInstant.isBefore(conditionInstant)
            Operator.LESS_EQUAL -> !attributeInstant.isAfter(conditionInstant)
            else -> throw IllegalArgumentException("Unsupported operator for date evaluation: $operator")
        }
    }

    private fun convertToInstant(value: Any): Instant? {
        return when (value) {
            is Instant -> value
            is LocalDateTime -> value.toInstant(ZoneOffset.UTC)
            is LocalDate -> value.atStartOfDay().toInstant(ZoneOffset.UTC)
            is String -> parseStringToInstant(value)
            is Number -> Instant.ofEpochMilli(value.toLong()) // Unix timestamp
            else -> null
        }
    }

    private fun parseStringToInstant(dateString: String): Instant? {
        val formatters = listOf(
            DateTimeFormatter.ISO_INSTANT,          // "2024-08-16T10:15:30.123Z"
            DateTimeFormatter.ISO_LOCAL_DATE_TIME,  // "2024-08-16T10:15:30"
            DateTimeFormatter.ISO_LOCAL_DATE,       // "2024-08-16"
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"),
            DateTimeFormatter.ofPattern("yyyy-MM-dd"),
            DateTimeFormatter.ofPattern("MM/dd/yyyy"),
            DateTimeFormatter.ofPattern("dd-MM-yyyy")
        )

        for (formatter in formatters) {
            try {
                return when {
                    formatter == DateTimeFormatter.ISO_INSTANT ->
                        Instant.parse(dateString)
                    formatter == DateTimeFormatter.ISO_LOCAL_DATE ->
                        LocalDate.parse(dateString, formatter).atStartOfDay().toInstant(ZoneOffset.UTC)
                    dateString.contains("T") ->
                        LocalDateTime.parse(dateString, formatter).toInstant(ZoneOffset.UTC)
                    else ->
                        LocalDate.parse(dateString, formatter).atStartOfDay().toInstant(ZoneOffset.UTC)
                }
            } catch (e: Exception) {
                continue // Errors won't be handled, the next formatter might work
            }
        }
        return null
    }

}