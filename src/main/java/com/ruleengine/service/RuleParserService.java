package com.ruleengine.service;

import com.ruleengine.model.Node;
import com.ruleengine.model.Rule;
import com.ruleengine.exception.CustomException;
import java.util.*;
import java.util.regex.Pattern;
import java.util.regex.Matcher;

public class RuleParserService {

    private static final String OPERATOR_PATTERN = "AND|OR";
    private static final String CONDITION_PATTERN = "[a-zA-Z_]+\\s*[><=]+\\s*['\"]?[\\w.]+['\"]?";

    public Node parseRuleString(String ruleString) {
        // Remove unnecessary parentheses and whitespace
        ruleString = ruleString.trim().replaceAll("^\\(+|\\)+$", "");

        // Base case: single condition
        if (Pattern.matches(CONDITION_PATTERN, ruleString)) {
            return createOperandNode(ruleString);
        }

        // Find the main operator (AND/OR)
        String operator = findMainOperator(ruleString);
        if (operator == null) {
            throw new CustomException("Invalid rule syntax");
        }

        // Split the rule into left and right parts
        String[] parts = splitOnOperator(ruleString, operator);

        // Create operator node and recursively parse children
        Node node = new Node();
        node.setType("operator");
        node.setValue(operator);
        node.setLeft(parseRuleString(parts[0]));
        node.setRight(parseRuleString(parts[1]));

        return node;
    }

    private String findMainOperator(String rule) {
        int parenthesesCount = 0;
        String[] tokens = rule.split("\\s+");

        for (int i = 0; i < tokens.length; i++) {
            parenthesesCount += countParentheses(tokens[i]);

            if (parenthesesCount == 0 && (tokens[i].equals("AND") || tokens[i].equals("OR"))) {
                return tokens[i];
            }
        }
        return null;
    }

    private int countParentheses(String token) {
        int count = 0;
        for (char c : token.toCharArray()) {
            if (c == '(') count++;
            if (c == ')') count--;
        }
        return count;
    }

    private String[] splitOnOperator(String rule, String operator) {
        int parenthesesCount = 0;
        int operatorIndex = -1;
        String[] tokens = rule.split("\\s+");

        StringBuilder leftPart = new StringBuilder();
        StringBuilder rightPart = new StringBuilder();
        boolean operatorFound = false;

        for (String token : tokens) {
            if (!operatorFound) {
                parenthesesCount += countParentheses(token);
                if (parenthesesCount == 0 && token.equals(operator)) {
                    operatorFound = true;
                    continue;
                }
                leftPart.append(token).append(" ");
            } else {
                rightPart.append(token).append(" ");
            }
        }

        return new String[]{leftPart.toString().trim(), rightPart.toString().trim()};
    }

    private Node createOperandNode(String condition) {
        Node node = new Node();
        node.setType("operand");
        node.setValue(condition.trim());
        return node;
    }
}