package com.ruleengine;

import com.ruleengine.model.Node;
import com.ruleengine.model.Rule;
import com.ruleengine.repository.RuleRepository;
import com.ruleengine.service.RuleService;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class RuleServiceTest {

    @Mock
    private RuleRepository ruleRepository;

    @InjectMocks
    private RuleService ruleService;

    public RuleServiceTest() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    public void testCreateRule_VerifyAST() {
        // Arrange
        String ruleString = "(age > 30 AND salary > 50000)";
        String ruleName = "Sample Rule";

        // Act
        Rule rule = ruleService.createRule(ruleString, ruleName);

        // Assert
        assertNotNull(rule.getRootNode());
        Node rootNode = rule.getRootNode();
        assertEquals("AND", rootNode.getValue()); // Root node should be "AND"
        assertEquals("operand", rootNode.getLeft().getType());
        assertEquals("operand", rootNode.getRight().getType());
        assertEquals("age > 30", rootNode.getLeft().getValue());
        assertEquals("salary > 50000", rootNode.getRight().getValue());
    }
}
