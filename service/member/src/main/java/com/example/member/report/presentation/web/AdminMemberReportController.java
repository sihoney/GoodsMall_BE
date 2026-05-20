package com.example.member.report.presentation.web;

import com.example.member.report.application.dto.command.ReviewMemberReportCommand;
import com.example.member.report.application.port.in.MemberReportUsecase;
import com.example.member.common.presentation.web.dto.ApiResponse;
import com.example.member.report.presentation.web.dto.MemberReportResponse;
import com.example.member.report.presentation.web.dto.ReviewMemberReportRequest;
import com.todaylunch.common.security.auth.annotation.CurrentMember;
import com.todaylunch.common.security.auth.dto.AuthenticatedMember;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
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
@RequestMapping("/api/admin/member-reports")
@RequiredArgsConstructor
@Tag(name = "관리자 회원 신고", description = "관리자 회원 신고 검토 API")
public class AdminMemberReportController {

    private final MemberReportUsecase memberReportUsecase;

    @GetMapping
    @Operation(summary = "전체 회원 신고 조회", description = "등록된 모든 회원 신고를 조회합니다.")
    public ResponseEntity<ApiResponse<List<MemberReportResponse>>> getAllReports(
            @CurrentMember AuthenticatedMember authenticatedMember
    ) {
        return ResponseEntity.ok(ApiResponse.success(
                memberReportUsecase.getAllReports(authenticatedMember).stream()
                        .map(MemberReportResponse::from)
                        .toList()
        ));
    }

    @GetMapping("/{reportId}")
    @Operation(summary = "회원 신고 상세 조회", description = "특정 회원 신고의 상세 정보를 조회합니다.")
    public ResponseEntity<ApiResponse<MemberReportResponse>> getReportDetail(
            @CurrentMember AuthenticatedMember authenticatedMember,
            @PathVariable(name = "reportId") UUID reportId
    ) {
        return ResponseEntity.ok(ApiResponse.success(
                MemberReportResponse.from(memberReportUsecase.getReportDetail(authenticatedMember, reportId))
        ));
    }

    @GetMapping("/members/{memberId}")
    @Operation(summary = "회원별 신고 조회", description = "특정 회원에 대한 신고 목록을 조회합니다.")
    public ResponseEntity<ApiResponse<List<MemberReportResponse>>> getReportsForMember(
            @CurrentMember AuthenticatedMember authenticatedMember,
            @PathVariable(name = "memberId") UUID memberId
    ) {
        return ResponseEntity.ok(ApiResponse.success(
                memberReportUsecase.getReportsForMember(authenticatedMember, memberId).stream()
                        .map(MemberReportResponse::from)
                        .toList()
        ));
    }

    @PatchMapping("/{reportId}/approve")
    @Operation(summary = "회원 신고 승인", description = "신고를 승인하고 필요하면 제재를 생성합니다.")
    public ResponseEntity<ApiResponse<MemberReportResponse>> approveReport(
            @CurrentMember AuthenticatedMember authenticatedMember,
            @PathVariable(name = "reportId") UUID reportId,
            @Valid @RequestBody ReviewMemberReportRequest request
    ) {
        return ResponseEntity.ok(ApiResponse.success(
                MemberReportResponse.from(
                        memberReportUsecase.approveReport(
                                authenticatedMember,
                                reportId,
                                new ReviewMemberReportCommand(
                                        request.reviewComment(),
                                        request.restrictionType(),
                                        request.durationHours()
                                )
                        )
                )
        ));
    }

    @PatchMapping("/{reportId}/reject")
    @Operation(summary = "회원 신고 반려", description = "신고를 반려합니다.")
    public ResponseEntity<ApiResponse<MemberReportResponse>> rejectReport(
            @CurrentMember AuthenticatedMember authenticatedMember,
            @PathVariable(name = "reportId") UUID reportId,
            @Valid @RequestBody ReviewMemberReportRequest request
    ) {
        return ResponseEntity.ok(ApiResponse.success(
                MemberReportResponse.from(
                        memberReportUsecase.rejectReport(
                                authenticatedMember,
                                reportId,
                                new ReviewMemberReportCommand(
                                        request.reviewComment(),
                                        request.restrictionType(),
                                        request.durationHours()
                                )
                        )
                )
        ));
    }
}


