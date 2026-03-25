package com.example.member.application.dto;

import com.example.member.domain.enumtype.MemberRole;
import com.example.member.presentation.dto.CreateMemberRequest;

public record MemberCreateCommand(
        String email,
        String password,
        String nickname,
        String phone,
        String address,
        String profileImageUrl,
        MemberRole role
) {

    public static MemberCreateCommand from(CreateMemberRequest request) {
        return new MemberCreateCommand(
                request.email(),
                request.password(),
                request.nickname(),
                request.phone(),
                request.address(),
                request.profileImageUrl(),
                request.role()
        );
    }
}
