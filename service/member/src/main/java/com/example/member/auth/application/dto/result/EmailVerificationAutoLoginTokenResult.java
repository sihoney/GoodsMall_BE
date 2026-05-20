package com.example.member.auth.application.dto.result;

public record EmailVerificationAutoLoginTokenResult(
        String token,
        long expiresInSeconds
) {
}
