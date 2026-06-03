package com.example.member.auth.presentation.web;

import com.example.member.auth.application.dto.command.PasswordResetConfirmCommand;
import com.example.member.auth.application.dto.command.PasswordResetSendCommand;
import com.example.member.auth.application.service.password.PasswordResetService;
import com.example.member.auth.presentation.web.dto.PasswordResetConfirmRequest;
import com.example.member.auth.presentation.web.dto.PasswordResetConfirmResponse;
import com.example.member.auth.presentation.web.dto.PasswordResetSendRequest;
import com.example.member.auth.presentation.web.dto.PasswordResetSendResponse;
import com.example.member.common.presentation.web.dto.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth/password-resets")
@RequiredArgsConstructor
@Tag(name = "비밀번호 재설정", description = "비밀번호 재설정 API")
public class PasswordResetController {

    private final PasswordResetService passwordResetService;

    @PostMapping
    @Operation(summary = "비밀번호 재설정 메일 발송", description = "입력한 이메일로 비밀번호 재설정 링크를 발송합니다.")
    public ResponseEntity<ApiResponse<PasswordResetSendResponse>> sendPasswordReset(
            @Valid @RequestBody PasswordResetSendRequest request
    ) {
        return ResponseEntity.ok(ApiResponse.success(
                PasswordResetSendResponse.from(passwordResetService.sendPasswordReset(
                        new PasswordResetSendCommand(request.email())
                ))
        ));
    }

    @PostMapping("/confirm")
    @Operation(summary = "비밀번호 재설정 확인", description = "재설정 토큰과 새 비밀번호로 비밀번호를 변경합니다.")
    public ResponseEntity<ApiResponse<PasswordResetConfirmResponse>> confirmPasswordReset(
            @Valid @RequestBody PasswordResetConfirmRequest request
    ) {
        return ResponseEntity.ok(ApiResponse.success(
                PasswordResetConfirmResponse.from(passwordResetService.confirmPasswordReset(
                        new PasswordResetConfirmCommand(request.token(), request.newPassword())
                ))
        ));
    }
}
