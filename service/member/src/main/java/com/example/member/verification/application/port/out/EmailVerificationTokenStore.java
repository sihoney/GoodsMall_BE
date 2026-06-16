package com.example.member.verification.application.port.out;

import com.example.member.verification.infrastructure.redis.emailverification.SignupEmailVerificationToken;
import java.time.Duration;
import java.util.Optional;
import java.util.UUID;

public interface EmailVerificationTokenStore {

    void saveSignupToken(SignupEmailVerificationToken token, Duration ttl);

    Optional<SignupEmailVerificationToken> findSignupToken(String token);

    void deleteByToken(String token);

    void deleteByMemberId(UUID memberId);
}
