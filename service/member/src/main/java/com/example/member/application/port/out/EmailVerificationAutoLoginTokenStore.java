package com.example.member.application.port.out;

import com.example.member.infrastructure.redis.emailverification.EmailVerificationAutoLoginToken;
import java.time.Duration;
import java.util.Optional;

public interface EmailVerificationAutoLoginTokenStore {

    Optional<String> create(EmailVerificationAutoLoginToken token, Duration ttl);

    Optional<EmailVerificationAutoLoginToken> consume(String token);
}
