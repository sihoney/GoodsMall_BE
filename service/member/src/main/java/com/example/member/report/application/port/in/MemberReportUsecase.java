package com.example.member.report.application.port.in;

import com.example.member.report.application.dto.command.CreateMemberReportCommand;
import com.example.member.report.application.dto.command.ReviewMemberReportCommand;
import com.example.member.report.application.dto.result.MemberReportResult;
import com.todaylunch.common.security.auth.dto.AuthenticatedMember;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import java.util.List;
import java.util.UUID;

public interface MemberReportUsecase {

    MemberReportResult createReport(AuthenticatedMember authenticatedMember, @Valid @NotNull CreateMemberReportCommand command);

    List<MemberReportResult> getMyReports(AuthenticatedMember authenticatedMember);

    List<MemberReportResult> getReportsForMember(AuthenticatedMember authenticatedMember, UUID memberId);

    List<MemberReportResult> getAllReports(AuthenticatedMember authenticatedMember);

    MemberReportResult getReportDetail(AuthenticatedMember authenticatedMember, UUID reportId);

    MemberReportResult approveReport(
            AuthenticatedMember authenticatedMember,
            UUID reportId,
            @Valid @NotNull ReviewMemberReportCommand command
    );

    MemberReportResult rejectReport(
            AuthenticatedMember authenticatedMember,
            UUID reportId,
            @Valid @NotNull ReviewMemberReportCommand command
    );
}
