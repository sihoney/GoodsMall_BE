package com.example.member.presentation.dto;

import com.example.member.domain.enumtype.RestrictionType;
import java.util.UUID;

public record CreateMemberRestrictionRequest(
        UUID memberId,
        String reason,
        RestrictionType restrictionType,
        Integer durationHours
) {
}
