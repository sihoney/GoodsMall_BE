package com.example.member.report.presentation.web.dto;

import com.example.member.restriction.domain.enumtype.RestrictionType;

public record ReviewMemberReportRequest(
        String reviewComment,
        RestrictionType restrictionType,
        // TODO: add @Min(1) when durationHours is required for restriction decisions.
        Integer durationHours
) {
}

