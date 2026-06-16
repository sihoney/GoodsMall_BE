package com.example.member.auth.config;

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
        Duration stateTtl
) implements OAuthProviderProperties {
}
