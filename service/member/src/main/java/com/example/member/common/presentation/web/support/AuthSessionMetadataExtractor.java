package com.example.member.common.presentation.web.support;

import com.example.member.common.application.dto.AuthSessionMetadata;
import jakarta.servlet.http.HttpServletRequest;

public final class AuthSessionMetadataExtractor {

    private AuthSessionMetadataExtractor() {
    }

    public static AuthSessionMetadata extract(HttpServletRequest request) {
        if (request == null) {
            return AuthSessionMetadata.empty();
        }

        return new AuthSessionMetadata(
                request.getHeader("User-Agent"),
                extractClientIp(request)
        );
    }

    private static String extractClientIp(HttpServletRequest request) {
        String forwardedFor = request.getHeader("X-Forwarded-For");
        if (forwardedFor != null && !forwardedFor.isBlank()) {
            String[] values = forwardedFor.split(",");
            if (values.length > 0 && values[0] != null && !values[0].trim().isEmpty()) {
                return values[0].trim();
            }
        }

        String realIp = request.getHeader("X-Real-IP");
        if (realIp != null && !realIp.isBlank()) {
            return realIp.trim();
        }

        return request.getRemoteAddr();
    }
}
