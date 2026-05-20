package com.example.member.auth.application.dto.command;

public record PasswordResetSendCommand(
        String email
) {
}
