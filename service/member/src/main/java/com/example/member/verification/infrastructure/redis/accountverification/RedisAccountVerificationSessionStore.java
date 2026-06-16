package com.example.member.verification.infrastructure.redis.accountverification;

import java.time.Duration;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.HashOperations;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

/*
 * Redis schema
 *
 * [1] 계좌 인증 세션 본문
 * - key: account-verification:session:{sessionId}
 * - type: Hash
 * - ttl: 계좌 인증 만료 시간
 * - value:
 *   - sessionId: 계좌 인증 세션 ID
 *   - memberId: 회원 ID
 *   - draftId: 판매자 draft ID
 *   - codeHash: 인증 코드 hash
 *   - status: PENDING, FAILED, VERIFIED, EXPIRED, CANCELLED
 *   - attemptCount: 인증 코드 확인 실패 횟수
 *   - resendCount: 인증 코드 재발송 횟수
 *   - requestedAt: 인증 요청 시각
 *   - expiresAt: 인증 만료 시각
 *   - verifiedAt: 인증 완료 시각, 없으면 빈 문자열
 *   - cancelledAt: 인증 취소 시각, 없으면 빈 문자열
 *   - failureReason: 실패 또는 만료 사유, 없으면 빈 문자열
 *
 * [2] 회원별 현재 세션
 * - key: account-verification:member:{memberId}:current
 * - type: String
 * - ttl: 계좌 인증 만료 시간
 * - value: sessionId
 *
 * [3] 세션 lock
 * - key: account-verification:lock:{sessionId}
 * - type: String
 * - ttl: 짧은 lock 유지 시간
 * - value: "1"
 *
 * [4] 저장 예시
 * - account-verification:session:av_abc123
 *   sessionId = av_abc123
 *   memberId = 11111111-1111-1111-1111-111111111111
 *   draftId = ad_abc123
 *   codeHash = 9f86d081884c7d659a2feaa0c55ad015...
 *   status = PENDING
 *   attemptCount = 0
 *   resendCount = 0
 *   requestedAt = 2026-06-16T14:30:00
 *   expiresAt = 2026-06-16T14:35:00
 *   verifiedAt =
 *   cancelledAt =
 *   failureReason =
 */
@Component
@RequiredArgsConstructor
public class RedisAccountVerificationSessionStore implements AccountVerificationSessionStore {

    private static final String SESSION_KEY_PREFIX = "account-verification:session:";
    private static final String MEMBER_CURRENT_KEY_PREFIX = "account-verification:member:";
    private static final String MEMBER_CURRENT_KEY_SUFFIX = ":current";
    private static final String LOCK_KEY_PREFIX = "account-verification:lock:";

    private final StringRedisTemplate stringRedisTemplate;

    @Override
    public void saveSession(AccountVerificationSession session, Duration ttl) {
        String key = buildSessionKey(session.getSessionId());
        HashOperations<String, Object, Object> hashOperations = stringRedisTemplate.opsForHash();
        for (Map.Entry<String, String> entry : session.toMap().entrySet()) {
            hashOperations.put(key, entry.getKey(), entry.getValue());
        }
        if (ttl.isZero() || ttl.isNegative()) {
            stringRedisTemplate.delete(key);
            return;
        }
        stringRedisTemplate.expire(key, ttl);
    }

    @Override
    public Optional<AccountVerificationSession> findSession(String sessionId) {
        String key = buildSessionKey(sessionId);
        Map<Object, Object> entries = stringRedisTemplate.opsForHash().entries(key);
        if (entries == null || entries.isEmpty()) {
            return Optional.empty();
        }
        return Optional.of(AccountVerificationSession.fromMap(entries));
    }

    @Override
    public Optional<String> findCurrentSessionId(UUID memberId) {
        return Optional.ofNullable(stringRedisTemplate.opsForValue().get(buildMemberCurrentKey(memberId)));
    }

    @Override
    public void saveCurrentSession(UUID memberId, String sessionId, Duration ttl) {
        stringRedisTemplate.opsForValue().set(buildMemberCurrentKey(memberId), sessionId, ttl);
    }

    @Override
    public void deleteSession(String sessionId) {
        stringRedisTemplate.delete(buildSessionKey(sessionId));
        stringRedisTemplate.delete(buildLockKey(sessionId));
    }

    @Override
    public void deleteCurrentSession(UUID memberId) {
        stringRedisTemplate.delete(buildMemberCurrentKey(memberId));
    }

    @Override
    public boolean acquireLock(String sessionId, Duration ttl) {
        Boolean locked = stringRedisTemplate.opsForValue().setIfAbsent(buildLockKey(sessionId), "1", ttl);
        return Boolean.TRUE.equals(locked);
    }

    @Override
    public void releaseLock(String sessionId) {
        stringRedisTemplate.delete(buildLockKey(sessionId));
    }

    private String buildSessionKey(String sessionId) {
        return SESSION_KEY_PREFIX + sessionId;
    }

    private String buildMemberCurrentKey(UUID memberId) {
        return MEMBER_CURRENT_KEY_PREFIX + memberId + MEMBER_CURRENT_KEY_SUFFIX;
    }

    private String buildLockKey(String sessionId) {
        return LOCK_KEY_PREFIX + sessionId;
    }
}
