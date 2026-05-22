package com.example.member.auth.presentation.web;

import com.example.member.auth.application.dto.command.LoginCommand;
import com.example.member.auth.application.dto.command.TokenRefreshCommand;
import com.example.member.auth.application.port.in.AuthUsecase;
import com.example.member.auth.presentation.web.dto.LoginRequest;
import com.example.member.auth.presentation.web.dto.LoginResponse;
import com.example.member.auth.presentation.web.dto.TokenRefreshRequest;
import com.example.member.auth.presentation.web.dto.TokenRefreshResponse;
import com.example.member.common.presentation.web.dto.ApiResponse;
import com.example.member.common.presentation.web.support.AuthSessionMetadataExtractor;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Tag(name = "인증", description = "로그인 및 토큰 API")
public class AuthController {

    private final AuthUsecase authUsecase;

    @PostMapping("/login")
    @Operation(summary = "로그인", description = "이메일과 비밀번호로 로그인합니다.")
    public ResponseEntity<ApiResponse<LoginResponse>> login(
            @Valid @RequestBody LoginRequest request,
            HttpServletRequest httpServletRequest
    ) {
        return ResponseEntity.ok(ApiResponse.success(
                LoginResponse.from(authUsecase.login(new LoginCommand(
                        request.email(),
                        request.password(),
                        AuthSessionMetadataExtractor.extract(httpServletRequest)
                )))
        ));
    }

    @PostMapping("/refresh")
    @Operation(summary = "토큰 재발급", description = "access token과 refresh token을 재발급합니다.")
    public ResponseEntity<ApiResponse<TokenRefreshResponse>> refresh(
            @Valid @RequestBody TokenRefreshRequest request,
            HttpServletRequest httpServletRequest
    ) {
        return ResponseEntity.ok(ApiResponse.success(
                TokenRefreshResponse.from(authUsecase.refresh(new TokenRefreshCommand(
                        request.refreshToken(),
                        AuthSessionMetadataExtractor.extract(httpServletRequest)
                )))
        ));
    }
}
