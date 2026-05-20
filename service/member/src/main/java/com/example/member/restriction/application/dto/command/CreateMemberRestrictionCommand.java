package com.example.member.restriction.application.dto.command;

import com.example.member.restriction.domain.enumtype.RestrictionType;
import java.util.UUID;

public record CreateMemberRestrictionCommand(
        UUID memberId,
        String reason,
        RestrictionType restrictionType,
        Integer durationHours
) {
}
