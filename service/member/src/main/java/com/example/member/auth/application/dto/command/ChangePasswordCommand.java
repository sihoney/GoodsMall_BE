package com.example.member.auth.application.dto.command;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.util.UUID;

public record ChangePasswordCommand(
        @NotNull
        UUID memberId,
        @NotBlank
        String currentPassword,
        @NotBlank
        @Size(min = 8)
        String newPassword
) {
}
