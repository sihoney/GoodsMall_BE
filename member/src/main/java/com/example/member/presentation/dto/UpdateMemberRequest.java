package com.example.member.presentation.dto;

public record UpdateMemberRequest(
        String email,
        String password,
        String nickname,
        String phone,
        String address,
        String profileImageKey
) {
}
