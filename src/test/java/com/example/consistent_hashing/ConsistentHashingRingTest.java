package com.example.consistent_hashing;

import com.example.consistent_hashing.model.ConsistentHashingRing;
import com.example.consistent_hashing.model.Node;
import com.example.consistent_hashing.service.HashFunction;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.NavigableMap;

import static org.junit.jupiter.api.Assertions.*;

class ConsistentHashingRingTest {

    private ConsistentHashingRing ring;
    private HashFunction hashFunction;
    private final int VIRTUAL_NODES = 3;

    @BeforeEach
    void setUp() {
        // Predictable hash function for testing
        // Node A: A#0 -> 100, A#1 -> 110, A#2 -> 120
        // Node B: B#0 -> 200, B#1 -> 210, B#2 -> 220
        // Node C: C#0 -> 300, C#1 -> 310, C#2 -> 320
        hashFunction = key -> {
            if (key.startsWith("A#0")) return 100L;
            if (key.startsWith("A#1")) return 110L;
            if (key.startsWith("A#2")) return 120L;

            if (key.startsWith("B#0")) return 200L;
            if (key.startsWith("B#1")) return 210L;
            if (key.startsWith("B#2")) return 220L;

            if (key.startsWith("C#0")) return 300L;
            if (key.startsWith("C#1")) return 310L;
            if (key.startsWith("C#2")) return 320L;

            if (key.equals("key-90")) return 90L;   // Before A#0
            if (key.equals("key-105")) return 105L; // Between A#0 and A#1
            if (key.equals("key-150")) return 150L; // Between A and B
            if (key.equals("key-250")) return 250L; // Between B and C
            if (key.equals("key-350")) return 350L; // After C

            return (long) key.hashCode();
        };
        ring = new ConsistentHashingRing(hashFunction, VIRTUAL_NODES);
    }

    @Test
    void testAddNode() {
        Node nodeA = new Node("A", "hostA", 8080);
        ring.addNode(nodeA);

        NavigableMap<Long, Node> view = ring.getRingView();
        assertEquals(3, view.size());
        assertTrue(view.containsKey(100L));
        assertTrue(view.containsKey(110L));
        assertTrue(view.containsKey(120L));
    }

    @Test
    void testRemoveNode() {
        Node nodeA = new Node("A", "hostA", 8080);
        ring.addNode(nodeA);
        assertEquals(3, ring.getRingView().size());

        ring.removeNode(nodeA);
        assertEquals(0, ring.getRingView().size());
    }

    @Test
    void testGetPrimaryNode_EmptyRing() {
        assertThrows(IllegalStateException.class, () -> ring.getPrimaryNode("any"));
    }

    @Test
    void testGetPrimaryNode() {
        Node nodeA = new Node("A", "hostA", 8080); // 100, 110, 120
        Node nodeB = new Node("B", "hostB", 8080); // 200, 210, 220
        ring.addNode(nodeA);
        ring.addNode(nodeB);

        // key-90 -> 90L. Ceiling is 100L (A)
        assertEquals(nodeA, ring.getPrimaryNode("key-90"));

        // key-105 -> 105L. Ceiling is 110L (A)
        assertEquals(nodeA, ring.getPrimaryNode("key-105"));

        // key-150 -> 150L. Ceiling is 200L (B)
        assertEquals(nodeB, ring.getPrimaryNode("key-150"));

        // key-250 -> 250L. Ceiling is null (wrap around to first) -> 100L (A)
        assertEquals(nodeA, ring.getPrimaryNode("key-250"));
    }

    @Test
    void testGetReplicaNodes_EmptyRing() {
        assertThrows(IllegalStateException.class, () -> ring.getReplicaNodes("any", 2));
    }

    @Test
    void testGetReplicaNodes_InvalidFactor() {
        Node nodeA = new Node("A", "hostA", 8080);
        ring.addNode(nodeA);
        assertThrows(IllegalArgumentException.class, () -> ring.getReplicaNodes("any", 0));
    }

    @Test
    void testGetReplicaNodes() {
        Node nodeA = new Node("A", "hostA", 8080); // 100, 110, 120
        Node nodeB = new Node("B", "hostB", 8080); // 200, 210, 220
        Node nodeC = new Node("C", "hostC", 8080); // 300, 310, 320
        ring.addNode(nodeA);
        ring.addNode(nodeB);
        ring.addNode(nodeC);

        // key-150 -> 150L.
        // Ceiling is 200L (B#0).
        // Replicas for key-150 with factor 2: [B, C]
        List<Node> replicas = ring.getReplicaNodes("key-150", 2);
        assertEquals(2, replicas.size());
        assertEquals(nodeB, replicas.get(0));
        assertEquals(nodeC, replicas.get(1));

        // Replicas for key-150 with factor 3: [B, C, A]
        replicas = ring.getReplicaNodes("key-150", 3);
        assertEquals(3, replicas.size());
        assertEquals(nodeB, replicas.get(0));
        assertEquals(nodeC, replicas.get(1));
        assertEquals(nodeA, replicas.get(2));
    }

    @Test
    void testGetReplicaNodes_WrapAround() {
        Node nodeA = new Node("A", "hostA", 8080); // 100...
        Node nodeB = new Node("B", "hostB", 8080); // 200...
        ring.addNode(nodeA);
        ring.addNode(nodeB);

        // key-250 -> 250L. Ceiling is null -> wrap to first (A).
        // Primary is A.
        // Replicas (factor 2): A, B.

        List<Node> replicas = ring.getReplicaNodes("key-250", 2);
        assertEquals(2, replicas.size());
        assertEquals(nodeA, replicas.get(0));
        assertEquals(nodeB, replicas.get(1));
    }

    @Test
    void testGetRingView() {
        Node nodeA = new Node("A", "hostA", 8080);
        ring.addNode(nodeA);

        NavigableMap<Long, Node> view = ring.getRingView();
        assertNotNull(view);
        assertEquals(3, view.size());
        assertEquals(nodeA, view.get(100L));
    }
}
