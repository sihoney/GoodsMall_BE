package com.example.member.report.presentation.web.dto;

import com.example.member.report.domain.enumtype.ReportType;
import jakarta.validation.constraints.NotNull;
import java.util.UUID;

public record CreateMemberReportRequest(
        @NotNull(message = "reportedMemberId는 필수입니다.")
        UUID reportedMemberId,
        String reason,
        @NotNull(message = "reportType은 필수입니다.")
        ReportType reportType
) {
}

