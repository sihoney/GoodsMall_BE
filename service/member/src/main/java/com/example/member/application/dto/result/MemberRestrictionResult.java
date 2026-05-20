package com.example.member.application.dto.result;

import com.example.member.domain.enumtype.RestrictionType;
import java.time.LocalDateTime;
import java.util.UUID;

public record MemberRestrictionResult(
        UUID restrictionId,
        UUID memberId,
        String memberNickname,
        UUID adminId,
        String adminNickname,
        String reason,
        RestrictionType restrictionType,
        Integer durationHours,
        LocalDateTime endAt,
        boolean active,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
}
