package com.example.member.verification.presentation.web.dto;

import com.example.member.verification.application.dto.result.EmailVerificationConfirmResult;
import com.example.member.member.domain.enumtype.MemberStatus;
import java.util.UUID;

public record EmailVerificationConfirmResponse(
        UUID memberId,
        String email,
        MemberStatus status,
        String autoLoginToken,
        long autoLoginTokenExpiresInSeconds
) {

    public static EmailVerificationConfirmResponse from(EmailVerificationConfirmResult result) {
        return new EmailVerificationConfirmResponse(
                result.memberId(),
                result.email(),
                result.status(),
                result.autoLoginToken(),
                result.autoLoginTokenExpiresInSeconds()
        );
    }
}

