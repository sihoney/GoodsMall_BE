package com.example.member.presentation.controller;

import com.example.member.application.service.EmailVerificationService;
import com.example.member.application.service.KakaoOAuthService;
import com.example.member.application.usecase.AuthUsecase;
import com.example.member.application.usecase.MemberUsecase;
import com.example.member.infrastructure.redis.KakaoOAuthAuthorizeState;
import com.example.member.infrastructure.redis.KakaoOAuthFlowType;
import com.example.member.presentation.dto.ApiResponse;
import com.example.member.presentation.dto.ConfirmEmailVerificationRequest;
import com.example.member.presentation.dto.CreateMemberRequest;
import com.example.member.presentation.dto.CreateMemberResponse;
import com.example.member.presentation.dto.EmailVerificationConfirmResponse;
import com.example.member.presentation.dto.EmailVerificationSendResponse;
import com.example.member.presentation.dto.KakaoOAuthLinkRequest;
import com.example.member.presentation.dto.KakaoOAuthLinkResponse;
import com.example.member.presentation.dto.KakaoOAuthAuthorizeUrlResponse;
import com.example.member.presentation.dto.KakaoOAuthResultResponse;
import com.example.member.presentation.dto.LoginRequest;
import com.example.member.presentation.dto.LoginResponse;
import com.example.member.presentation.dto.SendEmailVerificationRequest;
import com.example.member.presentation.dto.TokenRefreshRequest;
import com.example.member.presentation.dto.TokenRefreshResponse;
import com.todaylunch.common.security.auth.annotation.CurrentMember;
import com.todaylunch.common.security.auth.dto.AuthenticatedMember;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.net.URI;
import java.util.Locale;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.util.UriComponentsBuilder;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Tag(name = "인증", description = "인증(로그인/로그아웃/토큰 재발급) API")
public class AuthController {

