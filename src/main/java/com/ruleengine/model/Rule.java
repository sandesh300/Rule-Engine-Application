package com.ruleengine.model;

import jakarta.persistence.*;
import lombok.*;


import java.time.LocalDateTime;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "rules")
public class Rule {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY) // Auto-generate the ID
    private Long id;

    @Column(nullable = false)
    private String ruleName;

    @OneToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "root_node_id")
    private Node rootNode; // Reference to the root node of the AST

    @Column(nullable = false)
    private LocalDateTime createdAt; // Timestamp for rule creation
}
