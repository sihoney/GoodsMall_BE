package com.example.member.auth.application.dto.command;

public record PasswordResetConfirmCommand(
        String token,
        String newPassword
) {
}
