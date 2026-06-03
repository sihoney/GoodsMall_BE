package com.example.member.auth.application.service.oauth.profile;

import com.example.member.auth.application.dto.OAuthUserProfile;
import com.example.member.auth.domain.enumtype.OAuthProvider;
import com.example.member.auth.infrastructure.kakao.KakaoOAuthClient;
import com.example.member.auth.infrastructure.kakao.KakaoTokenResponse;
import com.example.member.auth.infrastructure.kakao.KakaoUserProfileResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class KakaoOAuthProfileService implements OAuthProfileService {

    private static final OAuthProvider PROVIDER = OAuthProvider.KAKAO;

    private final KakaoOAuthClient kakaoOAuthClient;

    @Override
    public OAuthProvider provider() {
        return PROVIDER;
    }

    @Override
    public String buildAuthorizeUrl(String state) {
        // [1] 인증 URL 생성
        return kakaoOAuthClient.buildAuthorizeUrl(state);
    }

    public KakaoUserProfileResponse fetchProfile(String code) {
        // [1] Code 검증
        KakaoTokenResponse tokenResponse = kakaoOAuthClient.exchangeCode(normalizeRequired(code, "code"));

        // [2] Token 응답 검증
        if (tokenResponse == null || tokenResponse.accessToken() == null || tokenResponse.accessToken().isBlank()) {
            throw new IllegalStateException("KAKAO_TOKEN_EXCHANGE_FAILED");
        }

        // [3] Profile 조회
        KakaoUserProfileResponse profile = kakaoOAuthClient.fetchUserProfile(tokenResponse.accessToken());

        // [4] Profile 응답 검증
        if (profile == null || profile.id() == null) {
            throw new IllegalStateException("KAKAO_PROFILE_FETCH_FAILED");
        }

        // [5] Profile 반환
        return profile;
    }

    @Override
    public OAuthUserProfile fetchOAuthUserProfile(String code) {
        // [1] Kakao Profile 조회
        KakaoUserProfileResponse profile = fetchProfile(code);

        // [2] 공통 Profile 변환
        return new OAuthUserProfile(
                PROVIDER,
                profile.id().toString(),
                kakaoEmail(profile),
                kakaoNickname(profile),
                kakaoProfileImageUrl(profile)
        );
    }

    private String kakaoEmail(KakaoUserProfileResponse profile) {
        return profile.kakaoAccount() == null ? null : profile.kakaoAccount().email();
    }

    private String kakaoNickname(KakaoUserProfileResponse profile) {
        return profile.kakaoAccount() == null || profile.kakaoAccount().profile() == null
                ? null
                : profile.kakaoAccount().profile().nickname();
    }

    private String kakaoProfileImageUrl(KakaoUserProfileResponse profile) {
        return profile.kakaoAccount() == null || profile.kakaoAccount().profile() == null
                ? null
                : profile.kakaoAccount().profile().profileImageUrl();
    }

    private String normalizeRequired(String value, String fieldName) {
        if (value == null || value.trim().isEmpty()) {
            throw new IllegalArgumentException(fieldName + "은(는) 필수입니다.");
        }
        return value.trim();
    }
}
