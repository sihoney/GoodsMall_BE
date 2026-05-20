package com.example.member.restriction.presentation.web.dto;

import com.example.member.restriction.application.dto.result.MemberRestrictionResult;
import com.example.member.restriction.domain.enumtype.RestrictionType;
import java.time.LocalDateTime;
import java.util.UUID;

public record MemberRestrictionResponse(
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

    public static MemberRestrictionResponse from(MemberRestrictionResult result) {
        return new MemberRestrictionResponse(
                result.restrictionId(),
                result.memberId(),
                result.memberNickname(),
                result.adminId(),
                result.adminNickname(),
                result.reason(),
                result.restrictionType(),
                result.durationHours(),
                result.endAt(),
                result.active(),
                result.createdAt(),
                result.updatedAt()
        );
    }
}

