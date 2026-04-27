package com.example.member.presentation.web.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record ChangePasswordRequest(
        @NotBlank
        String currentPassword,
        @NotBlank
        @Size(min = 8, max = 100)
        String newPassword
) {
}

