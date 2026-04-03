package com.example.member.infrastructure.redis;

import java.time.Duration;
import java.util.Optional;
import java.util.UUID;

public interface RefreshTokenStore {

    void save(UUID memberId, String refreshToken, Duration ttl);

    Optional<String> findByMemberId(UUID memberId);

    void delete(UUID memberId);
}
