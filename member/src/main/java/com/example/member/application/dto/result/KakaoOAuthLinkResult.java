package com.example.member.application.dto.result;

public record KakaoOAuthLinkResult(
        boolean linked,
        String provider,
        String providerUserId
) {
}
