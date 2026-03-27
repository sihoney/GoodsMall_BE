package com.example.member.application.service;

import com.example.member.application.usecase.AuthUsecase;
import com.example.member.common.exception.InvalidLoginException;
import com.example.member.common.exception.RefreshTokenNotFoundException;
import com.example.member.domain.entity.Member;
import com.example.member.infrastructure.redis.RefreshTokenStore;
import com.example.member.infrastructure.repository.MemberRepository;
import com.example.member.presentation.dto.LoginRequest;
import com.example.member.presentation.dto.LoginResponse;
import com.example.member.presentation.dto.TokenRefreshRequest;
import com.example.member.presentation.dto.TokenRefreshResponse;
import com.example.member.security.JwtTokenProvider;
import com.todaylunch.common.security.exception.InvalidTokenException;
import java.time.Duration;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService implements AuthUsecase {

    private final MemberRepository memberRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;
    private final RefreshTokenStore refreshTokenStore;

    public LoginResponse login(LoginRequest request) {
        validateLoginRequest(request);

        // 이메일로 회원 조회
        Member member = memberRepository.findByEmail(normalizeRequired(request.email(), "email"))
                .orElseThrow(InvalidLoginException::new);

        // 비밀번호 검증
        if (!passwordEncoder.matches(normalizeRequired(request.password(), "password"), member.getPassword())) {
            throw new InvalidLoginException();
        }

        // 토큰 발급 
        String accessToken = jwtTokenProvider.createAccessToken(member);
        String refreshToken = jwtTokenProvider.createRefreshToken(member);

        // Redis에 리프레시 토큰 저장 (memberId를 키로 사용)
        refreshTokenStore.save(
                member.getMemberId(),
                refreshToken,
                Duration.ofMillis(jwtTokenProvider.getRefreshExpiration())
        );

        return new LoginResponse(
                accessToken,
                refreshToken,
                "Bearer",
                jwtTokenProvider.getAccessExpiration(),
                jwtTokenProvider.getRefreshExpiration()
        );
    }

    public TokenRefreshResponse refresh(TokenRefreshRequest request) {
        validateRefreshRequest(request);

        String refreshToken = normalizeRequired(request.refreshToken(), "refreshToken");
        jwtTokenProvider.validateRefreshToken(refreshToken);

        UUID memberId = jwtTokenProvider.extractMemberId(refreshToken);
        String storedToken = refreshTokenStore.findByMemberId(memberId)
                .orElseThrow(RefreshTokenNotFoundException::new);

        if (!storedToken.equals(refreshToken)) {
            throw new InvalidTokenException();
        }

        Member member = memberRepository.findById(memberId)
                .orElseThrow(InvalidTokenException::new);

        return new TokenRefreshResponse(
                jwtTokenProvider.createAccessToken(member),
                storedToken,
                "Bearer",
                jwtTokenProvider.getAccessExpiration(),
                jwtTokenProvider.getRefreshExpiration()
        );
    }

    public void logout(UUID memberId) {
        // Redis에서 리프레시 토큰 삭제
        refreshTokenStore.delete(memberId);
    }

    private void validateLoginRequest(LoginRequest request) {
        if (request == null) {
            throw new IllegalArgumentException("Login request body is required.");
        }
    }

    private void validateRefreshRequest(TokenRefreshRequest request) {
        if (request == null) {
            throw new IllegalArgumentException("Refresh request body is required.");
        }
    }

    private String normalizeRequired(String value, String fieldName) {
        if (value == null || value.trim().isEmpty()) {
            throw new IllegalArgumentException(fieldName + " is required.");
        }
        return value.trim();
    }
}
