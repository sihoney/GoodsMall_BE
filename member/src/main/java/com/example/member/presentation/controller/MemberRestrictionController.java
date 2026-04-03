package com.example.member.presentation.controller;

import com.example.member.application.usecase.MemberRestrictionUsecase;
import com.example.member.presentation.dto.ApiResponse;
import com.example.member.presentation.dto.CreateMemberRestrictionRequest;
import com.example.member.presentation.dto.MemberRestrictionResponse;
import com.todaylunch.common.security.auth.annotation.CurrentMember;
import com.todaylunch.common.security.auth.dto.AuthenticatedMember;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin/member-restrictions")
@RequiredArgsConstructor
@Tag(name = "Member Restriction", description = "Admin member restriction APIs")
public class MemberRestrictionController {

    private final MemberRestrictionUsecase memberRestrictionUsecase;
    
    @PostMapping
    @Operation(summary = "회원 제제 활성화", description = "회원 제제를 생성하는 API입니다.")
    public ResponseEntity<ApiResponse<MemberRestrictionResponse>> createRestriction(
            @CurrentMember AuthenticatedMember authenticatedMember,
            @RequestBody CreateMemberRestrictionRequest request
    ) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(memberRestrictionUsecase.createRestriction(authenticatedMember, request)));
    }

    @PatchMapping("/{restrictionId}/deactivate")
    @Operation(summary = "회원 제제 비활성화", description = "기존 회원 제제를 비활성화하는 API입니다.")
    public ResponseEntity<ApiResponse<MemberRestrictionResponse>> deactivateRestriction(
            @CurrentMember AuthenticatedMember authenticatedMember,
            @PathVariable UUID restrictionId
    ) {
        return ResponseEntity.ok(ApiResponse.success(
                memberRestrictionUsecase.deactivateRestriction(authenticatedMember, restrictionId)
        ));
    }

    @GetMapping("/members/{memberId}")
    @Operation(summary = "회원 제제 조회", description = "특정 회원의 모든 제제를 조회하는 API입니다.")
    public ResponseEntity<ApiResponse<List<MemberRestrictionResponse>>> getMemberRestrictions(
            @CurrentMember AuthenticatedMember authenticatedMember,
            @PathVariable UUID memberId
    ) {
        return ResponseEntity.ok(ApiResponse.success(
                memberRestrictionUsecase.getMemberRestrictions(authenticatedMember, memberId)
        ));
    }
}
