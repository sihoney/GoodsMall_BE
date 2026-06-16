package com.example.member.auth.infrastructure.redis.passwordreset;

import java.time.Duration;
import java.util.Map;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

/*
 * Redis schema
 *
 * [1] 비밀번호 재설정 토큰
 * - key: auth:password-reset:{token}
 * - type: Hash
 * - ttl: 비밀번호 재설정 토큰 만료 시간
 * - value:
 *   - memberId: 회원 ID
 *   - email: 회원 이메일
 *   - createdAt: 토큰 생성 시각
 *
 * [2] 저장 예시
 * - auth:password-reset:reset-token-abc123
 *   memberId = 11111111-1111-1111-1111-111111111111
 *   email = user@example.com
 *   createdAt = 2026-06-16T14:30:00Z
 */
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
