package com.example.member.report.presentation.web;

import com.example.member.report.application.dto.command.CreateMemberReportCommand;
import com.example.member.report.application.port.in.MemberReportUsecase;
import com.example.member.common.presentation.web.dto.ApiResponse;
import com.example.member.report.presentation.web.dto.CreateMemberReportRequest;
import com.example.member.report.presentation.web.dto.MemberReportResponse;
import com.todaylunch.common.security.auth.annotation.CurrentMember;
import com.todaylunch.common.security.auth.dto.AuthenticatedMember;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/member-reports")
@RequiredArgsConstructor
@Tag(name = "회원 신고", description = "회원 신고 API")
public class MemberReportController {

    private final MemberReportUsecase memberReportUsecase;

    @PostMapping
    @Operation(summary = "회원 신고 생성", description = "회원에 대한 신고를 생성합니다.")
    public ResponseEntity<ApiResponse<MemberReportResponse>> createReport(
            @CurrentMember AuthenticatedMember authenticatedMember,
            @Valid @RequestBody CreateMemberReportRequest request
    ) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(MemberReportResponse.from(
                        memberReportUsecase.createReport(
                                authenticatedMember,
                                new CreateMemberReportCommand(
                                        request.reportedMemberId(),
                                        request.reason(),
                                        request.reportType()
                                )
                        )
                )));
    }

    @GetMapping("/me")
    @Operation(summary = "내 신고 조회", description = "현재 회원이 작성한 신고를 조회합니다.")
    public ResponseEntity<ApiResponse<List<MemberReportResponse>>> getMyReports(
            @CurrentMember AuthenticatedMember authenticatedMember
    ) {
        return ResponseEntity.ok(ApiResponse.success(
                memberReportUsecase.getMyReports(authenticatedMember).stream()
                        .map(MemberReportResponse::from)
                        .toList()
        ));
    }
}


