package com.ruleengine.controller;

import com.ruleengine.model.Node;
import com.ruleengine.model.Rule;
import com.ruleengine.service.RuleService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.HashMap;

@RestController
@RequestMapping("/api/rules")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")  // Enable CORS for development
public class RuleController {

    private final RuleService ruleService;

    @PostMapping("/create")
    public ResponseEntity<?> createRule(@RequestBody Map<String, String> requestBody) {
        try {
            String ruleString = requestBody.get("ruleString");
            String ruleName = requestBody.get("ruleName");

            if (ruleName == null || ruleName.trim().isEmpty()) {
                return ResponseEntity.badRequest()
                        .body(Map.of("error", "Rule name is required"));
            }

            if (ruleString == null || ruleString.trim().isEmpty()) {
                return ResponseEntity.badRequest()
                        .body(Map.of("error", "Rule string is required"));
            }

            Rule rule = ruleService.createRule(ruleString.trim(), ruleName.trim());
            return ResponseEntity.ok(Map.of(
                    "message", "Rule created successfully",
                    "rule", rule
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping("/evaluate")
    public ResponseEntity<?> evaluateRule(
            @RequestBody Map<String, Object> userData,
            @RequestParam Long ruleId) {
        try {
            boolean result = ruleService.evaluateRule(ruleId, userData);
            Map<String, Object> response = new HashMap<>();
            response.put("result", result);
            response.put("message", result ? "Rule conditions met" : "Rule conditions not met");
            response.put("evaluatedData", userData);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping("/combine")
    public ResponseEntity<?> combineRules(@RequestBody List<Long> ruleIds) {
        try {
            if (ruleIds == null || ruleIds.size() < 2) {
                return ResponseEntity.badRequest()
                        .body(Map.of("error", "At least two rules are required for combination"));
            }

            Node combinedRule = ruleService.combineRules(ruleIds);
            return ResponseEntity.ok(Map.of(
                    "message", "Rules combined successfully",
                    "combinedRule", combinedRule
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", e.getMessage()));
        }
    }

    @PutMapping("/modify")
    public ResponseEntity<?> modifyRule(
            @RequestParam Long ruleId,
            @RequestBody String newExpression) {
        try {
            if (newExpression == null || newExpression.trim().isEmpty()) {
                return ResponseEntity.badRequest()
                        .body(Map.of("error", "New expression is required"));
            }

            Rule modifiedRule = ruleService.modifyRule(ruleId, newExpression.trim());
            return ResponseEntity.ok(Map.of(
                    "message", "Rule modified successfully",
                    "rule", modifiedRule
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/getRules")
    public ResponseEntity<?> getRules() {
        try {
            List<Rule> rules = ruleService.getRules();
            return ResponseEntity.ok(Map.of(
                    "rules", rules,
                    "count", rules.size()
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", e.getMessage()));
        }
    }

    @DeleteMapping("/delete")
    public ResponseEntity<?> deleteRule(@RequestParam Long ruleId) {
        try {
            ruleService.deleteRule(ruleId);
            return ResponseEntity.ok(Map.of("message", "Rule deleted successfully"));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", e.getMessage()));
        }
    }
}