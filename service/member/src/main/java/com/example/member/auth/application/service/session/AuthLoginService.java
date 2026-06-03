package com.example.member.auth.application.service.session;

import com.example.member.auth.application.dto.command.LoginCommand;
import com.example.member.auth.application.dto.result.AuthTokenResult;
import com.example.member.auth.application.port.in.AuthLoginUsecase;
import com.example.member.auth.exception.InvalidLoginException;
import com.example.member.common.application.dto.AuthSessionMetadata;
import com.example.member.member.application.port.out.MemberPersistencePort;
import com.example.member.member.domain.entity.Member;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthLoginService implements AuthLoginUsecase {

    private final MemberPersistencePort memberPersistencePort;
    private final PasswordEncoder passwordEncoder;
    private final LoginEligibilityValidator loginEligibilityValidator;
    private final AuthTokenIssuer authTokenIssuer;

    @Override
    public AuthTokenResult login(LoginCommand command) {
        // [1] 요청 검증
        validateLoginCommand(command);

        // [2] 이메일 정규화
        String email = normalizeRequired(command.email(), "email");

        // [3] 회원 조회
        Member member = memberPersistencePort.findByEmail(email)
                .orElseThrow(InvalidLoginException::new);

        // [4] 비밀번호 검증
        if (!passwordEncoder.matches(normalizeRequired(command.password(), "password"), member.getPassword())) {
            throw new InvalidLoginException();
        }

        // [5] 로그인 가능 여부 검증
        loginEligibilityValidator.validate(member);

        // [6] 토큰 발급
        return authTokenIssuer.issue(member, command.authSessionMetadata());
    }

    @Override
    public AuthTokenResult loginAuthenticatedMember(Member member, AuthSessionMetadata metadata) {
        // [1] 로그인 가능 여부 검증
        loginEligibilityValidator.validate(member);

        // [2] 토큰 발급
        return authTokenIssuer.issue(member, metadata);
    }

    private void validateLoginCommand(LoginCommand command) {
        if (command == null) {
            throw new IllegalArgumentException("로그인 요청 본문은 필수입니다.");
        }
    }

    private String normalizeRequired(String value, String fieldName) {
        if (value == null || value.trim().isEmpty()) {
            throw new IllegalArgumentException(fieldName + "은(는) 필수입니다.");
        }

        return value.trim();
    }
}
