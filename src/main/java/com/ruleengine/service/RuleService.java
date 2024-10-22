package com.ruleengine.service;

import com.ruleengine.exception.CustomException;
import com.ruleengine.model.Node;
import com.ruleengine.model.Rule;
import com.ruleengine.repository.RuleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class RuleService {

    private final RuleRepository ruleRepository;

    // Create a rule from the input string
    public Rule createRule(String ruleString, String ruleName) {
        Node astRoot = parseRuleStringToAST(ruleString); // Parse rule to AST
        Rule rule = new Rule();
        rule.setRuleName(ruleName);
        rule.setRootNode(astRoot);
        rule.setCreatedAt(LocalDateTime.now());  // Use LocalDateTime for createdAt
        return ruleRepository.save(rule);
    }

    // Parse the rule string into an Abstract Syntax Tree (AST)
    private Node parseRuleStringToAST(String ruleString) {
        // TODO: Implement a real parser. For now, we return a mock AST for simplicity.
        Node root = new Node();
        root.setType("operator");
        root.setValue("AND");

        Node left = new Node();
        left.setType("operand");
        left.setValue("age > 30");

        Node right = new Node();
        right.setType("operand");
        right.setValue("salary > 50000");

        root.setLeft(left);
        root.setRight(right);
        return root;
    }

    // Evaluate a rule using the provided user data
    public boolean evaluateRule(Long ruleId, Map<String, Object> userData) {
        Optional<Rule> ruleOptional = ruleRepository.findById(ruleId);
        if (ruleOptional.isEmpty()) {
            throw new CustomException("Rule not found");
        }

        Node astRoot = ruleOptional.get().getRootNode();
        return evaluateNode(astRoot, userData); // Evaluate the AST against user data
    }

    // Recursive function to evaluate an AST node
    private boolean evaluateNode(Node node, Map<String, Object> data) {
        if (node == null) return true;

        if ("operand".equals(node.getType())) {
            return evaluateOperand(node.getValue(), data); // Evaluate operand (e.g., "age > 30")
        }

        // Assume it's an operator (AND/OR)
        boolean leftResult = evaluateNode(node.getLeft(), data);
        boolean rightResult = evaluateNode(node.getRight(), data);

        switch (node.getValue()) {
            case "AND":
                return leftResult && rightResult;
            case "OR":
                return leftResult || rightResult;
            default:
                throw new CustomException("Unknown operator: " + node.getValue());
        }
    }

    // Evaluate a single operand condition (e.g., "age > 30")
    private boolean evaluateOperand(String condition, Map<String, Object> data) {
        String[] parts = condition.split(" ");
        String attribute = parts[0];  // e.g., "age"
        String operator = parts[1];   // e.g., ">"
        String value = parts[2];      // e.g., "30"

        Object userValue = data.get(attribute);  // Fetch attribute value from user data
        if (userValue == null) {
            throw new CustomException("Attribute " + attribute + " not found in user data");
        }

        // Handle different comparison operators
        switch (operator) {
            case ">":
                return (Integer) userValue > Integer.parseInt(value);
            case "<":
                return (Integer) userValue < Integer.parseInt(value);
            case "=":
                return userValue.toString().equals(value);
            default:
                throw new CustomException("Invalid operator in condition: " + operator);
        }
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
        return ruleRepository.save(rule); // Save the modified rule
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
