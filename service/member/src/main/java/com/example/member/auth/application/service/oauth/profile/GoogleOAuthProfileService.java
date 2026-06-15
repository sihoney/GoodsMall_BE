package com.example.member.auth.application.service.oauth.profile;

import com.example.member.auth.application.dto.OAuthUserProfile;
import com.example.member.auth.domain.enumtype.OAuthProvider;
import com.example.member.auth.exception.AuthErrorCode;
import com.example.member.auth.infrastructure.google.GoogleOAuthClient;
import com.example.member.auth.infrastructure.google.GoogleTokenResponse;
import com.example.member.auth.infrastructure.google.GoogleUserProfileResponse;
import com.example.member.common.exception.BusinessException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class GoogleOAuthProfileService implements OAuthProfileService {

    private static final OAuthProvider PROVIDER = OAuthProvider.GOOGLE;

    private final GoogleOAuthClient googleOAuthClient;

    @Override
    public OAuthProvider provider() {
        return PROVIDER;
    }

    @Override
    public String buildAuthorizeUrl(String state) {
        // [1] 인증 URL 생성
        return googleOAuthClient.buildAuthorizeUrl(state);
    }

    public GoogleUserProfileResponse fetchProfile(String code) {
        // [1] Code 검증
        GoogleTokenResponse tokenResponse = googleOAuthClient.exchangeCode(normalizeRequired(code, "code"));

        // [2] Token 응답 검증
        if (tokenResponse == null || tokenResponse.accessToken() == null || tokenResponse.accessToken().isBlank()) {
            throw new IllegalStateException("GOOGLE_TOKEN_EXCHANGE_FAILED");
        }

        // [3] Profile 조회
        GoogleUserProfileResponse profile = googleOAuthClient.fetchUserProfile(tokenResponse.accessToken());

        // [4] Profile 응답 검증
        if (profile == null || profile.sub() == null || profile.sub().isBlank()) {
            throw new IllegalStateException("GOOGLE_PROFILE_FETCH_FAILED");
        }
        if (profile.email() == null || profile.email().isBlank()) {
            throw new IllegalStateException("GOOGLE_OAUTH_EMAIL_REQUIRED");
        }
        if (!Boolean.TRUE.equals(profile.emailVerified())) {
            throw new IllegalStateException("GOOGLE_OAUTH_EMAIL_NOT_VERIFIED");
        }

        // [5] Profile 반환
        return profile;
    }

    @Override
    public OAuthUserProfile fetchOAuthUserProfile(String code) {
        // [1] Google Profile 조회
        GoogleUserProfileResponse profile = fetchProfile(code);

        // [2] 공통 Profile 변환
        return new OAuthUserProfile(
                PROVIDER,
                profile.sub(),
                profile.email(),
                profile.name(),
                profile.picture()
        );
    }

    private String normalizeRequired(String value, String fieldName) {
        if (value == null || value.trim().isEmpty()) {
            throw new BusinessException(AuthErrorCode.OAUTH_INVALID_REQUEST);
        }
        return value.trim();
    }
}
