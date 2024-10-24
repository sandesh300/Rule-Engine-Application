package com.ruleengine.service;

import com.ruleengine.exception.CustomException;
import com.ruleengine.model.Node;
import com.ruleengine.model.Rule;
import com.ruleengine.repository.RuleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
@RequiredArgsConstructor
public class RuleService {

    private final RuleRepository ruleRepository;
    private static final Set<String> VALID_DEPARTMENTS = new HashSet<>(Arrays.asList("Sales", "Marketing", "IT", "HR"));
    private static final Pattern CONDITION_PATTERN =
            Pattern.compile("(\\w+)\\s*([<>=])\\s*('[^']*'|\\d+(\\.\\d+)?)");

    // Create a rule from the input string
    public Rule createRule(String ruleString, String ruleName) {
        validateRuleString(ruleString);
        Node astRoot = parseRuleStringToAST(ruleString);
        Rule rule = new Rule();
        rule.setRuleName(ruleName);
        rule.setRootNode(astRoot);
        rule.setCreatedAt(LocalDateTime.now());
        return ruleRepository.save(rule);
    }

    private void validateRuleString(String ruleString) {
        if (ruleString == null || ruleString.trim().isEmpty()) {
            throw new CustomException("Rule string cannot be empty");
        }

        // Check for balanced parentheses
        int balance = 0;
        for (char c : ruleString.toCharArray()) {
            if (c == '(') balance++;
            if (c == ')') balance--;
            if (balance < 0) {
                throw new CustomException("Invalid parentheses in rule");
            }
        }
        if (balance != 0) {
            throw new CustomException("Unmatched parentheses in rule");
        }

        // Validate operators
        if (!ruleString.contains("AND") && !ruleString.contains("OR")) {
            throw new CustomException("Rule must contain at least one logical operator (AND/OR)");
        }
    }

    private Node parseRuleStringToAST(String ruleString) {
        ruleString = ruleString.trim();

        // Remove outer parentheses if they exist
        while (ruleString.startsWith("(") && ruleString.endsWith(")")) {
            String inner = ruleString.substring(1, ruleString.length() - 1).trim();
            if (isBalancedParentheses(inner)) {
                ruleString = inner;
            } else {
                break;
            }
        }

        // Find the main logical operator (AND/OR) at the current level
        int parenthesesCount = 0;
        int mainOperatorIndex = -1;
        String mainOperator = null;

        char[] chars = ruleString.toCharArray();
        for (int i = 0; i < chars.length; i++) {
            if (chars[i] == '(') parenthesesCount++;
            else if (chars[i] == ')') parenthesesCount--;

            // Only look for operators at the current level (parenthesesCount == 0)
            if (parenthesesCount == 0) {
                if (i + 3 < ruleString.length()) {
                    String possibleOp = ruleString.substring(i, i + 3);
                    if (possibleOp.equals("AND") || possibleOp.equals("OR ")) {
                        mainOperatorIndex = i;
                        mainOperator = possibleOp.trim();
                        break;
                    }
                }
            }
        }

        // If no main operator found, this might be a leaf condition
        if (mainOperatorIndex == -1) {
            return parseCondition(ruleString);
        }

        // Split the rule string around the main operator
        String leftPart = ruleString.substring(0, mainOperatorIndex).trim();
        String rightPart = ruleString.substring(mainOperatorIndex + mainOperator.length()).trim();

        // Recursively parse left and right parts
        Node leftNode = parseRuleStringToAST(leftPart);
        Node rightNode = parseRuleStringToAST(rightPart);

        return new Node("operator", leftNode, rightNode, mainOperator);
    }

    private boolean isBalancedParentheses(String str) {
        int count = 0;
        for (char c : str.toCharArray()) {
            if (c == '(') count++;
            if (c == ')') count--;
            if (count < 0) return false;
        }
        return count == 0;
    }

    private Node parseCondition(String condition) {
        condition = condition.trim();
        while (condition.startsWith("(") && condition.endsWith(")")) {
            condition = condition.substring(1, condition.length() - 1).trim();
        }
        Matcher matcher = CONDITION_PATTERN.matcher(condition.trim());
        if (!matcher.matches()) {
            throw new CustomException("Invalid condition format: " + condition);
        }

        String attribute = matcher.group(1);
        String operator = matcher.group(2);
        String value = matcher.group(3);

        // Validate department values
        if (attribute.equals("department")) {
            String deptValue = value.replace("'", "");
            if (!VALID_DEPARTMENTS.contains(deptValue)) {
                throw new CustomException("Invalid department value: " + deptValue);
            }
        }

        return new Node("operand", null, null, condition.trim());
    }

    public boolean evaluateRule(Long ruleId, Map<String, Object> userData) {
        Optional<Rule> ruleOptional = ruleRepository.findById(ruleId);
        if (ruleOptional.isEmpty()) {
            throw new CustomException("Rule not found");
        }

        validateUserData(userData);
        Node astRoot = ruleOptional.get().getRootNode();
        return evaluateNode(astRoot, userData);
    }

