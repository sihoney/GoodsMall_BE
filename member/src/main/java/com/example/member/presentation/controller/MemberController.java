package com.example.member.presentation.controller;

import com.example.member.application.usecase.MemberOauthAccountUsecase;
import com.example.member.application.usecase.MemberUsecase;
import com.example.member.presentation.dto.ApiResponse;
import com.example.member.presentation.dto.MemberOauthAccountListResponse;
import com.example.member.presentation.dto.MemberOauthAccountUnlinkResponse;
import com.example.member.presentation.dto.MemberResponse;
import com.example.member.presentation.dto.UpdateMemberRequest;
import com.todaylunch.common.security.auth.annotation.CurrentMember;
import com.todaylunch.common.security.auth.dto.AuthenticatedMember;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import java.util.UUID;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/members")
@RequiredArgsConstructor
@Tag(name = "Member", description = "회원 CRUD API")
public class MemberController {

    private final MemberUsecase memberUsecase;
    private final MemberOauthAccountUsecase memberOauthAccountUsecase;

    @GetMapping("/me")
    @Operation(summary = "현재 회원 조회", description = "인증된 사용자의 정보를 조회합니다.")
    public ResponseEntity<ApiResponse<MemberResponse>> getCurrentMember(
            @CurrentMember AuthenticatedMember authenticatedMember
    ) {
        return ResponseEntity.ok(ApiResponse.success(
                memberUsecase.getCurrentMember(authenticatedMember.memberId())
        ));
    }

    @PatchMapping("/me")
    @Operation(summary = "현재 회원 수정", description = "인증된 사용자의 정보를 수정합니다.")
    public ResponseEntity<ApiResponse<MemberResponse>> updateCurrentMember(
            @CurrentMember AuthenticatedMember authenticatedMember,
            @RequestBody UpdateMemberRequest request
    ) {
        return ResponseEntity.ok(ApiResponse.success(
                memberUsecase.updateCurrentMember(authenticatedMember.memberId(), request)
        ));
    }

    @GetMapping("/me/oauth-accounts")
    @Operation(summary = "현재 회원의 연동 계정 조회", description = "현재 로그인한 사용자의 외부 계정 연동 목록을 조회합니다.")
    public ResponseEntity<ApiResponse<MemberOauthAccountListResponse>> getCurrentMemberOauthAccounts(
            @CurrentMember AuthenticatedMember authenticatedMember
    ) {
        return ResponseEntity.ok(ApiResponse.success(
                memberOauthAccountUsecase.getCurrentMemberOauthAccounts(authenticatedMember.memberId())
        ));
    }

    @DeleteMapping("/me/oauth-accounts/{provider}")
    @Operation(summary = "현재 회원의 연동 계정 해제", description = "현재 로그인한 사용자의 특정 외부 계정 연동을 해제합니다.")
    public ResponseEntity<ApiResponse<MemberOauthAccountUnlinkResponse>> unlinkCurrentMemberOauthAccount(
            @CurrentMember AuthenticatedMember authenticatedMember,
            @PathVariable(name = "provider") String provider
    ) {
        return ResponseEntity.ok(ApiResponse.success(
                memberOauthAccountUsecase.unlinkCurrentMemberOauthAccount(authenticatedMember.memberId(), provider)
        ));
    }

    @GetMapping("/{memberId}")
    @Operation(summary = "회원 조회", description = "(개발용) 특정 사용자를 조회합니다.")
    public MemberResponse getMember(@PathVariable(name = "memberId") UUID memberId) {
        return memberUsecase.getMember(memberId);
    }

    @PatchMapping("/{memberId}")
    @Operation(summary = "회원 수정", description = "(개발용) 특정 사용자를 수정합니다.")
    public MemberResponse updateMember(
            @PathVariable(name = "memberId") UUID memberId,
            @RequestBody UpdateMemberRequest request
    ) {
        return memberUsecase.updateMember(memberId, request);
    }
}
