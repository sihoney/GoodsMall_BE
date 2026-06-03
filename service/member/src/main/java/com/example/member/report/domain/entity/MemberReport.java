package com.example.member.report.domain.entity;

import com.example.member.report.domain.enumtype.ReportStatus;
import com.example.member.report.domain.enumtype.ReportType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import java.util.Objects;
import java.util.UUID;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
// TODO: Separate persistence mapping from the domain model so this can become a pure domain entity.
@Entity
@Table(name = "member_report")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class MemberReport {

    @Id
    @Column(name = "report_id", nullable = false, updatable = false)
    private UUID reportId;

    @Column(name = "reporter_id", nullable = false, updatable = false)
    private UUID reporterId;

    @Column(name = "reported_member_id", nullable = false, updatable = false)
    private UUID reportedMemberId;

    @Column(name = "reason", nullable = false)
    private String reason;

    @Enumerated(EnumType.STRING)
    @Column(name = "report_type", nullable = false)
    private ReportType reportType;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private ReportStatus status;

    @Column(name = "review_comment")
    private String reviewComment;

    @Column(name = "reviewed_by")
    private UUID reviewedBy;

    @Column(name = "reviewed_at")
    private LocalDateTime reviewedAt;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    private MemberReport(
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
        this.reportId = Objects.requireNonNull(reportId);
        this.reporterId = Objects.requireNonNull(reporterId);
        this.reportedMemberId = Objects.requireNonNull(reportedMemberId);
        this.reason = validateReason(reason);
        this.reportType = Objects.requireNonNull(reportType);
        this.status = Objects.requireNonNull(status);
        this.reviewComment = normalizeNullable(reviewComment);
        this.reviewedBy = reviewedBy;
        this.reviewedAt = reviewedAt;
        this.createdAt = Objects.requireNonNull(createdAt);
        this.updatedAt = Objects.requireNonNull(updatedAt);
    }

    public static MemberReport create(
            UUID reportId,
            UUID reporterId,
            UUID reportedMemberId,
            String reason,
            ReportType reportType,
            LocalDateTime createdAt
    ) {
        LocalDateTime now = Objects.requireNonNull(createdAt);
        return new MemberReport(
                reportId,
                reporterId,
                reportedMemberId,
                reason,
                reportType,
                ReportStatus.PENDING,
                null,
                null,
                null,
                now,
                now
        );
    }

    public void approve(UUID reviewedBy, String reviewComment, LocalDateTime reviewedAt) {
        validatePending();
        this.status = ReportStatus.APPROVED;
        this.reviewComment = normalizeNullable(reviewComment);
        this.reviewedBy = Objects.requireNonNull(reviewedBy);
        this.reviewedAt = Objects.requireNonNull(reviewedAt);
        this.updatedAt = reviewedAt;
    }

    public void reject(UUID reviewedBy, String reviewComment, LocalDateTime reviewedAt) {
        validatePending();
        this.status = ReportStatus.REJECTED;
        this.reviewComment = normalizeNullable(reviewComment);
        this.reviewedBy = Objects.requireNonNull(reviewedBy);
        this.reviewedAt = Objects.requireNonNull(reviewedAt);
        this.updatedAt = reviewedAt;
    }

    private void validatePending() {
        if (status != ReportStatus.PENDING) {
            throw new IllegalStateException("대기 중인 신고만 검토할 수 있습니다.");
        }
    }

    private static String validateReason(String reason) {
        if (reason == null || reason.trim().isEmpty()) {
            throw new IllegalArgumentException("reason은 필수입니다.");
        }
        return reason.trim();
    }

    private static String normalizeNullable(String value) {
        if (value == null) {
            return null;
        }
        String normalized = value.trim();
        return normalized.isEmpty() ? null : normalized;
    }
}
