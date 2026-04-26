package com.example.member.presentation.dto;

public record PasswordResetConfirmResponse(
        String message
) {
    public static PasswordResetConfirmResponse success() {
        return new PasswordResetConfirmResponse("비밀번호가 재설정되었습니다.");
    }
}
