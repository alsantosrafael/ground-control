package com.platform.groundcontrol.application.services

import com.platform.groundcontrol.domain.enums.Reason
import com.platform.groundcontrol.domain.evaluators.ConditionEvaluator
import com.platform.groundcontrol.domain.valueobjects.*
import org.springframework.cache.annotation.Cacheable
import org.springframework.stereotype.Service
import java.time.Instant
import kotlin.math.abs

@Service
class EvaluationEngineService(
    private val conditionEvaluators: List<ConditionEvaluator>
) {

    @Cacheable("evaluations", key = "#flag.code.value + ':' + #context.subjectId")
    fun evaluate(flag: FeatureFlag, context: EvaluationContext): EvaluationResult {
        if (!flag.enabled) {
            return EvaluationResult(
                false,
                value = null,
                reason = Reason.FLAG_DISABLED
            )
        }

        if(flag.isExpired()) {
            return EvaluationResult(
                false,
                value = null,
                reason = Reason.FLAG_EXPIRED
            )
        }

        val sortedRules = flag.rolloutRules.filter { it.active }
            .sortedBy { it.priority ?: 0 }

        for (rule in sortedRules) {
            val ruleResult = evaluateRule(rule, context, flag)
            if (ruleResult != null) {
                return ruleResult
            }
        }

        return EvaluationResult(
            enabled = true,
            value = flag.value,
            valueType = flag.valueType,
            reason = Reason.DEFAULT
        )
    }

    private fun evaluateRule(rule: RolloutRule, context: EvaluationContext, flag: FeatureFlag): EvaluationResult? {
        val now = Instant.now()
        if (rule.startAt != null && now.isBefore(rule.startAt)) {
            return null
        }
        if (rule.endAt != null && now.isAfter(rule.endAt)) {
            return null
        }

        val conditionsMatch = if (rule.hasConditions()) {
            evaluateConditions(rule, context)
        } else {
            true
        }

        if (!conditionsMatch) {
            return null
        }

        val passesPercentage = rule.percentage?.let { percentage ->
            isSubjectInPercentage(context.subjectId, flag.code.value, percentage)
        } ?: true

        if (!passesPercentage) {
            return null
        }

        // Rule matched! Return the result
        return EvaluationResult(
            enabled = true,
            value = rule.getRuleValue(),
            valueType = flag.valueType,
            variant = rule.variantName,
            reason = Reason.RULE_MATCH
        )
    }

    private fun evaluateConditions(rule: RolloutRule, context: EvaluationContext): Boolean {
        // ALL conditions must pass (AND logic)
        return rule.conditions.all { condition ->
            evaluateCondition(condition, context)
        }
    }

    private fun evaluateCondition(condition: Condition, context: EvaluationContext): Boolean {
        val attributeValue = context.attributes[condition.attribute] ?: return false
        val evaluator = conditionEvaluators.firstOrNull {
            it.canHandle(condition.operator, condition.dataType) 
        } ?: throw IllegalArgumentException(
            "No evaluator found for operator ${condition.operator} with data type ${condition.dataType}"
        )

        return try {
            evaluator.evaluate(attributeValue, condition.value, condition.operator)
        } catch (_: Exception) {
            false
        }
    }

    private fun isSubjectInPercentage(subjectId: String, flagCode: String, percentage: Double): Boolean {
        val hash = "$flagCode:$subjectId".hashCode()
        val bucket = abs(hash) % 100
        return bucket < percentage.toInt()
    }
}