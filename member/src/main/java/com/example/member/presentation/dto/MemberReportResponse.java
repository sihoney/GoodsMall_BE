package com.example.member.presentation.dto;

import com.example.member.domain.entity.MemberReport;
import com.example.member.domain.enumtype.ReportStatus;
import com.example.member.domain.enumtype.ReportType;
import java.time.LocalDateTime;
import java.util.UUID;

public record MemberReportResponse(
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

    public static MemberReportResponse from(MemberReport memberReport) {
        return new MemberReportResponse(
                memberReport.getReportId(),
                memberReport.getReporterId(),
                memberReport.getReportedMemberId(),
                memberReport.getReason(),
                memberReport.getReportType(),
                memberReport.getStatus(),
                memberReport.getReviewComment(),
                memberReport.getReviewedBy(),
                memberReport.getReviewedAt(),
                memberReport.getCreatedAt(),
                memberReport.getUpdatedAt()
        );
    }
}
