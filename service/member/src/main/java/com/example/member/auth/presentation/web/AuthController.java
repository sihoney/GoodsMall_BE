package com.example.member.auth.presentation.web;

import com.example.member.auth.application.dto.command.LoginCommand;
import com.example.member.auth.application.dto.command.TokenRefreshCommand;
import com.example.member.auth.application.dto.result.AuthTokenResult;
import com.example.member.auth.application.port.in.AuthLoginUsecase;
import com.example.member.auth.application.port.in.AuthSessionUsecase;
import com.example.member.auth.application.port.in.AuthTokenRefreshUsecase;
import com.example.member.auth.presentation.web.support.RefreshTokenCookieWriter;
import com.example.member.auth.presentation.web.dto.AuthSessionListResponse;
import com.example.member.auth.presentation.web.dto.LoginRequest;
import com.example.member.auth.presentation.web.dto.LoginResponse;
import com.example.member.auth.presentation.web.dto.TokenRefreshRequest;
import com.example.member.auth.presentation.web.dto.TokenRefreshResponse;
import com.example.member.common.presentation.web.dto.ApiResponse;
import com.example.member.common.presentation.web.support.AuthSessionMetadataExtractor;
import com.todaylunch.common.security.auth.annotation.CurrentMember;
import com.todaylunch.common.security.auth.dto.AuthenticatedMember;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Tag(name = "인증", description = "로그인/토큰")
public class AuthController {

    private final AuthLoginUsecase authLoginUsecase;
    private final AuthTokenRefreshUsecase authTokenRefreshUsecase;
    private final AuthSessionUsecase authSessionUsecase;
    private final RefreshTokenCookieWriter refreshTokenCookieWriter;

    @PostMapping("/login")
    @Operation(summary = "로그인", description = "토큰 발급")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "잘못된 요청"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "인증 실패")
    })
    public ResponseEntity<ApiResponse<LoginResponse>> login(
            @Valid @RequestBody LoginRequest request,
            HttpServletRequest httpServletRequest
    ) {
        AuthTokenResult result = authLoginUsecase.login(new LoginCommand(
                request.email(),
                request.password(),
                AuthSessionMetadataExtractor.extract(httpServletRequest)
        ));

        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, refreshTokenCookieWriter
                        .create(result.refreshToken(), result.refreshTokenExpiresIn())
                        .toString())
                .body(ApiResponse.success(LoginResponse.from(result)));
    }

    @PostMapping("/refresh")
    @Operation(summary = "토큰 재발급", description = "토큰 갱신")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "잘못된 요청"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "토큰 오류")
    })
    public ResponseEntity<ApiResponse<TokenRefreshResponse>> refresh(
            @RequestBody(required = false) TokenRefreshRequest request,
            HttpServletRequest httpServletRequest
    ) {
        String refreshToken = refreshTokenCookieWriter.resolveRefreshToken(
                httpServletRequest,
                request == null ? null : request.refreshToken()
        );
        AuthTokenResult result = authTokenRefreshUsecase.refresh(new TokenRefreshCommand(
                refreshToken,
                AuthSessionMetadataExtractor.extract(httpServletRequest)
        ));

        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, refreshTokenCookieWriter
                        .create(result.refreshToken(), result.refreshTokenExpiresIn())
                        .toString())
                .body(ApiResponse.success(TokenRefreshResponse.from(result)));
    }

    @GetMapping("/sessions")
    @Operation(summary = "로그인 세션 목록 조회", description = "현재 회원의 활성 로그인 세션 목록을 조회합니다.")
    public ResponseEntity<ApiResponse<AuthSessionListResponse>> getSessions(
            @CurrentMember AuthenticatedMember authenticatedMember
    ) {
        return ResponseEntity.ok(ApiResponse.success(
                AuthSessionListResponse.from(
                        authSessionUsecase.getSessions(authenticatedMember.memberId(), authenticatedMember.sessionId())
                )
        ));
    }

    @DeleteMapping("/sessions/{sessionId}")
    @Operation(summary = "특정 로그인 세션 종료", description = "현재 회원의 특정 활성 로그인 세션을 종료합니다.")
    public ResponseEntity<ApiResponse<Void>> logoutSession(
            @CurrentMember AuthenticatedMember authenticatedMember,
            @RequestHeader(HttpHeaders.AUTHORIZATION) String authorizationHeader,
            @PathVariable UUID sessionId
    ) {
        authSessionUsecase.logoutSession(
                authorizationHeader,
                authenticatedMember.memberId(),
                authenticatedMember.sessionId(),
                sessionId
        );
        ResponseEntity.BodyBuilder response = ResponseEntity.ok();
        if (authenticatedMember.sessionId().equals(sessionId)) {
            response.header(HttpHeaders.SET_COOKIE, refreshTokenCookieWriter.clear().toString());
        }
        return response.body(ApiResponse.success(null));
    }

    @PostMapping("/logout/current")
    @Operation(summary = "현재 세션 로그아웃", description = "현재 access token에 해당하는 세션을 종료합니다.")
    public ResponseEntity<ApiResponse<Void>> logoutCurrentSession(
            @RequestHeader(HttpHeaders.AUTHORIZATION) String authorizationHeader
    ) {
        authSessionUsecase.logoutCurrentSession(authorizationHeader);
        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, refreshTokenCookieWriter.clear().toString())
                .body(ApiResponse.success(null));
    }

    @PostMapping("/logout/all")
    @Operation(summary = "전체 세션 로그아웃", description = "현재 회원의 모든 세션을 종료합니다.")
    public ResponseEntity<ApiResponse<Void>> logoutAllSessions(
            @RequestHeader(HttpHeaders.AUTHORIZATION) String authorizationHeader
    ) {
        authSessionUsecase.logoutAllSessions(authorizationHeader);
        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, refreshTokenCookieWriter.clear().toString())
                .body(ApiResponse.success(null));
    }
}
