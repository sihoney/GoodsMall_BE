package com.example.member.application.dto.result;

import com.example.member.domain.enumtype.ReportStatus;
import com.example.member.domain.enumtype.ReportType;
import java.time.LocalDateTime;
import java.util.UUID;

public record MemberReportResult(
        UUID reportId,
        UUID reporterId,
        UUID reportedMemberId,
        String reason,
        ReportType reportType,
        ReportStatus status,
        String reviewComment,
        UUID reviewedBy,
        LocalDateTime reviewedAt,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
}
