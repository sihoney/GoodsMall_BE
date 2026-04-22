package com.example.member.presentation.dto;

import com.example.member.domain.entity.MemberOauthAccount;
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

    public static MemberOauthAccountResponse from(MemberOauthAccount account, boolean canUnlink) {
        return new MemberOauthAccountResponse(
                account.getOauthAccountId(),
                account.getProvider().name(),
                account.getProviderEmail(),
                account.getProviderNickname(),
                account.getCreatedAt(),
                account.getUpdatedAt(),
                canUnlink
        );
    }
}
