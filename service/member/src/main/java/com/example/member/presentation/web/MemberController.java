package com.example.member.presentation.web;

import com.example.member.application.dto.command.ChangePasswordCommand;
import com.example.member.application.dto.command.UpdateMemberCommand;
import com.example.member.application.dto.command.WithdrawMemberCommand;
import com.example.member.application.dto.query.GetMemberQuery;
import com.example.member.application.port.in.MemberOauthAccountUsecase;
import com.example.member.application.port.in.MemberUsecase;
import com.example.member.presentation.web.dto.ApiResponse;
import com.example.member.presentation.web.dto.ChangePasswordRequest;
import com.example.member.presentation.web.dto.ChangePasswordResponse;
import com.example.member.presentation.web.dto.MemberOauthAccountListResponse;
import com.example.member.presentation.web.dto.MemberOauthAccountUnlinkResponse;
import com.example.member.presentation.web.dto.MemberResponse;
import com.example.member.presentation.web.dto.UpdateMemberRequest;
import com.example.member.presentation.web.dto.WithdrawMemberRequest;
import com.example.member.presentation.web.dto.WithdrawMemberResponse;
import com.todaylunch.common.security.auth.annotation.CurrentMember;
import com.todaylunch.common.security.auth.dto.AuthenticatedMember;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/members")
@RequiredArgsConstructor
@Tag(name = "회원", description = "회원 CRUD API")
public class MemberController {

    private final MemberUsecase memberUsecase;
    private final MemberOauthAccountUsecase memberOauthAccountUsecase;

    @GetMapping("/me")
    @Operation(summary = "현재 회원 조회", description = "현재 인증된 회원 정보를 조회합니다.")
    public ResponseEntity<ApiResponse<MemberResponse>> getCurrentMember(
            @CurrentMember AuthenticatedMember authenticatedMember
    ) {
        return ResponseEntity.ok(ApiResponse.success(
                MemberResponse.from(memberUsecase.getCurrentMember(
                        new GetMemberQuery(authenticatedMember.memberId())
                ))
        ));
    }

    @PatchMapping("/me")
    @Operation(summary = "현재 회원 수정", description = "현재 인증된 회원의 프로필 정보를 수정합니다.")
    public ResponseEntity<ApiResponse<MemberResponse>> updateCurrentMember(
            @CurrentMember AuthenticatedMember authenticatedMember,
            @Valid @RequestBody UpdateMemberRequest request
    ) {
        return ResponseEntity.ok(ApiResponse.success(
                MemberResponse.from(memberUsecase.updateCurrentMember(new UpdateMemberCommand(
                        authenticatedMember.memberId(),
                        request.nickname(),
                        request.phone(),
                        request.address(),
                        request.profileImageKey()
                )))
        ));
    }

    @PatchMapping("/me/password")
    @Operation(summary = "현재 회원 비밀번호 변경", description = "현재 인증된 회원의 비밀번호를 변경합니다.")
    public ResponseEntity<ApiResponse<ChangePasswordResponse>> changeCurrentMemberPassword(
            @CurrentMember AuthenticatedMember authenticatedMember,
            @Valid @RequestBody ChangePasswordRequest request
    ) {
        return ResponseEntity.ok(ApiResponse.success(new ChangePasswordResponse(
                memberUsecase.changeCurrentMemberPassword(new ChangePasswordCommand(
                        authenticatedMember.memberId(),
                        request.currentPassword(),
                        request.newPassword()
                )).message()
        )));
    }

    @DeleteMapping("/me")
    @Operation(summary = "현재 회원 탈퇴", description = "현재 인증된 회원을 탈퇴 처리하고 모든 세션을 종료합니다.")
    public ResponseEntity<ApiResponse<WithdrawMemberResponse>> withdrawCurrentMember(
            @CurrentMember AuthenticatedMember authenticatedMember,
            @RequestHeader(HttpHeaders.AUTHORIZATION) String authorizationHeader,
            @Valid @RequestBody WithdrawMemberRequest request
    ) {
        return ResponseEntity.ok(ApiResponse.success(
                WithdrawMemberResponse.from(memberUsecase.withdrawCurrentMember(new WithdrawMemberCommand(
                        authenticatedMember.memberId(),
                        request.currentPassword(),
                        authorizationHeader
                )))
        ));
    }

    @GetMapping("/me/oauth-accounts")
    @Operation(summary = "연동된 OAuth 계정 조회", description = "현재 회원의 연동된 OAuth 계정을 조회합니다.")
    public ResponseEntity<ApiResponse<MemberOauthAccountListResponse>> getCurrentMemberOauthAccounts(
            @CurrentMember AuthenticatedMember authenticatedMember
    ) {
        return ResponseEntity.ok(ApiResponse.success(
                MemberOauthAccountListResponse.from(
                        memberOauthAccountUsecase.getCurrentMemberOauthAccounts(authenticatedMember.memberId())
                )
        ));
    }

    @DeleteMapping("/me/oauth-accounts/{provider}")
    @Operation(summary = "OAuth 계정 연동 해제", description = "현재 회원의 OAuth 계정 연동을 해제합니다.")
    public ResponseEntity<ApiResponse<MemberOauthAccountUnlinkResponse>> unlinkCurrentMemberOauthAccount(
            @CurrentMember AuthenticatedMember authenticatedMember,
            @PathVariable(name = "provider") String provider
    ) {
        return ResponseEntity.ok(ApiResponse.success(
                MemberOauthAccountUnlinkResponse.from(
                        memberOauthAccountUsecase.unlinkCurrentMemberOauthAccount(authenticatedMember.memberId(), provider)
                )
        ));
    }

    @GetMapping("/{memberId}")
    @Operation(summary = "회원 조회", description = "특정 회원 정보를 조회합니다.")
    public MemberResponse getMember(@PathVariable(name = "memberId") UUID memberId) {
        return MemberResponse.from(memberUsecase.getMember(new GetMemberQuery(memberId)));
    }

    @PatchMapping("/{memberId}")
    @Operation(summary = "회원 수정", description = "특정 회원 정보를 수정합니다.")
    public MemberResponse updateMember(
            @PathVariable(name = "memberId") UUID memberId,
            @Valid @RequestBody UpdateMemberRequest request
    ) {
        return MemberResponse.from(memberUsecase.updateMember(new UpdateMemberCommand(
                memberId,
                request.nickname(),
                request.phone(),
                request.address(),
                request.profileImageKey()
        )));
    }
}
