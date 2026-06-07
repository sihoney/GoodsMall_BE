package com.example.member.verification.application.service;


import com.example.member.common.exception.BusinessException;
import com.example.member.verification.exception.VerificationErrorCode;
import com.example.member.auth.application.port.in.AuthLoginUsecase;

import com.example.member.common.application.dto.AuthSessionMetadata;
import com.example.member.auth.application.dto.result.AuthTokenResult;
import com.example.member.auth.application.dto.result.EmailVerificationAutoLoginTokenResult;
import com.example.member.verification.application.port.out.EmailVerificationAutoLoginTokenStore;
import com.example.member.member.application.port.out.MemberPersistencePort;
import com.example.member.member.domain.entity.Member;
import java.time.Instant;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.example.member.verification.infrastructure.redis.emailverification.EmailVerificationAutoLoginToken;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class EmailVerificationAutoLoginService {

    private final EmailVerificationAutoLoginTokenStore emailVerificationAutoLoginTokenStore;
    private final MemberPersistencePort memberPersistencePort;
    private final AuthLoginUsecase authLoginUsecase;
    private final com.example.member.verification.config.EmailVerificationProperties emailVerificationProperties;

    @Transactional
    public EmailVerificationAutoLoginTokenResult issueToken(Member member) {
        UUID memberId = member == null ? null : member.getMemberId();
        if (memberId == null) {
            // TODO: 이 서비스가 외부 호출 경계가 되면 중복 필수값 검증을 command validation으로 이동한다.
            throw new BusinessException(VerificationErrorCode.EMAIL_VERIFICATION_NOT_ALLOWED);
        }

        String token = UUID.randomUUID().toString();
        emailVerificationAutoLoginTokenStore.create(
                new EmailVerificationAutoLoginToken(token, memberId, Instant.now()),
                emailVerificationProperties.autoLoginExpiration()
        );

        return new EmailVerificationAutoLoginTokenResult(
                token,
                emailVerificationProperties.autoLoginExpiration().toSeconds()
        );
    }

    @Transactional
    public AuthTokenResult authenticate(String autoLoginToken, AuthSessionMetadata metadata) {
        String normalizedToken = normalizeRequired(autoLoginToken, "autoLoginToken");
        EmailVerificationAutoLoginToken storedToken = emailVerificationAutoLoginTokenStore.consume(normalizedToken)
                .orElseThrow(() -> new BusinessException(VerificationErrorCode.EMAIL_VERIFICATION_AUTO_LOGIN_TOKEN_INVALID));

        Member member = memberPersistencePort.findById(storedToken.memberId())
                .orElseThrow(() -> new BusinessException(VerificationErrorCode.EMAIL_VERIFICATION_AUTO_LOGIN_TOKEN_INVALID));

        return authLoginUsecase.loginAuthenticatedMember(
                member,
                metadata == null ? AuthSessionMetadata.empty() : metadata
        );
    }

    private String normalizeRequired(String value, String fieldName) {
        if (value == null || value.trim().isEmpty()) {
            // TODO: 이 서비스가 외부 호출 경계가 되면 중복 필수값 검증을 command validation으로 이동한다.
            throw new BusinessException(VerificationErrorCode.EMAIL_VERIFICATION_AUTO_LOGIN_TOKEN_INVALID);
        }
        return value.trim();
    }
}
