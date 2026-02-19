package com.example.consistent_hashing.service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class SHA256Hash implements HashFunction {

    private final MessageDigest digest;

    public SHA256Hash() {
        try {
            this.digest = MessageDigest.getInstance("SHA-256");
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public long hash(String key) {
        byte[] bytes = digest.digest(key.getBytes(StandardCharsets.UTF_8));

        long hash = 0;
        for (int i = 0; i < 8; i++) {
            hash = (hash << 8) | (bytes[i] & 0xff);
        }

        return hash & 0x7fffffffffffffffL;
    }
}
