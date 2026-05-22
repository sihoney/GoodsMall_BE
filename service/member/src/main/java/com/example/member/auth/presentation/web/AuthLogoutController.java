package com.example.member.auth.presentation.web;

import com.example.member.auth.application.port.in.AuthSessionUsecase;
import com.example.member.common.presentation.web.dto.ApiResponse;
import com.example.member.auth.presentation.web.dto.AuthSessionListResponse;
import com.todaylunch.common.security.auth.annotation.CurrentMember;
import com.todaylunch.common.security.auth.dto.AuthenticatedMember;
import io.swagger.v3.oas.annotations.Operation;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthLogoutController {

    private final AuthSessionUsecase authSessionUsecase;

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
        return ResponseEntity.ok(ApiResponse.success(null));
    }

    @PostMapping("/logout/current")
    @Operation(summary = "현재 세션 로그아웃", description = "현재 access token에 해당하는 세션을 종료합니다.")
    public ResponseEntity<ApiResponse<Void>> logoutCurrentSession(
            @RequestHeader(HttpHeaders.AUTHORIZATION) String authorizationHeader
    ) {
        authSessionUsecase.logoutCurrentSession(authorizationHeader);
        return ResponseEntity.ok(ApiResponse.success(null));
    }

    @PostMapping("/logout/all")
    @Operation(summary = "전체 세션 로그아웃", description = "현재 회원의 모든 세션을 종료합니다.")
    public ResponseEntity<ApiResponse<Void>> logoutAllSessions(
            @RequestHeader(HttpHeaders.AUTHORIZATION) String authorizationHeader
    ) {
        authSessionUsecase.logoutAllSessions(authorizationHeader);
        return ResponseEntity.ok(ApiResponse.success(null));
    }
}
