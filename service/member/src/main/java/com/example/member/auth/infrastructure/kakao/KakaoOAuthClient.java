package com.example.member.auth.infrastructure.kakao;

import com.example.member.auth.config.KakaoOAuthProperties;
import java.net.URI;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.util.UriComponentsBuilder;

@Component
@RequiredArgsConstructor
public class KakaoOAuthClient {

    private final KakaoOAuthProperties kakaoOAuthProperties;
    private final RestClient restClient = RestClient.create();

    public String buildAuthorizeUrl(String state) {
        return UriComponentsBuilder.fromUriString(kakaoOAuthProperties.authorizeUri())
                .queryParam("client_id", kakaoOAuthProperties.clientId())
                .queryParam("redirect_uri", kakaoOAuthProperties.redirectUri())
                .queryParam("response_type", "code")
                .queryParam("state", state)
                .build(true)
                .toUriString();
    }

    public KakaoTokenResponse exchangeCode(String code) {
        MultiValueMap<String, String> form = new LinkedMultiValueMap<>();
        form.add("grant_type", "authorization_code");
        form.add("client_id", kakaoOAuthProperties.clientId());
        form.add("client_secret", kakaoOAuthProperties.clientSecret());
        form.add("redirect_uri", kakaoOAuthProperties.redirectUri());
        form.add("code", code);

        return restClient.post()
                .uri(URI.create(kakaoOAuthProperties.tokenUri()))
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .body(form)
                .retrieve()
                .body(KakaoTokenResponse.class);
    }

    public KakaoUserProfileResponse fetchUserProfile(String accessToken) {
        return restClient.get()
                .uri(URI.create(kakaoOAuthProperties.userInfoUri()))
                .header("Authorization", "Bearer " + accessToken)
                .retrieve()
                .body(KakaoUserProfileResponse.class);
    }
}
