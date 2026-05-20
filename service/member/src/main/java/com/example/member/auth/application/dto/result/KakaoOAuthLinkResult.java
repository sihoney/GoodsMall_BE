package com.example.member.auth.application.dto.result;

public record KakaoOAuthLinkResult(
        boolean linked,
        String provider,
        String providerUserId
) {
}
