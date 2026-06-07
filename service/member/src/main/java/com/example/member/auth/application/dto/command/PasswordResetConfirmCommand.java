package com.example.member.auth.application.dto.command;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record PasswordResetConfirmCommand(
        @NotBlank
        String token,
        @NotBlank
        @Size(min = 8)
        String newPassword
) {
}
