package com.example.member.presentation.controller;

import com.example.member.application.usecase.AccountVerificationUsecase;
import com.example.member.presentation.dto.AccountVerificationCancelResponse;
import com.example.member.presentation.dto.AccountVerificationConfirmRequest;
import com.example.member.presentation.dto.AccountVerificationConfirmResponse;
import com.example.member.presentation.dto.AccountVerificationCreateRequest;
import com.example.member.presentation.dto.AccountVerificationCurrentResponse;
import com.example.member.presentation.dto.AccountVerificationSendResponse;
import com.example.member.presentation.dto.ApiResponse;
import com.todaylunch.common.security.auth.annotation.CurrentMember;
import com.todaylunch.common.security.auth.dto.AuthenticatedMember;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/members/me/account-verifications")
@Tag(name = "Account Verification", description = "계정 인증 API")
public class AccountVerificationController {

    private final AccountVerificationUsecase accountVerificationUsecase;

    @PostMapping
    @Operation(
            summary = "계정 인증 요청 생성",
            description = "회원의 계정 인증 세션과 seller draft 를 생성하고 인증 코드를 반환합니다."
    )
    public ResponseEntity<ApiResponse<AccountVerificationSendResponse>> createAccountVerification(
            @CurrentMember AuthenticatedMember authenticatedMember,
            @RequestBody AccountVerificationCreateRequest request
    ) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(
                        accountVerificationUsecase.createAccountVerification(authenticatedMember.memberId(), request)
                ));
    }

    @PostMapping("/{sessionId}/confirm")
    @Operation(
            summary = "계정 인증 확인",
            description = "입력된 인증 코드를 검증하고 세션을 VERIFIED 로 변경한 뒤, 인증을 완료합니다."
    )
    public ResponseEntity<ApiResponse<AccountVerificationConfirmResponse>> confirmAccountVerification(
            @CurrentMember AuthenticatedMember authenticatedMember,
            @Parameter(description = "계정 인증 세션 ID", example = "av_01J4XYZ")
            @PathVariable(name = "sessionId") String sessionId,
            @RequestBody AccountVerificationConfirmRequest request
    ) {
        return ResponseEntity.ok(ApiResponse.success(
                accountVerificationUsecase.confirmAccountVerification(
                        authenticatedMember.memberId(),
                        authenticatedMember.sessionId(),
                        sessionId,
                        request
                )
        ));
    }

    @GetMapping("/current")
    @Operation(
            summary = "현재 계정 인증 상태 조회",
            description = "현재 진행 중인 계정 인증 세션과 draft 정보를 조회합니다."
    )
    public ResponseEntity<ApiResponse<AccountVerificationCurrentResponse>> getCurrentAccountVerification(
            @CurrentMember AuthenticatedMember authenticatedMember
    ) {
        return ResponseEntity.ok(ApiResponse.success(
                accountVerificationUsecase.getCurrentAccountVerification(authenticatedMember.memberId())
        ));
    }

    @PostMapping("/{sessionId}/resend")
    @Operation(
            summary = "계정 인증 코드 재전송",
            description = "기존 인증 코드를 무효화하고 새 인증 코드를 발송합니다."
    )
    public ResponseEntity<ApiResponse<AccountVerificationSendResponse>> resendAccountVerification(
            @CurrentMember AuthenticatedMember authenticatedMember,
            @Parameter(description = "계정 인증 세션 ID", example = "av_01J4XYZ")
            @PathVariable(name = "sessionId") String sessionId
    ) {
        return ResponseEntity.ok(ApiResponse.success(
                accountVerificationUsecase.resendAccountVerification(authenticatedMember.memberId(), sessionId)
        ));
    }

    @PostMapping("/{sessionId}/cancel")
    @Operation(
            summary = "계정 인증 취소",
            description = "진행 중인 계정 인증 세션을 취소합니다."
    )
    public ResponseEntity<ApiResponse<AccountVerificationCancelResponse>> cancelAccountVerification(
            @CurrentMember AuthenticatedMember authenticatedMember,
            @Parameter(description = "계정 인증 세션 ID", example = "av_01J4XYZ")
            @PathVariable(name = "sessionId") String sessionId
    ) {
        return ResponseEntity.ok(ApiResponse.success(
                accountVerificationUsecase.cancelAccountVerification(authenticatedMember.memberId(), sessionId)
        ));
    }
}
