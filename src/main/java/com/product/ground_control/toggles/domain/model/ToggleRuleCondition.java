package com.product.ground_control.toggles.domain.model;

/**
 * Defines a single matching condition within a rule.
 * The 'property' is the key in the context to look up for comparison.
 */
public record ToggleRuleCondition(
    String property,
    Operator operator,
    String value
) {
    public boolean matches(String contextValue) {
        if (contextValue == null) return false;

        return switch (operator) {
            case EQUALS -> contextValue.equals(value);
            case NOT_EQUALS -> !contextValue.equals(value);
            case CONTAINS -> contextValue.contains(value);
            case GREATER_THAN -> compareNumerically(contextValue, value) > 0;
            case GREATER_OR_EQUAL -> compareNumerically(contextValue, value) >= 0;
            case LESS_THAN -> compareNumerically(contextValue, value) < 0;
            case LESS_OR_EQUAL -> compareNumerically(contextValue, value) <= 0;
            case PERCENTAGE -> throw new IllegalStateException("PERCENTAGE operator must be handled at the Rule level");
        };
    }

    private int compareNumerically(String actual, String expected) {
        try {
            double actualNum = Double.parseDouble(actual);
            double expectedNum = Double.parseDouble(expected);
            return Double.compare(actualNum, expectedNum);
        } catch (NumberFormatException e) {
            return actual.compareTo(expected);
        }
    }
}
