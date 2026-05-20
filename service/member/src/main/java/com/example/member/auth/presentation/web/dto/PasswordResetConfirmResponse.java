package com.example.member.auth.presentation.web.dto;

import com.example.member.auth.application.dto.result.PasswordResetConfirmResult;

public record PasswordResetConfirmResponse(
        String message
) {
    public static PasswordResetConfirmResponse from(PasswordResetConfirmResult result) {
        return new PasswordResetConfirmResponse(result.message());
    }
}

