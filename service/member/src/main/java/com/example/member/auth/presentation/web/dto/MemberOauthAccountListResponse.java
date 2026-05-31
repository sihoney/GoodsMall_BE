package com.example.member.auth.presentation.web.dto;

import com.example.member.auth.application.dto.result.MemberOauthAccountListResult;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;

@Schema(description = "OAuth 계정 목록 응답")
public record MemberOauthAccountListResponse(
        @Schema(description = "연동 계정")
        List<MemberOauthAccountResponse> accounts,
        @Schema(description = "비밀번호 로그인", example = "true")
        boolean hasPasswordLogin,
        @Schema(description = "마지막 계정 해제 가능", example = "false")
        boolean canRemoveLastOauthAccount
) {
    public static MemberOauthAccountListResponse from(MemberOauthAccountListResult result) {
        return new MemberOauthAccountListResponse(
                result.accounts().stream()
                        .map(MemberOauthAccountResponse::from)
                        .toList(),
                result.hasPasswordLogin(),
                result.canRemoveLastOauthAccount()
        );
    }
}
