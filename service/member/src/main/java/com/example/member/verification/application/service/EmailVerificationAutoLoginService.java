package com.example.member.verification.application.service;

import com.example.member.auth.application.port.in.AuthLoginUsecase;

import com.example.member.common.application.dto.AuthSessionMetadata;
import com.example.member.auth.application.dto.result.AuthTokenResult;
import com.example.member.auth.application.dto.result.EmailVerificationAutoLoginTokenResult;
import com.example.member.verification.application.port.out.EmailVerificationAutoLoginTokenStore;
import com.example.member.member.application.port.out.MemberPersistencePort;
import com.example.member.verification.exception.InvalidEmailVerificationAutoLoginTokenException;
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
    private final com.example.member.common.config.EmailVerificationProperties emailVerificationProperties;

    @Transactional
    public EmailVerificationAutoLoginTokenResult issueToken(Member member) {
        UUID memberId = member == null ? null : member.getMemberId();
        if (memberId == null) {
            throw new IllegalArgumentException("member는 필수입니다.");
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
                .orElseThrow(InvalidEmailVerificationAutoLoginTokenException::new);

        Member member = memberPersistencePort.findById(storedToken.memberId())
                .orElseThrow(InvalidEmailVerificationAutoLoginTokenException::new);

        return authLoginUsecase.loginAuthenticatedMember(
                member,
                metadata == null ? AuthSessionMetadata.empty() : metadata
        );
    }

    private String normalizeRequired(String value, String fieldName) {
        if (value == null || value.trim().isEmpty()) {
            throw new IllegalArgumentException(fieldName + "는 필수입니다.");
        }
        return value.trim();
    }
}
