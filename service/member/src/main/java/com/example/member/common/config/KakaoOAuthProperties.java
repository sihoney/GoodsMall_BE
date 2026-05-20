package com.example.member.common.config;

import java.time.Duration;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "oauth.kakao")
public record KakaoOAuthProperties(
        String clientId,
        String clientSecret,
        String authorizeUri,
        String tokenUri,
        String userInfoUri,
        String redirectUri,
        String frontendCallbackUrl,
        Duration stateTtl,
        Duration pendingLinkTtl,
        Duration resultTtl
) {
}
