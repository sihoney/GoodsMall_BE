package com.example.member.auth.presentation.web.dto;

import com.example.member.auth.application.dto.result.MemberOauthAccountUnlinkResult;

public record MemberOauthAccountUnlinkResponse(
        boolean unlinked,
        String provider
) {
    public static MemberOauthAccountUnlinkResponse from(MemberOauthAccountUnlinkResult result) {
        return new MemberOauthAccountUnlinkResponse(result.unlinked(), result.provider());
    }
}

