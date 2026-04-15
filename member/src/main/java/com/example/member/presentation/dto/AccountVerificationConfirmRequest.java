package com.example.member.presentation.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "모의 계좌인증 코드 확인 요청")
public record AccountVerificationConfirmRequest(
        @Schema(description = "인증 코드", example = "482931")
        String code
) {
}