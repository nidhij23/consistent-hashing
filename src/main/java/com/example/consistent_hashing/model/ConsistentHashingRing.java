package com.example.consistent_hashing.model;

import com.example.consistent_hashing.service.HashFunction;

import java.util.*;

public class ConsistentHashingRing {

    private final NavigableMap<Long,VirtualNode> ring = new TreeMap<>();
    private final int virtualNodeCount;
    private final HashFunction hashFunction;


    public ConsistentHashingRing(HashFunction hashFunction, int virtualNodeCount) {
        this.hashFunction = hashFunction;
        this.virtualNodeCount = virtualNodeCount;
    }
    public void addNode(Node node) {
        for (int i =0;i<virtualNodeCount;i++) {
            String virtualKey = node.getId() + "#" + i;
            long hash = hashFunction.hash(virtualKey);
            ring.put(hash, new VirtualNode(node, i));
        }
    }
    public void removeNode(Node node){
        for(int i =0;i<virtualNodeCount;i++) {
            String virtualKey = node.getId()+"#"+i;
            long hash = hashFunction.hash(virtualKey);
            ring.remove(hash);
        }
    }

    public Node getPrimaryNode(String key){
       if(ring.isEmpty()){
           throw new IllegalStateException("Ring is Empty");
       }
       long hash = hashFunction.hash(key);
       Map.Entry<Long,VirtualNode> entry = ring.ceilingEntry(hash);

       if (entry==null) {
           entry = ring.firstEntry();
       }
       return entry.getValue().getPhysicalNode();
    }

    public List<Node> getReplicaNodes(String key, int replicationFactor
    ){
        if (ring.isEmpty()) {
            throw new IllegalStateException("Ring is empty");
        }

        if (replicationFactor <= 0) {
            throw new IllegalArgumentException("Replication factor must be > 0");
        }

        Set<Node> replicas = new LinkedHashSet<>();

        long hash = hashFunction.hash(key);
        Map.Entry<Long, VirtualNode> entry = ring.ceilingEntry(hash);

        if (entry == null) {
            entry = ring.firstEntry();
        }

        Iterator<Map.Entry<Long, VirtualNode>> iterator =
                ring.tailMap(entry.getKey(), true).entrySet().iterator();

        while (replicas.size() < replicationFactor) {
            if (!iterator.hasNext()) {
                iterator = ring.entrySet().iterator(); // wrap-around
            }

            Node node = iterator.next().getValue().getPhysicalNode();
            replicas.add(node);
        }

        return new ArrayList<>(replicas);
    }

    public NavigableMap<Long, Node> getRingView() {
        NavigableMap<Long, Node> view = new TreeMap<>();
        for (Map.Entry<Long, VirtualNode> entry : ring.entrySet()) {
            view.put(entry.getKey(), entry.getValue().getPhysicalNode());
        }
        return view;
    }
}
