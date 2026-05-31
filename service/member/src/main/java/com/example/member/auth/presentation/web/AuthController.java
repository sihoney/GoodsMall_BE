package com.example.member.auth.presentation.web;

import com.example.member.auth.application.dto.command.LoginCommand;
import com.example.member.auth.application.dto.command.TokenRefreshCommand;
import com.example.member.auth.application.port.in.AuthLoginUsecase;
import com.example.member.auth.application.port.in.AuthTokenRefreshUsecase;
import com.example.member.auth.presentation.web.dto.LoginRequest;
import com.example.member.auth.presentation.web.dto.LoginResponse;
import com.example.member.auth.presentation.web.dto.TokenRefreshRequest;
import com.example.member.auth.presentation.web.dto.TokenRefreshResponse;
import com.example.member.common.presentation.web.dto.ApiResponse;
import com.example.member.common.presentation.web.support.AuthSessionMetadataExtractor;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
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
@Tag(name = "인증", description = "로그인/토큰")
public class AuthController {

    private final AuthLoginUsecase authLoginUsecase;
    private final AuthTokenRefreshUsecase authTokenRefreshUsecase;

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
        return ResponseEntity.ok(ApiResponse.success(
                LoginResponse.from(authLoginUsecase.login(new LoginCommand(
                        request.email(),
                        request.password(),
                        AuthSessionMetadataExtractor.extract(httpServletRequest)
                )))
        ));
    }

    @PostMapping("/refresh")
    @Operation(summary = "토큰 재발급", description = "토큰 갱신")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "잘못된 요청"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "토큰 오류")
    })
    public ResponseEntity<ApiResponse<TokenRefreshResponse>> refresh(
            @Valid @RequestBody TokenRefreshRequest request,
            HttpServletRequest httpServletRequest
    ) {
        return ResponseEntity.ok(ApiResponse.success(
                TokenRefreshResponse.from(authTokenRefreshUsecase.refresh(new TokenRefreshCommand(
                        request.refreshToken(),
                        AuthSessionMetadataExtractor.extract(httpServletRequest)
                )))
        ));
    }
}
