package com.example.member.auth.infrastructure.redis.passwordreset;

import java.time.Duration;
import java.util.Map;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class RedisPasswordResetTokenStore implements PasswordResetTokenStore {

    private static final String KEY_PREFIX = "auth:password-reset:";

    private final StringRedisTemplate stringRedisTemplate;

    @Override
    public Optional<String> create(PasswordResetToken passwordResetToken, Duration ttl) {
        String key = buildKey(passwordResetToken.token());
        for (Map.Entry<String, String> entry : passwordResetToken.toMap().entrySet()) {
            stringRedisTemplate.opsForHash().put(key, entry.getKey(), entry.getValue());
        }
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

        return Optional.of(PasswordResetToken.fromMap(token, entries));
    }

    @Override
    public void delete(String token) {
        stringRedisTemplate.delete(buildKey(token));
    }

    private String buildKey(String token) {
        return KEY_PREFIX + token;
    }
}
