package com.example.member.presentation.web.dto;

import com.example.member.application.dto.result.PasswordResetSendResult;

public record PasswordResetSendResponse(
        String message
) {
    public static PasswordResetSendResponse from(PasswordResetSendResult result) {
        return new PasswordResetSendResponse(result.message());
    }
}

