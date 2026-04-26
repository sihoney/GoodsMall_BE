package com.example.member.infrastructure.redis;

import java.time.Duration;
import java.util.Optional;

public interface PasswordResetTokenStore {

    Optional<String> create(PasswordResetToken passwordResetToken, Duration ttl);

    Optional<PasswordResetToken> find(String token);

    void delete(String token);
}
