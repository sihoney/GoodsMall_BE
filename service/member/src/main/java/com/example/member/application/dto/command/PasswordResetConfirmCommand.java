package com.example.member.application.dto.command;

public record PasswordResetConfirmCommand(
        String token,
        String newPassword
) {
}
