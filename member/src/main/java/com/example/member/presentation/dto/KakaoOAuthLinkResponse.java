package com.example.member.presentation.dto;

public record KakaoOAuthLinkResponse(
        boolean linked,
        String provider,
        String providerUserId
) {
}
