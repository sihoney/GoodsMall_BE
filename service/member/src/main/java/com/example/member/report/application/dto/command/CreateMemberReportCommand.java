package com.example.member.report.application.dto.command;

import com.example.member.report.domain.enumtype.ReportType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.util.UUID;

public record CreateMemberReportCommand(
        @NotNull
        UUID reportedMemberId,
        @NotBlank
        String reason,
        @NotNull
        ReportType reportType
) {
}
