package com.example.member.report.application.dto.command;

import com.example.member.restriction.domain.enumtype.RestrictionType;
import jakarta.validation.constraints.Positive;

public record ReviewMemberReportCommand(
        String reviewComment,
        RestrictionType restrictionType,
        @Positive
        Integer durationHours
) {
}
