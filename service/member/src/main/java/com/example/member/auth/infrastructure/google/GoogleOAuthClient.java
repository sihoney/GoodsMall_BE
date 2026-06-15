package com.example.member.auth.infrastructure.google;

import com.example.member.auth.config.GoogleOAuthProperties;
import java.net.URI;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClient;
import org.springframework.web.util.UriComponentsBuilder;

@Component
@RequiredArgsConstructor
public class GoogleOAuthClient {

    private static final String SCOPE = "openid email profile";

    private final GoogleOAuthProperties googleOAuthProperties;
    private final RestClient restClient = RestClient.create();

    public String buildAuthorizeUrl(String state) {
        return UriComponentsBuilder.fromUriString(googleOAuthProperties.authorizeUri())
                .queryParam("client_id", googleOAuthProperties.clientId())
                .queryParam("redirect_uri", googleOAuthProperties.redirectUri())
                .queryParam("response_type", "code")
                .queryParam("scope", SCOPE)
                .queryParam("state", state)
                .build()
                .encode()
                .toUriString();
    }

    public GoogleTokenResponse exchangeCode(String code) {
        MultiValueMap<String, String> form = new LinkedMultiValueMap<>();
        form.add("grant_type", "authorization_code");
        form.add("client_id", googleOAuthProperties.clientId());
        form.add("client_secret", googleOAuthProperties.clientSecret());
        form.add("redirect_uri", googleOAuthProperties.redirectUri());
        form.add("code", code);

        return restClient.post()
                .uri(URI.create(googleOAuthProperties.tokenUri()))
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .body(form)
                .retrieve()
                .body(GoogleTokenResponse.class);
    }

    public GoogleUserProfileResponse fetchUserProfile(String accessToken) {
        return restClient.get()
                .uri(URI.create(googleOAuthProperties.userInfoUri()))
                .header("Authorization", "Bearer " + accessToken)
                .retrieve()
                .body(GoogleUserProfileResponse.class);
    }
}
