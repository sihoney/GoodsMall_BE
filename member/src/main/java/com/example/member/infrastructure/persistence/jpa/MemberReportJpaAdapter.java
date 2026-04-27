package com.example.member.infrastructure.persistence.jpa;

import com.example.member.application.port.out.MemberReportPersistencePort;
import com.example.member.domain.entity.MemberReport;
import com.example.member.domain.enumtype.ReportStatus;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class MemberReportJpaAdapter implements MemberReportPersistencePort {

    private final MemberReportJpaRepository memberReportJpaRepository;

    @Override
    public MemberReport save(MemberReport memberReport) {
        return memberReportJpaRepository.save(memberReport);
    }

    @Override
    public Optional<MemberReport> findById(UUID reportId) {
        return memberReportJpaRepository.findById(reportId);
    }

    @Override
    public boolean existsPendingReport(UUID reporterId, UUID reportedMemberId) {
        return memberReportJpaRepository.existsByReporterIdAndReportedMemberIdAndStatus(
                reporterId,
                reportedMemberId,
                ReportStatus.PENDING
        );
    }

    @Override
    public List<MemberReport> findAllByReporterId(UUID reporterId) {
        return memberReportJpaRepository.findAllByReporterIdOrderByCreatedAtDesc(reporterId);
    }

    @Override
    public List<MemberReport> findAllByReportedMemberId(UUID reportedMemberId) {
        return memberReportJpaRepository.findAllByReportedMemberIdOrderByCreatedAtDesc(reportedMemberId);
    }

    @Override
    public List<MemberReport> findAll() {
        return memberReportJpaRepository.findAllByOrderByCreatedAtDesc();
    }
}
