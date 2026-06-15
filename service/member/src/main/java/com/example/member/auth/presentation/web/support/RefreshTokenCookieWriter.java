package com.example.member.auth.presentation.web.support;

import com.example.member.auth.config.RefreshTokenCookieProperties;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import java.time.Duration;
import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Component;

@Component
public class RefreshTokenCookieWriter {

    private final RefreshTokenCookieProperties properties;

    public RefreshTokenCookieWriter(RefreshTokenCookieProperties properties) {
        this.properties = properties;
    }

    public ResponseCookie create(String refreshToken, long refreshTokenExpiresInMillis) {
        return baseCookie(refreshToken)
                .maxAge(Duration.ofMillis(refreshTokenExpiresInMillis))
                .build();
    }

    public ResponseCookie clear() {
        return baseCookie("")
                .maxAge(Duration.ZERO)
                .build();
    }

    public String resolveRefreshToken(HttpServletRequest request, String fallbackRefreshToken) {
        String cookieRefreshToken = resolveFromCookie(request);
        if (cookieRefreshToken != null && !cookieRefreshToken.isBlank()) {
            return cookieRefreshToken;
        }
        return fallbackRefreshToken;
    }

    private String resolveFromCookie(HttpServletRequest request) {
        Cookie[] cookies = request.getCookies();
        if (cookies == null) {
            return null;
        }

        for (Cookie cookie : cookies) {
            if (properties.name().equals(cookie.getName())) {
                return cookie.getValue();
            }
        }
        return null;
    }

    private ResponseCookie.ResponseCookieBuilder baseCookie(String value) {
        return ResponseCookie.from(properties.name(), value)
                .httpOnly(true)
                .secure(properties.secure())
                .sameSite(properties.sameSite())
                .path(properties.path());
    }
}
