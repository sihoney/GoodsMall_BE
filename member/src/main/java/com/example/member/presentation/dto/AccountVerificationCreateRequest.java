package com.example.member.presentation.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "모의 계좌인증 요청")
public record AccountVerificationCreateRequest(
        @Schema(description = "은행명", example = "KAKAO")
        String bankName,
        @Schema(description = "계좌번호", example = "1234567890123")
        String accountNumber
) {
}