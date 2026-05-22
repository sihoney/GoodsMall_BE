package com.example.member.verification.presentation.web;

import com.example.member.auth.presentation.web.dto.EmailVerificationAutoLoginRequest;
import com.example.member.auth.presentation.web.dto.EmailVerificationAutoLoginResponse;
import com.example.member.common.presentation.web.dto.ApiResponse;
import com.example.member.common.presentation.web.support.AuthSessionMetadataExtractor;
import com.example.member.verification.application.service.EmailVerificationAutoLoginService;
import com.example.member.verification.application.service.EmailVerificationService;
import com.example.member.verification.presentation.web.dto.ConfirmEmailVerificationRequest;
import com.example.member.verification.presentation.web.dto.EmailVerificationConfirmResponse;
import com.example.member.verification.presentation.web.dto.EmailVerificationSendResponse;
import com.example.member.verification.presentation.web.dto.SendEmailVerificationRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth/email-verifications")
@RequiredArgsConstructor
@Tag(name = "이메일 인증", description = "회원가입 이메일 인증 API")
public class EmailVerificationController {

    private final EmailVerificationService emailVerificationService;
    private final EmailVerificationAutoLoginService emailVerificationAutoLoginService;

    @PostMapping
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

    @PostMapping("/confirm")
    @Operation(summary = "이메일 인증 확인", description = "이메일 인증 코드를 검증하고 회원을 활성화합니다.")
    public ResponseEntity<ApiResponse<EmailVerificationConfirmResponse>> confirmEmailVerification(
            @Valid @RequestBody ConfirmEmailVerificationRequest request
    ) {
        var result = emailVerificationService.confirmSignupVerification(request.token());
        return ResponseEntity.ok(ApiResponse.success(EmailVerificationConfirmResponse.from(result)));
    }

    @PostMapping("/auto-login")
    @Operation(summary = "이메일 인증 자동 로그인", description = "이메일 인증 후 발급된 1회용 토큰으로 자동 로그인을 완료합니다.")
    public ResponseEntity<ApiResponse<EmailVerificationAutoLoginResponse>> autoLoginAfterEmailVerification(
            @Valid @RequestBody EmailVerificationAutoLoginRequest request,
            HttpServletRequest httpServletRequest
    ) {
        return ResponseEntity.ok(ApiResponse.success(
                EmailVerificationAutoLoginResponse.from(
                        emailVerificationAutoLoginService.authenticate(
                                request.autoLoginToken(),
                                AuthSessionMetadataExtractor.extract(httpServletRequest)
                        )
                )
        ));
    }
}
