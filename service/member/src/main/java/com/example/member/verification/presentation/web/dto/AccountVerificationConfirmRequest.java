package com.example.member.verification.presentation.web.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

@Schema(description = "계좌 인증 확인 요청")
public record AccountVerificationConfirmRequest(
        @NotBlank(message = "인증 코드는 필수입니다.")
        @Schema(description = "인증 코드", example = "482931")
        String code
) {
}
