package com.example.member.auth.presentation.web;

import com.example.member.auth.application.dto.result.OAuthCallbackResult;
import com.example.member.auth.application.dto.result.OAuthResult;
import com.example.member.auth.application.service.oauth.OAuthFacade;
import com.example.member.auth.domain.enumtype.OAuthProvider;
import com.example.member.auth.presentation.web.dto.OAuthAuthorizeUrlResponse;
import com.example.member.auth.presentation.web.dto.OAuthResultResponse;
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
@RequestMapping("/api/auth/oauth/kakao")
@RequiredArgsConstructor
@Tag(name = "카카오 OAuth", description = "카카오 OAuth 로그인 API")
public class KakaoOAuthController {

    private static final OAuthProvider PROVIDER = OAuthProvider.KAKAO;

    private final OAuthFacade oauthFacade;
    private final RefreshTokenCookieWriter refreshTokenCookieWriter;

    @GetMapping("/authorize")
    @Operation(summary = "카카오 로그인 시작", description = "카카오 로그인용 OAuth 인가 URL을 반환합니다.")
    public ResponseEntity<ApiResponse<OAuthAuthorizeUrlResponse>> authorizeKakaoLogin() {
        return ResponseEntity.ok(ApiResponse.success(
                new OAuthAuthorizeUrlResponse(oauthFacade.createLoginAuthorizeUrl(PROVIDER))
        ));
    }

    @GetMapping("/callback")
    @Operation(summary = "카카오 OAuth 콜백", description = "카카오 OAuth 결과를 저장하고 프론트 콜백 URL로 리다이렉트합니다.")
    public ResponseEntity<Void> kakaoCallback(
            @RequestParam(name = "code", required = false) String code,
            @RequestParam(name = "state", required = false) String state,
            @RequestParam(name = "error", required = false) String error,
            @RequestParam(name = "error_description", required = false) String errorDescription,
            HttpServletRequest httpServletRequest
    ) {
        OAuthCallbackResult callbackResult = oauthFacade.handleCallback(PROVIDER,
                code,
                state,
                error,
                errorDescription,
                AuthSessionMetadataExtractor.extract(httpServletRequest)
        );
        URI redirectUri = UriComponentsBuilder
                .fromUriString(oauthFacade.getFrontendCallbackUrl(PROVIDER))
                .queryParam("resultKey", callbackResult.resultKey())
                .build(true)
                .toUri();

        return ResponseEntity.status(HttpStatus.FOUND).location(redirectUri).build();
    }

    @GetMapping("/result")
    @Operation(summary = "카카오 OAuth 결과 조회", description = "resultKey로 1회용 카카오 OAuth 결과를 조회합니다.")
    public ResponseEntity<ApiResponse<OAuthResultResponse>> getOAuthResult(
            @RequestParam(name = "resultKey") String resultKey
    ) {
        OAuthResult result = oauthFacade.consumeOAuthResult(PROVIDER, resultKey);
        ResponseEntity.BodyBuilder response = ResponseEntity.ok();
        if (result.refreshToken() != null) {
            response.header(HttpHeaders.SET_COOKIE, refreshTokenCookieWriter
                    .create(result.refreshToken(), result.refreshTokenExpiresIn())
                    .toString());
        }
        return response.body(ApiResponse.success(OAuthResultResponse.from(result)));
    }
}
