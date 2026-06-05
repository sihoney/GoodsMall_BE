package com.example.member.auth.config;

import java.time.Duration;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "member.password-reset")
public record PasswordResetProperties(
        Duration expiration,
        String frontendResetUrl
) {

    public PasswordResetProperties {
        expiration = expiration == null ? Duration.ofMinutes(30) : expiration;
        frontendResetUrl = normalize(frontendResetUrl, "http://localhost:5173/password-reset");
    }

    private static String normalize(String value, String defaultValue) {
        if (value == null || value.trim().isEmpty()) {
            return defaultValue;
        }
        return value.trim();
    }
}
