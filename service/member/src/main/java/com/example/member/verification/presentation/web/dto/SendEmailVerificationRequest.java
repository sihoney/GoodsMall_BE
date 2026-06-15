package com.example.member.verification.presentation.web.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record SendEmailVerificationRequest(
        @NotBlank
        @Email
        String email
) {
}

