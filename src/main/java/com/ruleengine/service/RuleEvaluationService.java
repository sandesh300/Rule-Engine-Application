package com.ruleengine.service;

import com.ruleengine.exception.CustomException;
import com.ruleengine.model.Node;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


@Service
public class RuleEvaluationService {

    public boolean evaluateNode(Node node, Map<String, Object> data) {
        if (node == null) return true;

        if ("operand".equals(node.getType())) {
            return evaluateCondition(node.getValue(), data);
        }

        boolean leftResult = evaluateNode(node.getLeft(), data);
        boolean rightResult = evaluateNode(node.getRight(), data);

        return switch (node.getValue()) {
            case "AND" -> leftResult && rightResult;
            case "OR" -> leftResult || rightResult;
            default -> throw new CustomException("Unknown operator: " + node.getValue());
        };
    }

    private boolean evaluateCondition(String condition, Map<String, Object> data) {
        // Parse condition into components
        Pattern pattern = Pattern.compile("([a-zA-Z_]+)\\s*([><=]+)\\s*(['\"]?[\\w.]+['\"]?)");
        Matcher matcher = pattern.matcher(condition);

        if (!matcher.matches()) {
            throw new CustomException("Invalid condition format: " + condition);
        }

        String field = matcher.group(1);
        String operator = matcher.group(2);
        String valueStr = matcher.group(3).replaceAll("['\"]", "");

        Object actualValue = data.get(field);
        if (actualValue == null) {
            throw new CustomException("Field not found in data: " + field);
        }

        // Convert value based on actual value type
        Object expectedValue = convertValue(valueStr, actualValue.getClass());

        return compareValues(actualValue, expectedValue, operator);
    }

    @SuppressWarnings("unchecked")
    private boolean compareValues(Object actual, Object expected, String operator) {
        if (actual instanceof Number && expected instanceof Number) {
            double actualNum = ((Number) actual).doubleValue();
            double expectedNum = ((Number) expected).doubleValue();

            return switch (operator) {
                case ">" -> actualNum > expectedNum;
                case ">=" -> actualNum >= expectedNum;
                case "<" -> actualNum < expectedNum;
                case "<=" -> actualNum <= expectedNum;
                case "=" -> actualNum == expectedNum;
                default -> throw new CustomException("Invalid operator for numeric comparison: " + operator);
            };
        }

        if (actual instanceof String && expected instanceof String) {
            return switch (operator) {
                case "=" -> actual.equals(expected);
                default -> throw new CustomException("Invalid operator for string comparison: " + operator);
            };
        }

        throw new CustomException("Incompatible types for comparison");
    }

    private Object convertValue(String value, Class<?> targetType) {
        try {
            if (targetType == Integer.class) {
                return Integer.parseInt(value);
            } else if (targetType == Double.class) {
                return Double.parseDouble(value);
            } else if (targetType == String.class) {
                return value;
            }
            throw new CustomException("Unsupported type conversion: " + targetType);
        } catch (NumberFormatException e) {
            throw new CustomException("Invalid number format: " + value);
        }
    }
}