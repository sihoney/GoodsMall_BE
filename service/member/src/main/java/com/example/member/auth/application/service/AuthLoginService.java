package com.example.member.auth.application.service;

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
        validateLoginCommand(command);

        String email = normalizeRequired(command.email(), "email");
        Member member = memberPersistencePort.findByEmail(email)
                .orElseThrow(InvalidLoginException::new);

        if (!passwordEncoder.matches(normalizeRequired(command.password(), "password"), member.getPassword())) {
            throw new InvalidLoginException();
        }

        loginEligibilityValidator.validate(member);
        return authTokenIssuer.issue(member, command.authSessionMetadata());
    }

    @Override
    public AuthTokenResult login(Member member, AuthSessionMetadata metadata) {
        loginEligibilityValidator.validate(member);
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
