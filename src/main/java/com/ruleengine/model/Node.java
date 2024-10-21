package com.ruleengine.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "node")
public class Node {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String type; // "operator" or "operand"

    @OneToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "left_node_id")
    private Node left; // Left child node (nullable for operand)

    @OneToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "right_node_id")
    private Node right; // Right child node (nullable for operand)

    @Column
    private String value; // Holds value for operand (e.g., "age > 30")

    // Add this constructor for creating new nodes without an ID (ID will be auto-generated)
    public Node(String type, Node left, Node right, String value) {
        this.type = type;
        this.left = left;
        this.right = right;
        this.value = value;
    }
}