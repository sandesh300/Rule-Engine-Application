package com.ruleengine;


import com.ruleengine.exception.CustomException;
import com.ruleengine.model.Node;
import com.ruleengine.model.Rule;
import com.ruleengine.repository.RuleRepository;
import com.ruleengine.service.RuleService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RuleServiceTest {

    @Mock
    private RuleRepository ruleRepository;

    @InjectMocks
    private RuleService ruleService;

    private Rule seniorSalesRule;
    private Rule experiencedITRule;
    private Map<String, Object> userData;

    @BeforeEach
    void setUp() {
        // Setup test data
        seniorSalesRule = new Rule();
        seniorSalesRule.setId(1L);
        seniorSalesRule.setRuleName("Senior Sales Rule");

        experiencedITRule = new Rule();
        experiencedITRule.setId(2L);
        experiencedITRule.setRuleName("Experienced IT Rule");

        // Setup sample user data
        userData = new HashMap<>();
        userData.put("age", 35);
        userData.put("department", "Sales");
        userData.put("salary", 75000);
        userData.put("experience", 8);
    }

    @Test
    void createRule_ValidRuleString_Success() {
        // Given
        String ruleString = "age > 30 AND department = 'Sales'";
        String ruleName = "Senior Sales Rule";
        when(ruleRepository.save(any(Rule.class))).thenAnswer(i -> i.getArguments()[0]);

        // When
        Rule result = ruleService.createRule(ruleString, ruleName);

        // Then
        assertNotNull(result);
        assertNotNull(result.getRootNode());
        assertEquals("operator", result.getRootNode().getType());
        assertEquals("AND", result.getRootNode().getValue());
        verify(ruleRepository).save(any(Rule.class));
    }

    @Test
    void createRule_InvalidRuleString_ThrowsException() {
        // Given
        String invalidRuleString = "age > 30 AND";

        // When & Then
        assertThrows(CustomException.class, () ->
                ruleService.createRule(invalidRuleString, "Invalid Rule")
        );
    }

    @Test
    void createRule_UnbalancedParentheses_ThrowsException() {
        // Given
        String unbalancedRule = "(age > 30 AND department = 'Sales'";

        // When & Then
        assertThrows(CustomException.class, () ->
                ruleService.createRule(unbalancedRule, "Unbalanced Rule")
        );
    }

    @Test
    void evaluateRule_ValidData_Success() {
        // Given
        String ruleString = "age > 30 AND department = 'Sales'";
        Rule rule = ruleService.createRule(ruleString, "Test Rule");
        when(ruleRepository.findById(1L)).thenReturn(Optional.of(rule));

        // When
        boolean result = ruleService.evaluateRule(1L, userData);

        // Then
        assertTrue(result);
    }

    @Test
    void evaluateRule_InvalidData_ThrowsException() {
        // Given
        Map<String, Object> invalidData = new HashMap<>();
        invalidData.put("age", "invalid");

        // When & Then
        assertThrows(CustomException.class, () ->
                ruleService.evaluateRule(1L, invalidData)
        );
    }

    @Test
    void combineRules_ValidRules_Success() {
        // Given
        String rule1String = "age > 30 AND department = 'Sales'";
        String rule2String = "experience > 5";

        Rule rule1 = ruleService.createRule(rule1String, "Rule 1");
        Rule rule2 = ruleService.createRule(rule2String, "Rule 2");

        rule1.setId(1L);
        rule2.setId(2L);

        when(ruleRepository.findById(1L)).thenReturn(Optional.of(rule1));
        when(ruleRepository.findById(2L)).thenReturn(Optional.of(rule2));

        // When
        Node combinedNode = ruleService.combineRules(Arrays.asList(1L, 2L));

        // Then
        assertNotNull(combinedNode);
        assertEquals("operator", combinedNode.getType());
        assertEquals("AND", combinedNode.getValue());
    }

    @Test
    void evaluateRule_ComplexRule_Success() {
        // Given
        String complexRule = "(age > 30 AND department = 'Sales') OR (experience > 5 AND salary > 70000)";
        Rule rule = ruleService.createRule(complexRule, "Complex Rule");
        when(ruleRepository.findById(1L)).thenReturn(Optional.of(rule));

        // When
        boolean result = ruleService.evaluateRule(1L, userData);

        // Then
        assertTrue(result);
    }

    @Test
    void modifyRule_ValidModification_Success() {
        // Given
        String originalRule = "age > 30";
        String modifiedRule = "age > 35";
        Rule rule = ruleService.createRule(originalRule, "Original Rule");
        rule.setId(1L);

        when(ruleRepository.findById(1L)).thenReturn(Optional.of(rule));
        when(ruleRepository.save(any(Rule.class))).thenAnswer(i -> i.getArguments()[0]);

        // When
        Rule result = ruleService.modifyRule(1L, modifiedRule);

        // Then
        assertNotNull(result);
        assertNotNull(result.getRootNode());
        assertEquals("operand", result.getRootNode().getType());
        assertTrue(result.getRootNode().getValue().contains("35"));
    }

    @Test
    void evaluateRule_DifferentDepartments_Success() {
        // Test data for different departments
        List<String> departments = Arrays.asList("Sales", "Marketing", "IT", "HR");

        for (String dept : departments) {
            // Given
            userData.put("department", dept);
            String ruleString = "department = '" + dept + "'";
            Rule rule = ruleService.createRule(ruleString, dept + " Rule");
            when(ruleRepository.findById(any())).thenReturn(Optional.of(rule));

            // When
            boolean result = ruleService.evaluateRule(1L, userData);

            // Then
            assertTrue(result);
        }
    }

    @Test
    void evaluateRule_NumericComparisons_Success() {
        // Test different numeric comparisons
        Map<String, String> comparisons = new HashMap<>();
        comparisons.put("age > 30", "age");
        comparisons.put("salary > 50000", "salary");
        comparisons.put("experience > 5", "experience");

        for (Map.Entry<String, String> entry : comparisons.entrySet()) {
            // Given
            Rule rule = ruleService.createRule(entry.getKey(), "Numeric Rule");
            when(ruleRepository.findById(any())).thenReturn(Optional.of(rule));

            // When
            boolean result = ruleService.evaluateRule(1L, userData);

            // Then
            assertTrue(result, "Failed for " + entry.getValue() + " comparison");
        }
    }

    @Test
    void deleteRule_ExistingRule_Success() {
        // Given
        when(ruleRepository.findById(1L)).thenReturn(Optional.of(new Rule()));
        doNothing().when(ruleRepository).deleteById(1L);

        // When
        ruleService.deleteRule(1L);

        // Then
        verify(ruleRepository).deleteById(1L);
    }

    @Test
    void deleteRule_NonExistentRule_ThrowsException() {
        // Given
        when(ruleRepository.findById(1L)).thenReturn(Optional.empty());

        // When & Then
        assertThrows(CustomException.class, () ->
                ruleService.deleteRule(1L)
        );
    }
}