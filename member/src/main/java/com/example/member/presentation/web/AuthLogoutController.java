package com.example.member.presentation.web;

import com.example.member.application.port.in.AuthUsecase;
import com.example.member.presentation.web.dto.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthLogoutController {

    private final AuthUsecase authUsecase;

    @PostMapping("/logout/current")
    @Operation(summary = "현재 세션 로그아웃", description = "현재 access token에 해당하는 세션을 종료합니다.")
    public ResponseEntity<ApiResponse<Void>> logoutCurrentSession(
            @RequestHeader(HttpHeaders.AUTHORIZATION) String authorizationHeader
    ) {
        authUsecase.logoutCurrentSession(authorizationHeader);
        return ResponseEntity.ok(ApiResponse.success(null));
    }

    @PostMapping("/logout/all")
    @Operation(summary = "전체 세션 로그아웃", description = "현재 회원의 모든 세션을 종료합니다.")
    public ResponseEntity<ApiResponse<Void>> logoutAllSessions(
            @RequestHeader(HttpHeaders.AUTHORIZATION) String authorizationHeader
    ) {
        authUsecase.logoutAllSessions(authorizationHeader);
        return ResponseEntity.ok(ApiResponse.success(null));
    }
}
