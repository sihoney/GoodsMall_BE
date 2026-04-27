package com.example.member.application.dto.result;

import com.example.member.domain.enumtype.MemberStatus;
import com.todaylunch.common.security.auth.enumtype.MemberRole;
import java.time.LocalDateTime;
import java.util.UUID;

public record MemberResult(
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
}
