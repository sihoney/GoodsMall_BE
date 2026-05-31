package com.example.member.auth.presentation.web.dto;

import com.example.member.auth.application.dto.result.MemberOauthAccountUnlinkResult;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "OAuth 해제 응답")
public record MemberOauthAccountUnlinkResponse(
        @Schema(description = "해제 여부", example = "true")
        boolean unlinked,
        @Schema(description = "제공자", example = "KAKAO")
        String provider
) {
    public static MemberOauthAccountUnlinkResponse from(MemberOauthAccountUnlinkResult result) {
        return new MemberOauthAccountUnlinkResponse(result.unlinked(), result.provider());
    }
}
