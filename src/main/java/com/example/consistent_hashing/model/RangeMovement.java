package com.example.consistent_hashing.model;

public class RangeMovement {
    private final Node from;
    private final Node to;
   private final long start;
    private final long end;

    public RangeMovement(Node from, Node to, long start, long end) {
        this.from = from;
        this.to = to;
        this.start = start;
        this.end = end;
    }
}
