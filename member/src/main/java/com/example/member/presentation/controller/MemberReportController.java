package com.example.member.presentation.controller;

import com.example.member.application.usecase.MemberReportUsecase;
import com.example.member.presentation.dto.ApiResponse;
import com.example.member.presentation.dto.CreateMemberReportRequest;
import com.example.member.presentation.dto.MemberReportResponse;
import com.todaylunch.common.security.auth.annotation.CurrentMember;
import com.todaylunch.common.security.auth.dto.AuthenticatedMember;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
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
@RequestMapping("/api/v1/member-reports")
@RequiredArgsConstructor
@Tag(name = "Member Report", description = "Member report APIs")
public class MemberReportController {

    private final MemberReportUsecase memberReportUsecase;

    @PostMapping
    @Operation(summary = "회원 신고 생성", description = "회원을 신고하는 API입니다.")
    public ResponseEntity<ApiResponse<MemberReportResponse>> createReport(
            @CurrentMember AuthenticatedMember authenticatedMember,
            @RequestBody CreateMemberReportRequest request
    ) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(memberReportUsecase.createReport(authenticatedMember, request)));
    }

    @GetMapping("/me")
    @Operation(summary = "내 신고 조회", description = "현재 회원이 생성한 신고를 조회하는 API입니다.")
    public ResponseEntity<ApiResponse<List<MemberReportResponse>>> getMyReports(
            @CurrentMember AuthenticatedMember authenticatedMember
    ) {
        return ResponseEntity.ok(ApiResponse.success(memberReportUsecase.getMyReports(authenticatedMember)));
    }
}
