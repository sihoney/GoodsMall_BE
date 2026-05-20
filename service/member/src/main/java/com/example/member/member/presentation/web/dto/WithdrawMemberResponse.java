package com.example.member.member.presentation.web.dto;

import com.example.member.member.application.dto.result.WithdrawMemberResult;
import com.example.member.member.domain.enumtype.MemberStatus;
import java.time.LocalDateTime;
import java.util.UUID;

public record WithdrawMemberResponse(
        UUID memberId,
        MemberStatus status,
        LocalDateTime withdrawnAt,
        String message
) {
    public static WithdrawMemberResponse from(WithdrawMemberResult result) {
        return new WithdrawMemberResponse(
                result.memberId(),
                result.status(),
                result.withdrawnAt(),
                result.message()
        );
    }
}
