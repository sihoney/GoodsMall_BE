package com.example.member.auth.application.service.oauth;

import com.example.member.auth.application.dto.OAuthUserProfile;
import com.example.member.auth.application.dto.result.OAuthCallbackResult;
import com.example.member.auth.application.dto.result.OAuthResult;
import com.example.member.auth.application.service.oauth.profile.OAuthProfileServiceRegistry;
import com.example.member.auth.domain.enumtype.OAuthProvider;
import com.example.member.common.application.dto.AuthSessionMetadata;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class OAuthFacade {

    private final OAuthStateService oauthStateService;
    private final OAuthProfileServiceRegistry oauthProfileServiceRegistry;
    private final OAuthLoginSignupService oauthLoginSignupService;
    private final OAuthErrorResultMapper oauthErrorResultMapper;

    public String createLoginAuthorizeUrl(OAuthProvider provider) {
        // [1] State 생성
        String state = oauthStateService.createLoginAuthorizeState(provider);

        // [2] 인증 URL 생성
        return oauthProfileServiceRegistry.get(provider).buildAuthorizeUrl(state);
    }

    @Transactional
    public OAuthCallbackResult handleCallback(
            OAuthProvider provider,
            String code,
            String state,
            String error,
            String errorDescription,
            AuthSessionMetadata metadata
    ) {
        OAuthResult result;

        try {
            // [1] State 소비
            oauthStateService.consumeAuthorizeState(provider, state);

            // [2] Callback 처리
            if (error != null && !error.isBlank()) {
                result = OAuthResult.error(
                        provider.name() + "_OAUTH_PROVIDER_ERROR",
                        errorDescription == null || errorDescription.isBlank()
                                ? provider.name() + " authorization was cancelled or failed."
                                : errorDescription
                );
            } else {
                // [2-1] OAuth 로그인/회원가입
                OAuthUserProfile profile = oauthProfileServiceRegistry.get(provider).fetchOAuthUserProfile(code);
                result = oauthLoginSignupService.loginOrSignupByProfile(
                        profile,
                        metadata
                );
            }
        } catch (Exception exception) {
            // [3] Error Result 생성
            result = oauthErrorResultMapper.createErrorResult(provider, exception);
        }

        // [4] Result 저장
        String resultKey = oauthStateService.createOAuthResultKey(provider, result);

        // [5] Callback Result 반환
        return new OAuthCallbackResult(resultKey);
    }

    public OAuthResult consumeOAuthResult(OAuthProvider provider, String resultKey) {
        // [1] 결과 조회
        return oauthStateService.consumeOAuthResult(provider, resultKey);
    }

    public String getFrontendCallbackUrl(OAuthProvider provider) {
        // [1] Callback URL 조회
        return oauthStateService.getFrontendCallbackUrl(provider);
    }
}
