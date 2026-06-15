package com.example.member.auth.application.dto;

import com.example.member.auth.domain.enumtype.OAuthProvider;

public record OAuthUserProfile(
        OAuthProvider provider,
        String providerUserId,
        String email,
        String nickname,
        String profileImageUrl
) {
}
