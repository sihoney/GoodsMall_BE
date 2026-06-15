package com.example.member.verification.infrastructure.redis.accountverification;

import java.time.Duration;
import java.util.Optional;
import java.util.UUID;

public interface AccountVerificationSessionStore {

    void saveSession(AccountVerificationSession session, Duration ttl);

    Optional<AccountVerificationSession> findSession(String sessionId);

    Optional<String> findCurrentSessionId(UUID memberId);

    void saveCurrentSession(UUID memberId, String sessionId, Duration ttl);

    void deleteSession(String sessionId);

    void deleteCurrentSession(UUID memberId);

    boolean acquireLock(String sessionId, Duration ttl);

    void releaseLock(String sessionId);
}
