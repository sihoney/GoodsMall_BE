package com.example.member.auth.presentation.web;

import com.example.member.auth.application.dto.result.OAuthCallbackResult;
import com.example.member.auth.application.service.oauth.OAuthFacade;
import com.example.member.auth.domain.enumtype.OAuthProvider;
import com.example.member.auth.presentation.web.dto.OAuthAuthorizeUrlResponse;
import com.example.member.auth.presentation.web.dto.OAuthResultResponse;
import com.example.member.common.presentation.web.dto.ApiResponse;
import com.example.member.common.presentation.web.support.AuthSessionMetadataExtractor;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import java.net.URI;
import lombok.RequiredArgsConstructor;
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

    @GetMapping("/authorize")
    @Operation(summary = "Google 로그인 시작", description = "Google 로그인용 OAuth 인가 URL을 반환합니다.")
    public ResponseEntity<ApiResponse<OAuthAuthorizeUrlResponse>> authorizeGoogleLogin() {
        return ResponseEntity.ok(ApiResponse.success(
                new OAuthAuthorizeUrlResponse(oauthFacade.createLoginAuthorizeUrl(PROVIDER))
        ));
    }

    @GetMapping("/callback")
    @Operation(summary = "Google OAuth 콜백", description = "Google OAuth 결과를 저장하고 프론트 콜백 URL로 리다이렉트합니다.")
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
        URI redirectUri = UriComponentsBuilder
                .fromUriString(oauthFacade.getFrontendCallbackUrl(PROVIDER))
                .queryParam("resultKey", callbackResult.resultKey())
                .build(true)
                .toUri();

        return ResponseEntity.status(HttpStatus.FOUND).location(redirectUri).build();
    }

    @GetMapping("/result")
    @Operation(summary = "Google OAuth 결과 조회", description = "resultKey로 1회용 Google OAuth 결과를 조회합니다.")
    public ResponseEntity<ApiResponse<OAuthResultResponse>> getGoogleOAuthResult(
            @RequestParam(name = "resultKey") String resultKey
    ) {
        return ResponseEntity.ok(ApiResponse.success(
                OAuthResultResponse.from(oauthFacade.consumeOAuthResult(PROVIDER, resultKey))
        ));
    }
}
