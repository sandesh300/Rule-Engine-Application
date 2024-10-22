package com.ruleengine.controller;


import com.ruleengine.model.Node;
import com.ruleengine.model.Rule;
import com.ruleengine.service.RuleParserService;
import com.ruleengine.service.RuleService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/rules")
@RequiredArgsConstructor
public class RuleController {

    private final RuleService ruleService;

    // Create a new rule
    @PostMapping("/create")
    public ResponseEntity<Rule> createRule(@RequestBody Map<String, String> requestBody) {
        String ruleString = requestBody.get("ruleString");
        String ruleName = requestBody.get("ruleName");
        Rule rule = ruleService.createRule(ruleString, ruleName);
        return ResponseEntity.ok(rule);
    }


    // Evaluate a rule
    @PostMapping("/evaluate")
    public ResponseEntity<Boolean> evaluateRule(@RequestBody Map<String, Object> userData, @RequestParam Long ruleId) {
        boolean result = ruleService.evaluateRule(ruleId, userData);
        return ResponseEntity.ok(result);
    }

    // Combine multiple rules
    @PostMapping("/combine")
    public ResponseEntity<Node> combineRules(@RequestBody List<Long> ruleIds) {
        Node combinedRule = ruleService.combineRules(ruleIds);
        return ResponseEntity.ok(combinedRule);
    }

    // Modify an existing rule
    @PutMapping("/modify")
    public ResponseEntity<Rule> modifyRule(@RequestParam Long ruleId, @RequestBody String newExpression) {
        Rule modifiedRule = ruleService.modifyRule(ruleId, newExpression);
        return ResponseEntity.ok(modifiedRule);
    }

    // Get all rules
    @GetMapping("/getRules")
    public ResponseEntity<List<Rule>> getRules() {
        List<Rule> rules = ruleService.getRules();
        return ResponseEntity.ok(rules);
    }

    // Delete a rule by ID
    @DeleteMapping("/delete")
    public ResponseEntity<String> deleteRule(@RequestParam Long ruleId) {
        ruleService.deleteRule(ruleId);
        return ResponseEntity.ok("Rule deleted successfully");
    }
}
