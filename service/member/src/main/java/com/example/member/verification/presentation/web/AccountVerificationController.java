package com.example.member.verification.presentation.web;

import com.example.member.verification.application.dto.command.AccountVerificationConfirmCommand;
import com.example.member.verification.application.dto.command.AccountVerificationCreateCommand;
import com.example.member.verification.application.port.in.AccountVerificationUsecase;
import com.example.member.verification.presentation.web.dto.AccountVerificationCancelResponse;
import com.example.member.verification.presentation.web.dto.AccountVerificationConfirmRequest;
import com.example.member.verification.presentation.web.dto.AccountVerificationConfirmResponse;
import com.example.member.verification.presentation.web.dto.AccountVerificationCreateRequest;
import com.example.member.verification.presentation.web.dto.AccountVerificationCurrentResponse;
import com.example.member.verification.presentation.web.dto.AccountVerificationSendResponse;
import com.example.member.common.presentation.web.dto.ApiResponse;
import com.todaylunch.common.security.auth.annotation.CurrentMember;
import com.todaylunch.common.security.auth.dto.AuthenticatedMember;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
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
@Tag(name = "계좌 인증", description = "계좌 인증 API")
public class AccountVerificationController {

    private final AccountVerificationUsecase accountVerificationUsecase;

    @PostMapping
    @Operation(
            summary = "계좌 인증 생성",
            description = "계좌 인증 세션과 판매자 초안을 생성하고 인증 코드를 반환합니다."
    )
    public ResponseEntity<ApiResponse<AccountVerificationSendResponse>> createAccountVerification(
            @CurrentMember AuthenticatedMember authenticatedMember,
            @Valid @RequestBody AccountVerificationCreateRequest request
    ) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(
                        AccountVerificationSendResponse.from(
                                accountVerificationUsecase.createAccountVerification(
                                        authenticatedMember.memberId(),
                                        new AccountVerificationCreateCommand(
                                                request.bankName(),
                                                request.accountNumber()
                                        )
                                )
                        )
                ));
    }

    @PostMapping("/{sessionId}/confirm")
    @Operation(
            summary = "계좌 인증 확인",
            description = "제출한 인증 코드를 검증하고 계좌 인증을 완료합니다."
    )
    public ResponseEntity<ApiResponse<AccountVerificationConfirmResponse>> confirmAccountVerification(
            @CurrentMember AuthenticatedMember authenticatedMember,
            @Parameter(description = "계좌 인증 세션 ID", example = "av_01J4XYZ")
            @PathVariable(name = "sessionId") String sessionId,
            @Valid @RequestBody AccountVerificationConfirmRequest request
    ) {
        return ResponseEntity.ok(ApiResponse.success(
                AccountVerificationConfirmResponse.from(
                        accountVerificationUsecase.confirmAccountVerification(
                                authenticatedMember.memberId(),
                                authenticatedMember.sessionId(),
                                sessionId,
                                new AccountVerificationConfirmCommand(request.code())
                        )
                )
        ));
    }

    @GetMapping("/current")
    @Operation(
            summary = "현재 계좌 인증 조회",
            description = "현재 계좌 인증 세션과 판매자 초안 정보를 조회합니다."
    )
    public ResponseEntity<ApiResponse<AccountVerificationCurrentResponse>> getCurrentAccountVerification(
            @CurrentMember AuthenticatedMember authenticatedMember
    ) {
        return ResponseEntity.ok(ApiResponse.success(
                AccountVerificationCurrentResponse.from(
                        accountVerificationUsecase.getCurrentAccountVerification(authenticatedMember.memberId())
                )
        ));
    }

    @PostMapping("/{sessionId}/resend")
    @Operation(
            summary = "인증 코드 재전송",
            description = "기존 인증 코드를 무효화하고 새 인증 코드를 발급합니다."
    )
    public ResponseEntity<ApiResponse<AccountVerificationSendResponse>> resendAccountVerification(
            @CurrentMember AuthenticatedMember authenticatedMember,
            @Parameter(description = "계좌 인증 세션 ID", example = "av_01J4XYZ")
            @PathVariable(name = "sessionId") String sessionId
    ) {
        return ResponseEntity.ok(ApiResponse.success(
                AccountVerificationSendResponse.from(
                        accountVerificationUsecase.resendAccountVerification(authenticatedMember.memberId(), sessionId)
                )
        ));
    }

    @PostMapping("/{sessionId}/cancel")
    @Operation(
            summary = "계좌 인증 취소",
            description = "진행 중인 계좌 인증 세션을 취소합니다."
    )
    public ResponseEntity<ApiResponse<AccountVerificationCancelResponse>> cancelAccountVerification(
            @CurrentMember AuthenticatedMember authenticatedMember,
            @Parameter(description = "계좌 인증 세션 ID", example = "av_01J4XYZ")
            @PathVariable(name = "sessionId") String sessionId
    ) {
        return ResponseEntity.ok(ApiResponse.success(
                AccountVerificationCancelResponse.from(
                        accountVerificationUsecase.cancelAccountVerification(authenticatedMember.memberId(), sessionId)
                )
        ));
    }
}


