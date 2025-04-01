package com.example.demo.domain.util;

import java.util.UUID;

public class UuidUtil {
    public static UUID fromString(String uuidString) {
        try {
            return UUID.fromString(uuidString);
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Invalid UUID string: " + uuidString, e);
        }
    }
}
