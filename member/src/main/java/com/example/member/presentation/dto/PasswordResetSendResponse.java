package com.example.member.presentation.dto;

public record PasswordResetSendResponse(
        String message
) {
    public static PasswordResetSendResponse success() {
        return new PasswordResetSendResponse("입력한 이메일로 비밀번호 재설정 안내를 발송했습니다.");
    }
}
