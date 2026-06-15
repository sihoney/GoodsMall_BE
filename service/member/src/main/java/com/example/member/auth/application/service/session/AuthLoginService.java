package com.example.member.auth.application.service.session;

import com.example.member.auth.application.dto.command.LoginCommand;
import com.example.member.auth.application.dto.result.AuthTokenResult;
import com.example.member.auth.application.port.in.AuthLoginUsecase;
import com.example.member.auth.exception.AuthErrorCode;
import com.example.member.common.application.dto.AuthSessionMetadata;
import com.example.member.common.exception.BusinessException;
import com.example.member.member.application.port.out.MemberPersistencePort;
import com.example.member.member.domain.entity.Member;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

@Service
@Validated
@RequiredArgsConstructor
public class AuthLoginService implements AuthLoginUsecase {

    private final MemberPersistencePort memberPersistencePort;
    private final PasswordEncoder passwordEncoder;
    private final LoginEligibilityValidator loginEligibilityValidator;
    private final AuthTokenIssuer authTokenIssuer;

    // [인증] 로그인 서비스
    @Override
    public AuthTokenResult login(LoginCommand command) {
        // [1] 이메일 정규화
        String email = command.email().trim();

        // [2] 회원 조회
        Member member = memberPersistencePort.findByEmail(email)
                .orElseThrow(() -> new BusinessException(AuthErrorCode.INVALID_LOGIN));

        // [3] 비밀번호 검증
        if (!passwordEncoder.matches(command.password().trim(), member.getPassword())) {
            throw new BusinessException(AuthErrorCode.INVALID_LOGIN);
        }

        // [4] 로그인 가능 여부 검증
        loginEligibilityValidator.validate(member);

        // [5] 토큰 발급
        return authTokenIssuer.issue(member, command.authSessionMetadata());
    }

    // [인증] 인증된 회원 로그인 서비스 (예: 소셜 로그인 후)
    @Override
    public AuthTokenResult loginAuthenticatedMember(Member member, AuthSessionMetadata metadata) {
        // [1] 로그인 가능 여부 검증
        loginEligibilityValidator.validate(member);

        // [2] 토큰 발급
        return authTokenIssuer.issue(member, metadata);
    }

}
