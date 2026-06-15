package com.example.member.report.infrastructure.persistence.jpa;

import com.example.member.report.domain.entity.MemberReport;
import com.example.member.report.domain.enumtype.ReportStatus;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MemberReportJpaRepository extends JpaRepository<MemberReport, UUID> {

    boolean existsByReporterIdAndReportedMemberIdAndStatus(UUID reporterId, UUID reportedMemberId, ReportStatus status);

    List<MemberReport> findAllByReporterIdOrderByCreatedAtDesc(UUID reporterId);

    List<MemberReport> findAllByReportedMemberIdOrderByCreatedAtDesc(UUID reportedMemberId);

    List<MemberReport> findAllByOrderByCreatedAtDesc();
}
