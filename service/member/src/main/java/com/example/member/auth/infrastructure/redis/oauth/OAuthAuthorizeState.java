package com.example.member.auth.infrastructure.redis.oauth;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

public record OAuthAuthorizeState(
        String state,
        Instant createdAt
) {

    private static final String FIELD_CREATED_AT = "createdAt";

    public static OAuthAuthorizeState fromMap(String state, Map<Object, Object> entries) {
        return new OAuthAuthorizeState(
                state,
                Instant.parse(stringValue(entries, FIELD_CREATED_AT))
        );
    }

    public Map<String, String> toMap() {
        Map<String, String> values = new HashMap<>();
        values.put(FIELD_CREATED_AT, createdAt.toString());
        return values;
    }

    private static String stringValue(Map<Object, Object> entries, String fieldName) {
        Object value = entries.get(fieldName);
        if (value == null || value.toString().isBlank()) {
            throw new IllegalArgumentException("Redis 필드가 누락되었습니다. " + fieldName);
        }
        return value.toString();
    }
}
