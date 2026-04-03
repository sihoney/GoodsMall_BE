package com.example.member.presentation.dto;

import com.example.member.domain.entity.Member;
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

    public static CreateMemberResponse from(Member member, String profileImageUrl) {
        return new CreateMemberResponse(
                member.getMemberId(),
                member.getNickname(),
                profileImageUrl,
                member.getRole(),
                member.getStatus(),
                member.getCreatedAt()
        );
    }
}
