package com.example.member.application.service;

import com.example.member.domain.entity.Member;
import com.example.member.domain.exception.InvalidLoginException;
import com.example.member.domain.exception.InvalidTokenException;
import com.example.member.domain.exception.RefreshTokenNotFoundException;
import com.example.member.infrastructure.redis.RefreshTokenStore;
import com.example.member.infrastructure.repository.MemberRepository;
import com.example.member.presentation.dto.LoginRequest;
import com.example.member.presentation.dto.LoginResponse;
import com.example.member.presentation.dto.TokenRefreshRequest;
import com.example.member.presentation.dto.TokenRefreshResponse;
import com.example.member.security.JwtTokenProvider;
import java.time.Duration;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final MemberRepository memberRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;
    private final RefreshTokenStore refreshTokenStore;

    // todo: 로그인 검증(access/refresh token 발급)
    public LoginResponse login(LoginRequest request) {
        validateLoginRequest(request);

        Member member = memberRepository.findByEmail(normalizeRequired(request.email(), "email"))
                .orElseThrow(InvalidLoginException::new);

        if (!passwordEncoder.matches(normalizeRequired(request.password(), "password"), member.getPassword())) {
            throw new InvalidLoginException();
        }

        String accessToken = jwtTokenProvider.createAccessToken(member);
        String refreshToken = jwtTokenProvider.createRefreshToken(member);
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
