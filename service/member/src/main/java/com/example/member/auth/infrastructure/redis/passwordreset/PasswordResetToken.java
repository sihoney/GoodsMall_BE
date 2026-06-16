package com.example.member.auth.infrastructure.redis.passwordreset;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public record PasswordResetToken(
        String token,
        UUID memberId,
        String email,
        Instant createdAt
) {

    private static final String FIELD_MEMBER_ID = "memberId";
    private static final String FIELD_EMAIL = "email";
    private static final String FIELD_CREATED_AT = "createdAt";

    public static PasswordResetToken fromMap(String token, Map<Object, Object> entries) {
        return new PasswordResetToken(
                token,
                UUID.fromString(stringValue(entries, FIELD_MEMBER_ID)),
                stringValue(entries, FIELD_EMAIL),
                Instant.parse(stringValue(entries, FIELD_CREATED_AT))
        );
    }

    public Map<String, String> toMap() {
        Map<String, String> values = new HashMap<>();
        values.put(FIELD_MEMBER_ID, memberId.toString());
        values.put(FIELD_EMAIL, email);
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
