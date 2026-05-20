package com.example.member.application.dto.command;

import com.example.member.domain.enumtype.ReportType;
import java.util.UUID;

public record CreateMemberReportCommand(
        UUID reportedMemberId,
        String reason,
        ReportType reportType
) {
}
