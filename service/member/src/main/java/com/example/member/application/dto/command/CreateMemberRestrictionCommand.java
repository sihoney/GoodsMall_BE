package com.example.member.application.dto.command;

import com.example.member.domain.enumtype.RestrictionType;
import java.util.UUID;

public record CreateMemberRestrictionCommand(
        UUID memberId,
        String reason,
        RestrictionType restrictionType,
        Integer durationHours
) {
}
