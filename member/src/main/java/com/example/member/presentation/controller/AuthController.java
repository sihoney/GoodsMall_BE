package com.example.member.presentation.controller;

import com.example.member.application.service.AuthService;
import com.example.member.application.service.MemberService;
import com.example.member.application.usecase.AuthUsecase;
import com.example.member.application.usecase.MemberUsecase;
import com.example.member.presentation.dto.ApiResponse;
import com.example.member.presentation.dto.CreateMemberRequest;
import com.example.member.presentation.dto.CreateMemberResponse;
import com.example.member.presentation.dto.LoginRequest;
import com.example.member.presentation.dto.LoginResponse;
import com.example.member.presentation.dto.TokenRefreshRequest;
import com.example.member.presentation.dto.TokenRefreshResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.UUID;
import lombok.RequiredArgsConstructor;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
@Tag(name = "인증", description = "인증(로그인/로그아웃/토큰재발급) API")
public class AuthController {

    private final AuthUsecase authUsecase;
    private final MemberUsecase memberUsecase;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary="사용자 생성", description="사용자을 생성합니다.")
    public ResponseEntity<ApiResponse<CreateMemberResponse>> createMember(
        @Validated @RequestBody CreateMemberRequest request
    ) {
        return ResponseEntity.ok(ApiResponse.success(memberUsecase.createMember(request)));
    }

    @PostMapping("/login")
    @Operation(summary = "로그인", description = "엑세스 토큰과 리프레시 토큰을 발급합니다.")
    public ResponseEntity<ApiResponse<LoginResponse>> login(@RequestBody LoginRequest request) {
        return ResponseEntity.ok(ApiResponse.success(authUsecase.login(request)));
    }

    @PostMapping("/refresh")
    @Operation(summary = "토큰 재발급", description = "리프레시 토큰을 사용하여 엑세스 토큰을 재발급합니다.")
    public ResponseEntity<ApiResponse<TokenRefreshResponse>> refresh(@RequestBody TokenRefreshRequest request) {
        return ResponseEntity.ok(ApiResponse.success(authUsecase.refresh(request)));
    }

    @PostMapping("/logout/{memberId}")
    @Operation(summary = "로그아웃", description = "Redis에서 사용자 리프레시 토큰을 삭제합니다.")
    public ResponseEntity<ApiResponse<Void>> logout(@PathVariable UUID memberId) {
        authUsecase.logout(memberId);
        return ResponseEntity.ok(ApiResponse.success(null));
    }
}
