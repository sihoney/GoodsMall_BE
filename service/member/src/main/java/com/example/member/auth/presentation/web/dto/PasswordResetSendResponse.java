package com.example.member.auth.presentation.web.dto;

import com.example.member.auth.application.dto.result.PasswordResetSendResult;

public record PasswordResetSendResponse(
        String message
) {
    public static PasswordResetSendResponse from(PasswordResetSendResult result) {
        return new PasswordResetSendResponse(result.message());
    }
}

