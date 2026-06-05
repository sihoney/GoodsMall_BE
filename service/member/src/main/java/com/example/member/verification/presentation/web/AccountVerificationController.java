package com.example.member.verification.presentation.web;


import com.example.member.common.exception.BusinessException;
import com.example.member.verification.exception.VerificationErrorCode;
import com.example.member.auth.presentation.web.support.RefreshTokenCookieWriter;
import com.example.member.common.presentation.web.dto.ApiResponse;
import com.example.member.verification.application.dto.command.AccountVerificationConfirmCommand;
import com.example.member.verification.application.dto.command.AccountVerificationCreateCommand;
import com.example.member.verification.application.dto.result.AccountVerificationConfirmResult;
import com.example.member.verification.application.port.in.AccountVerificationUsecase;
import com.example.member.verification.presentation.web.dto.AccountVerificationCancelResponse;
import com.example.member.verification.presentation.web.dto.AccountVerificationConfirmRequest;
import com.example.member.verification.presentation.web.dto.AccountVerificationConfirmResponse;
import com.example.member.verification.presentation.web.dto.AccountVerificationCreateRequest;
import com.example.member.verification.presentation.web.dto.AccountVerificationCurrentResponse;
import com.example.member.verification.presentation.web.dto.AccountVerificationSendResponse;
import com.todaylunch.common.security.auth.annotation.CurrentMember;
import com.todaylunch.common.security.auth.dto.AuthenticatedMember;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
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
@Tag(name = "계좌 인증", description = "판매자 계좌")
@SecurityRequirement(name = "bearerAuth")
public class AccountVerificationController {

    private final AccountVerificationUsecase accountVerificationUsecase;
    private final RefreshTokenCookieWriter refreshTokenCookieWriter;

    @PostMapping
    @Operation(summary = "계좌 인증 생성", description = "인증 코드")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "201", description = "생성"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "잘못된 요청"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "인증 필요")
    })
    public ResponseEntity<ApiResponse<AccountVerificationSendResponse>> createAccountVerification(
            @Parameter(hidden = true) @CurrentMember AuthenticatedMember authenticatedMember,
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
    @Operation(summary = "계좌 인증 확인", description = "코드 검증")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "잘못된 요청"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "인증 필요"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "세션 없음")
    })
    public ResponseEntity<ApiResponse<AccountVerificationConfirmResponse>> confirmAccountVerification(
            @Parameter(hidden = true) @CurrentMember AuthenticatedMember authenticatedMember,
            @Parameter(description = "인증 세션 ID", example = "av_01J4XYZ")
            @PathVariable(name = "sessionId") String sessionId,
            @Valid @RequestBody AccountVerificationConfirmRequest request
    ) {
        AccountVerificationConfirmResult result = accountVerificationUsecase.confirmAccountVerification(
                authenticatedMember.memberId(),
                authenticatedMember.sessionId(),
                sessionId,
                new AccountVerificationConfirmCommand(request.code())
        );

        ResponseEntity.BodyBuilder response = ResponseEntity.ok();
        if (result.auth() != null && result.auth().refreshToken() != null) {
            response.header(HttpHeaders.SET_COOKIE, refreshTokenCookieWriter
                    .create(result.auth().refreshToken(), result.auth().refreshTokenExpiresIn())
                    .toString());
        }
        return response.body(ApiResponse.success(AccountVerificationConfirmResponse.from(result)));
    }

    @GetMapping("/current")
    @Operation(summary = "현재 계좌 인증", description = "진행 상태")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "인증 필요"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "세션 없음")
    })
    public ResponseEntity<ApiResponse<AccountVerificationCurrentResponse>> getCurrentAccountVerification(
            @Parameter(hidden = true) @CurrentMember AuthenticatedMember authenticatedMember
    ) {
        return ResponseEntity.ok(ApiResponse.success(
                AccountVerificationCurrentResponse.from(
                        accountVerificationUsecase.getCurrentAccountVerification(authenticatedMember.memberId())
                )
        ));
    }

    @PostMapping("/{sessionId}/resend")
    @Operation(summary = "인증 코드 재전송", description = "새 코드")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "재전송 불가"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "인증 필요"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "세션 없음")
    })
    public ResponseEntity<ApiResponse<AccountVerificationSendResponse>> resendAccountVerification(
            @Parameter(hidden = true) @CurrentMember AuthenticatedMember authenticatedMember,
            @Parameter(description = "인증 세션 ID", example = "av_01J4XYZ")
            @PathVariable(name = "sessionId") String sessionId
    ) {
        return ResponseEntity.ok(ApiResponse.success(
                AccountVerificationSendResponse.from(
                        accountVerificationUsecase.resendAccountVerification(authenticatedMember.memberId(), sessionId)
                )
        ));
    }

    @PostMapping("/{sessionId}/cancel")
    @Operation(summary = "계좌 인증 취소", description = "진행 취소")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "인증 필요"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "세션 없음")
    })
    public ResponseEntity<ApiResponse<AccountVerificationCancelResponse>> cancelAccountVerification(
            @Parameter(hidden = true) @CurrentMember AuthenticatedMember authenticatedMember,
            @Parameter(description = "인증 세션 ID", example = "av_01J4XYZ")
            @PathVariable(name = "sessionId") String sessionId
    ) {
        return ResponseEntity.ok(ApiResponse.success(
                AccountVerificationCancelResponse.from(
                        accountVerificationUsecase.cancelAccountVerification(authenticatedMember.memberId(), sessionId)
                )
        ));
    }
}
