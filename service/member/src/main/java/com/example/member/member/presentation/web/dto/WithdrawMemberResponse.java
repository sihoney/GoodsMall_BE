package com.example.member.member.presentation.web.dto;

import com.example.member.member.application.dto.result.WithdrawMemberResult;
import com.example.member.member.domain.enumtype.MemberStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;
import java.util.UUID;

@Schema(description = "회원 탈퇴 응답")
public record WithdrawMemberResponse(
        @Schema(description = "회원 ID", example = "11111111-1111-1111-1111-111111111111")
        UUID memberId,
        @Schema(description = "탈퇴 후 상태", example = "WITHDRAWN")
        MemberStatus status,
        @Schema(description = "탈퇴 시각")
        LocalDateTime withdrawnAt,
        @Schema(description = "결과 메시지", example = "회원 탈퇴 완료")
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
