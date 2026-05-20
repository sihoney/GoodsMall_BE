package com.example.member.common.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "member.account-verification.crypto")
public record AccountVerificationCryptoProperties(
        String secretKey
) {

    public AccountVerificationCryptoProperties {
        secretKey = normalize(secretKey, "local-dev-account-verification-secret-key");
    }

    private static String normalize(String value, String defaultValue) {
        if (value == null || value.trim().isEmpty()) {
            return defaultValue;
        }
        return value.trim();
    }
}
