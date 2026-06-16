package com.example.member.verification.application.service;

import com.example.member.auth.application.dto.result.EmailVerificationAutoLoginTokenResult;
import com.example.member.common.exception.BusinessException;
import com.example.member.member.application.port.out.MemberPersistencePort;
import com.example.member.member.domain.entity.Member;
import com.example.member.member.domain.enumtype.MemberStatus;
import com.example.member.member.exception.MemberErrorCode;
import com.example.member.verification.application.dto.result.EmailVerificationConfirmResult;
import com.example.member.verification.application.dto.result.EmailVerificationSendResult;
import com.example.member.verification.application.port.out.EmailSenderPort;
import com.example.member.verification.application.port.out.EmailVerificationTokenStore;
import com.example.member.verification.config.EmailVerificationProperties;
import com.example.member.verification.exception.VerificationErrorCode;
import com.example.member.verification.infrastructure.redis.emailverification.SignupEmailVerificationToken;
import java.time.LocalDateTime;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class EmailVerificationService {

    private static final String SIGNUP_PURPOSE = "SIGNUP";
    private static final String PENDING_STATUS = "PENDING";

    private final EmailVerificationTokenStore emailVerificationTokenStore;
    private final MemberPersistencePort memberPersistencePort;
    private final EmailSenderPort emailSender;
    private final EmailVerificationProperties emailVerificationProperties;
    private final EmailVerificationAutoLoginService emailVerificationAutoLoginService;

    @Transactional
    public EmailVerificationSendResult createSignupVerification(Member member) {
        Member targetMember = validateSignupTarget(member);
        LocalDateTime expiresAt = LocalDateTime.now().plus(emailVerificationProperties.expiration());
        String token = UUID.randomUUID().toString();

        SignupEmailVerificationToken verificationToken = new SignupEmailVerificationToken(
                token,
                targetMember.getMemberId(),
                targetMember.getEmail(),
                expiresAt
        );

        emailVerificationTokenStore.saveSignupToken(
                verificationToken,
                emailVerificationProperties.expiration()
        );

        try {
            emailSender.send(
                    targetMember.getEmail(),
                    buildSignupVerificationSubject(),
                    buildSignupVerificationBody(targetMember.getEmail(), token),
                    true
            );
        } catch (RuntimeException exception) {
            emailVerificationTokenStore.deleteByToken(token);
            throw exception;
        }

        return new EmailVerificationSendResult(
                targetMember.getEmail(),
                SIGNUP_PURPOSE,
                PENDING_STATUS,
                expiresAt
        );
    }

    @Transactional
    public EmailVerificationConfirmResult confirmSignupVerification(String token) {
        String normalizedToken = normalizeRequired(token, "token");
        SignupEmailVerificationToken verificationToken = emailVerificationTokenStore.findSignupToken(normalizedToken)
                .orElseThrow(() -> new BusinessException(VerificationErrorCode.EMAIL_VERIFICATION_TOKEN_INVALID));

        if (!verificationToken.expiresAt().isAfter(LocalDateTime.now())) {
            emailVerificationTokenStore.deleteByToken(normalizedToken);
            throw new BusinessException(VerificationErrorCode.EMAIL_VERIFICATION_TOKEN_EXPIRED);
        }

        Member member = memberPersistencePort.findById(verificationToken.memberId())
                .orElseThrow(() -> new BusinessException(MemberErrorCode.MEMBER_NOT_FOUND));

        if (member.getStatus() == MemberStatus.PENDING_VERIFICATION) {
            member.changeStatus(MemberStatus.ACTIVE, LocalDateTime.now());
        } else if (member.getStatus() != MemberStatus.ACTIVE) {
            throw new BusinessException(
                    VerificationErrorCode.EMAIL_VERIFICATION_NOT_ALLOWED,
                    "현재 회원 상태에서는 이메일 인증을 완료할 수 없습니다."
            );
        }

        emailVerificationTokenStore.deleteByToken(normalizedToken);
        EmailVerificationAutoLoginTokenResult autoLoginToken = emailVerificationAutoLoginService.issueToken(member);
        return new EmailVerificationConfirmResult(
                member.getMemberId(),
                member.getEmail(),
                member.getStatus(),
                autoLoginToken.token(),
                autoLoginToken.expiresInSeconds()
        );
    }

    @Transactional
    public EmailVerificationSendResult resendSignupVerification(String email) {
        String normalizedEmail = normalizeRequired(email, "email");
        Member member = memberPersistencePort.findByEmail(normalizedEmail)
                .orElseThrow(() -> new BusinessException(MemberErrorCode.MEMBER_NOT_FOUND));

        if (member.isActive()) {
            throw new BusinessException(
                    VerificationErrorCode.EMAIL_VERIFICATION_NOT_ALLOWED,
                    "ACTIVE 상태의 회원은 이메일 인증을 재요청할 수 없습니다."
            );
        }
        if (member.getStatus() != MemberStatus.PENDING_VERIFICATION) {
            throw new BusinessException(
                    VerificationErrorCode.EMAIL_VERIFICATION_NOT_ALLOWED,
                    "현재 회원 상태에서는 이메일 인증을 재요청할 수 없습니다."
            );
        }

        return createSignupVerification(member);
    }

    private Member validateSignupTarget(Member member) {
        if (member == null) {
            throw new BusinessException(VerificationErrorCode.EMAIL_VERIFICATION_NOT_ALLOWED);
        }
        if (member.getStatus() != MemberStatus.PENDING_VERIFICATION) {
            throw new BusinessException(
                    VerificationErrorCode.EMAIL_VERIFICATION_NOT_ALLOWED,
                    "이메일 인증 대상은 PENDING_VERIFICATION 상태여야 합니다."
            );
        }
        return member;
    }

    private String buildSignupVerificationSubject() {
        return "[Goods Mall] 이메일 인증을 완료해 주세요";
    }

    private String buildSignupVerificationBody(String email, String token) {
        String verificationUrl = "%s?token=%s".formatted(
                emailVerificationProperties.frontendConfirmUrl(),
                token
        );
        long expirationMinutes = emailVerificationProperties.expiration().toMinutes();

        return """
                <!DOCTYPE html>
                <html lang="ko">
                <head>
                    <meta charset="UTF-8" />
                    <meta name="viewport" content="width=device-width, initial-scale=1.0" />
                    <title>Goods Mall 이메일 인증</title>
                </head>
                <body style="margin:0; padding:0; background-color:#f6f1ff; font-family:'Apple SD Gothic Neo','Malgun Gothic',sans-serif; color:#2f2340;">
                    <div style="max-width:640px; margin:0 auto; padding:32px 20px;">
                        <div style="background-color:#ffffff; border-radius:24px; padding:40px 32px; box-shadow:0 16px 40px rgba(106,79,155,0.12);">
                            <div style="display:inline-block; padding:10px 14px; border-radius:16px; background-color:#efe4ff; color:#6d28d9; font-weight:700; font-size:14px;">
                                Goods Mall
                            </div>
                            <h1 style="margin:24px 0 12px; font-size:28px; line-height:1.3; color:#241533;">
                                이메일 인증을 완료해 주세요
                            </h1>
                            <p style="margin:0 0 16px; font-size:16px; line-height:1.7; color:#5a4a6a;">
                                안녕하세요.<br />
                                Goods Mall 회원가입을 완료하려면 아래 버튼으로 이메일 인증을 진행해 주세요.
                            </p>

                            <div style="margin:24px 0; padding:18px 20px; border-radius:18px; background-color:#f7f3ff;">
                                <div style="font-size:12px; font-weight:700; letter-spacing:0.08em; text-transform:uppercase; color:#7c5ab8;">
                                    인증 대상 이메일
                                </div>
                                <div style="margin-top:8px; font-size:16px; font-weight:600; color:#2f2340; word-break:break-all;">
                                    %s
                                </div>
                            </div>

                            <div style="margin:32px 0;">
                                <a href="%s" style="display:inline-block; padding:16px 24px; border-radius:16px; background-color:#7c3aed; color:#ffffff; text-decoration:none; font-size:16px; font-weight:700;">
                                    이메일 인증하기
                                </a>
                            </div>

                            <p style="margin:0 0 12px; font-size:14px; line-height:1.7; color:#5a4a6a;">
                                버튼이 동작하지 않으면 아래 주소를 브라우저에 직접 입력해 주세요.
                            </p>
                            <p style="margin:0 0 24px; font-size:14px; line-height:1.7; color:#6d28d9; word-break:break-all;">
                                %s
                            </p>

                            <p style="margin:0; font-size:14px; line-height:1.7; color:#5a4a6a;">
                                인증 링크는 %d분 동안 유효합니다.<br />
                                본인이 요청하지 않았다면 이 메일을 무시해 주세요.
                            </p>
                        </div>
                    </div>
                </body>
                </html>
                """.formatted(email, verificationUrl, verificationUrl, expirationMinutes);
    }

    private String normalizeRequired(String value, String fieldName) {
        if (value == null || value.trim().isEmpty()) {
            if ("token".equals(fieldName)) {
                throw new BusinessException(VerificationErrorCode.EMAIL_VERIFICATION_TOKEN_INVALID);
            }
            throw new BusinessException(VerificationErrorCode.EMAIL_VERIFICATION_NOT_ALLOWED);
        }
        return value.trim();
    }
}