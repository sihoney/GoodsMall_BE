package com.example.member.member.application.dto.result;

import com.example.member.member.domain.enumtype.MemberStatus;
import com.todaylunch.common.security.auth.enumtype.MemberRole;
import java.time.LocalDateTime;
import java.util.UUID;

public record CreateMemberResult(
        UUID memberId,
        String nickname,
        String profileImageUrl,
        MemberRole role,
        MemberStatus status,
        LocalDateTime createdAt
) {
}
