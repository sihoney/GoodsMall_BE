package com.example.member.application.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.example.member.common.exception.MemberReportNotFoundException;
import com.example.member.common.exception.DuplicateMemberReportException;
import com.example.member.common.exception.SelfReportNotAllowedException;
import com.example.member.domain.entity.Member;
import com.example.member.domain.entity.MemberReport;
import com.example.member.domain.enumtype.MemberStatus;
import com.example.member.domain.enumtype.ReportStatus;
import com.example.member.domain.enumtype.ReportType;
import com.example.member.domain.enumtype.RestrictionType;
import com.example.member.infrastructure.repository.MemberReportRepository;
import com.example.member.infrastructure.repository.MemberRepository;
import com.example.member.presentation.dto.CreateMemberReportRequest;
import com.example.member.presentation.dto.CreateMemberRestrictionRequest;
import com.example.member.presentation.dto.MemberReportResponse;
import com.example.member.presentation.dto.ReviewMemberReportRequest;
import com.todaylunch.common.security.auth.dto.AuthenticatedMember;
import com.todaylunch.common.security.auth.enumtype.MemberRole;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class MemberReportServiceTest {

    @Mock
    private MemberRepository memberRepository;

    @Mock
    private MemberReportRepository memberReportRepository;

    @Mock
    private MemberRestrictionService memberRestrictionService;

    @InjectMocks
    private MemberReportService memberReportService;

    @Test
    void createReport_success_savesReport() {
        UUID reporterId = UUID.randomUUID();
        UUID reportedMemberId = UUID.randomUUID();
        AuthenticatedMember reporter = new AuthenticatedMember(reporterId, MemberRole.USER, UUID.randomUUID());
        CreateMemberReportRequest request = new CreateMemberReportRequest(
                reportedMemberId,
                "spam messages",
                ReportType.SPAM
        );

        when(memberRepository.findById(reporterId)).thenReturn(Optional.of(createMember(reporterId)));
        when(memberRepository.findById(reportedMemberId)).thenReturn(Optional.of(createMember(reportedMemberId)));
        when(memberReportRepository.existsPendingReport(reporterId, reportedMemberId)).thenReturn(false);
        when(memberReportRepository.save(any(MemberReport.class))).thenAnswer(invocation -> invocation.getArgument(0));

        MemberReportResponse response = memberReportService.createReport(reporter, request);

        ArgumentCaptor<MemberReport> reportCaptor = ArgumentCaptor.forClass(MemberReport.class);
        verify(memberReportRepository).save(reportCaptor.capture());
        assertEquals(reporterId, reportCaptor.getValue().getReporterId());
        assertEquals(reportedMemberId, reportCaptor.getValue().getReportedMemberId());
        assertEquals(ReportStatus.PENDING, response.status());
    }

    @Test
    void createReport_selfReport_throwsException() {
        UUID memberId = UUID.randomUUID();
        AuthenticatedMember reporter = new AuthenticatedMember(memberId, MemberRole.USER, UUID.randomUUID());
        CreateMemberReportRequest request = new CreateMemberReportRequest(
                memberId,
                "self report",
                ReportType.ETC
        );

        assertThrows(SelfReportNotAllowedException.class, () -> memberReportService.createReport(reporter, request));
    }

    @Test
    void createReport_duplicatePendingReport_throwsException() {
        UUID reporterId = UUID.randomUUID();
        UUID reportedMemberId = UUID.randomUUID();
        AuthenticatedMember reporter = new AuthenticatedMember(reporterId, MemberRole.USER, UUID.randomUUID());
        CreateMemberReportRequest request = new CreateMemberReportRequest(
                reportedMemberId,
                "fraud",
                ReportType.FRAUD
        );

        when(memberRepository.findById(reporterId)).thenReturn(Optional.of(createMember(reporterId)));
        when(memberRepository.findById(reportedMemberId)).thenReturn(Optional.of(createMember(reportedMemberId)));
        when(memberReportRepository.existsPendingReport(reporterId, reportedMemberId)).thenReturn(true);

        assertThrows(DuplicateMemberReportException.class, () -> memberReportService.createReport(reporter, request));
        verify(memberReportRepository, never()).save(any(MemberReport.class));
    }

    @Test
    void approveReport_withRestriction_createsRestriction() {
        UUID adminId = UUID.randomUUID();
        UUID reportedMemberId = UUID.randomUUID();
        AuthenticatedMember admin = new AuthenticatedMember(adminId, MemberRole.ADMIN, UUID.randomUUID());
        MemberReport memberReport = MemberReport.create(
                UUID.randomUUID(),
                UUID.randomUUID(),
                reportedMemberId,
                "abuse",
                ReportType.ABUSE,
                LocalDateTime.now()
        );
        ReviewMemberReportRequest request = new ReviewMemberReportRequest(
                "confirmed",
                RestrictionType.LOGIN_BAN,
                24
        );

        when(memberReportRepository.findById(memberReport.getReportId())).thenReturn(Optional.of(memberReport));

        MemberReportResponse response = memberReportService.approveReport(admin, memberReport.getReportId(), request);

        verify(memberRestrictionService).createRestriction(any(), any(CreateMemberRestrictionRequest.class));
        assertEquals(ReportStatus.APPROVED, response.status());
        assertEquals(adminId, response.reviewedBy());
    }

    @Test
    void getReportDetail_success_returnsReport() {
        AuthenticatedMember admin = new AuthenticatedMember(UUID.randomUUID(), MemberRole.ADMIN, UUID.randomUUID());
        MemberReport memberReport = MemberReport.create(
                UUID.randomUUID(),
                UUID.randomUUID(),
                UUID.randomUUID(),
                "abuse",
                ReportType.ABUSE,
                LocalDateTime.now()
        );

        when(memberReportRepository.findById(memberReport.getReportId())).thenReturn(Optional.of(memberReport));

        MemberReportResponse response = memberReportService.getReportDetail(admin, memberReport.getReportId());

        assertEquals(memberReport.getReportId(), response.reportId());
        assertEquals(memberReport.getReason(), response.reason());
    }

    @Test
    void getReportDetail_missingReport_throwsException() {
        AuthenticatedMember admin = new AuthenticatedMember(UUID.randomUUID(), MemberRole.ADMIN, UUID.randomUUID());
        UUID reportId = UUID.randomUUID();

        when(memberReportRepository.findById(reportId)).thenReturn(Optional.empty());

        assertThrows(MemberReportNotFoundException.class,
                () -> memberReportService.getReportDetail(admin, reportId));
    }

    private Member createMember(UUID memberId) {
        LocalDateTime now = LocalDateTime.now();
        return Member.create(
                memberId,
                "member@test.com",
                "encoded-password",
                "tester",
                null,
                null,
                null,
                MemberRole.USER,
                MemberStatus.ACTIVE,
                now,
                now
        );
    }
}
