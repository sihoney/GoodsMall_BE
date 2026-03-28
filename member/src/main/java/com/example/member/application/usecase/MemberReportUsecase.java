package com.example.member.application.usecase;

import com.example.member.presentation.dto.CreateMemberReportRequest;
import com.example.member.presentation.dto.MemberReportResponse;
import com.example.member.presentation.dto.ReviewMemberReportRequest;
import com.todaylunch.common.security.auth.dto.AuthenticatedMember;
import java.util.List;
import java.util.UUID;

public interface MemberReportUsecase {

    MemberReportResponse createReport(AuthenticatedMember authenticatedMember, CreateMemberReportRequest request);

    List<MemberReportResponse> getMyReports(AuthenticatedMember authenticatedMember);

    List<MemberReportResponse> getReportsForMember(AuthenticatedMember authenticatedMember, UUID memberId);

    List<MemberReportResponse> getAllReports(AuthenticatedMember authenticatedMember);

    MemberReportResponse approveReport(
            AuthenticatedMember authenticatedMember,
            UUID reportId,
            ReviewMemberReportRequest request
    );

    MemberReportResponse rejectReport(
            AuthenticatedMember authenticatedMember,
            UUID reportId,
            ReviewMemberReportRequest request
    );
}
