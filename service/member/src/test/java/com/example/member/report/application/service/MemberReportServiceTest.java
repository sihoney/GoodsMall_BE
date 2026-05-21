package com.example.member.report.application.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.example.member.report.application.dto.command.CreateMemberReportCommand;
import com.example.member.restriction.application.dto.command.CreateMemberRestrictionCommand;
import com.example.member.restriction.application.service.MemberRestrictionService;
import com.example.member.report.application.dto.command.ReviewMemberReportCommand;
import com.example.member.report.application.dto.result.MemberReportResult;
import com.example.member.report.exception.DuplicateMemberReportException;
import com.example.member.report.exception.MemberReportNotFoundException;
import com.example.member.report.exception.SelfReportNotAllowedException;
import com.example.member.member.domain.entity.Member;
import com.example.member.report.domain.entity.MemberReport;
import com.example.member.member.domain.enumtype.MemberStatus;
import com.example.member.report.domain.enumtype.ReportStatus;
import com.example.member.report.domain.enumtype.ReportType;
import com.example.member.restriction.domain.enumtype.RestrictionType;
import com.example.member.member.infrastructure.persistence.jpa.MemberJpaAdapter;
import com.example.member.report.infrastructure.persistence.jpa.MemberReportJpaAdapter;
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
    private MemberJpaAdapter memberPersistencePort;

    @Mock
    private MemberReportJpaAdapter memberReportPersistencePort;

    @Mock
    private MemberRestrictionService memberRestrictionService;

    @InjectMocks
    private MemberReportService memberReportService;

    @Test
    void createReport_success_savesReport() {
        UUID reporterId = UUID.randomUUID();
        UUID reportedMemberId = UUID.randomUUID();
        AuthenticatedMember reporter = new AuthenticatedMember(reporterId, MemberRole.USER, UUID.randomUUID());
        CreateMemberReportCommand command = new CreateMemberReportCommand(
                reportedMemberId,
                "spam messages",
                ReportType.SPAM
        );

        when(memberPersistencePort.findById(reporterId)).thenReturn(Optional.of(createMember(reporterId)));
        when(memberPersistencePort.findById(reportedMemberId)).thenReturn(Optional.of(createMember(reportedMemberId)));
        when(memberReportPersistencePort.existsPendingReport(reporterId, reportedMemberId)).thenReturn(false);
        when(memberReportPersistencePort.save(any(MemberReport.class))).thenAnswer(invocation -> invocation.getArgument(0));

        MemberReportResult response = memberReportService.createReport(reporter, command);

        ArgumentCaptor<MemberReport> reportCaptor = ArgumentCaptor.forClass(MemberReport.class);
        verify(memberReportPersistencePort).save(reportCaptor.capture());
        assertEquals(reporterId, reportCaptor.getValue().getReporterId());
        assertEquals(reportedMemberId, reportCaptor.getValue().getReportedMemberId());
        assertEquals(ReportStatus.PENDING, response.status());
    }

    @Test
    void createReport_selfReport_throwsException() {
        UUID memberId = UUID.randomUUID();
        AuthenticatedMember reporter = new AuthenticatedMember(memberId, MemberRole.USER, UUID.randomUUID());
        CreateMemberReportCommand command = new CreateMemberReportCommand(
                memberId,
                "self report",
                ReportType.ETC
        );

        assertThrows(SelfReportNotAllowedException.class, () -> memberReportService.createReport(reporter, command));
    }

    @Test
    void createReport_duplicatePendingReport_throwsException() {
        UUID reporterId = UUID.randomUUID();
        UUID reportedMemberId = UUID.randomUUID();
        AuthenticatedMember reporter = new AuthenticatedMember(reporterId, MemberRole.USER, UUID.randomUUID());
        CreateMemberReportCommand command = new CreateMemberReportCommand(
                reportedMemberId,
                "fraud",
                ReportType.FRAUD
        );

        when(memberPersistencePort.findById(reporterId)).thenReturn(Optional.of(createMember(reporterId)));
        when(memberPersistencePort.findById(reportedMemberId)).thenReturn(Optional.of(createMember(reportedMemberId)));
        when(memberReportPersistencePort.existsPendingReport(reporterId, reportedMemberId)).thenReturn(true);

        assertThrows(DuplicateMemberReportException.class, () -> memberReportService.createReport(reporter, command));
        verify(memberReportPersistencePort, never()).save(any(MemberReport.class));
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
        ReviewMemberReportCommand command = new ReviewMemberReportCommand(
                "confirmed",
                RestrictionType.LOGIN_BAN,
                24
        );

        when(memberReportPersistencePort.findById(memberReport.getReportId())).thenReturn(Optional.of(memberReport));

        MemberReportResult response = memberReportService.approveReport(admin, memberReport.getReportId(), command);

        verify(memberRestrictionService).createRestriction(any(), any(CreateMemberRestrictionCommand.class));
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

        when(memberReportPersistencePort.findById(memberReport.getReportId())).thenReturn(Optional.of(memberReport));

        MemberReportResult response = memberReportService.getReportDetail(admin, memberReport.getReportId());

        assertEquals(memberReport.getReportId(), response.reportId());
        assertEquals(memberReport.getReason(), response.reason());
    }

    @Test
    void getReportDetail_missingReport_throwsException() {
        AuthenticatedMember admin = new AuthenticatedMember(UUID.randomUUID(), MemberRole.ADMIN, UUID.randomUUID());
        UUID reportId = UUID.randomUUID();

        when(memberReportPersistencePort.findById(reportId)).thenReturn(Optional.empty());

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
