package com.example.member.presentation.web.dto;

import com.example.member.application.dto.result.KakaoOAuthLinkResult;

public record KakaoOAuthLinkResponse(
        boolean linked,
        String provider,
        String providerUserId
) {
    public static KakaoOAuthLinkResponse from(KakaoOAuthLinkResult result) {
        return new KakaoOAuthLinkResponse(result.linked(), result.provider(), result.providerUserId());
    }
}

