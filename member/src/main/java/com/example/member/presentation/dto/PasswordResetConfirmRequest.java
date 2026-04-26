package com.example.member.presentation.dto;

public record PasswordResetConfirmRequest(
        String token,
        String newPassword
) {
}
