package com.example.member.application.dto.result;

public record MemberOauthAccountUnlinkResult(
        boolean unlinked,
        String provider
) {
}
