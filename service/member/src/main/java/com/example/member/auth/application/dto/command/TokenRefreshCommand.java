package com.example.member.auth.application.dto.command;

import com.example.member.common.application.dto.AuthSessionMetadata;
import jakarta.validation.constraints.NotBlank;

public record TokenRefreshCommand(
        @NotBlank
        String refreshToken,
        AuthSessionMetadata authSessionMetadata
) {
}
