package com.example.member.auth.infrastructure.redis.auth;

import com.example.member.common.application.dto.AuthSessionMetadata;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

public record AuthSession(
        UUID memberId,
        UUID sessionId,
        String refreshTokenId,
        Instant createdAt,
        Instant lastAccessedAt,
        Instant lastRefreshedAt,
        String userAgent,
        String ipAddress
) {

    private static final String MEMBER_ID_FIELD = "memberId";
    private static final String SESSION_ID_FIELD = "sessionId";
    private static final String REFRESH_TOKEN_ID_FIELD = "refreshTokenId";
    private static final String CREATED_AT_FIELD = "createdAt";
    private static final String LAST_ACCESSED_AT_FIELD = "lastAccessedAt";
    private static final String LAST_REFRESHED_AT_FIELD = "lastRefreshedAt";
    private static final String USER_AGENT_FIELD = "userAgent";
    private static final String IP_ADDRESS_FIELD = "ipAddress";

    public static AuthSession create(
            UUID memberId,
            UUID sessionId,
            String refreshTokenId,
            AuthSessionMetadata metadata
    ) {
        Instant now = Instant.now();
        AuthSessionMetadata normalizedMetadata = normalizeMetadata(metadata);

        return new AuthSession(
                Objects.requireNonNull(memberId),
                Objects.requireNonNull(sessionId),
                Objects.requireNonNull(refreshTokenId),
                now,
                now,
                now,
                normalizedMetadata.userAgent(),
                normalizedMetadata.ipAddress()
        );
    }

    public AuthSession refresh(String refreshTokenId, AuthSessionMetadata metadata) {
        Instant now = Instant.now();
        AuthSessionMetadata normalizedMetadata = normalizeMetadata(metadata);

        return new AuthSession(
                memberId,
                sessionId,
                Objects.requireNonNull(refreshTokenId),
                createdAt,
                now,
                now,
                normalizedMetadata.userAgent(),
                normalizedMetadata.ipAddress()
        );
    }

    public static AuthSession fromMap(Map<Object, Object> entries) {
        return new AuthSession(
                UUID.fromString(stringValue(entries, MEMBER_ID_FIELD)),
                UUID.fromString(stringValue(entries, SESSION_ID_FIELD)),
                stringValue(entries, REFRESH_TOKEN_ID_FIELD),
                Instant.parse(stringValue(entries, CREATED_AT_FIELD)),
                Instant.parse(stringValue(entries, LAST_ACCESSED_AT_FIELD)),
                Instant.parse(stringValue(entries, LAST_REFRESHED_AT_FIELD)),
                emptyToNull(optionalString(entries, USER_AGENT_FIELD)),
                emptyToNull(optionalString(entries, IP_ADDRESS_FIELD))
        );
    }

    public Map<String, String> toMap() {
        Map<String, String> values = new HashMap<>();
        values.put(MEMBER_ID_FIELD, memberId.toString());
        values.put(SESSION_ID_FIELD, sessionId.toString());
        values.put(REFRESH_TOKEN_ID_FIELD, refreshTokenId);
        values.put(CREATED_AT_FIELD, createdAt.toString());
        values.put(LAST_ACCESSED_AT_FIELD, lastAccessedAt.toString());
        values.put(LAST_REFRESHED_AT_FIELD, lastRefreshedAt.toString());
        values.put(USER_AGENT_FIELD, nullToEmpty(userAgent));
        values.put(IP_ADDRESS_FIELD, nullToEmpty(ipAddress));
        return values;
    }

    private static AuthSessionMetadata normalizeMetadata(AuthSessionMetadata metadata) {
        if (metadata == null) {
            return AuthSessionMetadata.empty();
        }

        return new AuthSessionMetadata(
                normalizeValue(metadata.userAgent(), 512),
                normalizeValue(metadata.ipAddress(), 128)
        );
    }

    private static String normalizeValue(String value, int maxLength) {
        if (value == null || value.isBlank()) {
            return "";
        }

        String trimmed = value.trim();
        return trimmed.length() <= maxLength ? trimmed : trimmed.substring(0, maxLength);
    }

    private static String stringValue(Map<Object, Object> entries, String fieldName) {
        Object value = entries.get(fieldName);
        if (value == null || value.toString().isBlank()) {
            throw new IllegalArgumentException("Redis 필드가 누락되었습니다. " + fieldName);
        }
        return value.toString();
    }

    private static String optionalString(Map<Object, Object> entries, String fieldName) {
        Object value = entries.get(fieldName);
        return value == null ? null : value.toString();
    }

    private static String nullToEmpty(String value) {
        return value == null ? "" : value;
    }

    private static String emptyToNull(String value) {
        return value == null || value.isBlank() ? null : value;
    }
}
