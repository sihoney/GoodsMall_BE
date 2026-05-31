package com.example.member.auth.presentation.web.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "비밀번호 변경 응답")
public record ChangePasswordResponse(
        @Schema(description = "결과 메시지", example = "비밀번호가 변경되었습니다.")
        String message
) {
    public static ChangePasswordResponse success() {
        return new ChangePasswordResponse("비밀번호가 변경되었습니다.");
    }
}
