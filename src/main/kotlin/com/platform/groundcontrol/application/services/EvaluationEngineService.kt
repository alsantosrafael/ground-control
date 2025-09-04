package com.platform.groundcontrol.application.services

import com.platform.groundcontrol.domain.enums.Reason
import com.platform.groundcontrol.domain.evaluators.ConditionEvaluator
import com.platform.groundcontrol.domain.valueobjects.*
import org.slf4j.LoggerFactory
import org.slf4j.MDC
import org.springframework.cache.annotation.Cacheable
import org.springframework.stereotype.Service
import java.time.Instant
import kotlin.math.abs

@Service
class EvaluationEngineService(
    private val conditionEvaluators: List<ConditionEvaluator>
) {
    
    companion object {
        private val logger = LoggerFactory.getLogger(EvaluationEngineService::class.java)
    }

    @Cacheable("evaluations", key = "#flag.code + ':' + (#context.subjectId ?: 'global')")
    fun evaluate(flag: FeatureFlag, context: EvaluationContext): EvaluationResult {
        val startTime = System.currentTimeMillis()
        val flagCode = flag.code
        val subjectId = context.subjectId
        
        // Set MDC for distributed tracing
        MDC.put("flagCode", flagCode)
        MDC.put("subjectId", subjectId)
        MDC.put("operation", "evaluate")
        
        try {
            logger.debug("Starting flag evaluation: flag={}, subject={}, attributeCount={}", 
                flagCode, subjectId, context.attributes.size)
            
            // Early exit conditions with specific logging
            if (!flag.enabled) {
                logger.info("Flag evaluation: DISABLED - flag={}, subject={}", flagCode, subjectId)
                return EvaluationResult(
                    false,
                    value = null,
                    reason = Reason.FLAG_DISABLED
                )
            }

            if(flag.isExpired()) {
                logger.warn("Flag evaluation: EXPIRED - flag={}, subject={}, dueAt={}", 
                    flagCode, subjectId, flag.dueAt)
                return EvaluationResult(
                    false,
                    value = null,
                    reason = Reason.FLAG_EXPIRED
                )
            }

            val sortedRules = flag.rolloutRules.filter { it.active }
                .sortedBy { it.priority ?: 0 }

            logger.debug("Rule evaluation: flag={}, activeRules={}, totalRules={}", 
                flagCode, sortedRules.size, flag.rolloutRules.size)

            for ((index, rule) in sortedRules.withIndex()) {
                logger.debug("Evaluating rule {}/{}: flag={}, ruleId={}, priority={}", 
                    index + 1, sortedRules.size, flagCode, rule.id, rule.priority)
                
                val ruleResult = evaluateRule(rule, context, flag)
                if (ruleResult != null) {
                    val evalTime = System.currentTimeMillis() - startTime
                    logger.info("Flag evaluation: RULE_MATCH - flag={}, subject={}, ruleId={}, variant={}, evalTimeMs={}", 
                        flagCode, subjectId, rule.id, ruleResult.variant, evalTime)
                    return ruleResult
                }
            }

            // No rules matched - return default value
            val evalTime = System.currentTimeMillis() - startTime
            logger.info("Flag evaluation: DEFAULT - flag={}, subject={}, value={}, evalTimeMs={}", 
                flagCode, subjectId, flag.value, evalTime)
            
            return EvaluationResult(
                enabled = true,
                value = flag.value,
                valueType = flag.valueType,
                reason = Reason.DEFAULT
            )
            
        } catch (e: Exception) {
            val evalTime = System.currentTimeMillis() - startTime
            logger.error("Flag evaluation: ERROR - flag={}, subject={}, error={}, evalTimeMs={}", 
                flagCode, subjectId, e.message, evalTime, e)
            throw e
        } finally {
            MDC.clear()
        }
    }

    private fun evaluateRule(rule: RolloutRule, context: EvaluationContext, flag: FeatureFlag): EvaluationResult? {
        val ruleId = rule.id
        val flagCode = flag.code
        val subjectId = context.subjectId
        
        // Time-based rule validation
        val now = Instant.now()
        if (rule.startAt != null && now.isBefore(rule.startAt)) {
            logger.debug("Rule evaluation: TIME_NOT_STARTED - flag={}, ruleId={}, startAt={}", 
                flagCode, ruleId, rule.startAt)
            return null
        }
        if (rule.endAt != null && now.isAfter(rule.endAt)) {
            logger.debug("Rule evaluation: TIME_EXPIRED - flag={}, ruleId={}, endAt={}", 
                flagCode, ruleId, rule.endAt)
            return null
        }

        // Condition evaluation
        val conditionsMatch = if (rule.hasConditions()) {
            logger.debug("Rule evaluation: CHECKING_CONDITIONS - flag={}, ruleId={}, conditionCount={}", 
                flagCode, ruleId, rule.conditions.size)
            evaluateConditions(rule, context)
        } else {
            logger.debug("Rule evaluation: NO_CONDITIONS - flag={}, ruleId={}", flagCode, ruleId)
            true
        }

        if (!conditionsMatch) {
            logger.debug("Rule evaluation: CONDITIONS_FAILED - flag={}, ruleId={}, subject={}", 
                flagCode, ruleId, subjectId)
            return null
        }

        // Percentage rollout evaluation
        val passesPercentage = rule.percentage?.let { percentage ->
            if (context.subjectId == null) {
                logger.debug("Rule evaluation: PERCENTAGE_SKIPPED - flag={}, ruleId={}, reason=no_subject_id", 
                    flagCode, ruleId)
                false
            } else {
                val passes = isSubjectInPercentage(context.subjectId, flag.code, percentage)
                logger.debug("Rule evaluation: PERCENTAGE_CHECK - flag={}, ruleId={}, subject={}, percentage={}, passes={}", 
                    flagCode, ruleId, subjectId, percentage, passes)
                passes
            }
        } ?: true

        if (!passesPercentage) {
            logger.debug("Rule evaluation: PERCENTAGE_FAILED - flag={}, ruleId={}, subject={}", 
                flagCode, ruleId, subjectId)
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
        val flagCode = MDC.get("flagCode")
        val ruleId = rule.id
        
        // ALL conditions must pass (AND logic)
        val results = rule.conditions.map { condition ->
            val result = evaluateCondition(condition, context)
            logger.debug("Condition evaluation: flag={}, ruleId={}, attribute={}, operator={}, expected={}, actual={}, result={}", 
                flagCode, ruleId, condition.attribute, condition.operator, condition.value, 
                context.attributes[condition.attribute], result)
            result
        }
        
        val allPassed = results.all { it }
        logger.debug("Conditions evaluation: flag={}, ruleId={}, total={}, passed={}, result={}", 
            flagCode, ruleId, results.size, results.count { it }, allPassed)
        
        return allPassed
    }

    private fun evaluateCondition(condition: Condition, context: EvaluationContext): Boolean {
        val attributeValue = context.attributes[condition.attribute]
        
        if (attributeValue == null) {
            logger.debug("Condition evaluation: MISSING_ATTRIBUTE - attribute={}, operator={}", 
                condition.attribute, condition.operator)
            return false
        }
        
        val evaluator = conditionEvaluators.firstOrNull {
            it.canHandle(condition.operator, condition.dataType) 
        } ?: throw IllegalArgumentException(
            "No evaluator found for operator ${condition.operator} with data type ${condition.dataType}"
        ).also {
            logger.error("Condition evaluation: NO_EVALUATOR - operator={}, dataType={}", 
                condition.operator, condition.dataType)
        }

        return try {
            evaluator.evaluate(attributeValue, condition.value, condition.operator)
        } catch (e: Exception) {
            logger.warn("Condition evaluation: EVALUATOR_ERROR - attribute={}, operator={}, error={}", 
                condition.attribute, condition.operator, e.message)
            false
        }
    }

    private fun isSubjectInPercentage(subjectId: String, flagCode: String, percentage: Double): Boolean {
        val hash = "$flagCode:$subjectId".hashCode()
        val bucket = abs(hash) % 100
        return bucket < percentage.toInt()
    }
}