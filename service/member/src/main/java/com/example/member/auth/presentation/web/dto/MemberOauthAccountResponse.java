package com.example.member.auth.presentation.web.dto;

import com.example.member.auth.application.dto.result.MemberOauthAccountItemResult;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;
import java.util.UUID;

@Schema(description = "OAuth 계정")
public record MemberOauthAccountResponse(
        @Schema(description = "OAuth 계정 ID", example = "22222222-2222-2222-2222-222222222222")
        UUID oauthAccountId,
        @Schema(description = "제공자", example = "KAKAO")
        String provider,
        @Schema(description = "제공자 이메일", example = "member@example.com")
        String providerEmail,
        @Schema(description = "제공자 닉네임", example = "goods-user")
        String providerNickname,
        @Schema(description = "연동 시각")
        LocalDateTime linkedAt,
        @Schema(description = "수정 시각")
        LocalDateTime updatedAt,
        @Schema(description = "해제 가능", example = "true")
        boolean canUnlink
) {

    public static MemberOauthAccountResponse from(MemberOauthAccountItemResult result) {
        return new MemberOauthAccountResponse(
                result.oauthAccountId(),
                result.provider(),
                result.providerEmail(),
                result.providerNickname(),
                result.linkedAt(),
                result.updatedAt(),
                result.canUnlink()
        );
    }
}
