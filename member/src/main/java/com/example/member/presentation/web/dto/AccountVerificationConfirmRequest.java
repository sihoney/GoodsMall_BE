package com.example.member.presentation.web.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

@Schema(description = "모의 계좌 인증 코드 확인 요청")
public record AccountVerificationConfirmRequest(
        @NotBlank(message = "code는 필수입니다.")
        @Schema(description = "인증 코드", example = "482931")
        String code
) {
}
