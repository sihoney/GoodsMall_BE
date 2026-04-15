package com.example.member.presentation.controller;

import com.example.member.application.service.KakaoOAuthService;
import com.example.member.application.service.EmailVerificationService;
import com.example.member.application.usecase.AuthUsecase;
import com.example.member.application.usecase.MemberUsecase;
import com.example.member.presentation.dto.ApiResponse;
import com.example.member.presentation.dto.ConfirmEmailVerificationRequest;
import com.example.member.presentation.dto.CreateMemberRequest;
import com.example.member.presentation.dto.CreateMemberResponse;
import com.example.member.presentation.dto.EmailVerificationConfirmResponse;
import com.example.member.presentation.dto.EmailVerificationSendResponse;
import com.example.member.presentation.dto.KakaoOAuthLinkRequest;
import com.example.member.presentation.dto.KakaoOAuthLinkResponse;
import com.example.member.presentation.dto.KakaoOAuthLoginResponse;
import com.example.member.presentation.dto.LoginRequest;
import com.example.member.presentation.dto.LoginResponse;
import com.example.member.presentation.dto.SendEmailVerificationRequest;
import com.example.member.presentation.dto.TokenRefreshRequest;
import com.example.member.presentation.dto.TokenRefreshResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import com.todaylunch.common.security.auth.annotation.CurrentMember;
import com.todaylunch.common.security.auth.dto.AuthenticatedMember;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Tag(name = "인증", description = "인증(로그인/로그아웃/토큰재발급) API")
public class AuthController {

    private final AuthUsecase authUsecase;
    private final MemberUsecase memberUsecase;
    private final EmailVerificationService emailVerificationService;
    private final KakaoOAuthService kakaoOAuthService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary="사용자 생성", description="사용자을 생성합니다.")
    public ResponseEntity<ApiResponse<CreateMemberResponse>> createMember(
        @Validated @RequestBody CreateMemberRequest request
    ) {
        return ResponseEntity.ok(ApiResponse.success(memberUsecase.createMember(request)));
    }

    @PostMapping("/login")
    @Operation(summary = "로그인", description = "엑세스 토큰과 리프레시 토큰을 발급합니다.")
    public ResponseEntity<ApiResponse<LoginResponse>> login(@RequestBody LoginRequest request) {
        return ResponseEntity.ok(ApiResponse.success(authUsecase.login(request)));
    }

    @PostMapping("/refresh")
    @Operation(summary = "토큰 재발급", description = "리프레시 토큰을 사용하여 엑세스 토큰을 재발급합니다.")
    public ResponseEntity<ApiResponse<TokenRefreshResponse>> refresh(@RequestBody TokenRefreshRequest request) {
        return ResponseEntity.ok(ApiResponse.success(authUsecase.refresh(request)));
    }

    @GetMapping("/oauth/kakao/authorize")
    @Operation(summary = "카카오 로그인 시작", description = "카카오 로그인 페이지로 이동합니다.")
    public void authorizeKakaoLogin(HttpServletResponse response) throws IOException {
        String state = kakaoOAuthService.createAuthorizeState();
        response.sendRedirect(kakaoOAuthService.buildAuthorizeUrl(state));
    }

    @GetMapping("/oauth/kakao/callback")
    @Operation(summary = "카카오 로그인 콜백", description = "카카오 인증 코드를 검증하고 우리 서비스 토큰 또는 연결 안내를 반환합니다.")
    public ResponseEntity<ApiResponse<KakaoOAuthLoginResponse>> kakaoCallback(
            @RequestParam String code,
            @RequestParam String state
    ) {
        return ResponseEntity.ok(ApiResponse.success(
                kakaoOAuthService.loginByCode(code, state)
        ));
    }

    @PostMapping("/oauth/kakao/link")
    @Operation(summary = "카카오 계정 연결", description = "현재 로그인한 회원과 카카오 계정을 연결합니다.")
    public ResponseEntity<ApiResponse<KakaoOAuthLinkResponse>> linkKakaoAccount(
            @CurrentMember AuthenticatedMember authenticatedMember,
            @RequestBody KakaoOAuthLinkRequest request
    ) {
        return ResponseEntity.ok(ApiResponse.success(
                kakaoOAuthService.linkCurrentMember(authenticatedMember.memberId(), request.linkToken())
        ));
    }

    @PostMapping("/logout/{memberId}")
    @Operation(summary = "로그아웃", description = "Redis에서 사용자 리프레시 토큰을 삭제합니다.")
    public ResponseEntity<ApiResponse<Void>> logout(@PathVariable UUID memberId) {
        authUsecase.logout(memberId);
        return ResponseEntity.ok(ApiResponse.success(null));
    }

    @PostMapping("/email-verifications")
    @Operation(summary = "이메일 인증 재발송", description = "회원 가입 이메일 인증 토큰을 재발급하고 이메일로 발송합니다.")
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
    @Operation(summary = "이메일 인증 확인", description = "회원 가입 이메일 인증 토큰을 검증하여 회원의 상태를 ACTIVE로 변경합니다.")
    public ResponseEntity<ApiResponse<EmailVerificationConfirmResponse>> confirmEmailVerification(
            @RequestBody ConfirmEmailVerificationRequest request
    ) {
        var member = emailVerificationService.confirmSignupVerification(request.token());
        return ResponseEntity.ok(ApiResponse.success(EmailVerificationConfirmResponse.from(member)));
    }
}
