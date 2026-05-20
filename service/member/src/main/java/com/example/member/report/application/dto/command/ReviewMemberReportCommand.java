package com.example.member.report.application.dto.command;

import com.example.member.restriction.domain.enumtype.RestrictionType;

public record ReviewMemberReportCommand(
        String reviewComment,
        RestrictionType restrictionType,
        Integer durationHours
) {
}
