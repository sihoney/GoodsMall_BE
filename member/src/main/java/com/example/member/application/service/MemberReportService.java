package com.example.member.application.service;

import com.example.member.application.usecase.MemberReportUsecase;
import com.example.member.common.exception.AdminAccessDeniedException;
import com.example.member.common.exception.DuplicateMemberReportException;
import com.example.member.common.exception.MemberNotFoundException;
import com.example.member.common.exception.MemberReportNotFoundException;
import com.example.member.common.exception.SelfReportNotAllowedException;
import com.example.member.domain.entity.MemberReport;
import com.example.member.infrastructure.repository.MemberReportRepository;
import com.example.member.infrastructure.repository.MemberRepository;
import com.example.member.presentation.dto.CreateMemberReportRequest;
import com.example.member.presentation.dto.CreateMemberRestrictionRequest;
import com.example.member.presentation.dto.MemberReportResponse;
import com.example.member.presentation.dto.ReviewMemberReportRequest;
import com.todaylunch.common.security.auth.dto.AuthenticatedMember;
import com.todaylunch.common.security.auth.enumtype.MemberRole;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class MemberReportService implements MemberReportUsecase {

    private final MemberRepository memberRepository;
    private final MemberReportRepository memberReportRepository;
    private final MemberRestrictionService memberRestrictionService;

    @Transactional
    @Override
    public MemberReportResponse createReport(
            AuthenticatedMember authenticatedMember,
            CreateMemberReportRequest request
    ) {
        validateReporter(authenticatedMember);
        validateCreateRequest(request);

        UUID reporterId = authenticatedMember.memberId();
        UUID reportedMemberId = request.reportedMemberId();
        if (reporterId.equals(reportedMemberId)) {
            throw new SelfReportNotAllowedException();
        }

        memberRepository.findById(reporterId).orElseThrow(MemberNotFoundException::new);
        memberRepository.findById(reportedMemberId).orElseThrow(MemberNotFoundException::new);

        if (memberReportRepository.existsPendingReport(reporterId, reportedMemberId)) {
            throw new DuplicateMemberReportException();
        }

        MemberReport memberReport = MemberReport.create(
                UUID.randomUUID(),
                reporterId,
                reportedMemberId,
                request.reason(),
                request.reportType(),
                LocalDateTime.now()
        );
        return MemberReportResponse.from(memberReportRepository.save(memberReport));
    }

    @Override
    public List<MemberReportResponse> getMyReports(AuthenticatedMember authenticatedMember) {
        validateReporter(authenticatedMember);
        return memberReportRepository.findAllByReporterId(authenticatedMember.memberId()).stream()
                .map(MemberReportResponse::from)
                .toList();
    }

    @Override
    public List<MemberReportResponse> getReportsForMember(AuthenticatedMember authenticatedMember, UUID memberId) {
        validateAdmin(authenticatedMember);
        memberRepository.findById(memberId).orElseThrow(MemberNotFoundException::new);
        return memberReportRepository.findAllByReportedMemberId(memberId).stream()
                .map(MemberReportResponse::from)
                .toList();
    }

    @Override
    public List<MemberReportResponse> getAllReports(AuthenticatedMember authenticatedMember) {
        validateAdmin(authenticatedMember);
        return memberReportRepository.findAll().stream()
                .map(MemberReportResponse::from)
                .toList();
    }

    @Override
    public MemberReportResponse getReportDetail(AuthenticatedMember authenticatedMember, UUID reportId) {
        validateAdmin(authenticatedMember);
        return MemberReportResponse.from(
                memberReportRepository.findById(reportId)
                        .orElseThrow(MemberReportNotFoundException::new)
        );
    }

    @Transactional
    @Override
    public MemberReportResponse approveReport(
            AuthenticatedMember authenticatedMember,
            UUID reportId,
            ReviewMemberReportRequest request
    ) {
        validateAdmin(authenticatedMember);
        validateReviewRequest(request);

        MemberReport memberReport = memberReportRepository.findById(reportId)
                .orElseThrow(MemberReportNotFoundException::new);
        memberReport.approve(authenticatedMember.memberId(), request.reviewComment(), LocalDateTime.now());

        if (request.restrictionType() != null || request.durationHours() != null) {
            if (request.restrictionType() == null || request.durationHours() == null) {
                throw new IllegalArgumentException("restrictionType and durationHours must be provided together.");
            }

            memberRestrictionService.createRestriction(
                    authenticatedMember,
                    new CreateMemberRestrictionRequest(
                            memberReport.getReportedMemberId(),
                            memberReport.getReason(),
                            request.restrictionType(),
                            request.durationHours()
                    )
            );
        }

        return MemberReportResponse.from(memberReport);
    }

    @Transactional
    @Override
    public MemberReportResponse rejectReport(
            AuthenticatedMember authenticatedMember,
            UUID reportId,
            ReviewMemberReportRequest request
    ) {
        validateAdmin(authenticatedMember);
        validateReviewRequest(request);

        MemberReport memberReport = memberReportRepository.findById(reportId)
                .orElseThrow(MemberReportNotFoundException::new);
        memberReport.reject(authenticatedMember.memberId(), request.reviewComment(), LocalDateTime.now());
        return MemberReportResponse.from(memberReport);
    }

    private void validateReporter(AuthenticatedMember authenticatedMember) {
        if (authenticatedMember == null || authenticatedMember.memberId() == null) {
            throw new IllegalArgumentException("Authenticated member is required.");
        }
    }

    private void validateAdmin(AuthenticatedMember authenticatedMember) {
        if (authenticatedMember == null || authenticatedMember.role() != MemberRole.ADMIN) {
            throw new AdminAccessDeniedException();
        }
    }

    private void validateCreateRequest(CreateMemberReportRequest request) {
        if (request == null) {
            throw new IllegalArgumentException("Create member report request body is required.");
        }
        if (request.reportedMemberId() == null) {
            throw new IllegalArgumentException("reportedMemberId is required.");
        }
        if (request.reportType() == null) {
            throw new IllegalArgumentException("reportType is required.");
        }
    }

    private void validateReviewRequest(ReviewMemberReportRequest request) {
        if (request == null) {
            throw new IllegalArgumentException("Review member report request body is required.");
        }
    }
}
