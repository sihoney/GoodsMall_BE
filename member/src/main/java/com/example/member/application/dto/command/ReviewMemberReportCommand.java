package com.example.member.application.dto.command;

import com.example.member.domain.enumtype.RestrictionType;

public record ReviewMemberReportCommand(
        String reviewComment,
        RestrictionType restrictionType,
        Integer durationHours
) {
}
