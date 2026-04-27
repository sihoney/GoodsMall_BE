package com.example.member.presentation.web.dto;

import com.example.member.domain.enumtype.RestrictionType;

public record ReviewMemberReportRequest(
        String reviewComment,
        RestrictionType restrictionType,
        Integer durationHours
) {
}

