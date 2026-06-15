package com.example.member.verification.config;

import java.time.Duration;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "member.email-verification")
public record EmailVerificationProperties(
        Duration expiration,
        String frontendConfirmUrl,
        Duration autoLoginExpiration
) {

    public EmailVerificationProperties {
        expiration = expiration == null ? Duration.ofHours(24) : expiration;
        frontendConfirmUrl = normalize(frontendConfirmUrl, "http://localhost:5173/email-verification");
        autoLoginExpiration = autoLoginExpiration == null ? Duration.ofMinutes(3) : autoLoginExpiration;
    }

    private static String normalize(String value, String defaultValue) {
        if (value == null || value.trim().isEmpty()) {
            return defaultValue;
        }
        return value.trim();
    }
}
