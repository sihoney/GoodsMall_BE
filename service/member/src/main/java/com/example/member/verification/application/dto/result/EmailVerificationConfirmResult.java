package com.example.member.verification.application.dto.result;

import com.example.member.member.domain.enumtype.MemberStatus;
import java.util.UUID;

public record EmailVerificationConfirmResult(
        UUID memberId,
        String email,
        MemberStatus status,
        String autoLoginToken,
        long autoLoginTokenExpiresInSeconds
) {
}
