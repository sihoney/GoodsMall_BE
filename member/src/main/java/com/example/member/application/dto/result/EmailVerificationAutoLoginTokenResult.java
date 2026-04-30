package com.example.member.application.dto.result;

public record EmailVerificationAutoLoginTokenResult(
        String token,
        long expiresInSeconds
) {
}
