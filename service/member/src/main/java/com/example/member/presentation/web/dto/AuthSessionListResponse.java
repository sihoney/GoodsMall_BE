package com.example.member.presentation.web.dto;

import com.example.member.application.dto.result.AuthSessionListResult;

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
