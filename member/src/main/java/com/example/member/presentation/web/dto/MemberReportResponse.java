package com.example.member.presentation.web.dto;

import com.example.member.application.dto.result.MemberReportResult;
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

    public static MemberReportResponse from(MemberReportResult result) {
        return new MemberReportResponse(
                result.reportId(),
                result.reporterId(),
                result.reportedMemberId(),
                result.reason(),
                result.reportType(),
                result.status(),
                result.reviewComment(),
                result.reviewedBy(),
                result.reviewedAt(),
                result.createdAt(),
                result.updatedAt()
        );
    }
}

