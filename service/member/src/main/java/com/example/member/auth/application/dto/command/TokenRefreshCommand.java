package com.example.member.auth.application.dto.command;

public record TokenRefreshCommand(
        String refreshToken
) {
}
