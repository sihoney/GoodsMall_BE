package com.example.member.presentation.dto;

public record UpdateMemberRequest(
        String nickname,
        String phone,
        String address,
        String profileImageKey
) {
}
