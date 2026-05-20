package com.example.member.auth.presentation.web.dto;

import com.example.member.auth.application.dto.result.AuthSessionListResult;

public record AuthSessionListResponse(
        java.util.List<AuthSessionResponse> sessions
) {
    public static AuthSessionListResponse from(AuthSessionListResult result) {
        return new AuthSessionListResponse(
                result.sessions().stream()
                        .map(AuthSessionResponse::from)
                        .toList()
        );
    }
}
