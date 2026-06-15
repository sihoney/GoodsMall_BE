package com.example.member.verification.presentation.web.dto;

import jakarta.validation.constraints.NotBlank;

public record ConfirmEmailVerificationRequest(
        @NotBlank
        String token
) {
}

