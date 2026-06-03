package com.example.member.common.config;

import java.time.Duration;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "oauth.google")
public record GoogleOAuthProperties(
        String clientId,
        String clientSecret,
        String authorizeUri,
        String tokenUri,
        String userInfoUri,
        String redirectUri,
        String frontendCallbackUrl,
        Duration stateTtl,
        Duration resultTtl
) implements OAuthProviderProperties {
}
