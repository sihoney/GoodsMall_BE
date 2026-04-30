package com.example.member.presentation.web;

import com.example.member.application.dto.command.AuthSessionMetadata;
import com.example.member.application.dto.command.CreateMemberCommand;
import com.example.member.application.dto.command.LoginCommand;
import com.example.member.application.dto.command.PasswordResetConfirmCommand;
import com.example.member.application.dto.command.PasswordResetSendCommand;
import com.example.member.application.dto.command.TokenRefreshCommand;
import com.example.member.application.dto.result.KakaoOAuthResult;
import com.example.member.application.service.EmailVerificationAutoLoginService;
import com.example.member.application.service.EmailVerificationService;
import com.example.member.application.service.KakaoOAuthService;
import com.example.member.application.service.PasswordResetService;
import com.example.member.application.port.in.AuthUsecase;
import com.example.member.application.port.in.MemberUsecase;
import com.example.member.infrastructure.redis.oauth.KakaoOAuthAuthorizeState;
import com.example.member.infrastructure.redis.oauth.KakaoOAuthFlowType;
import com.example.member.presentation.web.dto.ApiResponse;
import com.example.member.presentation.web.dto.ConfirmEmailVerificationRequest;
import com.example.member.presentation.web.dto.CreateMemberRequest;
import com.example.member.presentation.web.dto.CreateMemberResponse;
import com.example.member.presentation.web.dto.EmailVerificationConfirmResponse;
import com.example.member.presentation.web.dto.EmailVerificationAutoLoginRequest;
import com.example.member.presentation.web.dto.EmailVerificationAutoLoginResponse;
import com.example.member.presentation.web.dto.EmailVerificationSendResponse;
import com.example.member.presentation.web.dto.KakaoOAuthAuthorizeUrlResponse;
import com.example.member.presentation.web.dto.KakaoOAuthLinkRequest;
import com.example.member.presentation.web.dto.KakaoOAuthLinkResponse;
import com.example.member.presentation.web.dto.KakaoOAuthResultResponse;
import com.example.member.presentation.web.dto.LoginRequest;
import com.example.member.presentation.web.dto.LoginResponse;
import com.example.member.presentation.web.dto.PasswordResetConfirmRequest;
import com.example.member.presentation.web.dto.PasswordResetConfirmResponse;
import com.example.member.presentation.web.dto.PasswordResetSendRequest;
import com.example.member.presentation.web.dto.PasswordResetSendResponse;
import com.example.member.presentation.web.dto.SendEmailVerificationRequest;
import com.example.member.presentation.web.dto.TokenRefreshRequest;
import com.example.member.presentation.web.dto.TokenRefreshResponse;
import com.todaylunch.common.security.auth.annotation.CurrentMember;
import com.todaylunch.common.security.auth.dto.AuthenticatedMember;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import java.net.URI;
import java.util.Locale;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
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
@Tag(name = "인증", description = "회원 인증, 로그인, OAuth API")
public class AuthController {

