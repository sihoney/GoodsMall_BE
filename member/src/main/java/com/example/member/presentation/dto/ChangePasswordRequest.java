package com.example.member.presentation.dto;

public record ChangePasswordRequest(
        String currentPassword,
        String newPassword
) {
}
