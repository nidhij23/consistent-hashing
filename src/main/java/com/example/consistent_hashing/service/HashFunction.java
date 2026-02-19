package com.example.consistent_hashing.service;

public interface HashFunction {
    long hash(String key);
}
