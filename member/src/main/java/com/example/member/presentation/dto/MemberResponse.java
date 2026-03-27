package com.example.member.presentation.dto;

import com.example.member.domain.entity.Member;
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

    public static MemberResponse from(Member member) {
        return new MemberResponse(
                member.getMemberId(),
                member.getEmail(),
                member.getNickname(),
                member.getPhone(),
                member.getAddress(),
                member.getProfileImageUrl(),
                member.getRole(),
                member.getStatus(),
                member.getCreatedAt(),
                member.getUpdatedAt()
        );
    }
}
