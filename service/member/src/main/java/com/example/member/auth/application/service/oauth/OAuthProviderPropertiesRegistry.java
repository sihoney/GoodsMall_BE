package com.example.member.auth.application.service.oauth;

import com.example.member.auth.domain.enumtype.OAuthProvider;
import com.example.member.auth.config.GoogleOAuthProperties;
import com.example.member.auth.config.KakaoOAuthProperties;
import com.example.member.auth.config.OAuthProviderProperties;
import com.example.member.auth.exception.AuthErrorCode;
import com.example.member.common.exception.BusinessException;
import java.util.EnumMap;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class OAuthProviderPropertiesRegistry {

    private final KakaoOAuthProperties kakaoOAuthProperties;
    private final GoogleOAuthProperties googleOAuthProperties;

    public OAuthProviderProperties get(OAuthProvider provider) {
        Map<OAuthProvider, OAuthProviderProperties> properties = new EnumMap<>(OAuthProvider.class);
        properties.put(OAuthProvider.KAKAO, kakaoOAuthProperties);
        properties.put(OAuthProvider.GOOGLE, googleOAuthProperties);

        OAuthProviderProperties providerProperties = properties.get(provider);
        if (providerProperties == null) {
            throw new BusinessException(AuthErrorCode.UNSUPPORTED_OAUTH_PROVIDER);
        }
        return providerProperties;
    }
}
