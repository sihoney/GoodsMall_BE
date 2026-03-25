package com.example.member.presentation.controller;

import com.example.member.application.service.AuthService;
import com.example.member.presentation.dto.ApiResponse;
import com.example.member.presentation.dto.LoginRequest;
import com.example.member.presentation.dto.LoginResponse;
import com.example.member.presentation.dto.TokenRefreshRequest;
import com.example.member.presentation.dto.TokenRefreshResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
@Tag(name = "인증", description = "인증(로그인/로그아웃/토큰재발급) API")
public class AuthController {

    private final AuthService authService;

    @PostMapping("/login")
    @Operation(summary = "로그인", description = "엑세스 토큰과 리프레시 토큰을 발급합니다.")
    public ResponseEntity<ApiResponse<LoginResponse>> login(@RequestBody LoginRequest request) {
        return ResponseEntity.ok(ApiResponse.success(authService.login(request)));
    }

    @PostMapping("/refresh")
    @Operation(summary = "토큰 재발급", description = "리프레시 토큰을 사용하여 엑세스 토큰을 재발급합니다.")
    public ResponseEntity<ApiResponse<TokenRefreshResponse>> refresh(@RequestBody TokenRefreshRequest request) {
        return ResponseEntity.ok(ApiResponse.success(authService.refresh(request)));
    }

    @PostMapping("/logout/{memberId}")
    @Operation(summary = "로그아웃", description = "Redis에서 사용자 리프레시 토큰을 삭제합니다.")
    public ResponseEntity<ApiResponse<Void>> logout(@PathVariable UUID memberId) {
        authService.logout(memberId);
        return ResponseEntity.ok(ApiResponse.success(null));
    }
}
