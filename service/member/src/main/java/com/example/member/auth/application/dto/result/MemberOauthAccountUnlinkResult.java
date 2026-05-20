package com.example.member.auth.application.dto.result;

public record MemberOauthAccountUnlinkResult(
        boolean unlinked,
        String provider
) {
}
