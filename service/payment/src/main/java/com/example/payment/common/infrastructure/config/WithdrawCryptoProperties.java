package com.example.payment.common.infrastructure.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "payment.withdraw.crypto")
public record WithdrawCryptoProperties(
        String secretKey
) {

    public WithdrawCryptoProperties {
        secretKey = normalize(secretKey, "local-dev-payment-withdraw-secret-key");
    }

    private static String normalize(String value, String defaultValue) {
        if (value == null || value.trim().isEmpty()) {
            return defaultValue;
        }
        return value.trim();
    }
}
