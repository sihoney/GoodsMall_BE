package com.example.member.presentation.web.dto;

import com.example.member.application.dto.result.MemberOauthAccountItemResult;
import java.time.LocalDateTime;
import java.util.UUID;

public record MemberOauthAccountResponse(
        UUID oauthAccountId,
        String provider,
        String providerEmail,
        String providerNickname,
        LocalDateTime linkedAt,
        LocalDateTime updatedAt,
        boolean canUnlink
) {

    public static MemberOauthAccountResponse from(MemberOauthAccountItemResult result) {
        return new MemberOauthAccountResponse(
                result.oauthAccountId(),
                result.provider(),
                result.providerEmail(),
                result.providerNickname(),
                result.linkedAt(),
                result.updatedAt(),
                result.canUnlink()
        );
    }
}

