package com.example.member.auth.application.dto.command;

import com.example.member.common.application.dto.AuthSessionMetadata;

public record TokenRefreshCommand(
        String refreshToken,
        AuthSessionMetadata authSessionMetadata
) {
}
