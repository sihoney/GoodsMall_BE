package com.example.member.auth.presentation.web.dto;

public record ChangePasswordResponse(
        String message
) {
    public static ChangePasswordResponse success() {
        return new ChangePasswordResponse("비밀번호가 변경되었습니다.");
    }
}