    private final AuthUsecase authUsecase;
    private final MemberUsecase memberUsecase;
    private final EmailVerificationService emailVerificationService;
    private final KakaoOAuthService kakaoOAuthService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "사용자 생성", description = "사용자를 생성합니다.")
    public ResponseEntity<ApiResponse<CreateMemberResponse>> createMember(
            @Validated @RequestBody CreateMemberRequest request
    ) {
        return ResponseEntity.ok(ApiResponse.success(memberUsecase.createMember(request)));
    }

    @PostMapping("/login")
    @Operation(summary = "로그인", description = "액세스 토큰과 리프레시 토큰을 발급합니다.")
    public ResponseEntity<ApiResponse<LoginResponse>> login(@RequestBody LoginRequest request) {
        return ResponseEntity.ok(ApiResponse.success(authUsecase.login(request)));
    }

    @PostMapping("/refresh")
    @Operation(summary = "토큰 재발급", description = "리프레시 토큰으로 새 액세스 토큰 쌍을 발급합니다.")
    public ResponseEntity<ApiResponse<TokenRefreshResponse>> refresh(@RequestBody TokenRefreshRequest request) {
        return ResponseEntity.ok(ApiResponse.success(authUsecase.refresh(request)));
    }

    @GetMapping("/oauth/kakao/authorize")
    @Operation(summary = "카카오 로그인 시작", description = "카카오 로그인 페이지로 이동합니다.")
    public void authorizeKakaoLogin(HttpServletResponse response) throws IOException {
        String state = kakaoOAuthService.createLoginAuthorizeState();
        response.sendRedirect(kakaoOAuthService.buildAuthorizeUrl(state));
    }

    @GetMapping("/oauth/kakao/link/authorize")
    @Operation(summary = "카카오 계정 연동 시작", description = "현재 로그인한 계정에 카카오 연동을 시작합니다.")
    public void authorizeKakaoLink(
            @CurrentMember AuthenticatedMember authenticatedMember,
            HttpServletResponse response
    ) throws IOException {
        String state = kakaoOAuthService.createLinkAuthorizeState(authenticatedMember.memberId());
        response.sendRedirect(kakaoOAuthService.buildAuthorizeUrl(state));
    }

    @GetMapping("/oauth/kakao/link/authorize-url")
    @Operation(summary = "카카오 계정 연동 URL 조회", description = "로그인된 사용자의 카카오 계정 연동용 authorize URL을 반환합니다.")
    public ResponseEntity<ApiResponse<KakaoOAuthAuthorizeUrlResponse>> getKakaoLinkAuthorizeUrl(
            @CurrentMember AuthenticatedMember authenticatedMember
    ) {
        String state = kakaoOAuthService.createLinkAuthorizeState(authenticatedMember.memberId());
        String authorizeUrl = kakaoOAuthService.buildAuthorizeUrl(state);

        return ResponseEntity.ok(ApiResponse.success(new KakaoOAuthAuthorizeUrlResponse(authorizeUrl)));
    }

    @GetMapping("/oauth/kakao/callback")
    @Operation(summary = "카카오 OAuth 콜백", description = "카카오 인증 결과를 resultKey로 저장하고 프론트로 리다이렉트합니다.")
    public ResponseEntity<Void> kakaoCallback(
            @RequestParam(required = false) String code,
            @RequestParam(required = false) String state,
            @RequestParam(required = false) String error,
            @RequestParam(name = "error_description", required = false) String errorDescription
    ) {
        KakaoOAuthFlowType flowType = KakaoOAuthFlowType.LOGIN;
        KakaoOAuthResultResponse result;

        try {
            KakaoOAuthAuthorizeState authorizeState = kakaoOAuthService.consumeAuthorizeState(state);
            flowType = authorizeState.flowType();

            if (error != null && !error.isBlank()) {
                result = KakaoOAuthResultResponse.error(
                        "KAKAO_OAUTH_PROVIDER_ERROR",
                        errorDescription == null || errorDescription.isBlank()
                                ? "카카오 인증이 취소되었거나 실패했습니다."
                                : errorDescription
                );
            } else if (flowType == KakaoOAuthFlowType.LINK) {
                result = kakaoOAuthService.linkByCode(authorizeState.memberId(), code);
            } else {
                result = kakaoOAuthService.loginByCode(code);
            }
        } catch (Exception exception) {
            result = kakaoOAuthService.createErrorResult(flowType, exception);
        }

        String resultKey = kakaoOAuthService.createOAuthResultKey(result);
        URI redirectUri = UriComponentsBuilder
                .fromUriString(kakaoOAuthService.getFrontendCallbackUrl())
                .queryParam("resultKey", resultKey)
                .queryParam("flow", flowType.name().toLowerCase(Locale.ROOT))
                .build(true)
                .toUri();

        return ResponseEntity.status(HttpStatus.FOUND).location(redirectUri).build();
    }

    @GetMapping("/oauth/kakao/result")
    @Operation(summary = "카카오 OAuth 결과 조회", description = "1회용 resultKey로 카카오 OAuth 결과를 조회합니다.")
    public ResponseEntity<ApiResponse<KakaoOAuthResultResponse>> getKakaoOAuthResult(
            @RequestParam String resultKey
    ) {
        return ResponseEntity.ok(ApiResponse.success(
                kakaoOAuthService.consumeOAuthResult(resultKey)
        ));
    }

    @PostMapping("/oauth/kakao/link")
    @Operation(summary = "카카오 계정 연결", description = "로그인한 회원과 카카오 계정을 연결합니다.")
    public ResponseEntity<ApiResponse<KakaoOAuthLinkResponse>> linkKakaoAccount(
            @CurrentMember AuthenticatedMember authenticatedMember,
            @RequestBody KakaoOAuthLinkRequest request
    ) {
        return ResponseEntity.ok(ApiResponse.success(
                kakaoOAuthService.linkCurrentMember(authenticatedMember.memberId(), request.linkToken())
        ));
    }

    @PostMapping("/logout/{memberId}")
    @Operation(summary = "로그아웃", description = "회원의 모든 리프레시 세션을 제거합니다.")
    public ResponseEntity<ApiResponse<Void>> logout(@PathVariable UUID memberId) {
        authUsecase.logout(memberId);
        return ResponseEntity.ok(ApiResponse.success(null));
    }

    @PostMapping("/email-verifications")
    @Operation(summary = "이메일 인증 재발급", description = "회원 가입용 이메일 인증 토큰을 재발급하고 전송합니다.")
    public ResponseEntity<ApiResponse<EmailVerificationSendResponse>> sendEmailVerification(
            @RequestBody SendEmailVerificationRequest request
    ) {
        var emailVerification = emailVerificationService.resendSignupVerification(request.email());
        return ResponseEntity.ok(ApiResponse.success(new EmailVerificationSendResponse(
                emailVerification.getEmail(),
                emailVerification.getPurpose().name(),
                emailVerification.getStatus().name(),
                emailVerification.getExpiresAt()
        )));
    }

    @PostMapping("/email-verifications/confirm")
    @Operation(summary = "이메일 인증 확인", description = "이메일 인증 토큰을 검증하고 회원을 활성화합니다.")
    public ResponseEntity<ApiResponse<EmailVerificationConfirmResponse>> confirmEmailVerification(
            @RequestBody ConfirmEmailVerificationRequest request
    ) {
        var member = emailVerificationService.confirmSignupVerification(request.token());
        return ResponseEntity.ok(ApiResponse.success(EmailVerificationConfirmResponse.from(member)));
    }
}
