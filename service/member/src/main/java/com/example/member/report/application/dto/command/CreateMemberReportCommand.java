package com.example.member.report.application.dto.command;

import com.example.member.report.domain.enumtype.ReportType;
import java.util.UUID;

public record CreateMemberReportCommand(
        UUID reportedMemberId,
        String reason,
        ReportType reportType
) {
}
