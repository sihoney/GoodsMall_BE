package com.example.payment.common.infrastructure.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "toss.payments")
public record TossPaymentsProperties(
        String baseUrl,
        String clientKey,
        String secretKey,
        String successUrl,
        String failUrl,
        boolean widgetEnabled
) {
}
