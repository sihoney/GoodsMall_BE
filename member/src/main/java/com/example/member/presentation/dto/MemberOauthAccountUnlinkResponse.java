package com.example.member.presentation.dto;

public record MemberOauthAccountUnlinkResponse(
        boolean unlinked,
        String provider
) {
}
