package com.example.member.presentation.web.dto;

import com.example.member.application.dto.result.MemberOauthAccountUnlinkResult;

public record MemberOauthAccountUnlinkResponse(
        boolean unlinked,
        String provider
) {
    public static MemberOauthAccountUnlinkResponse from(MemberOauthAccountUnlinkResult result) {
        return new MemberOauthAccountUnlinkResponse(result.unlinked(), result.provider());
    }
}

