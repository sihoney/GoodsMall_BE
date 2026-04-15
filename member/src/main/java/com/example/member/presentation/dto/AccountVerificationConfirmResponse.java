package com.example.member.presentation.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;

@Schema(description = "모의 계좌인증 확인 응답")
public record AccountVerificationConfirmResponse(
        @Schema(description = "계좌 인증 세션 ID", example = "av_01J4XYZ")
        String sessionId,
        @Schema(description = "인증 성공 여부", example = "true")
        boolean verified,
        @Schema(description = "인증 상태", example = "VERIFIED")
        String status,
        @Schema(description = "인증 완료 시각")
        LocalDateTime verifiedAt,
        @Schema(description = "누적 인증 실패 횟수", example = "0")
        int attemptCount
) {
}