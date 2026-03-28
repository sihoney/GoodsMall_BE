package com.example.member.presentation.dto;

import com.example.member.domain.enumtype.ReportType;
import java.util.UUID;

public record CreateMemberReportRequest(
        UUID reportedMemberId,
        String reason,
        ReportType reportType
) {
}
