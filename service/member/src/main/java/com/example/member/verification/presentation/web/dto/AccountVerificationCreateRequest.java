package com.example.member.verification.presentation.web.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

@Schema(description = "계좌 인증 생성 요청")
public record AccountVerificationCreateRequest(
        @NotBlank(message = "은행명은 필수입니다.")
        @Schema(description = "은행명", example = "KAKAO")
        String bankName,
        @NotBlank(message = "계좌번호는 필수입니다.")
        @Schema(description = "계좌번호", example = "1234567890123")
        String accountNumber
) {
}
