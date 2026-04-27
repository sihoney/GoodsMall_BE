package com.example.member.presentation.web.dto;

import com.example.member.application.dto.result.MemberResult;
import com.example.member.domain.enumtype.MemberStatus;
import com.todaylunch.common.security.auth.enumtype.MemberRole;
import java.time.LocalDateTime;
import java.util.UUID;

public record MemberResponse(
        UUID memberId,
        String email,
        String nickname,
        String phone,
        String address,
        String profileImageUrl,
        MemberRole role,
        MemberStatus status,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {

    public static MemberResponse from(MemberResult result) {
        return new MemberResponse(
                result.memberId(),
                result.email(),
                result.nickname(),
                result.phone(),
                result.address(),
                result.profileImageUrl(),
                result.role(),
                result.status(),
                result.createdAt(),
                result.updatedAt()
        );
    }
}

