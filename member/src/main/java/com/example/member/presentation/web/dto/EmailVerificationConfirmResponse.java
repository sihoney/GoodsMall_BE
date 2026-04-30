package com.example.member.presentation.web.dto;

import com.example.member.application.dto.result.EmailVerificationConfirmResult;
import com.example.member.domain.enumtype.MemberStatus;
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

