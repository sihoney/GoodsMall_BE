package com.example.member.presentation.dto;

import com.todaylunch.common.security.auth.enumtype.MemberRole;

public record CreateMemberRequest(
        String email,
        String password,
        String nickname,
        String phone,
        String address,
        String profileImageKey,
        MemberRole role
) {
}
