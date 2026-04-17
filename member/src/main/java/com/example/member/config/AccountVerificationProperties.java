package com.example.member.config;

import java.time.Duration;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "member.account-verification")
public record AccountVerificationProperties(
        Duration expiration,
        int maxAttempts,
        int maxResendCount,
        Duration resendCooldown
) {

    public AccountVerificationProperties {
        expiration = expiration == null ? Duration.ofMinutes(5) : expiration;
        maxAttempts = maxAttempts <= 0 ? 5 : maxAttempts;
        maxResendCount = maxResendCount <= 0 ? 3 : maxResendCount;
        resendCooldown = resendCooldown == null ? Duration.ofSeconds(30) : resendCooldown;
    }
}
