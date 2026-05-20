package com.example.member.report.application.service;

import com.example.member.restriction.application.service.MemberRestrictionService;

import com.example.member.report.application.dto.command.CreateMemberReportCommand;
import com.example.member.restriction.application.dto.command.CreateMemberRestrictionCommand;
import com.example.member.report.application.dto.command.ReviewMemberReportCommand;
import com.example.member.report.application.dto.result.MemberReportResult;
import com.example.member.member.application.port.out.MemberPersistencePort;
import com.example.member.report.application.port.out.MemberReportPersistencePort;
import com.example.member.report.application.port.in.MemberReportUsecase;
import com.example.member.common.exception.DuplicateMemberReportException;
import com.example.member.common.exception.MemberNotFoundException;
import com.example.member.common.exception.MemberReportNotFoundException;
import com.example.member.common.exception.SelfReportNotAllowedException;
import com.example.member.member.domain.entity.Member;
import com.example.member.report.domain.entity.MemberReport;
import com.todaylunch.common.security.auth.dto.AuthenticatedMember;
import com.todaylunch.common.security.auth.util.RoleGuard;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class MemberReportService implements MemberReportUsecase {

    private final MemberPersistencePort memberPersistencePort;
    private final MemberReportPersistencePort memberReportPersistencePort;
    private final MemberRestrictionService memberRestrictionService;

    @Transactional
    @Override
    public MemberReportResult createReport(
            AuthenticatedMember authenticatedMember,
            CreateMemberReportCommand command
    ) {
        validateReporter(authenticatedMember);
        validateCreateCommand(command);

        UUID reporterId = authenticatedMember.memberId();
        UUID reportedMemberId = command.reportedMemberId();
        if (reporterId.equals(reportedMemberId)) {
            throw new SelfReportNotAllowedException();
        }

        memberPersistencePort.findById(reporterId).orElseThrow(MemberNotFoundException::new);
        memberPersistencePort.findById(reportedMemberId).orElseThrow(MemberNotFoundException::new);

        if (memberReportPersistencePort.existsPendingReport(reporterId, reportedMemberId)) {
            throw new DuplicateMemberReportException();
        }

        MemberReport memberReport = MemberReport.create(
                UUID.randomUUID(),
                reporterId,
                reportedMemberId,
                command.reason(),
                command.reportType(),
                LocalDateTime.now()
        );
        return toResult(memberReportPersistencePort.save(memberReport));
    }

    @Override
    public List<MemberReportResult> getMyReports(AuthenticatedMember authenticatedMember) {
        validateReporter(authenticatedMember);
        List<MemberReport> reports = memberReportPersistencePort.findAllByReporterId(authenticatedMember.memberId());
        Map<UUID, String> nicknamesById = resolveNicknames(reports);

        return reports.stream()
                .map(report -> toResult(report, nicknamesById))
                .toList();
    }

    @Override
    public List<MemberReportResult> getReportsForMember(AuthenticatedMember authenticatedMember, UUID memberId) {
        RoleGuard.requireAdmin(authenticatedMember);
        memberPersistencePort.findById(memberId).orElseThrow(MemberNotFoundException::new);
        List<MemberReport> reports = memberReportPersistencePort.findAllByReportedMemberId(memberId);
        Map<UUID, String> nicknamesById = resolveNicknames(reports);

        return reports.stream()
                .map(report -> toResult(report, nicknamesById))
                .toList();
    }

    @Override
    public List<MemberReportResult> getAllReports(AuthenticatedMember authenticatedMember) {
        RoleGuard.requireAdmin(authenticatedMember);
        List<MemberReport> reports = memberReportPersistencePort.findAll();
        Map<UUID, String> nicknamesById = resolveNicknames(reports);

        return reports.stream()
                .map(report -> toResult(report, nicknamesById))
                .toList();
    }

    @Override
    public MemberReportResult getReportDetail(AuthenticatedMember authenticatedMember, UUID reportId) {
        RoleGuard.requireAdmin(authenticatedMember);
        return toResult(
                memberReportPersistencePort.findById(reportId)
                        .orElseThrow(MemberReportNotFoundException::new)
        );
    }

    @Transactional
    @Override
    public MemberReportResult approveReport(
            AuthenticatedMember authenticatedMember,
            UUID reportId,
            ReviewMemberReportCommand command
    ) {
        RoleGuard.requireAdmin(authenticatedMember);
        validateReviewCommand(command);

        MemberReport memberReport = memberReportPersistencePort.findById(reportId)
                .orElseThrow(MemberReportNotFoundException::new);
        memberReport.approve(authenticatedMember.memberId(), command.reviewComment(), LocalDateTime.now());

        if (command.restrictionType() != null || command.durationHours() != null) {
            if (command.restrictionType() == null || command.durationHours() == null) {
                throw new IllegalArgumentException("restrictionType과 durationHours는 함께 입력해야 합니다.");
            }

            memberRestrictionService.createRestriction(
                    authenticatedMember,
                    new CreateMemberRestrictionCommand(
                            memberReport.getReportedMemberId(),
                            memberReport.getReason(),
                            command.restrictionType(),
                            command.durationHours()
                    )
            );
        }

        return toResult(memberReport);
    }

    @Transactional
    @Override
    public MemberReportResult rejectReport(
            AuthenticatedMember authenticatedMember,
            UUID reportId,
            ReviewMemberReportCommand command
    ) {
        RoleGuard.requireAdmin(authenticatedMember);
        validateReviewCommand(command);

        MemberReport memberReport = memberReportPersistencePort.findById(reportId)
                .orElseThrow(MemberReportNotFoundException::new);
        memberReport.reject(authenticatedMember.memberId(), command.reviewComment(), LocalDateTime.now());
        return toResult(memberReport);
    }

    private void validateReporter(AuthenticatedMember authenticatedMember) {
        if (authenticatedMember == null || authenticatedMember.memberId() == null) {
            throw new IllegalArgumentException("인증된 회원 정보는 필수입니다.");
        }
    }

    private void validateCreateCommand(CreateMemberReportCommand command) {
        if (command == null) {
            throw new IllegalArgumentException("회원 신고 생성 요청은 필수입니다.");
        }
        if (command.reportedMemberId() == null) {
            throw new IllegalArgumentException("reportedMemberId는 필수입니다.");
        }
        if (command.reportType() == null) {
            throw new IllegalArgumentException("reportType은 필수입니다.");
        }
    }

    private void validateReviewCommand(ReviewMemberReportCommand command) {
        if (command == null) {
            throw new IllegalArgumentException("회원 신고 검토 요청은 필수입니다.");
        }
    }

    private MemberReportResult toResult(MemberReport memberReport, Map<UUID, String> nicknamesById) {
        return new MemberReportResult(
                memberReport.getReportId(),
                memberReport.getReporterId(),
                nicknamesById.get(memberReport.getReporterId()),
                memberReport.getReportedMemberId(),
                nicknamesById.get(memberReport.getReportedMemberId()),
                memberReport.getReason(),
                memberReport.getReportType(),
                memberReport.getStatus(),
                memberReport.getReviewComment(),
                memberReport.getReviewedBy(),
                nicknamesById.get(memberReport.getReviewedBy()),
                memberReport.getReviewedAt(),
                memberReport.getCreatedAt(),
                memberReport.getUpdatedAt()
        );
    }

    private MemberReportResult toResult(MemberReport memberReport) {
        return toResult(memberReport, resolveNicknames(List.of(memberReport)));
    }

    private Map<UUID, String> resolveNicknames(List<MemberReport> reports) {
        HashSet<UUID> memberIds = new HashSet<>();

        for (MemberReport report : reports) {
            if (report.getReporterId() != null) {
                memberIds.add(report.getReporterId());
            }
            if (report.getReportedMemberId() != null) {
                memberIds.add(report.getReportedMemberId());
            }
            if (report.getReviewedBy() != null) {
                memberIds.add(report.getReviewedBy());
            }
        }

        return memberPersistencePort.findAllByIds(memberIds).stream()
                .collect(Collectors.toMap(Member::getMemberId, Member::getNickname, (left, right) -> left));
    }
}

