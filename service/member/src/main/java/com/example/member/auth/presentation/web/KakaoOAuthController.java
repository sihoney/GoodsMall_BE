package com.example.member.auth.presentation.web;

import com.example.member.auth.application.dto.result.KakaoOAuthResult;
import com.example.member.auth.application.service.KakaoOAuthService;
import com.example.member.auth.infrastructure.redis.oauth.KakaoOAuthAuthorizeState;
import com.example.member.auth.infrastructure.redis.oauth.KakaoOAuthFlowType;
import com.example.member.auth.presentation.web.dto.KakaoOAuthAuthorizeUrlResponse;
import com.example.member.auth.presentation.web.dto.KakaoOAuthLinkRequest;
import com.example.member.auth.presentation.web.dto.KakaoOAuthLinkResponse;
import com.example.member.auth.presentation.web.dto.KakaoOAuthResultResponse;
import com.example.member.common.presentation.web.dto.ApiResponse;
import com.example.member.common.presentation.web.support.AuthSessionMetadataExtractor;
import com.todaylunch.common.security.auth.annotation.CurrentMember;
import com.todaylunch.common.security.auth.dto.AuthenticatedMember;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import java.net.URI;
import java.util.Locale;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.util.UriComponentsBuilder;

@RestController
@RequestMapping("/api/auth/oauth/kakao")
@RequiredArgsConstructor
@Tag(name = "카카오 OAuth", description = "카카오 OAuth 로그인 및 계정 연동 API")
public class KakaoOAuthController {

    private final KakaoOAuthService kakaoOAuthService;

    @GetMapping("/authorize")
    @Operation(summary = "카카오 로그인 시작", description = "카카오 로그인용 OAuth 인가 URL을 반환합니다.")
    public ResponseEntity<ApiResponse<KakaoOAuthAuthorizeUrlResponse>> authorizeKakaoLogin() {
        String state = kakaoOAuthService.createLoginAuthorizeState();
        String authorizeUrl = kakaoOAuthService.buildAuthorizeUrl(state);

        return ResponseEntity.ok(ApiResponse.success(new KakaoOAuthAuthorizeUrlResponse(authorizeUrl)));
    }

    @GetMapping("/link/authorize")
    @Operation(summary = "카카오 계정 연동 시작", description = "카카오 계정 연동용 OAuth 인가 URL을 반환합니다.")
    public ResponseEntity<ApiResponse<KakaoOAuthAuthorizeUrlResponse>> authorizeKakaoLink(
            @CurrentMember AuthenticatedMember authenticatedMember
    ) {
        String state = kakaoOAuthService.createLinkAuthorizeState(authenticatedMember.memberId());
        String authorizeUrl = kakaoOAuthService.buildAuthorizeUrl(state);

        return ResponseEntity.ok(ApiResponse.success(new KakaoOAuthAuthorizeUrlResponse(authorizeUrl)));
    }

    @GetMapping("/link/authorize-url")
    @Operation(summary = "카카오 연동 인가 URL 조회", description = "현재 회원의 카카오 OAuth 인가 URL을 조회합니다.")
    public ResponseEntity<ApiResponse<KakaoOAuthAuthorizeUrlResponse>> getKakaoLinkAuthorizeUrl(
            @CurrentMember AuthenticatedMember authenticatedMember
    ) {
        String state = kakaoOAuthService.createLinkAuthorizeState(authenticatedMember.memberId());
        String authorizeUrl = kakaoOAuthService.buildAuthorizeUrl(state);

        return ResponseEntity.ok(ApiResponse.success(new KakaoOAuthAuthorizeUrlResponse(authorizeUrl)));
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
        KakaoOAuthFlowType flowType = KakaoOAuthFlowType.LOGIN;
        KakaoOAuthResult result;

        try {
            KakaoOAuthAuthorizeState authorizeState = kakaoOAuthService.consumeAuthorizeState(state);
            flowType = authorizeState.flowType();

            if (error != null && !error.isBlank()) {
                result = KakaoOAuthResult.error(
                        "KAKAO_OAUTH_PROVIDER_ERROR",
                        errorDescription == null || errorDescription.isBlank()
                                ? "Kakao authorization was cancelled or failed."
                                : errorDescription
                );
            } else if (flowType == KakaoOAuthFlowType.LINK) {
                result = kakaoOAuthService.linkByCode(authorizeState.memberId(), code);
            } else {
                result = kakaoOAuthService.loginByCode(
                        code,
                        AuthSessionMetadataExtractor.extract(httpServletRequest)
                );
            }
        } catch (Exception exception) {
            result = kakaoOAuthService.createErrorResult(flowType, exception);
        }

        String resultKey = kakaoOAuthService.createOAuthResultKey(result);
        URI redirectUri = UriComponentsBuilder
                .fromUriString(kakaoOAuthService.getFrontendCallbackUrl())
                .queryParam("resultKey", resultKey)
                .queryParam("flow", flowType.name().toLowerCase(Locale.ROOT))
                .build(true)
                .toUri();

        return ResponseEntity.status(HttpStatus.FOUND).location(redirectUri).build();
    }

    @GetMapping("/result")
    @Operation(summary = "카카오 OAuth 결과 조회", description = "resultKey로 1회용 카카오 OAuth 결과를 조회합니다.")
    public ResponseEntity<ApiResponse<KakaoOAuthResultResponse>> getKakaoOAuthResult(
            @RequestParam(name = "resultKey") String resultKey
    ) {
        return ResponseEntity.ok(ApiResponse.success(
                KakaoOAuthResultResponse.from(kakaoOAuthService.consumeOAuthResult(resultKey))
        ));
    }

    @PostMapping("/link")
    @Operation(summary = "카카오 계정 연동", description = "현재 회원과 카카오 계정을 연동합니다.")
    public ResponseEntity<ApiResponse<KakaoOAuthLinkResponse>> linkKakaoAccount(
            @CurrentMember AuthenticatedMember authenticatedMember,
            @Valid @RequestBody KakaoOAuthLinkRequest request
    ) {
        return ResponseEntity.ok(ApiResponse.success(
                KakaoOAuthLinkResponse.from(
                        kakaoOAuthService.linkCurrentMember(authenticatedMember.memberId(), request.linkToken())
                )
        ));
    }
}
