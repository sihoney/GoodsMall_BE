package com.example.member.auth.infrastructure.redis.oauth;

import com.example.member.auth.application.dto.result.OAuthResult;
import com.example.member.auth.application.dto.result.OAuthResultStatus;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

record OAuthResultSnapshot(
        OAuthResultStatus status,
        boolean linkRequired,
        String linkToken,
        String provider,
        String providerUserId,
        String email,
        String nickname,
        String accessToken,
        String refreshToken,
        UUID sessionId,
        String tokenType,
        long accessTokenExpiresIn,
        long refreshTokenExpiresIn,
        String errorCode,
        String errorMessage
) {

    private static final String FIELD_STATUS = "status";
    private static final String FIELD_LINK_REQUIRED = "linkRequired";
    private static final String FIELD_LINK_TOKEN = "linkToken";
    private static final String FIELD_PROVIDER = "provider";
    private static final String FIELD_PROVIDER_USER_ID = "providerUserId";
    private static final String FIELD_EMAIL = "email";
    private static final String FIELD_NICKNAME = "nickname";
    private static final String FIELD_ACCESS_TOKEN = "accessToken";
    private static final String FIELD_REFRESH_TOKEN = "refreshToken";
    private static final String FIELD_SESSION_ID = "sessionId";
    private static final String FIELD_TOKEN_TYPE = "tokenType";
    private static final String FIELD_ACCESS_TOKEN_EXPIRES_IN = "accessTokenExpiresIn";
    private static final String FIELD_REFRESH_TOKEN_EXPIRES_IN = "refreshTokenExpiresIn";
    private static final String FIELD_ERROR_CODE = "errorCode";
    private static final String FIELD_ERROR_MESSAGE = "errorMessage";

    static OAuthResultSnapshot from(OAuthResult result) {
        return new OAuthResultSnapshot(
                result.status(),
                result.linkRequired(),
                result.linkToken(),
                result.provider(),
                result.providerUserId(),
                result.email(),
                result.nickname(),
                result.accessToken(),
                result.refreshToken(),
                result.sessionId(),
                result.tokenType(),
                result.accessTokenExpiresIn(),
                result.refreshTokenExpiresIn(),
                result.errorCode(),
                result.errorMessage()
        );
    }

    static OAuthResultSnapshot fromMap(Map<Object, Object> entries) {
        return new OAuthResultSnapshot(
                OAuthResultStatus.valueOf(stringValue(entries, FIELD_STATUS)),
                Boolean.parseBoolean(stringValue(entries, FIELD_LINK_REQUIRED)),
                emptyToNull(optionalString(entries, FIELD_LINK_TOKEN)),
                emptyToNull(optionalString(entries, FIELD_PROVIDER)),
                emptyToNull(optionalString(entries, FIELD_PROVIDER_USER_ID)),
                emptyToNull(optionalString(entries, FIELD_EMAIL)),
                emptyToNull(optionalString(entries, FIELD_NICKNAME)),
                emptyToNull(optionalString(entries, FIELD_ACCESS_TOKEN)),
                emptyToNull(optionalString(entries, FIELD_REFRESH_TOKEN)),
                parseUuid(optionalString(entries, FIELD_SESSION_ID)),
                emptyToNull(optionalString(entries, FIELD_TOKEN_TYPE)),
                parseLong(optionalString(entries, FIELD_ACCESS_TOKEN_EXPIRES_IN)),
                parseLong(optionalString(entries, FIELD_REFRESH_TOKEN_EXPIRES_IN)),
                emptyToNull(optionalString(entries, FIELD_ERROR_CODE)),
                emptyToNull(optionalString(entries, FIELD_ERROR_MESSAGE))
        );
    }

    Map<String, String> toMap() {
        Map<String, String> values = new HashMap<>();
        values.put(FIELD_STATUS, status.name());
        values.put(FIELD_LINK_REQUIRED, String.valueOf(linkRequired));
        values.put(FIELD_LINK_TOKEN, nullToEmpty(linkToken));
        values.put(FIELD_PROVIDER, nullToEmpty(provider));
        values.put(FIELD_PROVIDER_USER_ID, nullToEmpty(providerUserId));
        values.put(FIELD_EMAIL, nullToEmpty(email));
        values.put(FIELD_NICKNAME, nullToEmpty(nickname));
        values.put(FIELD_ACCESS_TOKEN, nullToEmpty(accessToken));
        values.put(FIELD_REFRESH_TOKEN, nullToEmpty(refreshToken));
        values.put(FIELD_SESSION_ID, sessionId == null ? "" : sessionId.toString());
        values.put(FIELD_TOKEN_TYPE, nullToEmpty(tokenType));
        values.put(FIELD_ACCESS_TOKEN_EXPIRES_IN, String.valueOf(accessTokenExpiresIn));
        values.put(FIELD_REFRESH_TOKEN_EXPIRES_IN, String.valueOf(refreshTokenExpiresIn));
        values.put(FIELD_ERROR_CODE, nullToEmpty(errorCode));
        values.put(FIELD_ERROR_MESSAGE, nullToEmpty(errorMessage));
        return values;
    }

    OAuthResult toOAuthResult() {
        return new OAuthResult(
                status,
                linkRequired,
                linkToken,
                provider,
                providerUserId,
                email,
                nickname,
                accessToken,
                refreshToken,
                sessionId,
                tokenType,
                accessTokenExpiresIn,
                refreshTokenExpiresIn,
                errorCode,
                errorMessage
        );
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

    private static UUID parseUuid(String value) {
        String normalized = emptyToNull(value);
        return normalized == null ? null : UUID.fromString(normalized);
    }

    private static long parseLong(String value) {
        String normalized = emptyToNull(value);
        return normalized == null ? 0L : Long.parseLong(normalized);
    }
}
