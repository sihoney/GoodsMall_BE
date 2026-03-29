package com.example.member.infrastructure.repository;

import com.example.member.domain.entity.MemberReport;
import com.example.member.domain.enumtype.ReportStatus;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class MemberReportRepository {

    private final MemberReportJpaRepository memberReportJpaRepository;

    public MemberReport save(MemberReport memberReport) {
        return memberReportJpaRepository.save(memberReport);
    }

    public Optional<MemberReport> findById(UUID reportId) {
        return memberReportJpaRepository.findById(reportId);
    }

    public boolean existsPendingReport(UUID reporterId, UUID reportedMemberId) {
        return memberReportJpaRepository.existsByReporterIdAndReportedMemberIdAndStatus(
                reporterId,
                reportedMemberId,
                ReportStatus.PENDING
        );
    }

    public List<MemberReport> findAllByReporterId(UUID reporterId) {
        return memberReportJpaRepository.findAllByReporterIdOrderByCreatedAtDesc(reporterId);
    }

    public List<MemberReport> findAllByReportedMemberId(UUID reportedMemberId) {
        return memberReportJpaRepository.findAllByReportedMemberIdOrderByCreatedAtDesc(reportedMemberId);
    }

    public List<MemberReport> findAll() {
        return memberReportJpaRepository.findAllByOrderByCreatedAtDesc();
    }
}
