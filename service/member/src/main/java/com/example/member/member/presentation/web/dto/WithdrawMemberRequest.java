package com.example.member.member.presentation.web.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

@Schema(description = "회원 탈퇴 요청")
public record WithdrawMemberRequest(
        @Schema(description = "현재 비밀번호", example = "password1234")
        @NotBlank
        String currentPassword
) {
}
