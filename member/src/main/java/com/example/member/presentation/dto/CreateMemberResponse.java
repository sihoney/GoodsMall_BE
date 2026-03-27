package com.example.member.presentation.dto;

import com.example.member.domain.entity.Member;
import com.example.member.domain.enumtype.MemberRole;
import com.example.member.domain.enumtype.MemberStatus;
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

    public static CreateMemberResponse from(Member member) {
        return new CreateMemberResponse(
                member.getMemberId(),
                member.getNickname(),
                member.getProfileImageUrl(),
                member.getRole(),
                member.getStatus(),
                member.getCreatedAt()
        );
    }
}
