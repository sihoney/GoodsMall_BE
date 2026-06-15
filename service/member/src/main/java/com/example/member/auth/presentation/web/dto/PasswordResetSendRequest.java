package com.example.member.auth.presentation.web.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record PasswordResetSendRequest(
        @NotBlank
        @Email
        String email
) {
}

