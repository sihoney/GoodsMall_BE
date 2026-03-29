package com.example.member.presentation.dto;

import com.example.member.domain.entity.MemberRestriction;
import com.example.member.domain.enumtype.RestrictionType;
import java.time.LocalDateTime;
import java.util.UUID;

public record MemberRestrictionResponse(
        UUID restrictionId,
        UUID memberId,
        UUID adminId,
        String reason,
        RestrictionType restrictionType,
        Integer durationHours,
        LocalDateTime endAt,
        boolean active,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {

    public static MemberRestrictionResponse from(MemberRestriction memberRestriction) {
        return new MemberRestrictionResponse(
                memberRestriction.getRestrictionId(),
                memberRestriction.getMemberId(),
                memberRestriction.getAdminId(),
                memberRestriction.getReason(),
                memberRestriction.getRestrictionType(),
                memberRestriction.getDurationHours(),
                memberRestriction.getEndAt(),
                memberRestriction.isActive(),
                memberRestriction.getCreatedAt(),
                memberRestriction.getUpdatedAt()
        );
    }
}
