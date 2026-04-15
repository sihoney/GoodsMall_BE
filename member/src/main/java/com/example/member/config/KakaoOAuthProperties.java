package com.example.member.config;

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
        Duration stateTtl,
        Duration pendingLinkTtl
) {
}
