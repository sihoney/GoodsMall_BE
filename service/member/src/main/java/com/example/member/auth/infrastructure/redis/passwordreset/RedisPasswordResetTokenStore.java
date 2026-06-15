package com.example.member.auth.infrastructure.redis.passwordreset;

import java.time.Duration;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class RedisPasswordResetTokenStore implements PasswordResetTokenStore {

    private static final String KEY_PREFIX = "auth:password-reset:";
    
    private static final String FIELD_MEMBER_ID = "memberId";
    private static final String FIELD_EMAIL = "email";
    private static final String FIELD_CREATED_AT = "createdAt";

    private final StringRedisTemplate stringRedisTemplate;

    @Override
    public Optional<String> create(PasswordResetToken passwordResetToken, Duration ttl) {
        String key = buildKey(passwordResetToken.token());
        stringRedisTemplate.opsForHash().put(key, FIELD_MEMBER_ID, passwordResetToken.memberId().toString());
        stringRedisTemplate.opsForHash().put(key, FIELD_EMAIL, passwordResetToken.email());
        stringRedisTemplate.opsForHash().put(key, FIELD_CREATED_AT, passwordResetToken.createdAt().toString());
        stringRedisTemplate.expire(key, ttl);
        return Optional.of(passwordResetToken.token());
    }

    @Override
    public Optional<PasswordResetToken> find(String token) {
        String key = buildKey(token);
        var entries = stringRedisTemplate.opsForHash().entries(key);
        if (entries == null || entries.isEmpty()) {
            return Optional.empty();
        }

        return Optional.of(new PasswordResetToken(
                token,
                UUID.fromString((String) entries.get(FIELD_MEMBER_ID)),
                (String) entries.get(FIELD_EMAIL),
                Instant.parse((String) entries.get(FIELD_CREATED_AT))
        ));
    }

    @Override
    public void delete(String token) {
        stringRedisTemplate.delete(buildKey(token));
    }

    private String buildKey(String token) {
        return KEY_PREFIX + token;
    }
}