    private void validateUserData(Map<String, Object> userData) {
        if (userData == null || userData.isEmpty()) {
            throw new CustomException("User data cannot be empty");
        }

        // Validate required fields
        List<String> requiredFields = Arrays.asList("age", "department", "salary", "experience");
        for (String field : requiredFields) {
            if (!userData.containsKey(field)) {
                throw new CustomException("Missing required field: " + field);
            }
        }

        // Validate data types
        if (!(userData.get("age") instanceof Integer)) {
            throw new CustomException("Age must be an integer");
        }
        if (!(userData.get("salary") instanceof Integer)) {
            throw new CustomException("Salary must be an integer");
        }
        if (!(userData.get("experience") instanceof Integer)) {
            throw new CustomException("Experience must be an integer");
        }
        if (!(userData.get("department") instanceof String)) {
            throw new CustomException("Department must be a string");
        }
    }

    private boolean evaluateNode(Node node, Map<String, Object> data) {
        if (node == null) return true;

        if ("operand".equals(node.getType())) {
            return evaluateOperand(node.getValue(), data);
        }

        boolean leftResult = evaluateNode(node.getLeft(), data);
        boolean rightResult = evaluateNode(node.getRight(), data);

        return switch (node.getValue()) {
            case "AND" -> leftResult && rightResult;
            case "OR" -> leftResult || rightResult;
            default -> throw new CustomException("Unknown operator: " + node.getValue());
        };
    }

    private boolean evaluateOperand(String condition, Map<String, Object> data) {
        Matcher matcher = CONDITION_PATTERN.matcher(condition);
        if (!matcher.matches()) {
            throw new CustomException("Invalid condition format: " + condition);
        }

        String attribute = matcher.group(1);
        String operator = matcher.group(2);
        String valueStr = matcher.group(3).replace("'", "");

        Object userValue = data.get(attribute);
        if (userValue == null) {
            throw new CustomException("Attribute not found in user data: " + attribute);
        }

        // Handle string comparisons
        if (userValue instanceof String) {
            return evaluateStringComparison((String) userValue, operator, valueStr);
        }

        // Handle numeric comparisons
        if (userValue instanceof Number) {
            return evaluateNumericComparison((Number) userValue, operator, valueStr);
        }

        throw new CustomException("Unsupported data type for attribute: " + attribute);
    }

    private boolean evaluateStringComparison(String userValue, String operator, String targetValue) {
        return switch (operator) {
            case "=" -> userValue.equals(targetValue);
            default -> throw new CustomException("Invalid operator for string comparison: " + operator);
        };
    }

    private boolean evaluateNumericComparison(Number userValue, String operator, String targetValue) {
        double userNum = userValue.doubleValue();
        double targetNum = Double.parseDouble(targetValue);

        return switch (operator) {
            case ">" -> userNum > targetNum;
            case "<" -> userNum < targetNum;
            case "=" -> Math.abs(userNum - targetNum) < 0.0001;
            default -> throw new CustomException("Invalid operator for numeric comparison: " + operator);
        };
    }

    // Combine multiple rules into a single AST
    public Node combineRules(List<Long> ruleIds) {
        if (ruleIds.isEmpty()) {
            throw new CustomException("No rules to combine");
        }

        Node combinedRoot = new Node();
        combinedRoot.setType("operator");
        combinedRoot.setValue("AND");

        for (Long ruleId : ruleIds) {
            Optional<Rule> ruleOptional = ruleRepository.findById(ruleId);
            if (ruleOptional.isPresent()) {
                Node ruleNode = ruleOptional.get().getRootNode();
                // Add to the combined AST (for simplicity, combine with AND logic)
                if (combinedRoot.getLeft() == null) {
                    combinedRoot.setLeft(ruleNode);
                } else {
                    // Create a new node combining the previous combinedRoot and current ruleNode
                    Node newRoot = new Node("operator", combinedRoot, ruleNode, "AND");
                    combinedRoot = newRoot;
                }
            }
        }
        return combinedRoot;
    }

    // Modify an existing rule
    public Rule modifyRule(Long ruleId, String newExpression) {
        Optional<Rule> ruleOptional = ruleRepository.findById(ruleId);
        if (ruleOptional.isEmpty()) {
            throw new CustomException("Rule not found");
        }

        Rule rule = ruleOptional.get();
        Node newRoot = parseRuleStringToAST(newExpression); // Parse the new expression
        rule.setRootNode(newRoot);
        return ruleRepository.save(rule);
    }

    // Get all rules from the repository
    public List<Rule> getRules() {
        return ruleRepository.findAll();
    }

    // Delete a rule by ID
    public void deleteRule(Long ruleId) {
        Optional<Rule> ruleOptional = ruleRepository.findById(ruleId);
        if (ruleOptional.isEmpty()) {
            throw new CustomException("Rule not found");
        }
        ruleRepository.deleteById(ruleId);
    }
}
