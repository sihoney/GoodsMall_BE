package com.example.member.auth.presentation.web;

import com.example.member.auth.application.dto.result.OAuthCallbackResult;
import com.example.member.auth.application.service.oauth.OAuthFacade;
import com.example.member.auth.domain.enumtype.OAuthProvider;
import com.example.member.auth.presentation.web.dto.OAuthAuthorizeUrlResponse;
import com.example.member.auth.presentation.web.support.RefreshTokenCookieWriter;
import com.example.member.common.presentation.web.dto.ApiResponse;
import com.example.member.common.presentation.web.support.AuthSessionMetadataExtractor;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import java.net.URI;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.util.UriComponentsBuilder;

@RestController
@RequestMapping("/api/auth/oauth/google")
@RequiredArgsConstructor
@Tag(name = "Google OAuth", description = "Google OAuth 로그인 API")
public class GoogleOAuthController {

    private static final OAuthProvider PROVIDER = OAuthProvider.GOOGLE;

    private final OAuthFacade oauthFacade;
    private final RefreshTokenCookieWriter refreshTokenCookieWriter;

    @GetMapping("/authorize")
    @Operation(summary = "Google 로그인 시작", description = "Google 로그인용 OAuth 인가 URL을 반환합니다.")
    public ResponseEntity<ApiResponse<OAuthAuthorizeUrlResponse>> authorizeGoogleLogin() {
        return ResponseEntity.ok(ApiResponse.success(
                new OAuthAuthorizeUrlResponse(oauthFacade.createLoginAuthorizeUrl(PROVIDER))
        ));
    }

    @GetMapping("/callback")
    @Operation(summary = "Google OAuth 콜백", description = "Google OAuth 결과를 처리하고 프론트 콜백 URL로 리다이렉트합니다.")
    public ResponseEntity<Void> googleCallback(
            @RequestParam(name = "code", required = false) String code,
            @RequestParam(name = "state", required = false) String state,
            @RequestParam(name = "error", required = false) String error,
            @RequestParam(name = "error_description", required = false) String errorDescription,
            HttpServletRequest httpServletRequest
    ) {
        OAuthCallbackResult callbackResult = oauthFacade.handleCallback(
                PROVIDER,
                code,
                state,
                error,
                errorDescription,
                AuthSessionMetadataExtractor.extract(httpServletRequest)
        );

        URI redirectUri = buildRedirectUri(callbackResult);
        ResponseEntity.BodyBuilder response = ResponseEntity.status(HttpStatus.FOUND).location(redirectUri);
        if (callbackResult.success() && callbackResult.refreshToken() != null) {
            response.header(HttpHeaders.SET_COOKIE, refreshTokenCookieWriter
                    .create(callbackResult.refreshToken(), callbackResult.refreshTokenExpiresIn())
                    .toString());
        }
        return response.build();
    }

    private URI buildRedirectUri(OAuthCallbackResult callbackResult) {
        UriComponentsBuilder builder = UriComponentsBuilder
                .fromUriString(oauthFacade.getFrontendCallbackUrl(PROVIDER))
                .queryParam("success", callbackResult.success());

        if (!callbackResult.success()) {
            builder.queryParam("errorCode", callbackResult.errorCode());
            builder.queryParam("errorMessage", callbackResult.errorMessage());
        }
        return builder.build().toUri();
    }
}
