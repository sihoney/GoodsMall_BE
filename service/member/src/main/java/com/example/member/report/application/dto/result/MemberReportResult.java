package com.example.member.report.application.dto.result;

import com.example.member.report.domain.enumtype.ReportStatus;
import com.example.member.report.domain.enumtype.ReportType;
import java.time.LocalDateTime;
import java.util.UUID;

public record MemberReportResult(
        UUID reportId,
        UUID reporterId,
        String reporterNickname,
        UUID reportedMemberId,
        String reportedMemberNickname,
        String reason,
        ReportType reportType,
        ReportStatus status,
        String reviewComment,
        UUID reviewedBy,
        String reviewedByNickname,
        LocalDateTime reviewedAt,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
}
