package com.example.consistent_hashing.model;


import java.util.Objects;

public class Node {
   String id;
   String host;
   int port;

   public Node(String id, String host, int port) {
      this.id = id;
      this.host = host;
      this.port = port;
   }

   public String getId() {
      return id;
   }

   public String getHost() {
      return host;
   }

   public int getPort() {
      return port;
   }

   @Override
   public boolean equals(Object o) {
      if (this == o) return true;
      if (!(o instanceof Node)) return false;
      Node node = (Node) o;
      return Objects.equals(id, node.id);
   }

   @Override
   public int hashCode() {
      return Objects.hash(id);
   }

   @Override
   public String toString() {
      return "Node{id='" + id + "'}";
   }
}
