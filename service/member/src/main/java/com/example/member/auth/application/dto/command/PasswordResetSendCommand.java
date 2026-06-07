package com.example.member.auth.application.dto.command;

import jakarta.validation.constraints.NotBlank;

public record PasswordResetSendCommand(
        @NotBlank
        String email
) {
}
