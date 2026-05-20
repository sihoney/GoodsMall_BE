package com.example.member.presentation.web.dto;

import com.example.member.application.dto.result.CreateMemberResult;
import com.example.member.domain.enumtype.MemberStatus;
import com.todaylunch.common.security.auth.enumtype.MemberRole;
import java.time.LocalDateTime;
import java.util.UUID;

public record CreateMemberResponse(
        UUID memberId,
        String nickname,
        String profileImageUrl,
        MemberRole role,
        MemberStatus status,
        LocalDateTime createdAt
) {

    public static CreateMemberResponse from(CreateMemberResult result) {
        return new CreateMemberResponse(
                result.memberId(),
                result.nickname(),
                result.profileImageUrl(),
                result.role(),
                result.status(),
                result.createdAt()
        );
    }
}

