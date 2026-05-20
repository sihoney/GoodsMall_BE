package com.example.member.auth.presentation.web.dto;

import jakarta.validation.constraints.NotBlank;

public record EmailVerificationAutoLoginRequest(
        @NotBlank
        String autoLoginToken
) {
}
