package com.example.member.auth.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "auth.refresh-token-cookie")
public record RefreshTokenCookieProperties(
        String name,
        String path,
        String sameSite,
        Boolean secure
) {

    public RefreshTokenCookieProperties {
        name = name == null || name.isBlank() ? "refreshToken" : name;
        path = path == null || path.isBlank() ? "/api/auth" : path;
        sameSite = sameSite == null || sameSite.isBlank() ? "Lax" : sameSite;
        secure = secure != null && secure;
    }
}