    private final AuthUsecase authUsecase;
    private final MemberUsecase memberUsecase;
    private final EmailVerificationService emailVerificationService;
    private final EmailVerificationAutoLoginService emailVerificationAutoLoginService;
    private final KakaoOAuthService kakaoOAuthService;
    private final PasswordResetService passwordResetService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "회원 생성", description = "새 회원을 생성합니다.")
    public ResponseEntity<ApiResponse<CreateMemberResponse>> createMember(
            @Valid @RequestBody CreateMemberRequest request
    ) {
        return ResponseEntity.ok(ApiResponse.success(
                CreateMemberResponse.from(memberUsecase.createMember(new CreateMemberCommand(
                        request.email(),
                        request.password(),
                        request.nickname(),
                        request.phone(),
                        request.address(),
                        request.profileImageKey(),
                        request.role()
                )))
        ));
    }

    @PostMapping("/login")
    @Operation(summary = "로그인", description = "이메일과 비밀번호로 로그인합니다.")
    public ResponseEntity<ApiResponse<LoginResponse>> login(
            @Valid @RequestBody LoginRequest request,
            HttpServletRequest httpServletRequest
    ) {
        return ResponseEntity.ok(ApiResponse.success(
                LoginResponse.from(authUsecase.login(new LoginCommand(
                        request.email(),
                        request.password()
                ), extractSessionMetadata(httpServletRequest)))
        ));
    }

    @PostMapping("/refresh")
    @Operation(summary = "토큰 재발급", description = "access token과 refresh token을 재발급합니다.")
    public ResponseEntity<ApiResponse<TokenRefreshResponse>> refresh(
            @Valid @RequestBody TokenRefreshRequest request,
            HttpServletRequest httpServletRequest
    ) {
        return ResponseEntity.ok(ApiResponse.success(
                TokenRefreshResponse.from(authUsecase.refresh(new TokenRefreshCommand(
                        request.refreshToken()
                ), extractSessionMetadata(httpServletRequest)))
        ));
    }

    @PostMapping("/password-resets")
    @Operation(summary = "비밀번호 재설정 메일 발송", description = "입력한 이메일로 비밀번호 재설정 링크를 발송합니다.")
    public ResponseEntity<ApiResponse<PasswordResetSendResponse>> sendPasswordReset(
            @Valid @RequestBody PasswordResetSendRequest request
    ) {
        return ResponseEntity.ok(ApiResponse.success(
                PasswordResetSendResponse.from(passwordResetService.sendPasswordReset(
                        new PasswordResetSendCommand(request.email())
                ))
        ));
    }

    @PostMapping("/password-resets/confirm")
    @Operation(summary = "비밀번호 재설정 확인", description = "재설정 토큰과 새 비밀번호로 비밀번호를 변경합니다.")
    public ResponseEntity<ApiResponse<PasswordResetConfirmResponse>> confirmPasswordReset(
            @Valid @RequestBody PasswordResetConfirmRequest request
    ) {
        return ResponseEntity.ok(ApiResponse.success(
                PasswordResetConfirmResponse.from(passwordResetService.confirmPasswordReset(
                        new PasswordResetConfirmCommand(request.token(), request.newPassword())
                ))
        ));
    }

    @GetMapping("/oauth/kakao/authorize")
    @Operation(summary = "카카오 로그인 시작", description = "카카오 로그인용 OAuth 인가 URL을 반환합니다.")
    public ResponseEntity<ApiResponse<KakaoOAuthAuthorizeUrlResponse>> authorizeKakaoLogin() {
        String state = kakaoOAuthService.createLoginAuthorizeState();
        String authorizeUrl = kakaoOAuthService.buildAuthorizeUrl(state);

        return ResponseEntity.ok(ApiResponse.success(new KakaoOAuthAuthorizeUrlResponse(authorizeUrl)));
    }

    @GetMapping("/oauth/kakao/link/authorize")
    @Operation(summary = "카카오 계정 연동 시작", description = "카카오 계정 연동용 OAuth 인가 URL을 반환합니다.")
    public ResponseEntity<ApiResponse<KakaoOAuthAuthorizeUrlResponse>> authorizeKakaoLink(
            @CurrentMember AuthenticatedMember authenticatedMember
    ) {
        String state = kakaoOAuthService.createLinkAuthorizeState(authenticatedMember.memberId());
        String authorizeUrl = kakaoOAuthService.buildAuthorizeUrl(state);

        return ResponseEntity.ok(ApiResponse.success(new KakaoOAuthAuthorizeUrlResponse(authorizeUrl)));
    }

    @GetMapping("/oauth/kakao/link/authorize-url")
    @Operation(summary = "카카오 연동 인가 URL 조회", description = "현재 회원의 카카오 OAuth 인가 URL을 조회합니다.")
    public ResponseEntity<ApiResponse<KakaoOAuthAuthorizeUrlResponse>> getKakaoLinkAuthorizeUrl(
            @CurrentMember AuthenticatedMember authenticatedMember
    ) {
        String state = kakaoOAuthService.createLinkAuthorizeState(authenticatedMember.memberId());
        String authorizeUrl = kakaoOAuthService.buildAuthorizeUrl(state);

        return ResponseEntity.ok(ApiResponse.success(new KakaoOAuthAuthorizeUrlResponse(authorizeUrl)));
    }

    @GetMapping("/oauth/kakao/callback")
    @Operation(summary = "카카오 OAuth 콜백", description = "카카오 OAuth 결과를 저장하고 프론트 콜백 URL로 리다이렉트합니다.")
    public ResponseEntity<Void> kakaoCallback(
            @RequestParam(name = "code", required = false) String code,
            @RequestParam(name = "state", required = false) String state,
            @RequestParam(name = "error", required = false) String error,
            @RequestParam(name = "error_description", required = false) String errorDescription,
            HttpServletRequest httpServletRequest
    ) {
        KakaoOAuthFlowType flowType = KakaoOAuthFlowType.LOGIN;
        KakaoOAuthResult result;
        AuthSessionMetadata metadata = extractSessionMetadata(httpServletRequest);

        try {
            KakaoOAuthAuthorizeState authorizeState = kakaoOAuthService.consumeAuthorizeState(state);
            flowType = authorizeState.flowType();

            if (error != null && !error.isBlank()) {
                result = KakaoOAuthResult.error(
                        "KAKAO_OAUTH_PROVIDER_ERROR",
                        errorDescription == null || errorDescription.isBlank()
                                ? "Kakao authorization was cancelled or failed."
                                : errorDescription
                );
            } else if (flowType == KakaoOAuthFlowType.LINK) {
                result = kakaoOAuthService.linkByCode(authorizeState.memberId(), code);
            } else {
                result = kakaoOAuthService.loginByCode(code, metadata);
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
    @Operation(summary = "카카오 OAuth 결과 조회", description = "resultKey로 1회용 카카오 OAuth 결과를 조회합니다.")
    public ResponseEntity<ApiResponse<KakaoOAuthResultResponse>> getKakaoOAuthResult(
            @RequestParam(name = "resultKey") String resultKey
    ) {
        return ResponseEntity.ok(ApiResponse.success(
                KakaoOAuthResultResponse.from(kakaoOAuthService.consumeOAuthResult(resultKey))
        ));
    }

    @PostMapping("/oauth/kakao/link")
    @Operation(summary = "카카오 계정 연동", description = "현재 회원과 카카오 계정을 연동합니다.")
    public ResponseEntity<ApiResponse<KakaoOAuthLinkResponse>> linkKakaoAccount(
            @CurrentMember AuthenticatedMember authenticatedMember,
            @Valid @RequestBody KakaoOAuthLinkRequest request
    ) {
        return ResponseEntity.ok(ApiResponse.success(
                KakaoOAuthLinkResponse.from(
                        kakaoOAuthService.linkCurrentMember(authenticatedMember.memberId(), request.linkToken())
                )
        ));
    }

    @PostMapping("/logout/{memberId}")
    @Operation(summary = "로그아웃", description = "회원의 모든 refresh 세션을 삭제합니다.")
    public ResponseEntity<ApiResponse<Void>> logout(@PathVariable(name = "memberId") UUID memberId) {
        authUsecase.logout(memberId);
        return ResponseEntity.ok(ApiResponse.success(null));
    }

    @PostMapping("/email-verifications")
    @Operation(summary = "이메일 인증 발송", description = "회원가입용 이메일 인증 코드를 발송합니다.")
    public ResponseEntity<ApiResponse<EmailVerificationSendResponse>> sendEmailVerification(
            @Valid @RequestBody SendEmailVerificationRequest request
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
    @Operation(summary = "이메일 인증 확인", description = "이메일 인증 코드를 검증하고 회원을 활성화합니다.")
    public ResponseEntity<ApiResponse<EmailVerificationConfirmResponse>> confirmEmailVerification(
            @Valid @RequestBody ConfirmEmailVerificationRequest request
    ) {
        var result = emailVerificationService.confirmSignupVerification(request.token());
        return ResponseEntity.ok(ApiResponse.success(EmailVerificationConfirmResponse.from(result)));
    }

    @PostMapping("/email-verifications/auto-login")
    @Operation(summary = "이메일 인증 자동 로그인", description = "이메일 인증 후 발급된 1회용 토큰으로 자동 로그인을 완료합니다.")
    public ResponseEntity<ApiResponse<EmailVerificationAutoLoginResponse>> autoLoginAfterEmailVerification(
            @Valid @RequestBody EmailVerificationAutoLoginRequest request,
            HttpServletRequest httpServletRequest
    ) {
        return ResponseEntity.ok(ApiResponse.success(
                EmailVerificationAutoLoginResponse.from(
                        emailVerificationAutoLoginService.authenticate(
                                request.autoLoginToken(),
                                extractSessionMetadata(httpServletRequest)
                        )
                )
        ));
    }

    private AuthSessionMetadata extractSessionMetadata(HttpServletRequest request) {
        if (request == null) {
            return AuthSessionMetadata.empty();
        }

        return new AuthSessionMetadata(
                request.getHeader("User-Agent"),
                extractClientIp(request)
        );
    }

    private String extractClientIp(HttpServletRequest request) {
        String forwardedFor = request.getHeader("X-Forwarded-For");
        if (forwardedFor != null && !forwardedFor.isBlank()) {
            String[] values = forwardedFor.split(",");
            if (values.length > 0 && values[0] != null && !values[0].trim().isEmpty()) {
                return values[0].trim();
            }
        }

        String realIp = request.getHeader("X-Real-IP");
        if (realIp != null && !realIp.isBlank()) {
            return realIp.trim();
        }

        return request.getRemoteAddr();
    }
}


