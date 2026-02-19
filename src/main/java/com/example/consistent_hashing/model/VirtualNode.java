package com.example.consistent_hashing.model;

public class VirtualNode {
    private final Node physicalNode;
    private final int replicaIndex;

    VirtualNode(Node physicalNode, int replicaIndex) {
        this.physicalNode = physicalNode;
        this.replicaIndex = replicaIndex;
    }

    public Node getPhysicalNode() {
        return this.physicalNode;
    }
}
