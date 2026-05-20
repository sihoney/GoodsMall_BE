package com.example.member.report.presentation.web.dto;

import com.example.member.restriction.domain.enumtype.RestrictionType;

public record ReviewMemberReportRequest(
        String reviewComment,
        RestrictionType restrictionType,
        Integer durationHours
) {
}

