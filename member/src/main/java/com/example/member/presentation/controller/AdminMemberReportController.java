package com.example.member.presentation.controller;

import com.example.member.application.usecase.MemberReportUsecase;
import com.example.member.presentation.dto.ApiResponse;
import com.example.member.presentation.dto.MemberReportResponse;
import com.example.member.presentation.dto.ReviewMemberReportRequest;
import com.todaylunch.common.security.auth.annotation.CurrentMember;
import com.todaylunch.common.security.auth.dto.AuthenticatedMember;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/admin/member-reports")
@RequiredArgsConstructor
@Tag(name = "Admin Member Report", description = "Admin member report review APIs")
public class AdminMemberReportController {

    private final MemberReportUsecase memberReportUsecase;

    @GetMapping
    @Operation(summary = "회원 신고 조회", description = "모든 회원 신고를 조회하는 API입니다.")
    public ResponseEntity<ApiResponse<List<MemberReportResponse>>> getAllReports(
            @CurrentMember AuthenticatedMember authenticatedMember
    ) {
        return ResponseEntity.ok(ApiResponse.success(memberReportUsecase.getAllReports(authenticatedMember)));
    }

    @GetMapping("/members/{memberId}")
    @Operation(summary = "회원 신고 조회", description = "특정 회원에 대한 신고를 조회하는 API입니다.")
    public ResponseEntity<ApiResponse<List<MemberReportResponse>>> getReportsForMember(
            @CurrentMember AuthenticatedMember authenticatedMember,
            @PathVariable UUID memberId
    ) {
        return ResponseEntity.ok(ApiResponse.success(
                memberReportUsecase.getReportsForMember(authenticatedMember, memberId)
        ));
    }

    @PatchMapping("/{reportId}/approve")
    @Operation(summary = "회원 신고 승인", description = "신고를 승인하고 선택적으로 제제를 생성합니다.")
    public ResponseEntity<ApiResponse<MemberReportResponse>> approveReport(
            @CurrentMember AuthenticatedMember authenticatedMember,
            @PathVariable UUID reportId,
            @RequestBody ReviewMemberReportRequest request
    ) {
        return ResponseEntity.ok(ApiResponse.success(
                memberReportUsecase.approveReport(authenticatedMember, reportId, request)
        ));
    }

    @PatchMapping("/{reportId}/reject")
    @Operation(summary = "회원 신고 거부", description = "신고를 거부합니다.")
    public ResponseEntity<ApiResponse<MemberReportResponse>> rejectReport(
            @CurrentMember AuthenticatedMember authenticatedMember,
            @PathVariable UUID reportId,
            @RequestBody ReviewMemberReportRequest request
    ) {
        return ResponseEntity.ok(ApiResponse.success(
                memberReportUsecase.rejectReport(authenticatedMember, reportId, request)
        ));
    }
}
